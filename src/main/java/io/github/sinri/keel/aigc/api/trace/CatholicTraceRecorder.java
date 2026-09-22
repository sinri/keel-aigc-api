package io.github.sinri.keel.aigc.api.trace;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/** Owns bounded run buffers and a bounded, single-worker persistence queue. Disabled unless explicitly configured. */
public final class CatholicTraceRecorder implements AutoCloseable {
    public enum ContentMode { STRUCTURE, RESTRICTED_RAW }

    /** All limits include queued snapshots; sizes conservatively charge UTF-16 storage and object overhead. */
    public record Limits(long runBytes, long totalBytes, int eventsPerRun, int payloadCharacters,
                         int activeRuns, int queuedTraces, long queuedBytes, Duration ttl) {
        public Limits {
            if (runBytes < 32768 || totalBytes < runBytes || eventsPerRun < 8 || payloadCharacters < 64
                    || activeRuns < 1 || queuedTraces < 1 || queuedBytes < runBytes
                    || ttl == null || ttl.isNegative() || ttl.isZero()) throw new IllegalArgumentException("invalid trace limits");
        }
        public static Limits defaults() {
            return new Limits(2L << 20, 64L << 20, 1024, 16384, 128, 32, 32L << 20, Duration.ofMinutes(15));
        }
    }

    private final CatholicTraceSink sink;
    final Limits limits;
    final ContentMode mode;
    private final Set<String> allowedTags;
    private final Consumer<String> status;
    private final ThreadPoolExecutor writer;
    private final ScheduledExecutorService timer;
    private final Set<CatholicTraceSession> active = ConcurrentHashMap.newKeySet();
    private long retainedBytes;
    private long queuedBytes;
    private volatile boolean closed;
    private final java.util.concurrent.atomic.AtomicBoolean maintenancePending = new java.util.concurrent.atomic.AtomicBoolean();
    private final byte[] digestKey = new byte[32];
    private final AtomicLong dropped = new AtomicLong();
    private final AtomicLong saveFailures = new AtomicLong();
    private final AtomicLong lastAlert = new AtomicLong();

    public CatholicTraceRecorder(CatholicTraceSink sink) {
        this(sink, Limits.defaults(), ContentMode.STRUCTURE,
                Set.of("flow", "candidateId", "roundLabel"), message -> System.getLogger(
                        CatholicTraceRecorder.class.getName()).log(System.Logger.Level.INFO, message));
    }

    public CatholicTraceRecorder(CatholicTraceSink sink, Limits limits, ContentMode mode,
                                Set<String> allowedTags, Consumer<String> status) {
        new java.security.SecureRandom().nextBytes(digestKey);
        this.sink = java.util.Objects.requireNonNull(sink);
        this.limits = java.util.Objects.requireNonNull(limits);
        this.mode = java.util.Objects.requireNonNull(mode);
        this.allowedTags = Set.copyOf(allowedTags);
        this.status = java.util.Objects.requireNonNull(status);
        writer = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(limits.queuedTraces()), r -> daemon(r, "keel-trace-writer"),
                new ThreadPoolExecutor.AbortPolicy());
        timer = Executors.newSingleThreadScheduledExecutor(r -> daemon(r, "keel-trace-expiry"));
        long period = Math.max(1, Math.min(1000, limits.ttl().toMillis()));
        timer.scheduleWithFixedDelay(() -> {
            for (var session : active) if (session.expired()) session.discard("expired");
        }, period, period, TimeUnit.MILLISECONDS);
        timer.scheduleWithFixedDelay(this::scheduleMaintenance, 60, 60, TimeUnit.SECONDS);
    }

    private void scheduleMaintenance() {
        if (closed || !maintenancePending.compareAndSet(false, true)) return;
        try {
            writer.execute(() -> {
                try { sink.maintenance(); }
                catch (Throwable cause) {
                    alert("trace_retention_failed cause_type=" + cause.getClass().getName());
                } finally { maintenancePending.set(false); }
            });
        } catch (RejectedExecutionException ignored) { maintenancePending.set(false); }
    }

    String digest(String text) {
        try {
            var mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(digestKey, "HmacSHA256"));
            return java.util.HexFormat.of().formatHex(mac.doFinal(text.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (java.security.GeneralSecurityException e) { throw new IllegalStateException(e); }
    }

    private static Thread daemon(Runnable action, String name) {
        var thread = new Thread(action, name);
        thread.setDaemon(true);
        return thread;
    }

    public synchronized CatholicTraceSession start(Map<String, String> tags) {
        if (closed) {
            captureDropped("recorder_closed");
            return CatholicTraceSession.disabled();
        }
        if (active.size() >= limits.activeRuns()) {
            captureDropped("active_run_limit");
            return CatholicTraceSession.disabled();
        }
        var accepted = new java.util.LinkedHashMap<String, String>();
        tags.forEach((key, value) -> {
            if (allowedTags.contains(key) && accepted.size() < 16 && value != null)
                accepted.put(key.substring(0, Math.min(key.length(), 64)), value.substring(0, Math.min(value.length(), 128)));
        });
        // Charge session metadata even when it never receives events.
        if (!reserve(16384)) {
            captureDropped("global_budget");
            return CatholicTraceSession.disabled();
        }
        var session = new CatholicTraceSession(this, accepted);
        active.add(session);
        return session;
    }

    synchronized boolean reserve(long bytes) {
        if (bytes > limits.totalBytes() - retainedBytes) return false;
        retainedBytes += bytes;
        return true;
    }
    synchronized void release(long bytes) { retainedBytes -= bytes; }
    void finished(CatholicTraceSession session) { active.remove(session); }
    public synchronized long retainedBytes() { return retainedBytes; }
    public long captureDroppedCount() { return dropped.get(); }
    public long saveFailureCount() { return saveFailures.get(); }

    void captureDropped(String reason) {
        dropped.incrementAndGet();
        alert("trace_capture_dropped reason=" + reason);
    }
    private void alert(String message) {
        long now = System.nanoTime(), previous = lastAlert.get();
        if ((previous == 0 || now - previous > TimeUnit.SECONDS.toNanos(5)) && lastAlert.compareAndSet(previous, now))
            notice(message);
    }
    void notice(String message) {
        try { status.accept(message); }
        catch (Throwable ignored) {
            System.getLogger(CatholicTraceRecorder.class.getName()).log(System.Logger.Level.WARNING,
                    "trace status listener failed");
        }
    }

    void enqueue(CatholicTraceSession session, long bytes) {
        synchronized (this) {
            if (bytes > limits.queuedBytes() - queuedBytes) {
                rejected(session, "queue_byte_limit");
                return;
            }
            queuedBytes += bytes;
        }
        try {
            writer.execute(() -> {
                try {
                    String location = java.util.Objects.requireNonNull(sink.save(session.snapshot()), "trace sink returned null location");
                    session.saved(location);
                    notice("Trace saved trace_id=" + session.traceId() + " location=" + location);
                } catch (Throwable cause) {
                    saveFailures.incrementAndGet();
                    session.saveFailed(cause);
                    alert("trace_save_failed trace_id=" + session.traceId() + " cause_type=" + cause.getClass().getName());
                } finally {
                    synchronized (CatholicTraceRecorder.this) { queuedBytes -= bytes; }
                    session.release();
                }
            });
        } catch (RejectedExecutionException cause) {
            synchronized (this) { queuedBytes -= bytes; }
            rejected(session, "queue_full_or_closed");
        }
    }

    private void rejected(CatholicTraceSession session, String reason) {
        saveFailures.incrementAndGet();
        session.saveFailed(new IllegalStateException(reason));
        session.release();
        alert("trace_save_failed trace_id=" + session.traceId() + " reason=" + reason);
    }

    /** Stop accepting runs, release unfinished captures and asynchronously drain queued saves. */
    @Override public void close() {
        synchronized (this) { closed = true; }
        timer.shutdownNow();
        for (var session : active) session.discard("recorder_closed");
        writer.shutdown();
    }

    /** Blocking shutdown wait for application shutdown threads, never an event loop. */
    public boolean awaitClosed(Duration timeout) throws InterruptedException {
        return writer.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }
}
