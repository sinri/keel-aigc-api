package io.github.sinri.keel.aigc.api.internal;

import io.github.sinri.keel.aigc.api.llm.anthropic.AnthropicLLM;
import io.github.sinri.keel.aigc.api.llm.openai.responses.OpenAIResponsesLLM;
import io.github.sinri.keel.aigc.api.llm.dashscope.textgeneration.DashScopeTextGenerationLLM;
import io.github.sinri.keel.aigc.api.llm.dashscope.multimodalgeneration.DashScopeMultimodalGenerationLLM;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLM;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicUserMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.observation.CatholicLLMObserver;
import io.github.sinri.keel.aigc.api.llm.catholic.observation.CatholicLLMObservationStage;
import io.github.sinri.keel.base.async.Keel;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ProtocolStreamDiagnosticsTest {
    private static <T> T await(Future<T> future) throws Exception {
        return future.toCompletionStage().toCompletableFuture().get(15, TimeUnit.SECONDS);
    }

    private static String event(String data) { return "data: " + data + "\n\n"; }

    private static String prefix(String protocol) {
        return switch (protocol) {
            case "anthropic" -> event("{\"type\":\"message_start\",\"message\":{\"id\":\"probe\"}}")
                + event("{\"type\":\"content_block_delta\",\"index\":0,\"delta\":{\"type\":\"text_delta\",\"text\":\"hello\"}}");
            case "responses" -> event("{\"type\":\"response.created\",\"response\":{\"id\":\"probe\"}}")
                + event("{\"type\":\"response.output_text.delta\",\"delta\":\"hello\"}");
            default -> event("{\"request_id\":\"probe\",\"output\":{\"choices\":[{\"message\":{\"content\":\"hello\"},\"finish_reason\":\"null\"}]}}");
        };
    }

    private static String terminal(String protocol) {
        return switch (protocol) {
            case "anthropic" -> event("{\"type\":\"message_stop\"}");
            case "responses" -> event("{\"type\":\"response.completed\",\"response\":{\"id\":\"probe\",\"usage\":{\"input_tokens\":3,\"output_tokens\":2}}}");
            default -> event("{\"request_id\":\"probe\",\"output\":{\"choices\":[{\"message\":{\"content\":\"\"},\"finish_reason\":\"stop\"}]}}");
        };
    }

    private static String error(String protocol) {
        return switch (protocol) {
            case "anthropic" -> event("{\"type\":\"error\",\"error\":{\"type\":\"overloaded_error\",\"message\":\"probe-error\"}}");
            case "responses" -> event("{\"type\":\"response.failed\",\"response\":{\"error\":{\"code\":\"probe-error\"}}}");
            default -> "event: error\n" + event("{\"code\":\"probe-error\",\"message\":\"failure\"}");
        };
    }

    private static CatholicLLM client(String protocol, Keel keel, HttpClient client, String base, CatholicLLMObserver observer) {
        return switch (protocol) {
            case "anthropic" -> AnthropicLLM.builder().keel(keel).httpClient(client).apiKey("test").baseUrl(base).observer(observer).build();
            case "responses" -> OpenAIResponsesLLM.builder().keel(keel).httpClient(client).apiKey("test").baseUrl(base).observer(observer).build();
            case "dashscope-text" -> DashScopeTextGenerationLLM.builder().keel(keel).httpClient(client).apiKey("test").baseUrl(base).observer(observer).build();
            default -> DashScopeMultimodalGenerationLLM.builder().keel(keel).httpClient(client).apiKey("test").baseUrl(base).observer(observer).build();
        };
    }

    @Test
    void allProtocolsDiagnoseSuccessTruncationEmptyAndProviderErrors() throws Exception {
        var vertx = Vertx.vertx();
        var keel = Keel.create(vertx);
        var request = CatholicLLMRequest.builder().model("probe").addMessage(CatholicUserMessage.ofText("probe")).build();
        try {
            for (String protocol : new String[]{"anthropic", "responses", "dashscope-text", "dashscope-multimodal"}) {
                for (String scenario : new String[]{"success", "truncated", "empty", "error"}) {
                    String body = switch (scenario) {
                        case "success" -> prefix(protocol) + terminal(protocol);
                        case "truncated" -> prefix(protocol);
                        case "error" -> prefix(protocol) + error(protocol);
                        default -> "";
                    };
                    var metadata = new ConcurrentHashMap<String, Map<String, Object>>();
                    var stages = new CopyOnWriteArrayList<CatholicLLMObservationStage>();
                    var observer = new CatholicLLMObserver() {
                        public void onStreamDiagnostic(String id, String provider, String phase, Map<String, Object> details, long elapsed) {
                            metadata.put(phase, details);
                        }
                        public void onFailure(String id, String provider, CatholicLLMObservationStage stage, Throwable cause, long elapsed) {
                            stages.add(stage);
                        }
                    };
                    var server = await(vertx.createHttpServer().requestHandler(req -> req.response().end(body)).listen(0, "127.0.0.1"));
                    var http = vertx.createHttpClient();
                    try {
                        var llm = client(protocol, keel, http, "http://127.0.0.1:" + server.actualPort(), observer);
                        if (scenario.equals("success")) {
                            var result = await(llm.callStream(request));
                            assertEquals("hello", result.text(), protocol);
                            assertTrue(result.finished(), protocol);
                            assertTrue(stages.isEmpty(), protocol);
                        } else {
                            var exception = assertThrows(ExecutionException.class, () -> await(llm.callStream(request)), protocol + scenario);
                            assertTrue(exception.getCause().getMessage().contains(scenario.equals("error") ? "probe-error" :
                                scenario.equals("empty") ? "No valid response" : "Stream ended before"), exception.toString());
                            assertTrue(stages.contains(scenario.equals("error") ? CatholicLLMObservationStage.STREAM_READING :
                                CatholicLLMObservationStage.RESPONSE_CONVERSION));
                        }
                        assertEquals(scenario.equals("success") ? 1L : 0L,
                            metadata.get("stream_completed").get("terminal_chunks"), protocol);
                        assertNotNull(metadata.get("transport_completed"), protocol);
                        assertEquals(false, metadata.get("stream_completed").get("done_seen"), protocol);
                        if (!scenario.equals("error")) assertNotNull(metadata.get("response_build"), protocol);
                        else assertNotNull(metadata.get("parse_failed"), protocol);
                    } finally { await(http.close()); await(server.close()); }
                }
            }
        } finally { await(vertx.close()); }
    }

    @Test
    void responsesIncompleteIsAnExplicitFailure() {
        var handler = new io.github.sinri.keel.aigc.api.internal.openai.responses.OpenAIResponsesStreamHandler();
        var error = assertThrows(IllegalStateException.class, () -> handler.processSseLine(
            "data: {\"type\":\"response.incomplete\",\"response\":{\"incomplete_details\":{\"reason\":\"max_output_tokens\"}}}"));
        assertTrue(error.getMessage().contains("max_output_tokens"));
    }
}
