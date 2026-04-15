package io.github.sinri.keel.aigc.api.llm.catholic.tool;

import io.vertx.core.json.JsonObject;

/**
 * 工具调用请求，LLM请求执行某个工具时生成。
 */
public record CatholicToolCall(
    String id,
    String type,
    CatholicToolCallFunction function
) {
    /**
     * 工具调用的函数信息
     */
    public record CatholicToolCallFunction(
        String name,
        String arguments
    ) {
    }

    /**
     * 解析参数为JsonObject
     */
    public JsonObject parseArguments() {
        if (function.arguments() == null || function.arguments().isEmpty()) {
            return new JsonObject();
        }
        return new JsonObject(function.arguments());
    }

    /**
     * 获取函数名称
     */
    public String functionName() {
        return function.name();
    }
}