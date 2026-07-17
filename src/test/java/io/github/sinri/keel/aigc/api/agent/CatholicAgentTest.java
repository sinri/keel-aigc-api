package io.github.sinri.keel.aigc.api.agent;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLM;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponseChunk;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicAssistantMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicSystemMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.CatholicToolDefinition;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.function.FunctionDefinition;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.CatholicFunctionToolCall;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.CatholicFunctionToolCallImpl;
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

        CatholicAgentResult r = agent.interact("hi").toCompletionStage().toCompletableFuture().join();
        assertTrue(r.completed());
        assertEquals("hello", r.lastResponse().text());
        assertEquals(1, llm.callCount);
        assertEquals(2, r.transcript().size());
    }

    @Test
    void toolLoopRunsTwiceThenFinishes() {
        CountingLlm llm = new CountingLlm();
        CatholicFunctionToolCall call = new CatholicFunctionToolCallImpl(
            "call-1",
            new FunctionCall("get_x", "{}")
        );
        llm.queue.add(toolOnlyResponse(call));
        llm.queue.add(textOnlyResponse("done"));

        CatholicAgent agent = CatholicAgent.builder()
            .llm(llm)
            .model("m")
            .tools(List.of(CatholicToolDefinition.function(FunctionDefinition.of("get_x", "desc"))))
            .toolHandler(tc -> Future.succeededFuture("{\"ok\":true}"))
            .build();

        CatholicAgentResult r = agent.interact("run").toCompletionStage().toCompletableFuture().join();
        assertEquals("done", r.lastResponse().text());
        assertEquals(1, r.toolRounds());
        assertEquals(2, llm.callCount);
    }

    @Test
    void roundLimitReturnsInspectableResultWithoutExecutingAnotherToolRound() {
        CountingLlm llm = new CountingLlm();
        CatholicFunctionToolCall call = new CatholicFunctionToolCallImpl(
            "c",
            new FunctionCall("f", "{}")
        );
        llm.queue.add(toolOnlyResponse(call));
        llm.queue.add(toolOnlyResponse(call));

        CatholicAgent agent = CatholicAgent.builder()
            .llm(llm)
            .model("m")
            .maxRounds(1)
            .tools(List.of(CatholicToolDefinition.function(FunctionDefinition.of("f", "d"))))
            .toolHandler(tc -> Future.succeededFuture("{}"))
            .build();

        CatholicAgentResult result = agent.interact("x").toCompletionStage().toCompletableFuture().join();
        assertEquals(CatholicAgentTermination.ROUND_LIMIT_EXCEEDED, result.termination());
        assertEquals(1, result.llmRounds());
        assertEquals(0, result.toolRounds());
        assertEquals(1, llm.callCount);
        assertNotNull(result.lastResponse());
    }

    @Test
    void mechanismCanAddObservationAndContinueWithoutTools() {
        CountingLlm llm = new CountingLlm();
        llm.queue.add(textOnlyResponse("draft"));
        llm.queue.add(textOnlyResponse("final"));

        CatholicAgent agent = CatholicAgent.builder()
            .llm(llm).model("m").maxRounds(2)
            .observer(context -> Future.succeededFuture(context.llmRound() == 1
                ? CatholicAgentDirective.continueWith(CatholicSystemMessage.of("check completeness"))
                : CatholicAgentDirective.complete()))
            .build();

        CatholicAgentResult result = agent.interact("solve").toCompletionStage().toCompletableFuture().join();
        assertTrue(result.completed());
        assertEquals("final", result.lastResponse().text());
        assertEquals(2, result.llmRounds());
        assertEquals(0, result.toolRounds());
        assertEquals(4, result.transcript().size());
    }

    @Test
    void interactionsHaveIsolatedTranscripts() {
        CountingLlm llm = new CountingLlm();
        llm.queue.add(textOnlyResponse("one"));
        llm.queue.add(textOnlyResponse("two"));
        CatholicAgent agent = CatholicAgent.builder().llm(llm).model("m").build();

        CatholicAgentResult first = agent.interact("first").toCompletionStage().toCompletableFuture().join();
        CatholicAgentResult second = agent.interact("second").toCompletionStage().toCompletableFuture().join();
        assertEquals(2, first.transcript().size());
        assertEquals(2, second.transcript().size());
    }

    @Test
    void requiredToolChoiceOnlyAffectsFirstRequest() {
        CountingLlm llm = new CountingLlm();
        CatholicFunctionToolCall call = new CatholicFunctionToolCallImpl("c", new FunctionCall("f", "{}"));
        llm.queue.add(toolOnlyResponse(call));
        llm.queue.add(textOnlyResponse("done"));
        CatholicAgent agent = CatholicAgent.builder().llm(llm).model("m").maxRounds(2)
            .tools(List.of(CatholicToolDefinition.function(FunctionDefinition.of("f", "d"))))
            .toolHandler(tc -> Future.succeededFuture("{}"))
            .build();

        CatholicAgentResult result = new CatholicRequiredToolAgent(agent, "f").interact("x")
            .toCompletionStage().toCompletableFuture().join();
        assertTrue(result.completed());
        assertNotNull(llm.requests.get(0).options().extra().getJsonObject("tool_choice"));
        assertFalse(llm.requests.get(1).options().extra().containsKey("tool_choice"));
    }

    @Test
    void requiredToolAgentFailsWhenModelReturnsNoToolCall() {
        CountingLlm llm = new CountingLlm();
        llm.queue.add(textOnlyResponse("ignored tool choice"));
        CatholicAgent agent = CatholicAgent.builder().llm(llm).model("m")
            .tools(List.of(CatholicToolDefinition.function(FunctionDefinition.of("required", "d"))))
            .toolHandler(tc -> Future.succeededFuture("{}"))
            .build();

        Throwable cause = failureOf(new CatholicRequiredToolAgent(agent, "required").interact("x"));
        CatholicRequiredToolNotCalledException exception =
            assertInstanceOf(CatholicRequiredToolNotCalledException.class, cause);
        assertEquals("required", exception.functionName());
    }

    @Test
    void requiredToolAgentRejectsWrongToolBeforeExecutingIt() {
        CountingLlm llm = new CountingLlm();
        CatholicFunctionToolCall wrongCall = new CatholicFunctionToolCallImpl(
            "wrong-call", new FunctionCall("other", "{}"));
        llm.queue.add(toolOnlyResponse(wrongCall));
        int[] handlerCalls = {0};
        CatholicAgent agent = CatholicAgent.builder().llm(llm).model("m")
            .tools(List.of(
                CatholicToolDefinition.function(FunctionDefinition.of("required", "d")),
                CatholicToolDefinition.function(FunctionDefinition.of("other", "d"))))
            .toolHandler(tc -> {
                handlerCalls[0]++;
                return Future.succeededFuture("{}");
            })
            .build();

        Throwable cause = failureOf(new CatholicRequiredToolAgent(agent, "required").interact("x"));
        assertInstanceOf(CatholicRequiredToolNotCalledException.class, cause);
        assertEquals(0, handlerCalls[0]);
    }

    private static Throwable failureOf(Future<?> future) {
        Exception error = assertThrows(Exception.class,
            () -> future.toCompletionStage().toCompletableFuture().join());
        return error.getCause() != null ? error.getCause() : error;
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
        final List<CatholicLLMRequest> requests = new ArrayList<>();

        @Override
        public Future<CatholicLLMResponse> call(CatholicLLMRequest request) {
            callCount++;
            requests.add(request);
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
