package io.github.sinri.keel.aigc.api.internal.catholic.request;

import io.github.sinri.keel.aigc.api.llm.catholic.request.CatholicLLMRequestOptions;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * LLM请求的生成参数选项实现类。
 */
public record CatholicLLMRequestOptionsImpl(
    @Nullable Double temperature,
    @Nullable Integer maxTokens,
    @Nullable Double topP,
    @Nullable List<String> stop,
    JsonObject extra,
    @Nullable String requiredToolName
) implements CatholicLLMRequestOptions {

    public CatholicLLMRequestOptionsImpl {
        if (requiredToolName != null && requiredToolName.isBlank()) {
            throw new IllegalArgumentException("requiredToolName must not be blank");
        }
    }

    /** 保留原有构造方式。 */
    public CatholicLLMRequestOptionsImpl(@Nullable Double temperature, @Nullable Integer maxTokens,
            @Nullable Double topP, @Nullable List<String> stop, JsonObject extra) {
        this(temperature, maxTokens, topP, stop, extra, null);
    }

    /**
     * 创建默认选项
     */
    public static CatholicLLMRequestOptionsImpl defaultOptions() {
        return new CatholicLLMRequestOptionsImpl(null, null, null, null, new JsonObject());
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
    public static class Builder implements CatholicLLMRequestOptions.Builder {
        private @Nullable Double temperature;
        private @Nullable Integer maxTokens;
        private @Nullable Double topP;
        private @Nullable List<String> stop;
        private @Nullable String requiredToolName;
        private JsonObject extra = new JsonObject();

        @Override
        public Builder requiredToolName(String name) {
            this.requiredToolName = java.util.Objects.requireNonNull(name, "name");
            return this;
        }

        public Builder temperature(double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder maxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder topP(double topP) {
            this.topP = topP;
            return this;
        }

        public Builder stop(List<String> stop) {
            this.stop = stop;
            return this;
        }

        public Builder stop(String... stopSequences) {
            this.stop = List.of(stopSequences);
            return this;
        }

        public Builder extra(JsonObject extra) {
            this.extra = extra;
            return this;
        }

        public Builder putExtra(String key, Object value) {
            this.extra.put(key, value);
            return this;
        }

        public CatholicLLMRequestOptionsImpl build() {
            return new CatholicLLMRequestOptionsImpl(temperature, maxTokens, topP, stop, extra, requiredToolName);
        }
    }
}