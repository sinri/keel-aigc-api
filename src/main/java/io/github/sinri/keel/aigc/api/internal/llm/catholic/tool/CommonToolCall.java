package io.github.sinri.keel.aigc.api.internal.llm.catholic.tool;

import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.FunctionToolCall;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.ToolCall;
import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class CommonToolCall extends JsonifiableDataUnitImpl implements ToolCall {
    public CommonToolCall(JsonObject jsonObject) {
        super(jsonObject);
    }

    public CommonToolCall(String id, Integer index, @Nullable FunctionToolCall functionToolCall) {
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
    public FunctionToolCall getFunction() {
        var a = readJsonObject("function");
        Objects.requireNonNull(a);
        return new CommonFunctionToolCall(a);
    }

}