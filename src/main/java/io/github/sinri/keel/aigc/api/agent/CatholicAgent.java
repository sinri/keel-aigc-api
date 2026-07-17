package io.github.sinri.keel.aigc.api.agent;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLM;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicAssistantMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicChatMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicSystemMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicToolCallMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicUserMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.request.CatholicLLMRequestOptions;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.CatholicFunctionToolCall;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.CatholicFunctionToolDefinition;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.CatholicToolDefinition;
import io.github.sinri.keel.logger.api.LateObject;
import io.vertx.core.Future;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 通用、非流式 Agent 执行器。每次 {@link #interact} 都是相互隔离的一次用户交互：
 * 用户需求进入 LLM，观察器判断完成或继续；继续时执行必要的工具并再次交给 LLM，
 * 直至完成或达到轮次上限。
 * <p>
 * 本类不保存会话历史，也不承载“必须调用某个工具”等特定业务策略。
 */
public final class CatholicAgent {
    private final CatholicLLM llm;
    private final String model;
    private final List<CatholicToolDefinition> tools;
    private final CatholicLLMRequestOptions options;
    private final CatholicToolInvocationHandler toolHandler;
    private final CatholicAgentObserver observer;
    private final int maxRounds;
    private final List<CatholicChatMessage> initialMessages;

    private CatholicAgent(CatholicLLM llm, String model, List<CatholicToolDefinition> tools,
                          CatholicLLMRequestOptions options, CatholicToolInvocationHandler toolHandler,
                          CatholicAgentObserver observer, int maxRounds,
                          List<CatholicChatMessage> initialMessages) {
        this.llm = llm;
        this.model = model;
        this.tools = tools;
        this.options = copyOptions(options, null);
        this.toolHandler = toolHandler;
        this.observer = observer;
        this.maxRounds = maxRounds;
        this.initialMessages = initialMessages;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Future<CatholicAgentResult> interact(String userText) {
        return interact(CatholicUserMessage.ofText(Objects.requireNonNull(userText, "userText")));
    }

    public Future<CatholicAgentResult> interact(CatholicUserMessage userMessage) {
        return interact(userMessage, null, response -> Future.succeededFuture());
    }

    Future<CatholicAgentResult> interact(CatholicUserMessage userMessage,
                                        @Nullable String requiredTool,
                                        CatholicAgentFirstResponseValidator firstResponseValidator) {
        Objects.requireNonNull(userMessage, "userMessage");
        Objects.requireNonNull(firstResponseValidator, "firstResponseValidator");
        ArrayList<CatholicChatMessage> transcript = new ArrayList<>(initialMessages);
        transcript.add(userMessage);
        return execute(transcript, 1, 0, requiredTool, firstResponseValidator);
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

    private static Future<CatholicLLMResponse> completedResponse(CatholicAgentResult result) {
        if (result.completed()) {
            return Future.succeededFuture(result.lastResponse());
        }
        return Future.failedFuture(new CatholicAgentTooManyToolRoundsException(result.maxRounds()));
    }

    private Future<CatholicAgentResult> execute(ArrayList<CatholicChatMessage> transcript,
                                                int llmRound, int toolRounds,
                                                @Nullable String firstRoundRequiredTool,
                                                CatholicAgentFirstResponseValidator firstResponseValidator) {
        CatholicLLMRequestOptions requestOptions = firstRoundRequiredTool == null
            ? copyOptions(options, null)
            : copyOptions(options, firstRoundRequiredTool);
        CatholicLLMRequest request = CatholicLLMRequest.builder()
            .model(model)
            .messages(List.copyOf(transcript))
            .tools(tools)
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
                    return appendToolResultsSequential(transcript, assistant.toolCalls(), 0)
                        .compose(v -> execute(transcript, llmRound + 1, toolRounds + 1, null,
                            firstResponseValidator));
                }
                if (directive.messagesToAppend().isEmpty()) {
                    return Future.failedFuture(new IllegalStateException(
                        "observer requested continuation without tool calls or observation messages"));
                }
                return execute(transcript, llmRound + 1, toolRounds, null, firstResponseValidator);
            });
        });
    }

    private Future<Void> appendToolResultsSequential(ArrayList<CatholicChatMessage> transcript,
                                                     List<CatholicFunctionToolCall> calls, int index) {
        if (index >= calls.size()) return Future.succeededFuture();
        CatholicFunctionToolCall call = calls.get(index);
        return toolHandler.handle(call).compose(result -> {
            if (result == null) return Future.failedFuture(new IllegalStateException(
                "tool returned null: " + call.functionName()));
            transcript.add(CatholicToolCallMessage.of(call.id(), result));
            return appendToolResultsSequential(transcript, calls, index + 1);
        });
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

    public static final class Builder {
        private final LateObject<CatholicLLM> lateLlm = new LateObject<>();
        private final LateObject<String> lateModel = new LateObject<>();
        private final List<CatholicToolDefinition> tools = new ArrayList<>();
        private final LateObject<CatholicToolInvocationHandler> lateToolHandler = new LateObject<>();
        private final List<CatholicChatMessage> initialMessages = new ArrayList<>();
        private CatholicLLMRequestOptions options = CatholicLLMRequestOptions.defaultOptions();
        private CatholicAgentObserver observer = CatholicAgentObserver.defaultObserver();
        private int maxRounds = 32;

        public Builder llm(CatholicLLM llm) { lateLlm.set(Objects.requireNonNull(llm, "llm")); return this; }
        public Builder model(String model) { lateModel.set(Objects.requireNonNull(model, "model")); return this; }
        public Builder addTool(CatholicToolDefinition tool) { tools.add(Objects.requireNonNull(tool, "tool")); return this; }
        public Builder tools(List<CatholicToolDefinition> tools) {
            this.tools.clear();
            Objects.requireNonNull(tools, "tools").forEach(this::addTool);
            return this;
        }
        public Builder options(CatholicLLMRequestOptions options) { this.options = Objects.requireNonNull(options, "options"); return this; }
        public Builder toolHandler(CatholicToolInvocationHandler handler) { lateToolHandler.set(Objects.requireNonNull(handler, "toolHandler")); return this; }
        public Builder observer(CatholicAgentObserver observer) { this.observer = Objects.requireNonNull(observer, "observer"); return this; }
        public Builder maxRounds(int maxRounds) { this.maxRounds = maxRounds; return this; }
        /** @deprecated use {@link #maxRounds(int)} */
        @Deprecated public Builder maxToolRounds(int maxToolRounds) { return maxRounds(maxToolRounds + 1); }
        public Builder systemPrompt(@Nullable String text) {
            if (text != null && !text.isBlank()) initialMessages.add(CatholicSystemMessage.of(text));
            return this;
        }

        public CatholicAgent build() {
            if (!lateLlm.isInitialized()) throw new IllegalArgumentException("llm is required");
            if (!lateModel.isInitialized() || lateModel.get().isBlank()) throw new IllegalArgumentException("model is required");
            if (maxRounds < 1) throw new IllegalArgumentException("maxRounds must be at least 1");
            if (!tools.isEmpty() && !lateToolHandler.isInitialized())
                throw new IllegalArgumentException("toolHandler is required when tools are non-empty");
            CatholicToolInvocationHandler handler = tools.isEmpty()
                ? tc -> Future.failedFuture(new IllegalStateException("no tools configured"))
                : lateToolHandler.get();
            return new CatholicAgent(lateLlm.get(), lateModel.get(), List.copyOf(tools), options,
                handler, observer, maxRounds, List.copyOf(initialMessages));
        }
    }
}
