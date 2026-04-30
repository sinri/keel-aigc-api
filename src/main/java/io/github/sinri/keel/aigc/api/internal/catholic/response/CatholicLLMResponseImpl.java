package io.github.sinri.keel.aigc.api.internal.catholic.response;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicAssistantMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.response.CatholicLLMUsage;
import io.github.sinri.keel.logger.api.LateObject;

/**
 * CatholicLLMResponse的实现类。
 */
public class CatholicLLMResponseImpl implements CatholicLLMResponse {

    private final String id;
    private final CatholicAssistantMessage message;
    private final CatholicLLMUsage usage;
    private final boolean finished;

    public CatholicLLMResponseImpl(String id, CatholicAssistantMessage message, CatholicLLMUsage usage, boolean finished) {
        this.id = id;
        this.message = message;
        this.usage = usage != null ? usage : CatholicLLMUsage.empty();
        this.finished = finished;
    }

    /**
     * 创建Builder
     */
    public static Builder builder() {
        return new Builder();
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

    @Override
    public boolean finished() {
        return finished;
    }

    /**
     * Builder类
     */
    public static class Builder implements CatholicLLMResponse.Builder {
        private final LateObject<String> lateId = new LateObject<>();
        private final LateObject<CatholicAssistantMessage> lateMessage = new LateObject<>();
        private CatholicLLMUsage usage = CatholicLLMUsage.empty();
        private boolean finished = true;

        public Builder id(String id) {
            this.lateId.set(id);
            return this;
        }

        public Builder message(CatholicAssistantMessage message) {
            this.lateMessage.set(message);
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

        public Builder finished(boolean finished) {
            this.finished = finished;
            return this;
        }

        public CatholicLLMResponseImpl build() {
            if (!lateMessage.isInitialized()) {
                throw new IllegalArgumentException("message is required");
            }
            return new CatholicLLMResponseImpl(lateId.get(), lateMessage.get(), usage, finished);
        }
    }
}
