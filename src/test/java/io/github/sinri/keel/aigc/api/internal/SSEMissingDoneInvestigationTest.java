package io.github.sinri.keel.aigc.api.internal;

import io.github.sinri.keel.aigc.api.internal.catholic.response.CatholicLLMResponseChunkImpl;
import io.github.sinri.keel.aigc.api.internal.catholic.response.CatholicResponseChunkCollector;
import io.github.sinri.keel.aigc.api.internal.openai.chatcompletions.OpenAIChatCompletionsStreamHandler;
import io.github.sinri.keel.base.async.Keel;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/** Local HTTP probes documenting current behavior; no external service or credentials. */
class SSEMissingDoneInvestigationTest {
    private static final String DATA = "data: {\"id\":\"probe\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"hello\"},\"finish_reason\":\"stop\"}]}";

    private static <T> T await(Future<T> future) throws Exception {
        return future.toCompletionStage().toCompletableFuture().get(15, TimeUnit.SECONDS);
    }

    @Test
    void aggregateReportsDiagnosticsAndRejectsEmptyOrUnfinishedStreams() throws Exception {
        var vertx = Vertx.vertx();
        var keel = Keel.create(vertx);
        try {
            String[] bodies = {DATA + "\n\n", "data: [DONE]\n\n", DATA, DATA.replace("\"stop\"", "null") + "\n\n"};
            for (int i = 0; i < bodies.length; i++) {
                String body = bodies[i];
                var diagnostics = new java.util.concurrent.ConcurrentHashMap<String, java.util.Map<String, Object>>();
                var exchangeIds = java.util.concurrent.ConcurrentHashMap.<String>newKeySet();
                var stages = new java.util.concurrent.CopyOnWriteArrayList<io.github.sinri.keel.aigc.api.llm.catholic.observation.CatholicLLMObservationStage>();
                var observer = new io.github.sinri.keel.aigc.api.llm.catholic.observation.CatholicLLMObserver() {
                    public void onStreamDiagnostic(String id, String provider, String phase,
                            java.util.Map<String, Object> details, long elapsed) {
                        exchangeIds.add(id);
                        diagnostics.put(phase, details);
                    }
                    public void onFailure(String id, String provider,
                            io.github.sinri.keel.aigc.api.llm.catholic.observation.CatholicLLMObservationStage stage,
                            Throwable cause, long elapsed) { stages.add(stage); }
                };
                var server = await(vertx.createHttpServer().requestHandler(req -> req.response().end(body)).listen(0, "127.0.0.1"));
                var client = vertx.createHttpClient();
                try {
                    var llm = io.github.sinri.keel.aigc.api.llm.openai.chatcompletions.OpenAIChatCompletionsLLM.builder()
                        .keel(keel).httpClient(client).apiKey("local-test").baseUrl("http://127.0.0.1:" + server.actualPort())
                        .observer(observer).build();
                    var request = io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest.builder().model("probe")
                        .addMessage(io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicUserMessage.ofText("probe")).build();
                    if (i == 0) assertEquals("hello", await(llm.callStream(request)).text());
                    else {
                        var failure = assertThrows(java.util.concurrent.ExecutionException.class, () -> await(llm.callStream(request)));
                        assertInstanceOf(IllegalStateException.class, failure.getCause());
                        assertTrue(stages.contains(io.github.sinri.keel.aigc.api.llm.catholic.observation.CatholicLLMObservationStage.RESPONSE_CONVERSION));
                    }
                    assertEquals(1, exchangeIds.size());
                    assertEquals(i == 1, diagnostics.get("stream_completed").get("done_seen"));
                    assertEquals(i == 2 ? DATA.length() : 0, diagnostics.get("transport_completed").get("pending_bytes"));
                    assertNotNull(diagnostics.get("response_build").get("collector"));
                } finally { await(client.close()); await(server.close()); }
            }
        } finally { await(vertx.close()); }
    }

    @Test
    void preservesAsyncProcessorFailureAndIsolatesBrokenObserver() throws Exception {
        var vertx = Vertx.vertx();
        var keel = Keel.create(vertx);
        var original = new IllegalArgumentException("processor failure");
        try {
            var server = await(vertx.createHttpServer().requestHandler(req -> req.response().end(DATA + "\n\n" + DATA + "\n\n"))
                .listen(0, "127.0.0.1"));
            var client = vertx.createHttpClient();
            var calls = new AtomicInteger();
            var observer = new io.github.sinri.keel.aigc.api.llm.catholic.observation.CatholicLLMObserver() {
                public void onStreamDiagnostic(String id, String provider, String phase, java.util.Map<String, Object> details, long elapsed) {
                    throw new IllegalStateException("broken observer");
                }
            };
            var handler = new OpenAIChatCompletionsStreamHandler();
            var future = client.request(HttpMethod.GET, server.actualPort(), "127.0.0.1", "/").compose(req -> req.send())
                .compose(response -> SSE2Chunk.processOpenAiStyleSSEStream(keel, response, "probe", handler::parseSseLineOnly,
                    chunk -> { calls.incrementAndGet(); return keel.asyncSleep(50L).compose(v -> Future.failedFuture(original)); },
                    observer, io.github.sinri.keel.aigc.api.internal.catholic.observation.CatholicLLMObservationSupport.exchange("probe", "", true)));
            var failure = assertThrows(java.util.concurrent.ExecutionException.class, () -> await(future));
            assertSame(original, failure.getCause());
            assertEquals(1, calls.get());
            await(client.close()); await(server.close());
        } finally { await(vertx.close()); }
    }

    @Test
    void compareFramingAndCompletionAcrossBothCollectionPaths() throws Exception {
        var vertx = Vertx.vertx();
        var keel = Keel.create(vertx);
        String[] bodies = {DATA + "\n\ndata: [DONE]\n\n", DATA + "\n\n", DATA + "\n", DATA,
                DATA + "\r\n\r\n", "data: {invalid json}\n\n"};
        String[] labels = {"done", "no-done-complete-event", "no-done-single-newline", "no-done-no-newline", "crlf", "invalid-json"};
        try {
            for (boolean separateCollector : new boolean[]{false, true}) {
                for (int i = 0; i < bodies.length; i++) {
                    String body = bodies[i];
                    var server = await(vertx.createHttpServer().requestHandler(req -> {
                        req.response().putHeader("Content-Type", "text/event-stream");
                        req.response().end(body);
                    }).listen(0, "127.0.0.1"));
                    var client = vertx.createHttpClient();
                    try {
                        var handler = new OpenAIChatCompletionsStreamHandler();
                        var collector = new CatholicResponseChunkCollector();
                        var count = new AtomicInteger();
                        var response = await(client.request(HttpMethod.GET, server.actualPort(), "127.0.0.1", "/").compose(req -> req.send()).map(r -> { r.pause(); return r; }));
                        Future<Void> processing = SSE2Chunk.processOpenAiStyleSSEStream(keel, response, "probe",
                                separateCollector ? handler::parseSseLineOnly : handler::processSseLine,
                                chunk -> {
                                    count.incrementAndGet();
                                    if (separateCollector) collector.collect((CatholicLLMResponseChunkImpl) chunk);
                                    return Future.succeededFuture();
                                });
                        if (i == 5) {
                            var failure = assertThrows(java.util.concurrent.ExecutionException.class, () -> await(processing));
                            assertInstanceOf(IllegalArgumentException.class, failure.getCause());
                            continue;
                        }
                        await(processing);
                        var result = separateCollector ? collector.build() : handler.buildFinalResponse();
                        System.out.printf("PROBE separate=%s case=%s chunks=%d id=%s text=%s finished=%s%n",
                                separateCollector, labels[i], count.get(), result.id(), result.text(), result.finished());
                        assertEquals(i < 2 ? 1 : 0, count.get(), labels[i]);
                        assertEquals(i < 2 ? "hello" : null, result.text(), labels[i]);
                    } finally {
                        await(client.close());
                        await(server.close());
                    }
                }
            }
        } finally {
            await(vertx.close());
        }
    }

    @Test
    void probeImmediateResponseThroughProductionComposition() throws Exception {
        var vertx = Vertx.vertx();
        var keel = Keel.create(vertx);
        try {
            var server = await(vertx.createHttpServer().requestHandler(req -> req.response().end(DATA + "\n\n"))
                    .listen(0, "127.0.0.1"));
            var client = vertx.createHttpClient();
            int failures = 0;
            for (int i = 0; i < 10; i++) {
                var handler = new OpenAIChatCompletionsStreamHandler();
                try {
                    await(client.request(HttpMethod.GET, server.actualPort(), "127.0.0.1", "/")
                            .compose(req -> req.send())
                            .compose(response -> SSE2Chunk.processOpenAiStyleSSEStream(keel, response,
                                    "probe", handler::processSseLine, chunk -> Future.succeededFuture())));
                    assertEquals("hello", handler.buildFinalResponse().text());
                } catch (java.util.concurrent.ExecutionException e) {
                    failures++;
                    assertEquals("Response already ended", e.getCause().getMessage());
                }
            }
            assertEquals(0, failures, "fast responses must not end before handlers are installed");
            await(client.close());
            await(server.close());
        } finally {
            await(vertx.close());
        }
    }

    @Test
    void missingEventBoundaryDropsFinalToolArguments() throws Exception {
        var vertx = Vertx.vertx();
        var keel = Keel.create(vertx);
        String first = "data: {\"id\":\"probe\",\"choices\":[{\"delta\":{\"tool_calls\":[{\"index\":0,\"id\":\"call-1\",\"function\":{\"name\":\"lookup\",\"arguments\":\"{\"}}]}}]}\n\n";
        String last = "data: {\"id\":\"probe\",\"choices\":[{\"delta\":{\"tool_calls\":[{\"index\":0,\"function\":{\"arguments\":\"}\"}}]},\"finish_reason\":\"tool_calls\"}]}";
        try {
            for (String ending : new String[]{"\n\n", "\n"}) {
                var server = await(vertx.createHttpServer().requestHandler(req -> req.response().end(first + last + ending))
                        .listen(0, "127.0.0.1"));
                var client = vertx.createHttpClient();
                var handler = new OpenAIChatCompletionsStreamHandler();
                var collector = new CatholicResponseChunkCollector();
                var response = await(client.request(HttpMethod.GET, server.actualPort(), "127.0.0.1", "/")
                        .compose(req -> req.send()).map(r -> { r.pause(); return r; }));
                await(SSE2Chunk.processOpenAiStyleSSEStream(keel, response, "probe", handler::parseSseLineOnly,
                        chunk -> { collector.collect((CatholicLLMResponseChunkImpl) chunk); return Future.succeededFuture(); }));
                var result = collector.build();
                assertEquals(ending.length() == 2 ? "{}" : "{", result.message().toolCalls().get(0).function().arguments());
                assertEquals(ending.length() == 2, result.finished());
                System.out.printf("PROBE tool boundary=%d arguments=%s finished=%s%n", ending.length(),
                        result.message().toolCalls().get(0).function().arguments(), result.finished());
                await(client.close());
                await(server.close());
            }
        } finally {
            await(vertx.close());
        }
    }

    @Test
    void completionWaitsForDelayedChunkProcessorWithoutDone() throws Exception {
        var vertx = Vertx.vertx();
        var keel = Keel.create(vertx);
        try {
            var server = await(vertx.createHttpServer().requestHandler(req -> req.response().end(DATA + "\n\n"))
                    .listen(0, "127.0.0.1"));
            var client = vertx.createHttpClient();
            var handler = new OpenAIChatCompletionsStreamHandler();
            var processed = new AtomicInteger();
            var response = await(client.request(HttpMethod.GET, server.actualPort(), "127.0.0.1", "/").compose(req -> req.send()).map(r -> { r.pause(); return r; }));
            await(SSE2Chunk.processOpenAiStyleSSEStream(keel, response, "probe", handler::processSseLine,
                    chunk -> keel.asyncSleep(500L).map(v -> { processed.incrementAndGet(); return null; })));
            assertEquals(1, processed.get());
            await(client.close());
            await(server.close());
        } finally {
            await(vertx.close());
        }
    }
}
