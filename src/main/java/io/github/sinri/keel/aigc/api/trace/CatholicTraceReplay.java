package io.github.sinri.keel.aigc.api.trace;

import io.github.sinri.keel.aigc.api.internal.anthropic.AnthropicStreamHandler;
import io.github.sinri.keel.aigc.api.internal.dashscope.DashScopeStreamHandler;
import io.github.sinri.keel.aigc.api.internal.openai.chatcompletions.OpenAIChatCompletionsStreamHandler;
import io.github.sinri.keel.aigc.api.internal.openai.responses.OpenAIResponsesStreamHandler;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponseChunk;
import io.github.sinri.keel.core.cutter.IntravenouslyCutterOnString;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Base64;
import java.util.function.Function;
import java.util.function.Supplier;

/** Offline parser-only replay of complete, raw streaming captures. Never invokes HTTP clients or tools. */
public final class CatholicTraceReplay {
    private CatholicTraceReplay() {}

    public static JsonObject replayStream(JsonObject trace, String exchangeId) {
        if (trace.getInteger("schema_version", 0) != 1
                || !"RESTRICTED_RAW".equals(trace.getString("content_mode"))
                || !trace.getBoolean("evidence_complete", false))
            throw new IllegalArgumentException("exact replay requires complete original evidence in schema version 1");
        var bytes = new ByteArrayOutputStream();
        String provider = null;
        boolean stream = false, eof = false;
        long expectedBuffer = 0;
        for (Object value : trace.getJsonArray("events")) {
            var event = (JsonObject) value;
            if (!exchangeId.equals(event.getString("exchange_id"))) continue;
            provider = event.getString("provider", provider);
            if (event.getString("kind").equals("provider_request")) stream = event.getBoolean("stream", false);
            if (event.getString("kind").equals("transport_completed")) eof = event.getBoolean("http_eof", false);
            if (event.getString("kind").equals("transport_buffer")) {
                if (event.getBoolean("truncated", false) || event.getBoolean("transport_truncated", false)
                        || event.getLong("buffer_sequence", -1L) != expectedBuffer++)
                    throw new IllegalArgumentException("incomplete transport buffers");
                byte[] decoded = Base64.getDecoder().decode(event.getString("payload"));
                if (decoded.length != event.getInteger("original_bytes", -1))
                    throw new IllegalArgumentException("transport length mismatch");
                if (bytes.size() + decoded.length > (64L << 20)) throw new IllegalArgumentException("replay size limit");
                bytes.writeBytes(decoded);
            }
        }
        if (!stream || !eof || provider == null) throw new IllegalArgumentException("complete streaming exchange not found");
        Function<String, CatholicLLMResponseChunk> parse;
        Supplier<CatholicLLMResponse> build;
        boolean dashscope = provider.startsWith("dashscope-");
        switch (provider) {
            case "openai-chat-completions" -> {
                var handler = new OpenAIChatCompletionsStreamHandler();
                parse = handler::processSseLine; build = handler::buildFinalResponse;
            }
            case "openai-responses" -> {
                var handler = new OpenAIResponsesStreamHandler();
                parse = handler::processSseLine; build = handler::buildFinalResponse;
            }
            case "anthropic-messages" -> {
                var handler = new AnthropicStreamHandler();
                parse = handler::processSseLine; build = handler::buildFinalResponse;
            }
            case "dashscope-text-generation", "dashscope-multimodal-generation" -> {
                var handler = new DashScopeStreamHandler();
                parse = handler::processSseLine; build = handler::buildFinalResponse;
            }
            default -> throw new IllegalArgumentException("unsupported replay provider: " + provider);
        }
        var result = new JsonObject().put("exchange_id", exchangeId).put("provider", provider)
                .put("parser_version", CatholicTraceReplay.class.getPackage().getImplementationVersion());
        var framing = new ReplayFraming();
        List<String> events = framing.feed(Buffer.buffer(bytes.toByteArray()));
        int chunks = 0;
        try {
            for (String event : events) {
                for (String line : event.split("\n")) {
                    if (parse.apply(line) != null) chunks++;
                }
                if (dashscope && parse.apply("") != null) chunks++;
            }
            var response = build.get();
            var calls = new JsonArray();
            if (response.message().hasToolCalls()) for (var call : response.message().toolCalls()) {
                var entry = new JsonObject().put("tool_call_id", call.id()).put("function_name", call.functionName())
                        .put("arguments", call.function().arguments());
                try { call.parseArguments(); entry.put("arguments_valid", true); }
                catch (RuntimeException cause) { entry.put("arguments_valid", false).put("parse_error", cause.getClass().getName()); }
                for (Object captured : trace.getJsonArray("events")) {
                    var evidence = (JsonObject) captured;
                    if (exchangeId.equals(evidence.getString("exchange_id"))
                            && "aggregated_arguments".equals(evidence.getString("kind"))
                            && call.id().equals(evidence.getString("tool_call_id"))) {
                        entry.put("matches_recorded_aggregation", call.function().arguments().equals(evidence.getString("payload")));
                    }
                }
                calls.add(entry);
            }
            result.put("finished", response.finished()).put("text", response.text()).put("tool_calls", calls);
        } catch (RuntimeException cause) {
            result.put("parse_error", cause.getClass().getName()).put("parse_message", cause.getMessage());
        }
        return result.put("chunks", chunks).put("pending_characters", framing.pendingCharacters());
    }
    /** Uses the same byte framing as live transport without deploying a worker. */
    private static final class ReplayFraming extends IntravenouslyCutterOnString {
        ReplayFraming() { super(event -> Future.succeededFuture()); }

        List<String> feed(Buffer input) {
            getBufferRef().get().appendBuffer(input);
            return cut();
        }

        int pendingCharacters() { return getBufferRef().get().toString().length(); }
    }

}
