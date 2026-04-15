package io.github.sinri.keel.aigc.api.llm.catholic.tool;

import io.vertx.core.json.JsonObject;

/**
 * 工具定义，用于让LLM调用外部函数。
 */
public record CatholicTool(
    String type,
    CatholicToolFunction function
) {
    /**
     * 工具函数定义
     */
    public record CatholicToolFunction(
        String name,
        String description,
        JsonObject parameters
    ) {
    }

    /**
     * 创建函数类型工具的便捷方法
     */
    public static CatholicTool function(String name, String description, JsonObject parameters) {
        return new CatholicTool("function", new CatholicToolFunction(name, description, parameters));
    }

    /**
     * 创建无参数函数工具的便捷方法
     */
    public static CatholicTool function(String name, String description) {
        return function(name, description, new JsonObject());
    }
}