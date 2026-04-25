package io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.function;

import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

/**
 * 工具函数定义的实现。
 */
public record FunctionDefinitionImpl(
    String name,
    String description,
    @Nullable JsonObject parameters
) implements FunctionDefinition {

    /**
     * 创建函数定义
     */
    public static FunctionDefinition of(String name, String description, @Nullable JsonObject parameters) {
        return new FunctionDefinitionImpl(name, description, parameters);
    }

    /**
     * 创建无参数函数定义
     */
    public static FunctionDefinition of(String name, String description) {
        return new FunctionDefinitionImpl(name, description, new JsonObject());
    }
}