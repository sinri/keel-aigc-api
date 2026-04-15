package io.github.sinri.keel.aigc.api.llm.catholic;

/**
 * 一种通用 LLM 回复格式的接口定义，可能为文本回复或工具调用。
 * <p>
 * 在非 stream 调用下通过特定 LLM 回复报文转化而来；
 * 在 stream 调用下，通过收集和组装有序的 {@link CatholicLLMResponseChunk} 实例列表来构建。
 */
public interface CatholicLLMResponse {
    // todo
}
