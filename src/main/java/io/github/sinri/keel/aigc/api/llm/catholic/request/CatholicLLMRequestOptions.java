package io.github.sinri.keel.aigc.api.llm.catholic.request;

import io.github.sinri.keel.aigc.api.internal.catholic.request.CatholicLLMRequestOptionsImpl;
import org.jspecify.annotations.Nullable;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * LLM请求的生成参数选项接口。
 */
public interface CatholicLLMRequestOptions {
    /**
     * 温度参数（0-2）
     */
    @Nullable Double temperature();

    /**
     * 最大生成token数
     */
    @Nullable Integer maxTokens();

    /**
     * Top-p采样
     */
    @Nullable Double topP();

    /**
     * 停止词列表
     */
    @Nullable List<String> stop();

    /**
     * 指定必须调用的函数名称；非 null 时优先于 extra 中的 tool_choice。
     * 未设置时保留厂商原生参数。
     */
    default @Nullable String requiredToolName() { return null; }

    /**
     * 其他厂商特有参数（扩展用）
     */
    JsonObject extra();

    /**
     * 创建默认选项
     */
    static CatholicLLMRequestOptions defaultOptions() {
        return CatholicLLMRequestOptionsImpl.defaultOptions();
    }

    /**
     * 创建Builder
     */
    static Builder builder() {
        return CatholicLLMRequestOptionsImpl.builder();
    }

    /**
     * Builder接口，将构造逻辑暴露给外部模块。
     */
    interface Builder {
        /** 指定必须调用的函数。旧的自定义 Builder 如需支持此选项，应覆盖本方法。 */
        default Builder requiredToolName(String name) {
            throw new UnsupportedOperationException("requiredToolName is not supported by this builder");
        }

        Builder temperature(double temperature);

        Builder maxTokens(int maxTokens);

        Builder topP(double topP);

        Builder stop(List<String> stop);

        Builder stop(String... stopSequences);

        Builder extra(JsonObject extra);

        Builder putExtra(String key, Object value);

        CatholicLLMRequestOptions build();
    }
}