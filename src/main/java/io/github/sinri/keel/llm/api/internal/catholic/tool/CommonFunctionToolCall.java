package io.github.sinri.keel.llm.api.internal.catholic.tool;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.llm.api.catholic.tool.call.FunctionToolCall;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

public class CommonFunctionToolCall extends JsonifiableDataUnitImpl implements FunctionToolCall {
    public CommonFunctionToolCall(String name, String arguments) {
        this(new JsonObject()
                .put("name", name)
                .put("arguments", arguments));
    }

    public CommonFunctionToolCall(JsonObject jsonObject) {
        super(jsonObject);
    }

    @Override
    public @Nullable String getName() {
        return readString("name");
    }

    @Override
    public @Nullable String getArguments() {
        return readString("arguments");
    }


}
