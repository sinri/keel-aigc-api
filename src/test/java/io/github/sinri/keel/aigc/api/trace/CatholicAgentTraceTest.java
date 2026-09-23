package io.github.sinri.keel.aigc.api.trace;

import io.github.sinri.keel.aigc.api.agent.*;
import io.github.sinri.keel.aigc.api.agent.tool.*;
import io.github.sinri.keel.aigc.api.llm.catholic.*;
import io.github.sinri.keel.aigc.api.llm.catholic.message.*;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.NativeFunctionAdapter;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.*;
import io.github.sinri.keel.aigc.api.internal.catholic.response.CatholicLLMResponseImpl;
import io.github.sinri.keel.aigc.api.llm.openai.chatcompletions.OpenAIChatCompletionsLLM;
import io.github.sinri.keel.aigc.api.llm.openai.responses.OpenAIResponsesLLM;
import io.github.sinri.keel.aigc.api.llm.anthropic.AnthropicLLM;
import io.github.sinri.keel.aigc.api.llm.dashscope.textgeneration.DashScopeTextGenerationLLM;
import io.github.sinri.keel.aigc.api.llm.dashscope.multimodalgeneration.DashScopeMultimodalGenerationLLM;
import io.github.sinri.keel.base.async.Keel;
import io.vertx.core.*;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import static org.junit.jupiter.api.Assertions.*;

class CatholicAgentTraceTest {
    private static final String BAD = "{\n\"nested\": {\"secret\":\"value\"]}";
    static <T> T await(io.vertx.core.Future<T> future) throws Exception {
        return future.toCompletionStage().toCompletableFuture().get(15, TimeUnit.SECONDS);
    }
    private static String event(JsonObject body) { return "data: " + body.encode() + "\n\n"; }
    private static String event(String body) { return "data: " + body + "\n\n"; }
    private static CatholicToolInvocationHandlerWithNativeFunctionAdapters handler(AtomicInteger invoked) {
        var handler = CatholicToolInvocationHandler.createWithNativeFunctionAdapters();
        handler.registerNativeFunctionAdapter(new NativeFunctionAdapter() {
            public String functionName() { return "lookup"; }
            public String functionDescription() { return "lookup"; }
            public Map<String, ParameterDefinition> parameterDefinitionMap() { return Map.of(); }
            public io.vertx.core.Future<String> call(JsonObject fixed, JsonObject args) {
                invoked.incrementAndGet(); return io.vertx.core.Future.succeededFuture("ok");
            }
        });
        return handler;
    }
    static String body(String protocol) {
        var function = new JsonObject().put("name", "lookup").put("arguments", BAD);
        var calls = new JsonArray().add(new JsonObject().put("index", 0).put("id", "call-1")
                .put("type", "function").put("function", function));
        return switch (protocol) {
            case "chat" -> event(new JsonObject().put("id", "response-1").put("choices", new JsonArray().add(
                    new JsonObject().put("index", 0).put("delta", new JsonObject().put("tool_calls", calls))
                            .put("finish_reason", "tool_calls"))));
            case "responses" -> event("{\"type\":\"response.created\",\"response\":{\"id\":\"response-1\"}}")
                    + event("{\"type\":\"response.output_item.added\",\"output_index\":0,\"item\":{\"type\":\"function_call\",\"call_id\":\"call-1\",\"name\":\"lookup\"}}")
                    + event(new JsonObject().put("type", "response.function_call_arguments.delta").put("output_index", 0).put("delta", BAD))
                    + event("{\"type\":\"response.completed\",\"response\":{\"id\":\"response-1\"}}");
            case "anthropic" -> event("{\"type\":\"message_start\",\"message\":{\"id\":\"response-1\"}}")
                    + event("{\"type\":\"content_block_start\",\"index\":0,\"content_block\":{\"type\":\"tool_use\",\"id\":\"call-1\",\"name\":\"lookup\"}}")
                    + event(new JsonObject().put("type", "content_block_delta").put("index", 0)
                    .put("delta", new JsonObject().put("type", "input_json_delta").put("partial_json", BAD)))
                    + event("{\"type\":\"message_delta\",\"delta\":{\"stop_reason\":\"tool_use\"}}")
                    + event("{\"type\":\"message_stop\"}");
            default -> event(new JsonObject().put("request_id", "response-1").put("output", new JsonObject().put("choices",
                    new JsonArray().add(new JsonObject().put("message", new JsonObject().put("tool_calls", calls))
                            .put("finish_reason", "tool_calls")))));
        };
    }
    static CatholicLLM llm(String protocol, Keel keel, HttpClient http, String base) {
        return switch (protocol) {
            case "chat" -> OpenAIChatCompletionsLLM.builder().keel(keel).httpClient(http).baseUrl(base).apiKey("never-capture-key").build();
            case "responses" -> OpenAIResponsesLLM.builder().keel(keel).httpClient(http).baseUrl(base).apiKey("never-capture-key").build();
            case "anthropic" -> AnthropicLLM.builder().keel(keel).httpClient(http).baseUrl(base).apiKey("never-capture-key").build();
            case "dash-text" -> DashScopeTextGenerationLLM.builder().keel(keel).httpClient(http).baseUrl(base).apiKey("never-capture-key").build();
            default -> DashScopeMultimodalGenerationLLM.builder().keel(keel).httpClient(http).baseUrl(base).apiKey("never-capture-key").build();
        };
    }

    @Test void allProvidersRetainSuccessfulLlmEvidenceUntilArgumentFailureAndReplayWithoutTools() throws Exception {
        var vertx = Vertx.vertx();
        var keel = Keel.create(vertx);
        try {
            for (String protocol : List.of("chat", "responses", "anthropic", "dash-text", "dash-multi")) {
                var snapshots = new CopyOnWriteArrayList<JsonObject>();
                var recorder = CatholicTraceRecorderTest.recorder(t -> { snapshots.add(t); return "memory"; }, CatholicTraceRecorder.ContentMode.RESTRICTED_RAW);
                var server = await(vertx.createHttpServer().requestHandler(req -> req.bodyHandler(ignored ->
                        req.response().putHeader("Content-Type", "text/event-stream").end(body(protocol)))).listen(0, "127.0.0.1"));
                var http = vertx.createHttpClient();
                try {
                    var counter = new AtomicInteger();
                    var handler = handler(counter);
                    var agent = CatholicAgent.builder().llm(llm(protocol, keel, http, "http://127.0.0.1:" + server.actualPort()))
                            .model("probe").tools(handler.getRegisteredToolDefinitions()).toolHandler(handler).build();
                    var session = recorder.start(Map.of("candidateId", protocol));
                    var failure = assertThrows(ExecutionException.class, () -> await(agent.interact("probe", session)));
                    assertInstanceOf(DecodeException.class, failure.getCause());
                    CatholicTraceRecorderTest.saved(session);
                    assertEquals(0, counter.get());
                    var trace = snapshots.get(0);
                    assertEquals("TOOL_ARGUMENT_PARSE", trace.getString("failure_stage"), protocol);
                    assertTrue(trace.getBoolean("evidence_complete"), trace.encodePrettily());
                    assertFalse(trace.encode().contains("never-capture-key"));
                    List<JsonObject> events = trace.getJsonArray("events").stream().map(JsonObject.class::cast).toList();
                    assertTrue(events.stream().anyMatch(e -> e.getString("kind").equals("parse_failed") && !e.getBoolean("native_function_entered")));
                    assertTrue(events.stream().anyMatch(e -> e.getString("kind").equals("arguments_appended")));
                    assertTrue(events.stream().anyMatch(e -> e.getString("kind").equals("converted_arguments_delta")));
                    String exchange = events.stream().filter(e -> e.getString("kind").equals("provider_request")).findFirst().orElseThrow().getString("exchange_id");
                    var replay = CatholicTraceReplay.replayStream(trace, exchange);
                    assertTrue(replay.getBoolean("finished"), replay.encode());
                    var tool = replay.getJsonArray("tool_calls").getJsonObject(0);
                    assertEquals(BAD, tool.getString("arguments"));
                    assertFalse(tool.getBoolean("arguments_valid"));
                    assertEquals(0, counter.get(), "replay must never invoke tools");
                } finally {
                    await(http.close()); await(server.close()); recorder.close(); assertTrue(recorder.awaitClosed(Duration.ofSeconds(5)));
                }
            }
        } finally { await(vertx.close()); }
    }

    @Test void sharedAgentConcurrentCandidatesStayIsolatedAndCustomLlmIsPartial() throws Exception {
        var pending = new ConcurrentHashMap<String, Promise<CatholicLLMResponse>>();
        CatholicLLM custom = new CatholicLLM() {
            public io.vertx.core.Future<CatholicLLMResponse> call(CatholicLLMRequest r) { return callStream(r); }
            public io.vertx.core.Future<Void> callStream(CatholicLLMRequest r, Function<CatholicLLMResponseChunk, io.vertx.core.Future<Void>> p) { throw new UnsupportedOperationException(); }
            public io.vertx.core.Future<CatholicLLMResponse> callStream(CatholicLLMRequest r) {
                var promise = Promise.<CatholicLLMResponse>promise(); pending.put(r.traceContext().traceId(), promise); return promise.future();
            }
        };
        var traces = new CopyOnWriteArrayList<JsonObject>();
        var recorder = CatholicTraceRecorderTest.recorder(t -> { traces.add(t); return "memory"; }, CatholicTraceRecorder.ContentMode.STRUCTURE);
        try {
            var handler = handler(new AtomicInteger());
            var agent = CatholicAgent.builder().llm(custom).model("probe").tools(handler.getRegisteredToolDefinitions()).toolHandler(handler).build();
            var a = recorder.start(Map.of("candidateId", "A"));
            var b = recorder.start(Map.of("candidateId", "B"));
            var fa = agent.interact("A", a);
            var fb = agent.interact("B", b);
            pending.get(b.traceId()).complete(new CatholicLLMResponseImpl("b", new CatholicAssistantMessage(null,
                    List.of(new CatholicFunctionToolCallImpl("call-b", new FunctionCall("lookup", BAD)))), null, true));
            pending.get(a.traceId()).complete(new CatholicLLMResponseImpl("a", new CatholicAssistantMessage("done", null), null, true));
            assertTrue(await(fa).completed());
            assertThrows(ExecutionException.class, () -> await(fb));
            CatholicTraceRecorderTest.saved(b);
            assertNull(CatholicTraceRecorderTest.saved(a));
            assertEquals(1, traces.size());
            assertEquals(b.traceId(), traces.get(0).getString("trace_id"));
            assertEquals("B", traces.get(0).getJsonObject("tags").getString("candidateId"));
            assertEquals("partial", traces.get(0).getString("capture_capability"));
        } finally { recorder.close(); assertTrue(recorder.awaitClosed(Duration.ofSeconds(5))); }
    }

    @Test void crlfSuccessRetainsEvidenceForLaterArgumentFailure() throws Exception {
        var vertx = Vertx.vertx(); var keel = Keel.create(vertx);
        var traces = new CopyOnWriteArrayList<JsonObject>();
        var recorder = CatholicTraceRecorderTest.recorder(t -> { traces.add(t); return "memory"; }, CatholicTraceRecorder.ContentMode.RESTRICTED_RAW);
        var server = await(vertx.createHttpServer().requestHandler(req -> req.bodyHandler(v -> req.response().end(body("chat").replace("\n", "\r\n"))))
                .listen(0, "127.0.0.1"));
        var http = vertx.createHttpClient();
        try {
            var session = recorder.start(Map.of());
            var model = llm("chat", keel, http, "http://127.0.0.1:" + server.actualPort());
            var request = CatholicLLMRequest.builder().model("probe").addMessage(CatholicUserMessage.ofText("probe")).build();
            var response = await(model.callStream(request, session.context()));
            assertTrue(response.finished());
            var failure = assertThrows(RuntimeException.class,
                () -> response.message().toolCalls().get(0).parseArguments());
            session.fail("TOOL_ARGUMENTS", failure);
            CatholicTraceRecorderTest.saved(session);
            var trace = traces.get(0);
            var events = trace.getJsonArray("events").stream().map(JsonObject.class::cast).toList();
            assertTrue(events.stream().anyMatch(e -> "sse_event".equals(e.getString("kind"))));
            var buffer = events.stream().filter(e -> "transport_buffer".equals(e.getString("kind"))).findFirst().orElseThrow();
            var replay = CatholicTraceReplay.replayStream(trace, buffer.getString("exchange_id"));
            assertEquals(1, replay.getInteger("chunks"));
            assertEquals(0, replay.getInteger("pending_characters"));
            assertTrue(replay.getBoolean("finished"));
            assertEquals(BAD, replay.getJsonArray("tool_calls").getJsonObject(0).getString("arguments"));
            assertFalse(replay.getJsonArray("tool_calls").getJsonObject(0).getBoolean("arguments_valid"));
        } finally { await(http.close()); await(server.close()); await(vertx.close()); recorder.close(); assertTrue(recorder.awaitClosed(Duration.ofSeconds(5))); }
    }
    @Test void replaySupportsAllLineEndingsAndSplitUtf8WithPendingTail() {
        for (String ending : List.of("\n", "\r\n", "\r", "\r\n\n")) {
            String data = "data: " + new JsonObject().put("id", "replay")
                .put("choices", new JsonArray().add(new JsonObject().put("index", 0)
                    .put("delta", new JsonObject().put("content", "中文🙂"))
                    .put("finish_reason", "stop"))).encode();
            String separator = ending.equals("\r\n\n") ? ending : ending + ending;
            byte[] raw = (data + separator + "data: unfinished").getBytes(java.nio.charset.StandardCharsets.UTF_8);
            var events = new JsonArray().add(new JsonObject().put("kind", "provider_request")
                .put("exchange_id", "x").put("provider", "openai-chat-completions").put("stream", true));
            for (int i = 0; i < raw.length; i++) {
                events.add(new JsonObject().put("kind", "transport_buffer").put("exchange_id", "x")
                    .put("buffer_sequence", i).put("original_bytes", 1)
                    .put("payload", Base64.getEncoder().encodeToString(new byte[]{raw[i]})));
            }
            events.add(new JsonObject().put("kind", "transport_completed").put("exchange_id", "x").put("http_eof", true));
            var trace = new JsonObject().put("schema_version", 1).put("content_mode", "RESTRICTED_RAW")
                .put("evidence_complete", true).put("events", events);
            var replay = CatholicTraceReplay.replayStream(trace, "x");
            assertEquals(1, replay.getInteger("chunks"));
            assertEquals("中文🙂", replay.getString("text"));
            assertTrue(replay.getBoolean("finished"));
            assertEquals("data: unfinished".length(), replay.getInteger("pending_characters"));
        }
    }

    private static CatholicLLM fixed(CatholicLLMResponse response) {
        return new CatholicLLM() {
            public io.vertx.core.Future<CatholicLLMResponse> call(CatholicLLMRequest r) { return io.vertx.core.Future.succeededFuture(response); }
            public io.vertx.core.Future<CatholicLLMResponse> callStream(CatholicLLMRequest r) { return call(r); }
            public io.vertx.core.Future<Void> callStream(CatholicLLMRequest r, Function<CatholicLLMResponseChunk, io.vertx.core.Future<Void>> f) {
                return io.vertx.core.Future.failedFuture("unused");
            }
        };
    }

    @Test void earlierToolSuccessAndLaterParseFailureAreBothRecorded() throws Exception {
        var traces = new CopyOnWriteArrayList<JsonObject>();
        var recorder = CatholicTraceRecorderTest.recorder(t -> { traces.add(t); return "memory"; }, CatholicTraceRecorder.ContentMode.STRUCTURE);
        try {
            var executed = new AtomicInteger(); var handler = handler(executed);
            var response = new CatholicLLMResponseImpl("r", new CatholicAssistantMessage(null, List.of(
                    new CatholicFunctionToolCallImpl("first", new FunctionCall("lookup", "{}")),
                    new CatholicFunctionToolCallImpl("second", new FunctionCall("lookup", BAD)))), null, true);
            var agent = CatholicAgent.builder().llm(fixed(response)).model("probe")
                    .tools(handler.getRegisteredToolDefinitions()).toolHandler(handler).build();
            var session = recorder.start(Map.of());
            assertThrows(ExecutionException.class, () -> await(agent.interact("x", session)));
            CatholicTraceRecorderTest.saved(session);
            assertEquals(1, executed.get());
            var events = traces.get(0).getJsonArray("events").stream().map(JsonObject.class::cast).toList();
            assertTrue(events.stream().anyMatch(e -> "tool_succeeded".equals(e.getString("kind")) && "first".equals(e.getString("tool_call_id"))));
            assertTrue(events.stream().anyMatch(e -> "parse_failed".equals(e.getString("kind")) && "second".equals(e.getString("tool_call_id"))));
        } finally { recorder.close(); assertTrue(recorder.awaitClosed(Duration.ofSeconds(5))); }
    }

    @Test void observerAndValidationAndRoundLimitFailuresSaveWithoutChangingResults() throws Exception {
        var traces = new CopyOnWriteArrayList<JsonObject>();
        var recorder = CatholicTraceRecorderTest.recorder(t -> { traces.add(t); return "memory"; }, CatholicTraceRecorder.ContentMode.STRUCTURE);
        try {
            var response = new CatholicLLMResponseImpl("r", new CatholicAssistantMessage("done", null), null, true);
            var original = new IllegalStateException("observer-failure");
            var observerAgent = CatholicAgent.builder().llm(fixed(response)).model("m")
                    .observer(context -> { throw original; }).build();
            var observed = recorder.start(Map.of());
            assertSame(original, assertThrows(ExecutionException.class, () -> await(observerAgent.interact("x", observed))).getCause());
            CatholicTraceRecorderTest.saved(observed);
            assertEquals("AGENT_OBSERVER", traces.get(0).getString("failure_stage"));
            var agent = CatholicAgent.builder().llm(fixed(response)).model("m").build();
            var validation = recorder.start(Map.of());
            assertSame(original, assertThrows(ExecutionException.class, () -> await(agent.interact(List.of(),
                    CatholicUserMessage.ofText("x"), null, r -> { throw original; }, validation))).getCause());
            CatholicTraceRecorderTest.saved(validation);
            assertEquals("FIRST_RESPONSE_VALIDATION", traces.get(1).getString("failure_stage"));
            var handler = handler(new AtomicInteger());
            var tools = new CatholicLLMResponseImpl("r", new CatholicAssistantMessage(null,
                    List.of(new CatholicFunctionToolCallImpl("t", new FunctionCall("lookup", "{}")))), null, true);
            var limited = CatholicAgent.builder().llm(fixed(tools)).model("m").maxRounds(1)
                    .tools(handler.getRegisteredToolDefinitions()).toolHandler(handler).build();
            var roundLimit = recorder.start(Map.of());
            assertFalse(await(limited.interact("x", roundLimit)).completed());
            CatholicTraceRecorderTest.saved(roundLimit);
            assertEquals("ROUND_LIMIT", traces.get(2).getString("failure_stage"));
        } finally { recorder.close(); assertTrue(recorder.awaitClosed(Duration.ofSeconds(5))); }
    }

    @Test void existingNativeHandlerSubclassDispatchIsPreserved() throws Exception {
        var handler = new CatholicToolInvocationHandlerWithNativeFunctionAdapters() {
            @Override protected io.vertx.core.Future<String> handleToolCall(CatholicFunctionToolCall call) {
                return io.vertx.core.Future.succeededFuture("override");
            }
        };
        var call = new CatholicFunctionToolCallImpl("id", new FunctionCall("custom", "{}"));
        assertEquals("override", await(handler.handle(call)));
        var recorder = CatholicTraceRecorderTest.recorder(t -> "unused", CatholicTraceRecorder.ContentMode.STRUCTURE);
        try {
            var session = recorder.start(Map.of());
            assertEquals("override", await(handler.handle(call, session.context())));
            session.succeed();
        } finally { recorder.close(); assertTrue(recorder.awaitClosed(Duration.ofSeconds(5))); }
    }

}
