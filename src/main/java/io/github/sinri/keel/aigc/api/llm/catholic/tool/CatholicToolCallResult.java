package io.github.sinri.keel.aigc.api.llm.catholic.tool;

/**
 * 工具调用结果，包含执行结果用于反馈给LLM。
 */
public record CatholicToolCallResult(
    String toolCallId,
    String content
) {
    /**
     * 创建成功结果的便捷方法
     */
    public static CatholicToolCallResult success(String toolCallId, String content) {
        return new CatholicToolCallResult(toolCallId, content);
    }

    /**
     * 创建错误结果的便捷方法
     */
    public static CatholicToolCallResult error(String toolCallId, String errorMessage) {
        return new CatholicToolCallResult(toolCallId, "{\"error\": \"" + errorMessage + "\"}");
    }
}