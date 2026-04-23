package io.github.sinri.keel.aigc.api.llm.catholic.tool.call;

import io.vertx.core.json.JsonObject;

/**
 * 工具调用请求，LLM请求执行某个工具时生成。
 */
public interface CatholicFunctionToolCall extends CatholicToolCall {
    FunctionCall function();

    /**
     * 解析参数为JsonObject
     */
    default JsonObject parseArguments() {
        if (function().arguments().isEmpty()) {
            return new JsonObject();
        }
        return new JsonObject(function().arguments());
    }

    /**
     * 获取函数名称
     */
    default String functionName() {
        return function().name();
    }
}