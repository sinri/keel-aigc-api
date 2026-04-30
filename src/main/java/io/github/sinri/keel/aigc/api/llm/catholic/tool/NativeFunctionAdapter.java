package io.github.sinri.keel.aigc.api.llm.catholic.tool;

import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.CatholicToolDefinition;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.function.FunctionDefinition;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public interface NativeFunctionAdapter {
    String functionName();

    String functionDescription();

    Map<String, ParameterDefinition> parameterDefinitionMap();

    default Future<String> call(JsonObject args) {
        return call(null, args);
    }

    Future<String> call(@Nullable JsonObject fixedArgs, JsonObject args);

    default FunctionDefinition toFunctionDefinition() {
        FunctionDefinition.Builder builder = FunctionDefinition.builder();
        builder.name(functionName()).description(functionDescription());
        this.parameterDefinitionMap().forEach((name, definition) -> {
            builder.addParameter(definition.name, definition.type(), definition.description(), definition.required());
        });
        return builder.build();
    }

    default CatholicToolDefinition toCatholicToolDefinition() {
        return CatholicToolDefinition.function(toFunctionDefinition());
    }

    record ParameterDefinition(String name, String type, String description, boolean required) {
    }
}