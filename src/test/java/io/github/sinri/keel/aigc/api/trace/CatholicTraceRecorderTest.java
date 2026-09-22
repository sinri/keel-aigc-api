package io.github.sinri.keel.aigc.api.trace;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class CatholicTraceRecorderTest {
    @TempDir Path directory;
    private static final RuntimeException FAILURE = new IllegalArgumentException("secret-in-exception");
    static CatholicTraceRecorder recorder(CatholicTraceSink sink, CatholicTraceRecorder.ContentMode mode) {
        return new CatholicTraceRecorder(sink, CatholicTraceRecorder.Limits.defaults(), mode,
                Set.of("candidateId"), message -> {});
    }
    static String saved(CatholicTraceSession s) throws Exception {
        return s.persistence().toCompletableFuture().get(5, TimeUnit.SECONDS);
    }

    @Test void capturesMalformedStructureAndDiscardsSuccess() throws Exception {
        var snapshots = new CopyOnWriteArrayList<JsonObject>();
        var recorder = recorder(trace -> { snapshots.add(trace); return "memory"; }, CatholicTraceRecorder.ContentMode.STRUCTURE);
        var success = recorder.start(Map.of());
        success.context().payload("provider_request", Map.of(), "secret prompt", false);
        success.succeed();
        assertNull(saved(success));
        assertTrue(snapshots.isEmpty());
        assertEquals(0, recorder.retainedBytes());
        var failed = recorder.start(Map.of("candidateId", "42", "password", "excluded"));
        String args = "{\n\"password\": \"ab\\\"cd😀\", \"bad\": {]}";
        failed.context().payload("tool_arguments_ready", Map.of(), args, true);
        failed.context().payload("provider_request", Map.of(), "secret prompt", false);
        failed.fail("TOOL_ARGUMENT_PARSE", FAILURE);
        failed.fail("AGENT", FAILURE);
        assertEquals("memory", saved(failed));
        recorder.close(); assertTrue(recorder.awaitClosed(Duration.ofSeconds(5)));
        assertEquals(1, snapshots.size());
        var snapshot = snapshots.get(0);
        assertEquals("TOOL_ARGUMENT_PARSE", snapshot.getString("failure_stage"));
        String structure = snapshot.getJsonArray("events").getJsonObject(0).getString("payload");
        assertEquals(args.length(), structure.length());
        assertEquals(args.indexOf('\n'), structure.indexOf('\n'));
        assertFalse(snapshot.encode().contains("password"));
        assertFalse(snapshot.encode().contains("secret prompt"));
        assertFalse(snapshot.encode().contains("secret-in-exception"));
        assertFalse(snapshot.encode().contains("😀"));
        assertEquals(0, recorder.retainedBytes());
        assertThrows(IllegalArgumentException.class, () -> CatholicTraceReplay.replayStream(snapshot, "x"));
    }

    @Test void queueFailureIsVisibleAndDoesNotReplaceBusinessError() throws Exception {
        var messages = new CopyOnWriteArrayList<String>();
        var recorder = new CatholicTraceRecorder(trace -> { throw new java.io.IOException("disk full"); },
                CatholicTraceRecorder.Limits.defaults(), CatholicTraceRecorder.ContentMode.STRUCTURE, Set.of(), messages::add);
        var session = recorder.start(Map.of());
        var result = session.track(Future.failedFuture(FAILURE));
        assertSame(FAILURE, result.cause());
        assertThrows(ExecutionException.class, () -> saved(session));
        recorder.close(); assertTrue(recorder.awaitClosed(Duration.ofSeconds(5)));
        assertEquals(1, recorder.saveFailureCount());
        assertEquals(0, recorder.retainedBytes());
        assertTrue(messages.stream().anyMatch(m -> m.contains("trace_save_failed")));
        assertFalse(messages.stream().anyMatch(m -> m.startsWith("Trace saved")));
    }

    @Test void boundsRunGlobalAndQueuedMemoryAndReportsLoss() throws Exception {
        var gate = new CountDownLatch(1);
        var entered = new CountDownLatch(1);
        var snapshots = new CopyOnWriteArrayList<JsonObject>();
        var limits = new CatholicTraceRecorder.Limits(131072, 262144, 8, 256, 3, 1, 131072, Duration.ofMinutes(1));
        var recorder = new CatholicTraceRecorder(t -> { entered.countDown(); gate.await(5, TimeUnit.SECONDS); snapshots.add(t); return "ok"; },
                limits, CatholicTraceRecorder.ContentMode.RESTRICTED_RAW, Set.of(), m -> {});
        try {
            var first = recorder.start(Map.of());
            for (int i = 0; i < 100; i++) first.context().payload("delta", Map.of("i", i), "a".repeat(10000), true);
            first.fail("TEST", FAILURE);
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            var second = recorder.start(Map.of());
            second.fail("TEST", FAILURE);
            var third = recorder.start(Map.of());
            third.fail("TEST", FAILURE);
            assertTrue(recorder.retainedBytes() <= limits.totalBytes());
            assertTrue(recorder.captureDroppedCount() > 0);
            assertTrue(recorder.saveFailureCount() > 0);
            gate.countDown();
        } finally { gate.countDown(); recorder.close(); assertTrue(recorder.awaitClosed(Duration.ofSeconds(5))); }
        assertEquals(0, recorder.retainedBytes());
        assertFalse(snapshots.get(0).getBoolean("evidence_complete"));
        assertTrue(snapshots.get(0).getLong("dropped_events") > 0);
    }

    @Test void expiresUnfinishedRunsAndLimitsActiveSessions() throws Exception {
        var limits = new CatholicTraceRecorder.Limits(65536, 131072, 8, 64, 1, 1, 65536, Duration.ofMillis(50));
        var recorder = new CatholicTraceRecorder(t -> "unused", limits,
                CatholicTraceRecorder.ContentMode.STRUCTURE, Set.of(), m -> {});
        var first = recorder.start(Map.of());
        var rejected = recorder.start(Map.of());
        assertEquals(CatholicTraceSession.State.DISCARDED, rejected.state());
        assertThrows(ExecutionException.class, () -> saved(first));
        assertEquals(CatholicTraceSession.State.DISCARDED, first.state());
        recorder.close(); assertTrue(recorder.awaitClosed(Duration.ofSeconds(5)));
        assertEquals(0, recorder.retainedBytes());
    }

    @Test void fileSinkPublishesPrivateFilesAndEnforcesRetention() throws Exception {
        var sink = new CatholicFileTraceSink(directory, 100000, 1, Duration.ofDays(1));
        String first = sink.save(new JsonObject().put("trace_id", UUID.randomUUID().toString()));
        assertTrue(Files.exists(Path.of(first)));
        String second = sink.save(new JsonObject().put("trace_id", UUID.randomUUID().toString()));
        assertFalse(Files.exists(Path.of(first)));
        assertTrue(Files.exists(Path.of(second)));
        try (var files = Files.list(directory)) { assertEquals(1, files.count()); }
        if (Files.getFileStore(directory).supportsFileAttributeView("posix"))
            assertEquals("rw-------", java.nio.file.attribute.PosixFilePermissions.toString(Files.getPosixFilePermissions(Path.of(second))));
    }
    @Test void slowPersistenceDoesNotBlockEventLoopAndCaptureCostIsMeasured() throws Exception {
        var entered = new CountDownLatch(1);
        var gate = new CountDownLatch(1);
        var recorder = recorder(t -> {
            assertFalse(io.vertx.core.Context.isOnEventLoopThread());
            entered.countDown();
            assertTrue(gate.await(5, TimeUnit.SECONDS));
            return "memory";
        }, CatholicTraceRecorder.ContentMode.STRUCTURE);
        var vertx = io.vertx.core.Vertx.vertx();
        try {
            var completed = new CompletableFuture<Void>();
            var durations = new long[100];
            vertx.runOnContext(v -> {
                try {
                    for (int i = 0; i < durations.length; i++) {
                        var session = recorder.start(Map.of());
                        long start = System.nanoTime();
                        for (int j = 0; j < 16; j++) session.context().payload("arguments", Map.of("index", j),
                                "{\"value\":\"" + "s".repeat(1024) + "\"}", true);
                        durations[i] = System.nanoTime() - start;
                        session.succeed();
                    }
                    var failed = recorder.start(Map.of());
                    failed.fail("TEST", FAILURE);
                    // This timer must fire while the writer is deliberately blocked on the gate.
                    vertx.setTimer(10, ignored -> completed.complete(null));
                } catch (Throwable error) { completed.completeExceptionally(error); }
            });
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            completed.get(5, TimeUnit.SECONDS);
            Arrays.sort(durations);
            System.out.printf("TRACE_CAPTURE 16x1KiB p50_us=%d p95_us=%d max_us=%d retained_bytes=%d%n",
                    durations[50] / 1000, durations[95] / 1000, durations[99] / 1000, recorder.retainedBytes());
        } finally {
            gate.countDown(); recorder.close(); assertTrue(recorder.awaitClosed(Duration.ofSeconds(5)));
            vertx.close().toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
        }
    }

    @Test void splitSensitiveValuesAndEscapesDoNotLeakInStructureMode() throws Exception {
        var traces = new CopyOnWriteArrayList<JsonObject>();
        var recorder = recorder(t -> { traces.add(t); return "memory"; }, CatholicTraceRecorder.ContentMode.STRUCTURE);
        var session = recorder.start(Map.of());
        String[] parts = {"{\"password\":\"sec", "ret-123\\", "\"end😀\"}"};
        for (String part : parts) session.context().payload("converted_arguments_delta", Map.of("tool_index", 0), part, true);
        session.fail("TEST", FAILURE);
        saved(session); recorder.close(); assertTrue(recorder.awaitClosed(Duration.ofSeconds(5)));
        String joined = String.join("", parts);
        StringBuilder masked = new StringBuilder();
        for (int i = 0; i < parts.length; i++) masked.append(traces.get(0).getJsonArray("events").getJsonObject(i).getString("payload"));
        assertEquals(CatholicTraceSession.structure(joined), masked.toString());
        assertEquals(joined.length(), masked.length());
        assertFalse(traces.get(0).encode().contains("secret-123"));
    }

    @Test void concurrentCaptureRespectsGlobalAccounting() throws Exception {
        var limits = new CatholicTraceRecorder.Limits(1L << 20, 4L << 20, 128, 8192, 16, 2, 2L << 20, Duration.ofMinutes(1));
        var recorder = new CatholicTraceRecorder(t -> "unused", limits, CatholicTraceRecorder.ContentMode.STRUCTURE, Set.of(), m -> {});
        var workers = Executors.newFixedThreadPool(8);
        var gate = new CyclicBarrier(8);
        var peak = new java.util.concurrent.atomic.AtomicLong();
        long heapBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        try {
            var jobs = new ArrayList<java.util.concurrent.Future<?>>();
            for (int i = 0; i < 8; i++) jobs.add(workers.submit(() -> {
                try {
                    var session = recorder.start(Map.of());
                    gate.await(5, TimeUnit.SECONDS);
                    for (int j = 0; j < 100; j++) {
                        session.context().payload("delta", Map.of("index", j), "x".repeat(8192), true);
                        long retained = recorder.retainedBytes();
                        peak.accumulateAndGet(retained, Math::max);
                        assertTrue(retained <= limits.totalBytes());
                    }
                    session.succeed();
                } catch (Exception e) { throw new RuntimeException(e); }
            }));
            for (var job : jobs) job.get(10, TimeUnit.SECONDS);
            assertEquals(0, recorder.retainedBytes());
            long heapAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            System.out.printf("TRACE_CONCURRENT workers=8 accounted_peak_bytes=%d budget_bytes=%d observed_heap_delta_bytes=%d%n",
                    peak.get(), limits.totalBytes(), heapAfter - heapBefore);
        } finally { workers.shutdownNow(); recorder.close(); assertTrue(recorder.awaitClosed(Duration.ofSeconds(5))); }
    }

}
