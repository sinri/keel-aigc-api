package io.github.sinri.keel.aigc.api.internal.llm.catholic.tool;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.FunctionParameterDefinition;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.FunctionToolDefinition;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class CommonFunctionToolDefinition extends JsonifiableDataUnitImpl implements FunctionToolDefinition {
    public CommonFunctionToolDefinition() {
        this(new JsonObject());
    }

    public CommonFunctionToolDefinition(JsonObject jsonObject) {
        super(jsonObject);
    }

    public CommonFunctionToolDefinition(
            String name,
            String desc,
            List<FunctionParameterDefinition> parameterDefinitions
    ) {
        super(new JsonObject());
        name(name);
        description(desc);
        parameters(parameterDefinitions);
    }
}
