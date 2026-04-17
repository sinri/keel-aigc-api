package io.github.sinri.keel.aigc.api.llm.dashscope.multimodalgeneration;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.vertx.core.json.JsonArray;

/**
 * DashScope 多模态 API 响应包装类，在 CatholicLLMResponse 基础上
 * 提供多模态特有的字段访问。
 */
public record DashScopeMultimodalResponse(
    CatholicLLMResponse catholicResponse,
    String reasoningContent,
    JsonArray imageHw,
    Integer imageTokens,
    Integer videoTokens,
    Integer audioTokens
) {
    private static final JsonArray EMPTY_ARRAY = new JsonArray();

    /**
     * 获取标准 Catholic 响应
     */
    public CatholicLLMResponse catholicResponse() {
        return catholicResponse;
    }

    /**
     * 获取思考内容（reasoning_content），适用于开启了 enable_thinking 的模型
     */
    public String reasoningContent() {
        return reasoningContent;
    }

    /**
     * 获取图片尺寸信息 [height, width]，适用于开启了 vl_enable_image_hw_output 的模型
     */
    public JsonArray imageHw() {
        return imageHw != null ? imageHw : EMPTY_ARRAY;
    }
}