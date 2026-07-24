package io.github.sinri.keel.aigc.api.agent;

import io.github.sinri.keel.aigc.api.agent.skill.CatholicSkill;
import io.github.sinri.keel.aigc.api.agent.skill.CatholicSkillFrontmatter;
import io.github.sinri.keel.aigc.api.agent.skill.CatholicSkillProvider;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLM;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponseChunk;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicAssistantMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicChatMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicSystemMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicUserMessage;
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
        assertEquals(List.of("system", "user", "assistant"),
            llm.requests.get(1).messages().stream().map(CatholicChatMessage::role).toList());
        assertEquals("check completeness",
            ((CatholicSystemMessage) llm.requests.get(1).messages().get(0)).text());
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
    void interactionPrependsConfiguredMessagesAndSuppliedHistory() {
        CountingLlm llm = new CountingLlm();
        llm.nextResponse = textOnlyResponse("continued");
        CatholicAgent agent = CatholicAgent.builder().llm(llm).model("m")
            .systemPrompt("fixed instructions")
            .build();
        List<CatholicChatMessage> history = List.of(
            CatholicUserMessage.ofText("earlier question"),
            CatholicAssistantMessage.ofText("earlier answer"));

        CatholicAgentResult result = agent.interact(
            history, CatholicUserMessage.ofText("current question"))
            .toCompletionStage().toCompletableFuture().join();

        assertEquals(List.of("system", "user", "assistant", "user", "assistant"),
            result.transcript().stream().map(CatholicChatMessage::role).toList());
        assertEquals(result.transcript().subList(0, 4), llm.requests.get(0).messages());
        assertEquals(2, history.size());
    }

    @Test
    void eachLlmRequestContainsAtMostOneLeadingSystemMessage() {
        CountingLlm llm = new CountingLlm();
        llm.nextResponse = textOnlyResponse("continued");
        CatholicAgent agent = CatholicAgent.builder().llm(llm).model("m")
            .systemPrompt("first instruction")
            .systemPrompt("second instruction")
            .build();

        CatholicAgentResult result = agent.interact(
                List.of(
                    CatholicUserMessage.ofText("earlier question"),
                    CatholicSystemMessage.of("historical instruction"),
                    CatholicAssistantMessage.ofText("earlier answer")),
                CatholicUserMessage.ofText("current question"))
            .toCompletionStage().toCompletableFuture().join();

        List<CatholicChatMessage> requestMessages = llm.requests.get(0).messages();
        assertEquals(List.of("system", "user", "assistant", "user"),
            requestMessages.stream().map(CatholicChatMessage::role).toList());
        assertEquals("first instruction\n\nsecond instruction\n\nhistorical instruction",
            ((CatholicSystemMessage) requestMessages.get(0)).text());
        assertEquals(List.of("system", "system", "user", "system", "assistant", "user", "assistant"),
            result.transcript().stream().map(CatholicChatMessage::role).toList());
    }

    @Test
    void skillCatalogIsDisclosedAndActivatedProgressively() {
        CountingLlm llm = new CountingLlm();
        CatholicFunctionToolCall activation = new CatholicFunctionToolCallImpl(
            "skill-call", new FunctionCall(CatholicAgent.ACTIVATE_SKILL_FUNCTION_NAME,
            "{\"name\":\"pdf-processing\"}"));
        llm.queue.add(toolOnlyResponse(activation));
        llm.queue.add(textOnlyResponse("processed"));
        int[] loads = {0};

        CatholicAgent agent = CatholicAgent.builder().llm(llm).model("m")
            .skillProvider(new CatholicSkillProvider() {
                @Override
                public Future<List<CatholicSkillFrontmatter>> getSkillCandidates() {
                    return Future.succeededFuture(List.of(frontmatter(
                        "pdf-processing", "Process PDF documents when the user asks about PDFs.")));
                }

                @Override
                public Future<CatholicSkill> loadSkillByName(String skillName) {
                    loads[0]++;
                    return Future.succeededFuture(skill(skillName, "Process PDFs.",
                        "First inspect the PDF, then summarize it."));
                }
            })
            .build();

        CatholicAgentResult result = agent.interact("summarize a PDF")
            .toCompletionStage().toCompletableFuture().join();

        assertTrue(result.completed());
        assertEquals(1, loads[0]);
        assertEquals("processed", result.text());
        assertEquals(2, llm.requests.get(0).messages().size());
        assertTrue(((CatholicSystemMessage) llm.requests.get(0).messages().get(0)).text()
            .contains("pdf-processing"));
        assertTrue(llm.requests.get(0).tools().stream()
            .filter(io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.CatholicFunctionToolDefinition.class::isInstance)
            .map(io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.CatholicFunctionToolDefinition.class::cast)
            .anyMatch(t -> t.function().name().equals(CatholicAgent.ACTIVATE_SKILL_FUNCTION_NAME)));
        assertTrue(result.transcript().stream()
            .filter(io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicToolCallMessage.class::isInstance)
            .map(io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicToolCallMessage.class::cast)
            .anyMatch(message -> message.content().contains("First inspect the PDF")));
    }

    @Test
    void emptySkillCatalogAddsNeitherPromptNorActivationTool() {
        CountingLlm llm = new CountingLlm();
        llm.nextResponse = textOnlyResponse("plain");
        CatholicAgent agent = CatholicAgent.builder().llm(llm).model("m")
            .skillProvider(new CatholicSkillProvider() {
                @Override
                public Future<List<CatholicSkillFrontmatter>> getSkillCandidates() {
                    return Future.succeededFuture(List.of());
                }

                @Override
                public Future<CatholicSkill> loadSkillByName(String skillName) {
                    return Future.failedFuture("should not load");
                }
            }).build();

        CatholicAgentResult result = agent.interact("hello").toCompletionStage().toCompletableFuture().join();
        assertEquals(2, result.transcript().size());
        assertTrue(llm.requests.get(0).tools().isEmpty());
    }

    @Test
    void repeatedSkillActivationLoadsOnlyOncePerInteraction() {
        CountingLlm llm = new CountingLlm();
        CatholicFunctionToolCall activation = new CatholicFunctionToolCallImpl(
            "skill-call-1", new FunctionCall(CatholicAgent.ACTIVATE_SKILL_FUNCTION_NAME,
            "{\"name\":\"code-review\"}"));
        CatholicFunctionToolCall repeatedActivation = new CatholicFunctionToolCallImpl(
            "skill-call-2", new FunctionCall(CatholicAgent.ACTIVATE_SKILL_FUNCTION_NAME,
            "{\"name\":\"code-review\"}"));
        llm.queue.add(toolOnlyResponse(activation));
        llm.queue.add(toolOnlyResponse(repeatedActivation));
        llm.queue.add(textOnlyResponse("done"));
        int[] loads = {0};

        CatholicAgent agent = CatholicAgent.builder().llm(llm).model("m")
            .skillProvider(new CatholicSkillProvider() {
                @Override
                public Future<List<CatholicSkillFrontmatter>> getSkillCandidates() {
                    return Future.succeededFuture(List.of(
                        frontmatter("code-review", "Review code when a review is requested.")));
                }

                @Override
                public Future<CatholicSkill> loadSkillByName(String skillName) {
                    loads[0]++;
                    return Future.succeededFuture(skill(skillName, "Review code.", "Inspect every changed file."));
                }
            }).build();

        CatholicAgentResult result = agent.interact("review this")
            .toCompletionStage().toCompletableFuture().join();

        assertTrue(result.completed());
        assertEquals(1, loads[0]);
        assertTrue(result.transcript().stream()
            .filter(io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicToolCallMessage.class::isInstance)
            .map(io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicToolCallMessage.class::cast)
            .anyMatch(message -> message.content().contains("already active")));
    }

    @Test
    void skillActivationDeduplicationIsIsolatedBetweenInteractions() {
        CountingLlm llm = new CountingLlm();
        CatholicFunctionToolCall firstActivation = new CatholicFunctionToolCallImpl(
            "skill-call-1", new FunctionCall(CatholicAgent.ACTIVATE_SKILL_FUNCTION_NAME,
            "{\"name\":\"code-review\"}"));
        CatholicFunctionToolCall secondActivation = new CatholicFunctionToolCallImpl(
            "skill-call-2", new FunctionCall(CatholicAgent.ACTIVATE_SKILL_FUNCTION_NAME,
            "{\"name\":\"code-review\"}"));
        llm.queue.add(toolOnlyResponse(firstActivation));
        llm.queue.add(textOnlyResponse("first done"));
        llm.queue.add(toolOnlyResponse(secondActivation));
        llm.queue.add(textOnlyResponse("second done"));
        int[] loads = {0};

        CatholicAgent agent = CatholicAgent.builder().llm(llm).model("m")
            .skillProvider(new CatholicSkillProvider() {
                @Override
                public Future<List<CatholicSkillFrontmatter>> getSkillCandidates() {
                    return Future.succeededFuture(List.of(
                        frontmatter("code-review", "Review code when a review is requested.")));
                }

                @Override
                public Future<CatholicSkill> loadSkillByName(String skillName) {
                    loads[0]++;
                    return Future.succeededFuture(skill(skillName, "Review code.", "Inspect every changed file."));
                }
            }).build();

        assertTrue(agent.interact("first review").toCompletionStage().toCompletableFuture().join().completed());
        assertTrue(agent.interact("second review").toCompletionStage().toCompletableFuture().join().completed());
        assertEquals(2, loads[0]);
    }

    @Test
    void duplicateSkillNamesFailBeforeCallingLlm() {
        CountingLlm llm = new CountingLlm();
        CatholicAgent agent = CatholicAgent.builder().llm(llm).model("m")
            .skillProvider(new CatholicSkillProvider() {
                @Override
                public Future<List<CatholicSkillFrontmatter>> getSkillCandidates() {
                    return Future.succeededFuture(List.of(
                        frontmatter("same-skill", "first description"),
                        frontmatter("same-skill", "second description")));
                }

                @Override
                public Future<CatholicSkill> loadSkillByName(String skillName) {
                    return Future.failedFuture("should not load");
                }
            }).build();

        assertTrue(failureOf(agent.interact("x")).getMessage().contains("duplicate skill name"));
        assertEquals(0, llm.callCount);
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

    private static CatholicSkillFrontmatter frontmatter(String name, String description) {
        return new CatholicSkillFrontmatter() {
            @Override public String name() { return name; }
            @Override public String description() { return description; }
        };
    }

    private static CatholicSkill skill(String name, String description, String instructions) {
        return new CatholicSkill() {
            @Override public String name() { return name; }
            @Override public String description() { return description; }
            @Override public String instructions() { return instructions; }
        };
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
