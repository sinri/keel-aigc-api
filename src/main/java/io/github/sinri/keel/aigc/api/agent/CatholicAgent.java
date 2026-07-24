package io.github.sinri.keel.aigc.api.agent;

import io.github.sinri.keel.aigc.api.agent.skill.CatholicSkillFrontmatter;
import io.github.sinri.keel.aigc.api.agent.skill.CatholicSkillProvider;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLM;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.message.*;
import io.github.sinri.keel.aigc.api.llm.catholic.request.CatholicLLMRequestOptions;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.CatholicFunctionToolCall;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.CatholicFunctionToolDefinition;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.CatholicToolDefinition;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.function.FunctionDefinition;
import io.github.sinri.keel.logger.api.LateObject;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * 通用、非流式 Agent 执行器。每次 {@link #interact} 都是相互隔离的一次用户交互：
 * 用户需求进入 LLM，观察器判断完成或继续；继续时执行必要的工具并再次交给 LLM，
 * 直至完成或达到轮次上限。
 * <p>
 * 本类不保存会话历史，也不承载“必须调用某个工具”等特定业务策略。
 */
public final class CatholicAgent {
    public static final String ACTIVATE_SKILL_FUNCTION_NAME = "activate_skill";
    private final CatholicLLM llm;
    private final String model;
    private final List<CatholicToolDefinition> tools;
    private final CatholicLLMRequestOptions options;
    private final CatholicToolInvocationHandler toolHandler;
    private final CatholicAgentObserver observer;
    private final int maxRounds;
    private final List<CatholicChatMessage> initialMessages;
    private final @Nullable CatholicSkillProvider skillProvider;

    private CatholicAgent(CatholicLLM llm, String model, List<CatholicToolDefinition> tools,
                          CatholicLLMRequestOptions options, CatholicToolInvocationHandler toolHandler,
                          CatholicAgentObserver observer, int maxRounds,
                          List<CatholicChatMessage> initialMessages,
                          @Nullable CatholicSkillProvider skillProvider) {
        this.llm = llm;
        this.model = model;
        this.tools = tools;
        this.options = copyOptions(options, null);
        this.toolHandler = toolHandler;
        this.observer = observer;
        this.maxRounds = maxRounds;
        this.initialMessages = initialMessages;
        this.skillProvider = skillProvider;
    }

    public static Builder builder() {
        return new Builder();
    }

    private static Future<CatholicLLMResponse> completedResponse(CatholicAgentResult result) {
        if (result.completed()) {
            return Future.succeededFuture(result.lastResponse());
        }
        return Future.failedFuture(new CatholicAgentTooManyToolRoundsException(result.maxRounds()));
    }

    private static String buildSkillCatalog(List<CatholicSkillFrontmatter> candidates) {
        JsonArray catalog = new JsonArray();
        candidates.forEach(skill -> catalog.add(new JsonObject()
                .put("name", skill.name()).put("description", skill.description())));
        return "The following skills provide specialized instructions. When the user's task matches a "
                + "skill description, call " + ACTIVATE_SKILL_FUNCTION_NAME
                + " before proceeding. Do not call it for unrelated tasks.\n<available_skills>\n"
                + catalog.encodePrettily() + "\n</available_skills>";
    }

    /**
     * 不同 LLM 服务对多条或位于对话中间的 system message 支持并不一致。
     * 在请求边界将它们按原始出现顺序合并，并始终放在消息列表首位。
     * transcript 本身保持不变，以保留完整的 Agent 执行历史。
     */
    private static List<CatholicChatMessage> normalizeRequestMessages(
            List<? extends CatholicChatMessage> messages) {
        StringJoiner systemText = new StringJoiner("\n\n");
        ArrayList<CatholicChatMessage> nonSystemMessages = new ArrayList<>(messages.size());
        for (CatholicChatMessage message : messages) {
            if (message instanceof CatholicSystemMessage systemMessage) {
                systemText.add(systemMessage.text());
            } else {
                nonSystemMessages.add(message);
            }
        }
        if (systemText.length() == 0) {
            return List.copyOf(nonSystemMessages);
        }
        ArrayList<CatholicChatMessage> normalizedMessages =
                new ArrayList<>(nonSystemMessages.size() + 1);
        normalizedMessages.add(CatholicSystemMessage.of(systemText.toString()));
        normalizedMessages.addAll(nonSystemMessages);
        return List.copyOf(normalizedMessages);
    }

    private static void validateSkillFrontmatter(CatholicSkillFrontmatter skill) {
        String name = Objects.requireNonNull(skill.name(), "skill name");
        String description = Objects.requireNonNull(skill.description(), "skill description");
        if (!name.matches("[a-z0-9]+(?:-[a-z0-9]+)*") || name.length() > 64) {
            throw new IllegalArgumentException("invalid skill name: " + name);
        }
        if (description.isBlank() || description.length() > 1024) {
            throw new IllegalArgumentException("invalid description for skill: " + name);
        }
        String compatibility = skill.compatibility();
        if (compatibility != null
                && (compatibility.isBlank() || compatibility.length() > 500)) {
            throw new IllegalArgumentException("invalid compatibility for skill: " + name);
        }
        Objects.requireNonNull(skill.metadata(), "skill metadata");
    }

    private static CatholicLLMRequestOptions copyOptions(CatholicLLMRequestOptions source,
                                                         @Nullable String requiredTool) {
        CatholicLLMRequestOptions.Builder builder = CatholicLLMRequestOptions.builder();
        if (source.temperature() != null) builder.temperature(source.temperature());
        if (source.maxTokens() != null) builder.maxTokens(source.maxTokens());
        if (source.topP() != null) builder.topP(source.topP());
        if (source.stop() != null) builder.stop(List.copyOf(source.stop()));
        var extra = source.extra().copy();
        if (requiredTool != null) {
            extra.put("tool_choice", new io.vertx.core.json.JsonObject()
                    .put("type", "function")
                    .put("function", new io.vertx.core.json.JsonObject().put("name", requiredTool)));
        }
        return builder.extra(extra).build();
    }

    public Future<CatholicAgentResult> interact(String userText) {
        return interact(CatholicUserMessage.ofText(Objects.requireNonNull(userText, "userText")));
    }

    public Future<CatholicAgentResult> interact(CatholicUserMessage userMessage) {
        return interact(List.of(), userMessage);
    }

    /**
     * 使用调用方提供的既有对话消息继续一次交互。
     * 最终 transcript 的顺序为 Agent 初始上下文、{@code priorMessages}、当前用户消息。
     * 本 Agent 不会保存这些消息；后续交互仍需由调用方再次传入所需历史。
     */
    public Future<CatholicAgentResult> interact(List<? extends CatholicChatMessage> priorMessages,
                                                CatholicUserMessage userMessage) {
        return interact(priorMessages, userMessage, null, response -> Future.succeededFuture());
    }

    Future<CatholicAgentResult> interact(List<? extends CatholicChatMessage> priorMessages,
                                         CatholicUserMessage userMessage,
                                         @Nullable String requiredTool,
                                         CatholicAgentFirstResponseValidator firstResponseValidator) {
        Objects.requireNonNull(priorMessages, "priorMessages");
        Objects.requireNonNull(userMessage, "userMessage");
        Objects.requireNonNull(firstResponseValidator, "firstResponseValidator");
        List<CatholicChatMessage> priorMessagesSnapshot = priorMessages
                .stream()
                .map(message -> Objects.requireNonNull(message, "priorMessages contains null"))
                .map(x -> (CatholicChatMessage) x)
                .toList();
        return prepareSkillContext().compose(skillContext -> {
            ArrayList<CatholicChatMessage> transcript = new ArrayList<>(initialMessages);
            if (skillContext.catalogMessage() != null) transcript.add(skillContext.catalogMessage());
            transcript.addAll(priorMessagesSnapshot);
            transcript.add(userMessage);
            return execute(transcript, 1, 0, requiredTool, firstResponseValidator,
                    skillContext.tools(), skillContext.handler());
        });
    }

    boolean supportsFunction(String functionName) {
        return tools.stream()
                    .filter(CatholicFunctionToolDefinition.class::isInstance)
                    .map(CatholicFunctionToolDefinition.class::cast)
                    .anyMatch(tool -> tool.function().name().equals(functionName));
    }

    /**
     * 兼容旧 API；新代码应使用 {@link #interact} 读取明确的终止原因和完整 transcript。
     */
    public Future<CatholicLLMResponse> chat(String userText) {
        return interact(userText).compose(CatholicAgent::completedResponse);
    }

    public Future<CatholicLLMResponse> chat(CatholicUserMessage userMessage) {
        return interact(userMessage).compose(CatholicAgent::completedResponse);
    }

    private Future<CatholicAgentResult> execute(ArrayList<CatholicChatMessage> transcript,
                                                int llmRound, int toolRounds,
                                                @Nullable String firstRoundRequiredTool,
                                                CatholicAgentFirstResponseValidator firstResponseValidator,
                                                List<CatholicToolDefinition> interactionTools,
                                                CatholicToolInvocationHandler interactionToolHandler) {
        CatholicLLMRequestOptions requestOptions = firstRoundRequiredTool == null
                ? copyOptions(options, null)
                : copyOptions(options, firstRoundRequiredTool);
        CatholicLLMRequest request = CatholicLLMRequest.builder()
                                                       .model(model)
                                                       .messages(normalizeRequestMessages(transcript))
                                                       .tools(interactionTools)
                                                       .options(requestOptions)
                                                       .stream(false)
                                                       .build();

        return llm.call(request).compose(response -> {
            CatholicAssistantMessage assistant = response.message();
            transcript.add(assistant);
            Future<Void> validation = llmRound == 1
                    ? firstResponseValidator.validate(response)
                    : Future.succeededFuture();
            return validation.compose(ignored -> {
                CatholicAgentObservationContext context = new CatholicAgentObservationContext(
                        response, List.copyOf(transcript), llmRound, toolRounds, maxRounds);
                return observer.observe(context);
            }).compose(directive -> {
                if (directive == null) {
                    return Future.failedFuture(new IllegalStateException("observer returned null directive"));
                }
                if (directive.completed()) {
                    return Future.succeededFuture(CatholicAgentResult.completed(
                            transcript, response, llmRound, toolRounds, maxRounds));
                }
                transcript.addAll(directive.messagesToAppend());
                if (llmRound >= maxRounds) {
                    return Future.succeededFuture(CatholicAgentResult.roundLimit(
                            transcript, response, llmRound, toolRounds, maxRounds));
                }
                if (assistant.hasToolCalls()) {
                    return appendToolResultsSequential(transcript, assistant.toolCalls(), 0,
                            interactionToolHandler)
                            .compose(v -> execute(transcript, llmRound + 1, toolRounds + 1, null,
                                    firstResponseValidator, interactionTools, interactionToolHandler));
                }
                if (directive.messagesToAppend().isEmpty()) {
                    return Future.failedFuture(new IllegalStateException(
                            "observer requested continuation without tool calls or observation messages"));
                }
                return execute(transcript, llmRound + 1, toolRounds, null, firstResponseValidator,
                        interactionTools, interactionToolHandler);
            });
        });
    }

    private Future<Void> appendToolResultsSequential(ArrayList<CatholicChatMessage> transcript,
                                                     List<CatholicFunctionToolCall> calls, int index,
                                                     CatholicToolInvocationHandler interactionToolHandler) {
        if (index >= calls.size()) return Future.succeededFuture();
        CatholicFunctionToolCall call = calls.get(index);
        return interactionToolHandler.handle(call).compose(result -> {
            if (result == null) return Future.failedFuture(new IllegalStateException(
                    "tool returned null: " + call.functionName()));
            transcript.add(CatholicToolCallMessage.of(call.id(), result));
            return appendToolResultsSequential(transcript, calls, index + 1, interactionToolHandler);
        });
    }

    private Future<SkillContext> prepareSkillContext() {
        if (skillProvider == null) {
            return Future.succeededFuture(new SkillContext(tools, toolHandler, null));
        }
        return skillProvider.getSkillCandidates().compose(candidates -> {
            if (candidates == null) {
                return Future.failedFuture(new IllegalStateException("skill provider returned null candidates"));
            }
            Map<String, CatholicSkillFrontmatter> byName = new LinkedHashMap<>();
            for (CatholicSkillFrontmatter candidate : candidates) {
                if (candidate == null) {
                    return Future.failedFuture(new IllegalStateException("skill provider returned a null candidate"));
                }
                validateSkillFrontmatter(candidate);
                if (byName.putIfAbsent(candidate.name(), candidate) != null) {
                    return Future.failedFuture(new IllegalStateException("duplicate skill name: " + candidate.name()));
                }
            }
            if (byName.isEmpty()) {
                return Future.succeededFuture(new SkillContext(tools, toolHandler, null));
            }

            ArrayList<CatholicToolDefinition> interactionTools = new ArrayList<>(tools);
            JsonArray names = new JsonArray(new ArrayList<>(byName.keySet()));
            JsonObject parameters = new JsonObject()
                    .put("type", "object")
                    .put("properties", new JsonObject().put("name", new JsonObject()
                            .put("type", "string").put("enum", names)
                            .put("description", "The name of the skill to activate.")))
                    .put("required", new JsonArray().add("name"))
                    .put("additionalProperties", false);
            interactionTools.add(CatholicToolDefinition.function(FunctionDefinition.of(
                    ACTIVATE_SKILL_FUNCTION_NAME,
                    "Load the full instructions for one available skill before applying it.", parameters)));

            Set<String> activatedSkillNames = new HashSet<>();
            CatholicToolInvocationHandler interactionHandler = call -> {
                if (!ACTIVATE_SKILL_FUNCTION_NAME.equals(call.functionName())) return toolHandler.handle(call);
                String name;
                try {
                    name = call.parseArguments().getString("name");
                } catch (RuntimeException e) {
                    return Future.failedFuture(e);
                }
                if (name == null || !byName.containsKey(name)) {
                    return Future.failedFuture(new IllegalArgumentException("unknown skill: " + name));
                }
                if (activatedSkillNames.contains(name)) {
                    return Future.succeededFuture("Skill '" + name
                            + "' is already active; its instructions are already present in this conversation.");
                }
                return skillProvider.loadSkillByName(name).compose(skill -> {
                    if (skill == null) {
                        return Future.failedFuture(new IllegalStateException("skill provider returned null: " + name));
                    }
                    validateSkillFrontmatter(skill);
                    if (!name.equals(skill.name())) {
                        return Future.failedFuture(new IllegalStateException(
                                "loaded skill name mismatch: expected " + name + ", got " + skill.name()));
                    }
                    if (skill.instructions() == null || skill.instructions().isBlank()) {
                        return Future.failedFuture(new IllegalStateException("skill instructions are blank: " + name));
                    }
                    activatedSkillNames.add(name);
                    return Future.succeededFuture("<skill_instructions name=\"" + name + "\">\n"
                            + skill.instructions() + "\n</skill_instructions>");
                });
            };
            return Future.succeededFuture(new SkillContext(List.copyOf(interactionTools), interactionHandler,
                    CatholicSystemMessage.of(buildSkillCatalog(byName.values().stream().toList()))));
        });
    }

    private record SkillContext(List<CatholicToolDefinition> tools,
                                CatholicToolInvocationHandler handler,
                                @Nullable CatholicSystemMessage catalogMessage) {
    }

    public static final class Builder {
        private final LateObject<CatholicLLM> lateLlm = new LateObject<>();
        private final LateObject<String> lateModel = new LateObject<>();
        private final List<CatholicToolDefinition> tools = new ArrayList<>();
        private final LateObject<CatholicToolInvocationHandler> lateToolHandler = new LateObject<>();
        private final List<CatholicChatMessage> initialMessages = new ArrayList<>();
        private CatholicLLMRequestOptions options = CatholicLLMRequestOptions.defaultOptions();
        private CatholicAgentObserver observer = CatholicAgentObserver.defaultObserver();
        private int maxRounds = 32;
        private @Nullable CatholicSkillProvider skillProvider;

        public Builder llm(CatholicLLM llm) {
            lateLlm.set(Objects.requireNonNull(llm, "llm"));
            return this;
        }

        public Builder model(String model) {
            lateModel.set(Objects.requireNonNull(model, "model"));
            return this;
        }

        public Builder addTool(CatholicToolDefinition tool) {
            tools.add(Objects.requireNonNull(tool, "tool"));
            return this;
        }

        public Builder tools(List<CatholicToolDefinition> tools) {
            this.tools.clear();
            Objects.requireNonNull(tools, "tools").forEach(this::addTool);
            return this;
        }

        public Builder options(CatholicLLMRequestOptions options) {
            this.options = Objects.requireNonNull(options, "options");
            return this;
        }

        public Builder toolHandler(CatholicToolInvocationHandler handler) {
            lateToolHandler.set(Objects.requireNonNull(handler, "toolHandler"));
            return this;
        }

        public Builder observer(CatholicAgentObserver observer) {
            this.observer = Objects.requireNonNull(observer, "observer");
            return this;
        }

        public Builder maxRounds(int maxRounds) {
            this.maxRounds = maxRounds;
            return this;
        }

        public Builder skillProvider(@Nullable CatholicSkillProvider skillProvider) {
            this.skillProvider = skillProvider;
            return this;
        }

        /**
         * @deprecated use {@link #maxRounds(int)}
         */
        @Deprecated
        public Builder maxToolRounds(int maxToolRounds) {
            return maxRounds(maxToolRounds + 1);
        }

        public Builder systemPrompt(@Nullable String text) {
            if (text != null && !text.isBlank()) initialMessages.add(CatholicSystemMessage.of(text));
            return this;
        }

        public CatholicAgent build() {
            if (!lateLlm.isInitialized()) throw new IllegalArgumentException("llm is required");
            if (!lateModel.isInitialized() || lateModel.get().isBlank())
                throw new IllegalArgumentException("model is required");
            if (maxRounds < 1) throw new IllegalArgumentException("maxRounds must be at least 1");
            if (!tools.isEmpty() && !lateToolHandler.isInitialized())
                throw new IllegalArgumentException("toolHandler is required when tools are non-empty");
            if (tools.stream().filter(CatholicFunctionToolDefinition.class::isInstance)
                     .map(CatholicFunctionToolDefinition.class::cast)
                     .anyMatch(tool -> ACTIVATE_SKILL_FUNCTION_NAME.equals(tool.function().name()))) {
                throw new IllegalArgumentException("reserved tool name: " + ACTIVATE_SKILL_FUNCTION_NAME);
            }
            CatholicToolInvocationHandler handler = tools.isEmpty()
                    ? tc -> Future.failedFuture(new IllegalStateException("no tools configured"))
                    : lateToolHandler.get();
            return new CatholicAgent(lateLlm.get(), lateModel.get(), List.copyOf(tools), options,
                    handler, observer, maxRounds, List.copyOf(initialMessages), skillProvider);
        }
    }
}
