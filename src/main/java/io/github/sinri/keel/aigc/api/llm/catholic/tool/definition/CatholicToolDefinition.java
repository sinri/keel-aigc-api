package io.github.sinri.keel.aigc.api.llm.catholic.tool.definition;

import io.vertx.core.json.JsonObject;

/**
 * 工具定义的接口，支持多种工具类型（如 function）。
 */
public interface CatholicToolDefinition {
    /**
     * 工具类型标识。
     * <p>
     * 例如，对于{@link CatholicFunctionToolDefinition}, type 为 function
     */
    String type();

    /**
     * 相应的工具定义。
     * <p>
     * 例如，对于{@link CatholicFunctionToolDefinition}, type 为 function ，返回 FunctionDefinition 对象。
     */
    Object getToolDefinition();

    /**
     * 创建 function 类型的工具定义
     */
    static CatholicToolDefinition function(String name, String description, JsonObject parameters) {
        return CatholicFunctionToolDefinitionImpl.function(name, description, parameters);
    }

    /**
     * 创建无参数的 function 类型工具定义
     */
    static CatholicToolDefinition function(String name, String description) {
        return CatholicFunctionToolDefinitionImpl.function(name, description);
    }
}