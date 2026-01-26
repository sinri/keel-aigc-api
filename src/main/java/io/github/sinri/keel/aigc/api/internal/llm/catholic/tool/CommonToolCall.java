package io.github.sinri.keel.aigc.api.internal.llm.catholic.tool;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.FunctionToolCall;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.ToolCall;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

public class CommonToolCall extends JsonifiableDataUnitImpl implements ToolCall {
    public CommonToolCall(JsonObject jsonObject) {
        super(jsonObject);
    }

    public CommonToolCall(String id, Integer index,@Nullable FunctionToolCall functionToolCall) {
        this(new JsonObject()
                .put("id", id)
                .put("index", index)
                .put("type", "function")
                .put("function", functionToolCall == null ? null : functionToolCall.toJsonObject())
        );
    }

    @Override
    public String getType() {
        return readStringRequired("type");
    }

    @Override
    public String getId() {
        return readStringRequired("id");
    }

    @Override
    public Integer getIndex() {
        return readIntegerRequired("index");
    }

    @Override
    public @Nullable FunctionToolCall getFunction() {
        var a = readJsonObject("function");
        if (a == null) {
            return null;
        } else {
            return new CommonFunctionToolCall(a);
        }
    }

}