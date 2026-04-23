package io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.function;

import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

/**
 * 工具函数定义
 */
public interface FunctionDefinition {

    String name();

    String description();

    @Nullable JsonObject parameters();
}