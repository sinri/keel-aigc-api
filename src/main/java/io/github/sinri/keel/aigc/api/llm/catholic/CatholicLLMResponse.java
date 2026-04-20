package io.github.sinri.keel.aigc.api.llm.catholic;

import io.github.sinri.keel.aigc.api.internal.catholic.response.CatholicLLMResponseImpl;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicAssistantMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.response.CatholicLLMUsage;

/**
 * 一种通用 LLM 回复格式的接口定义，可能为文本回复或工具调用。
 * <p>
 * 在非 stream 调用下通过特定 LLM 回复报文转化而来；
 * 在 stream 调用下，通过收集和组装有序的 {@link CatholicLLMResponseChunk} 实例列表来构建。
 */
public interface CatholicLLMResponse {
    /**
     * 回复ID
     */
    String id();

    /**
     * 生成的消息内容
     */
    CatholicAssistantMessage message();

    /**
     * Token用量统计
     */
    CatholicLLMUsage usage();

    /**
     * 是否包含工具调用
     */
    default boolean hasToolCalls() {
        return message() != null && message().hasToolCalls();
    }

    /**
     * 获取纯文本回复（如果有）
     */
    default String text() {
        return message() != null ? message().text() : null;
    }

    /**
     * 是否有文本内容
     */
    default boolean hasText() {
        return message() != null && message().hasText();
    }

    /**
     * 流式回复是否完整收完（收到 [DONE] 标记或 finish_reason）。若为 false，表示连接中断导致数据不完整。
     */
    boolean finished();

    /**
     * 创建回复Builder
     */
    static CatholicLLMResponseImpl.Builder builder() {
        return CatholicLLMResponseImpl.builder();
    }
}