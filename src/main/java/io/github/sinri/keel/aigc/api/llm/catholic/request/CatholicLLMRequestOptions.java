package io.github.sinri.keel.aigc.api.llm.catholic.request;

import io.github.sinri.keel.aigc.api.internal.catholic.request.CatholicLLMRequestOptionsImpl;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * LLM请求的生成参数选项接口。
 */
public interface CatholicLLMRequestOptions {
    /**
     * 温度参数（0-2）
     */
    Double temperature();

    /**
     * 最大生成token数
     */
    Integer maxTokens();

    /**
     * Top-p采样
     */
    Double topP();

    /**
     * 停止词列表
     */
    List<String> stop();

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
    static CatholicLLMRequestOptionsImpl.Builder builder() {
        return CatholicLLMRequestOptionsImpl.builder();
    }
}