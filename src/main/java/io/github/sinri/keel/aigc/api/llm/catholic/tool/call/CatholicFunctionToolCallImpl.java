package io.github.sinri.keel.aigc.api.llm.catholic.tool.call;

/**
 * function 类型的工具调用实现。
 */
public record CatholicFunctionToolCallImpl(
    String id,
    FunctionCall function
) implements CatholicFunctionToolCall {

    @Override
    public String type() {
        return "function";
    }
}