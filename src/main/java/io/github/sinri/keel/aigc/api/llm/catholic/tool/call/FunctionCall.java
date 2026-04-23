package io.github.sinri.keel.aigc.api.llm.catholic.tool.call;

/**
 * 工具调用的函数信息
 */
public record FunctionCall(
        String name,
        String arguments
) {
}
