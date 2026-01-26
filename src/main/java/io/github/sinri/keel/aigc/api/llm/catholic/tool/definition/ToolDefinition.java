package io.github.sinri.keel.aigc.api.llm.catholic.tool.definition;

import io.github.sinri.keel.base.json.JsonifiableDataUnit;
import io.github.sinri.keel.aigc.api.internal.llm.catholic.tool.CommonToolDefinition;

/**
 * 工具定义接口。
 * <p>
 * 用于定义 LLM 可以调用的工具，当前主要支持函数类型的工具。
 *
 * @since 5.0.0
 */
public interface ToolDefinition extends JsonifiableDataUnit {

    /**
     * 创建新的工具定义实例。
     *
     * @return 工具定义实例
     */
    static ToolDefinition create() {
        return new CommonToolDefinition();
    }

    /**
     * 工具的类型。
     * 一般来说，当前仅支持{@code function}。
     */
    default String type() {
        return readStringRequired("type");
    }

    /**
     * 设置工具的类型。
     *
     * @param type 工具的类型
     */
    default ToolDefinition type(String type) {
        ensureEntry("type", type);
        return this;
    }

    /**
     * 在type为function的时候返回一个Function的实例。
     * 这也就是说，type不是function的时候，这个方法可能返回null。
     *
     * @return 函数工具定义，如果类型不是 function 则返回 null
     */
    FunctionToolDefinition function();

    /**
     * 在type为function的时候，设置所要调用的函数。
     *
     * @param functionToolDefinition 函数的定义
     * @return 当前实例，用于链式调用
     */
    default ToolDefinition function(FunctionToolDefinition functionToolDefinition) {
        type("function");
        ensureEntry("function", functionToolDefinition.toJsonObject());
        return this;
    }
}
