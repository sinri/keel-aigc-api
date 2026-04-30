package io.github.sinri.keel.aigc.api.llm.catholic.tool.definition;

import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.function.FunctionDefinition;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.function.FunctionDefinitionImpl;
import io.vertx.core.json.JsonObject;

/**
 * function 类型的工具定义实现。
 */
public record CatholicFunctionToolDefinitionImpl(
    FunctionDefinition function
) implements CatholicFunctionToolDefinition {
    /**
     * 创建 function 类型的工具定义
     */
    @Deprecated
    public static CatholicToolDefinition function(String name, String description, JsonObject parameters) {
        return new CatholicFunctionToolDefinitionImpl(FunctionDefinitionImpl.of(name, description, parameters));
    }

    /**
     * 创建无参数的 function 类型工具定义
     */
    @Deprecated
    public static CatholicToolDefinition function(String name, String description) {
        return new CatholicFunctionToolDefinitionImpl(FunctionDefinitionImpl.of(name, description));
    }
}