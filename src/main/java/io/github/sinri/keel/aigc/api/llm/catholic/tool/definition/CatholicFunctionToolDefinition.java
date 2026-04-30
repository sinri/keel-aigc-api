package io.github.sinri.keel.aigc.api.llm.catholic.tool.definition;

import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.function.FunctionDefinition;

/**
 * function 类型的工具实现。
 */
public interface CatholicFunctionToolDefinition extends CatholicToolDefinition {

    @Override
    default String type() {
        return "function";
    }

    FunctionDefinition function();

    @Override
    default Object getToolDefinition() {
        return function();
    }
}