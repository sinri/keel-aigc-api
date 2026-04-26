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
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.CatholicToolDefinition;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.CatholicFunctionToolCall;
import io.github.sinri.keel.logger.api.LateObject;
import io.vertx.core.Future;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 在 {@link CatholicLLM} 之上编排「用户消息 → 模型回复 →（若有）工具执行 → 再请求模型」的同步（非 stream）对话循环。
 * <p>
 * 会话状态为可变的消息列表；{@link #chat(String)} / {@link #chat(CatholicUserMessage)} 会追加用户输入并推进循环直至得到无工具调用的最终回复或超出轮次上限。
 */
public final class CatholicAgent {

    private final CatholicLLM llm;
    private final String model;
    private final List<CatholicToolDefinition> tools;
    private final CatholicLLMRequestOptions options;
    private final CatholicToolInvocationHandler toolHandler;
    private final int maxToolRounds;
    private final ArrayList<CatholicChatMessage> messages;

    private CatholicAgent(
        CatholicLLM llm,
        String model,
        List<CatholicToolDefinition> tools,
        CatholicLLMRequestOptions options,
        CatholicToolInvocationHandler toolHandler,
        int maxToolRounds,
        List<CatholicChatMessage> initialMessages
    ) {
        this.llm = llm;
        this.model = model;
        this.tools = tools;
        this.options = options;
        this.toolHandler = toolHandler;
        this.maxToolRounds = maxToolRounds;
        this.messages = new ArrayList<>(initialMessages);
    }

    /**
     * 追加一条用户文本并运行工具循环，返回最后一轮非流式调用的 {@link CatholicLLMResponse}。
     */
    public Future<CatholicLLMResponse> chat(String userText) {
        return chat(CatholicUserMessage.ofText(userText));
    }

    /**
     * 追加一条用户消息并运行工具循环。
     */
    public Future<CatholicLLMResponse> chat(CatholicUserMessage userMessage) {
        messages.add(userMessage);
        return executeToolLoop(0);
    }

    /**
     * 当前会话消息快照（含 system / user / assistant / tool），按轮次追加顺序排列。
     */
    public List<CatholicChatMessage> conversationMessages() {
        return List.copyOf(messages);
    }

    private Future<CatholicLLMResponse> executeToolLoop(int toolRoundIndex) {
        if (toolRoundIndex > maxToolRounds) {
            return Future.failedFuture(new CatholicAgentTooManyToolRoundsException(maxToolRounds));
        }

        CatholicLLMRequest request = CatholicLLMRequest.builder()
            .model(model)
            .messages(new ArrayList<>(messages))
            .tools(tools)
            .options(options)
            .stream(false)
            .build();

        return llm.call(request).compose(response -> {
            CatholicAssistantMessage assistant = copyAssistant(response.message());
            messages.add(assistant);

            if (!assistant.hasToolCalls()) {
                return Future.succeededFuture(response);
            }

            List<CatholicFunctionToolCall> calls = assistant.toolCalls();
            return appendToolResultsSequential(calls, 0)
                .compose(v -> executeToolLoop(toolRoundIndex + 1));
        });
    }

    private Future<Void> appendToolResultsSequential(List<CatholicFunctionToolCall> calls, int index) {
        if (index >= calls.size()) {
            return Future.succeededFuture();
        }
        CatholicFunctionToolCall call = calls.get(index);
        return toolHandler.handle(call).compose(result -> {
            messages.add(CatholicToolCallMessage.of(call.id(), result));
            return appendToolResultsSequential(calls, index + 1);
        });
    }

    private static CatholicAssistantMessage copyAssistant(CatholicAssistantMessage source) {
        List<CatholicFunctionToolCall> tc = source.hasToolCalls()
            ? new ArrayList<>(source.toolCalls())
            : Collections.emptyList();
        return CatholicAssistantMessage.ofMixed(source.text(), tc.isEmpty() ? null : tc);
    }

    /**
     * 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final LateObject<CatholicLLM> lateLlm = new LateObject<>();
        private final LateObject<String> lateModel = new LateObject<>();
        private final List<CatholicToolDefinition> tools = new ArrayList<>();
        private CatholicLLMRequestOptions options = CatholicLLMRequestOptions.defaultOptions();
        private final LateObject<CatholicToolInvocationHandler> lateToolHandler=new LateObject<>();
        private int maxToolRounds = 32;
        private final List<CatholicChatMessage> initialMessages = new ArrayList<>();

        public Builder llm(CatholicLLM llm) {
            this.lateLlm.set(llm);
            return this;
        }

        public Builder model(String model) {
            this.lateModel.set(model);
            return this;
        }

        public Builder addTool(CatholicToolDefinition tool) {
            this.tools.add(tool);
            return this;
        }

        public Builder tools(List<CatholicToolDefinition> tools) {
            this.tools.clear();
            this.tools.addAll(tools);
            return this;
        }

        public Builder options(CatholicLLMRequestOptions options) {
            this.options = options;
            return this;
        }

        public Builder toolHandler(CatholicToolInvocationHandler toolHandler) {
            this.lateToolHandler.set(  toolHandler);
            return this;
        }

        public Builder maxToolRounds(int maxToolRounds) {
            this.maxToolRounds = maxToolRounds;
            return this;
        }

        /**
         * 在会话开头插入一条系统消息（通常在首次 {@link #chat} 前调用一次）。
         */
        public Builder systemPrompt(String text) {
            if (!text.isEmpty()) {
                this.initialMessages.add(CatholicSystemMessage.of(text));
            }
            return this;
        }

        public CatholicAgent build() {
            if (!lateLlm.isInitialized()) {
                throw new IllegalArgumentException("llm is required");
            }
            if (!lateModel.isInitialized() || lateModel.get().isEmpty()) {
                throw new IllegalArgumentException("model is required");
            }
            if (!tools.isEmpty()) {
                if(!lateToolHandler.isInitialized()) {
                    throw new IllegalArgumentException("toolHandler is required when tools are non-empty");
                }
            }
            CatholicToolInvocationHandler resolvedHandler = tools.isEmpty()
                ? tc -> Future.succeededFuture("")
                : lateToolHandler.get();
            return new CatholicAgent(lateLlm.get(), lateModel.get(), List.copyOf(tools), options, resolvedHandler, maxToolRounds, initialMessages);
        }
    }
}
