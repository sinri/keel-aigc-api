package io.github.sinri.keel.aigc.api.trace;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/** One interaction's bounded evidence. Success discards; failure freezes and schedules persistence once. */
public final class CatholicTraceSession implements AutoCloseable {
    public enum State { ACTIVE, QUEUED, SAVED, SAVE_FAILED, DISCARDED }
    private final CatholicTraceRecorder recorder;
    private final String id = UUID.randomUUID().toString();
    private final Instant started = Instant.now();
    private final long startedNanos = System.nanoTime();
    private final Map<String, String> tags;
    private final ArrayDeque<Entry> events = new ArrayDeque<>();
    private final CompletableFuture<String> persistence = new CompletableFuture<>();
    private State state = State.ACTIVE;
    private long bytes = 16384;
    private long sequence;
    private long dropped;
    private boolean truncated;
    private boolean partial;
    private String failureStage;
    private String failureType;
    private String disposition;
    private static final Pattern LOCATION = Pattern.compile("line: (\\d+), column: (\\d+)");

    CatholicTraceSession(CatholicTraceRecorder recorder, Map<String, String> tags) {
        this.recorder = recorder;
        this.tags = Map.copyOf(tags);
    }
    static CatholicTraceSession disabled() {
        var session = new CatholicTraceSession(null, Map.of());
        session.bytes = 0;
        session.state = State.DISCARDED;
        session.disposition = "capture_unavailable";
        session.persistence.completeExceptionally(new IllegalStateException("trace capture unavailable"));
        return session;
    }
    public String traceId() { return id; }
    public CatholicTraceContext context() {
        return recorder == null ? CatholicTraceContext.none() : new CatholicTraceContext(this, Map.of());
    }
    public synchronized State state() { return state; }
    /** Completes with location on save, null on normal discard, exceptionally on failed capture/save. */
    public java.util.concurrent.CompletionStage<String> persistence() { return persistence.minimalCompletionStage(); }
    boolean expired() { return System.nanoTime() - startedNanos >= recorder.limits.ttl().toNanos(); }

    synchronized void record(String kind, Map<String, Object> context, Map<String, ?> metadata,
                             String payload, boolean arguments) {
        if (state != State.ACTIVE || recorder == null) return;
        var fields = new LinkedHashMap<String, Object>();
        copyFields(fields, context);
        copyFields(fields, metadata);
        if ("partial".equals(fields.get("capture_capability"))) partial = true;
        if (Boolean.TRUE.equals(fields.get("transport_truncated"))) truncated = true;
        long seq = sequence++;
        int originalLength = payload == null ? 0 : payload.length();
        int cap = Math.min(recorder.limits.payloadCharacters(), (int)Math.min(Integer.MAX_VALUE,
                Math.max(0, (recorder.limits.runBytes() - 32768) / 16)));
        String head = null, tail = null;
        if (payload != null) {
            int keep = Math.min(originalLength, cap);
            int prefix = originalLength <= cap ? keep : keep / 2;
            head = payload.substring(0, prefix);
            if (originalLength > cap) {
                tail = payload.substring(originalLength - (keep - prefix));
                truncated = true;
            }
        }
        long charge = 2048L + 16L * ((head == null ? 0 : head.length()) + (tail == null ? 0 : tail.length()));
        for (var field : fields.entrySet()) charge += 256L + 8L * (field.getKey().length()
                + (field.getValue() instanceof String string ? string.length() : 32));
        while (!events.isEmpty() && (bytes + charge > recorder.limits.runBytes()
                || events.size() >= recorder.limits.eventsPerRun())) evict();
        if (bytes + charge > recorder.limits.runBytes() || !recorder.reserve(charge)) {
            dropped++;
            recorder.captureDropped("buffer_budget");
            return;
        }
        events.add(new Entry(seq, System.nanoTime() - startedNanos, kind.substring(0, Math.min(kind.length(), 128)), fields, head, tail,
                originalLength, arguments, charge));
        bytes += charge;
    }

    private void copyFields(Map<String, Object> target, Map<String, ?> source) {
        // Never retain arbitrary graphs or large diagnostic values.
        for (var entry : source.entrySet()) {
            if (target.size() >= 20) { truncated = true; break; }
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value == null) continue;
            if (key.length() > 64 || value instanceof String string && string.length() > 128) truncated = true;
            Object scalar;
            if (value instanceof Integer || value instanceof Long || value instanceof Double || value instanceof Boolean) scalar = value;
            else if (value instanceof String string) scalar = string.substring(0, Math.min(128, string.length()));
            else scalar = value.getClass().getName();
            target.put(key.substring(0, Math.min(64, key.length())), scalar);
        }
    }
    synchronized void captureFailed() {
        if (state != State.ACTIVE) return;
        dropped++;
        if (recorder != null) recorder.captureDropped("capture_exception");
    }

    private void evict() {
        var entry = events.removeFirst();
        bytes -= entry.charge;
        recorder.release(entry.charge);
        dropped++;
        recorder.captureDropped("run_budget_or_event_limit");
    }

    synchronized void failure(CatholicTraceContext context, String stage, Throwable cause) {
        if (state != State.ACTIVE) return;
        if (failureStage == null) {
            failureStage = stage;
            failureType = cause.getClass().getName();
        }
        var details = new LinkedHashMap<String, Object>();
        details.put("stage", stage);
        details.put("exception_type", cause.getClass().getName());
        String message = cause.getMessage();
        if (message != null) {
            var match = LOCATION.matcher(message.substring(0, Math.min(message.length(), 4096)));
            int found = 0;
            while (match.find() && found < 2) {
                details.put("location_" + found + "_line", Long.parseLong(match.group(1)));
                details.put("location_" + found + "_column", Long.parseLong(match.group(2)));
                found++;
            }
        }
        context.event("failure", details);
        Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (int depth = 0; cause != null && depth < 4 && seen.add(cause); depth++, cause = cause.getCause()) {
            var frames = cause.getStackTrace();
            StringBuilder stack = new StringBuilder();
            for (int i = 0; i < Math.min(frames.length, 20); i++) stack.append(frames[i]).append('\n');
            context.payload("exception_stack", Map.of("cause_depth", depth,
                    "exception_type", cause.getClass().getName()), stack.toString(), false);
        }
    }

    /** Record failure and freeze once. The original Throwable is not retained in the buffer. */
    public synchronized void fail(String stage, Throwable cause) {
        if (state != State.ACTIVE) return;
        context().failure(stage, cause);
        state = State.QUEUED;
        recorder.finished(this);
        recorder.notice("Agent failed trace_id=" + id + " stage=" + failureStage + " capture_status=queued");
        recorder.enqueue(this, bytes);
    }

    /** Complete a standalone operation; caller may instead delay fail/succeed until its own post-processing finishes. */
    public <T> io.vertx.core.Future<T> track(io.vertx.core.Future<T> operation) {
        return operation.andThen(ar -> {
            if (ar.failed()) fail("OPERATION", ar.cause()); else succeed();
        });
    }

    public void succeed() { discard("success"); }
    @Override public void close() { discard("cancelled"); }
    synchronized void discard(String reason) {
        if (state != State.ACTIVE) return;
        state = State.DISCARDED;
        disposition = reason;
        recorder.finished(this);
        release();
        if (reason.equals("success")) persistence.complete(null);
        else {
            recorder.captureDropped(reason);
            persistence.completeExceptionally(new IllegalStateException("trace discarded: " + reason));
        }
    }
    synchronized void saved(String location) { state = State.SAVED; persistence.complete(location); }
    synchronized void saveFailed(Throwable cause) { state = State.SAVE_FAILED; persistence.completeExceptionally(cause); }
    synchronized void release() {
        events.clear();
        if (recorder != null) recorder.release(bytes);
        bytes = 0;
    }

    /** Worker-only serialization. STRUCTURE never includes prompt, result or raw transport text. */
    synchronized JsonObject snapshot() {
        JsonArray items = new JsonArray();
        for (Entry entry : events) {
            JsonObject item = new JsonObject(new LinkedHashMap<>(entry.fields)).put("sequence", entry.sequence)
                    .put("elapsed_nanos", entry.elapsedNanos).put("kind", entry.kind)
                    .put("original_characters", entry.originalLength);
            boolean raw = recorder.mode == CatholicTraceRecorder.ContentMode.RESTRICTED_RAW;
            boolean diagnostic = entry.kind.equals("exception_stack");
            if (entry.head != null && (raw || entry.arguments || diagnostic)) {
                item.put("payload", raw || diagnostic ? entry.head : structure(entry.head));
                item.put("representation", raw || diagnostic ? "original" : "structure");
                if (entry.tail != null) item.put("tail", raw || diagnostic ? entry.tail : structure(entry.tail))
                        .put("tail_offset", entry.originalLength - entry.tail.length());
            }
            if (entry.head != null) item.put("payload_hmac", recorder.digest(entry.head))
                    .put("digest_scope", entry.tail == null ? "complete" : "head_only");
            if (entry.tail != null) item.put("truncated", true).put("tail_hmac", recorder.digest(entry.tail));
            items.add(item);
        }
        return new JsonObject().put("schema_version", 1).put("trace_id", id)
                .put("library_version", Objects.requireNonNullElse(CatholicTraceSession.class.getPackage().getImplementationVersion(), "unpackaged"))
                .put("java_version", System.getProperty("java.version"))
                .put("started_at", started.toString()).put("tags", new JsonObject(new LinkedHashMap<>(tags)))
                .put("content_mode", recorder.mode.name()).put("failure_stage", failureStage)
                .put("failure_type", failureType).put("disposition", disposition)
                .put("dropped_events", dropped).put("truncated", truncated)
                .put("capture_capability", partial ? "partial" : "instrumented")
                .put("evidence_complete", !partial && !truncated && dropped == 0)
                .put("events", items);
    }

    /** Conservative character mask works on malformed JSON and independently split deltas, without guessing string boundaries. */
    public static String structure(String value) {
        StringBuilder result = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            result.append("{}[],:\"\\\r\n\t ".indexOf(c) >= 0 ? c : 'x');
        }
        return result.toString();
    }
    private record Entry(long sequence, long elapsedNanos, String kind, Map<String, Object> fields,
                         String head, String tail, int originalLength, boolean arguments, long charge) {}
}
