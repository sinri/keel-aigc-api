package io.github.sinri.keel.aigc.api.internal.catholic.response;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicAssistantMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.response.CatholicLLMUsage;

/**
 * CatholicLLMResponse的实现类。
 */
public class CatholicLLMResponseImpl implements CatholicLLMResponse {

    private final String id;
    private final CatholicAssistantMessage message;
    private final CatholicLLMUsage usage;

    public CatholicLLMResponseImpl(String id, CatholicAssistantMessage message, CatholicLLMUsage usage) {
        this.id = id;
        this.message = message;
        this.usage = usage != null ? usage : CatholicLLMUsage.empty();
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public CatholicAssistantMessage message() {
        return message;
    }

    @Override
    public CatholicLLMUsage usage() {
        return usage;
    }

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
        private String id;
        private CatholicAssistantMessage message;
        private CatholicLLMUsage usage = CatholicLLMUsage.empty();

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder message(CatholicAssistantMessage message) {
            this.message = message;
            return this;
        }

        public Builder usage(CatholicLLMUsage usage) {
            this.usage = usage;
            return this;
        }

        public Builder usage(Integer promptTokens, Integer completionTokens, Integer totalTokens) {
            this.usage = new CatholicLLMUsage(promptTokens, completionTokens, totalTokens);
            return this;
        }

        public CatholicLLMResponseImpl build() {
            if (message == null) {
                throw new IllegalArgumentException("message is required");
            }
            return new CatholicLLMResponseImpl(id, message, usage);
        }
    }
}