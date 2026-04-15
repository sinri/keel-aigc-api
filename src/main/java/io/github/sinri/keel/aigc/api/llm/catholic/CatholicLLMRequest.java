package io.github.sinri.keel.aigc.api.llm.catholic;

import io.github.sinri.keel.aigc.api.internal.catholic.request.CatholicLLMRequestImpl;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicChatMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.request.CatholicLLMRequestOptions;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.CatholicTool;

import java.util.List;

/**
 * 一种通用 LLM 请求格式的接口定义，包含所调用的模型、上下文和提示词、工具调用等参数，支持多模态请求。
 */
public interface CatholicLLMRequest {
    /**
     * 模型标识（如 "gpt-4o", "claude-3-5-sonnet", "qwen-max"）
     */
    String model();

    /**
     * 消息列表（对话历史）
     */
    List<CatholicChatMessage> messages();

    /**
     * 工具定义列表（可选）
     */
    List<CatholicTool> tools();

    /**
     * 生成参数选项
     */
    CatholicLLMRequestOptions options();

    /**
     * 是否流式调用
     */
    boolean stream();

    /**
     * 是否有工具定义
     */
    default boolean hasTools() {
        return tools() != null && !tools().isEmpty();
    }

    /**
     * 创建请求Builder
     */
    static CatholicLLMRequestImpl.Builder builder() {
        return CatholicLLMRequestImpl.builder();
    }
}