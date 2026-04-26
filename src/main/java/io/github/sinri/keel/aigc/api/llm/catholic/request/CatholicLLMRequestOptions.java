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