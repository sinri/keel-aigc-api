package io.github.sinri.keel.aigc.api.internal.catholic.request;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicChatMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.request.CatholicLLMRequestOptions;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.CatholicTool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CatholicLLMRequest的实现类。
 */
public class CatholicLLMRequestImpl implements CatholicLLMRequest {

    private final String model;
    private final List<CatholicChatMessage> messages;
    private final List<CatholicTool> tools;
    private final CatholicLLMRequestOptions options;
    private final boolean stream;

    public CatholicLLMRequestImpl(
        String model,
        List<CatholicChatMessage> messages,
        List<CatholicTool> tools,
        CatholicLLMRequestOptions options,
        boolean stream
    ) {
        this.model = model;
        this.messages = messages != null ? List.copyOf(messages) : Collections.emptyList();
        this.tools = tools != null ? List.copyOf(tools) : Collections.emptyList();
        this.options = options != null ? options : CatholicLLMRequestOptionsImpl.defaultOptions();
        this.stream = stream;
    }

    @Override
    public String model() {
        return model;
    }

    @Override
    public List<CatholicChatMessage> messages() {
        return messages;
    }

    @Override
    public List<CatholicTool> tools() {
        return tools;
    }

    @Override
    public CatholicLLMRequestOptions options() {
        return options;
    }

    @Override
    public boolean stream() {
        return stream;
    }

    /**
     * 是否有工具定义
     */
    public boolean hasTools() {
        return tools != null && !tools.isEmpty();
    }

    // === Builder ===

    /**
     * 创建Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder类
     */
    public static class Builder {
        private String model;
        private final List<CatholicChatMessage> messages = new ArrayList<>();
        private final List<CatholicTool> tools = new ArrayList<>();
        private CatholicLLMRequestOptions options = CatholicLLMRequestOptionsImpl.defaultOptions();
        private boolean stream = false;

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder addMessage(CatholicChatMessage message) {
            this.messages.add(message);
            return this;
        }

        public Builder messages(List<CatholicChatMessage> messages) {
            this.messages.clear();
            this.messages.addAll(messages);
            return this;
        }

        public Builder addTool(CatholicTool tool) {
            this.tools.add(tool);
            return this;
        }

        public Builder tools(List<CatholicTool> tools) {
            this.tools.clear();
            this.tools.addAll(tools);
            return this;
        }

        public Builder options(CatholicLLMRequestOptions options) {
            this.options = options;
            return this;
        }

        public Builder stream(boolean stream) {
            this.stream = stream;
            return this;
        }

        public Builder enableStream() {
            this.stream = true;
            return this;
        }

        public CatholicLLMRequestImpl build() {
            if (model == null || model.isEmpty()) {
                throw new IllegalArgumentException("model is required");
            }
            return new CatholicLLMRequestImpl(model, messages, tools, options, stream);
        }
    }
}