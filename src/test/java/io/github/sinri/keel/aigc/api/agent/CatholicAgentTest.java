package io.github.sinri.keel.aigc.api.agent;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLM;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponseChunk;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicAssistantMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.CatholicToolDefinition;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.CatholicFunctionToolCall;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.FunctionCall;
import io.vertx.core.Future;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class CatholicAgentTest {

    @Test
    void chatWithoutToolsReturnsFinalText() {
        CountingLlm llm = new CountingLlm();
        llm.nextResponse = textOnlyResponse("hello");

        CatholicAgent agent = CatholicAgent.builder()
            .llm(llm)
            .model("m")
            .build();

        CatholicLLMResponse r = agent.chat("hi").toCompletionStage().toCompletableFuture().join();
        assertEquals("hello", r.text());
        assertEquals(1, llm.callCount);
        assertEquals(2, agent.conversationMessages().size());
    }

    @Test
    void toolLoopRunsTwiceThenFinishes() {
        CountingLlm llm = new CountingLlm();
        CatholicFunctionToolCall call = new CatholicFunctionToolCall(
            "call-1",
            "function",
            new FunctionCall("get_x", "{}")
        );
        llm.queue.add(toolOnlyResponse(call));
        llm.queue.add(textOnlyResponse("done"));

        CatholicAgent agent = CatholicAgent.builder()
            .llm(llm)
            .model("m")
            .tools(List.of(CatholicToolDefinition.function("get_x", "desc")))
            .toolHandler(tc -> Future.succeededFuture("{\"ok\":true}"))
            .build();

        CatholicLLMResponse r = agent.chat("run").toCompletionStage().toCompletableFuture().join();
        assertEquals("done", r.text());
        assertEquals(2, llm.callCount);
    }

    @Test
    void maxToolRoundsFails() {
        CountingLlm llm = new CountingLlm();
        CatholicFunctionToolCall call = new CatholicFunctionToolCall(
            "c",
            "function",
            new FunctionCall("f", "{}")
        );
        llm.queue.add(toolOnlyResponse(call));
        llm.queue.add(toolOnlyResponse(call));

        CatholicAgent agent = CatholicAgent.builder()
            .llm(llm)
            .model("m")
            .maxToolRounds(1)
            .tools(List.of(CatholicToolDefinition.function("f", "d")))
            .toolHandler(tc -> Future.succeededFuture("{}"))
            .build();

        Future<CatholicLLMResponse> f = agent.chat("x");
        Throwable err = assertThrows(Exception.class, () -> f.toCompletionStage().toCompletableFuture().join());
        Throwable cause = err.getCause() != null ? err.getCause() : err;
        assertInstanceOf(CatholicAgentTooManyToolRoundsException.class, cause);
    }

    private static CatholicLLMResponse textOnlyResponse(String text) {
        return CatholicLLMResponse.builder()
            .id("id")
            .message(CatholicAssistantMessage.ofText(text))
            .build();
    }

    /**
     * 助手仅含工具调用（无正文），第二轮由队列给出纯文本。
     */
    private static CatholicLLMResponse toolOnlyResponse(CatholicFunctionToolCall call) {
        return CatholicLLMResponse.builder()
            .id("id")
            .message(CatholicAssistantMessage.ofToolCalls(List.of(call)))
            .build();
    }

    private static final class CountingLlm implements CatholicLLM {
        int callCount;
        CatholicLLMResponse nextResponse;
        final List<CatholicLLMResponse> queue = new ArrayList<>();

        @Override
        public Future<CatholicLLMResponse> call(CatholicLLMRequest request) {
            callCount++;
            if (!queue.isEmpty()) {
                return Future.succeededFuture(queue.remove(0));
            }
            return Future.succeededFuture(nextResponse);
        }

        @Override
        public Future<Void> callStream(CatholicLLMRequest request, Function<CatholicLLMResponseChunk, Future<Void>> chunkAsyncProcessor) {
            return Future.failedFuture(new UnsupportedOperationException());
        }

        @Override
        public Future<CatholicLLMResponse> callStream(CatholicLLMRequest request) {
            return Future.failedFuture(new UnsupportedOperationException());
        }
    }
}
