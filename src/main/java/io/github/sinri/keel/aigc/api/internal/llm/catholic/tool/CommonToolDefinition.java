package io.github.sinri.keel.aigc.api.internal.llm.catholic.tool;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.FunctionToolDefinition;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.ToolDefinition;
import io.vertx.core.json.JsonObject;

public class CommonToolDefinition extends JsonifiableDataUnitImpl implements ToolDefinition {
    public CommonToolDefinition() {
        this(new JsonObject());
    }

    public CommonToolDefinition(JsonObject jsonObject) {
        super(jsonObject);
    }

    public CommonToolDefinition(FunctionToolDefinition functionToolDefinition) {
        super(new JsonObject());
        function(functionToolDefinition);
    }

    @Override
    public FunctionToolDefinition function() {
        JsonObject f = readJsonObject("function");
        if (f == null) {
            throw new IllegalStateException("No function definition in this tool definition!");
        }
        return new CommonFunctionToolDefinition(f);
    }

}
