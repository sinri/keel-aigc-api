package io.github.sinri.keel.aigc.api.internal.llm.catholic.tool;

import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.FunctionToolCall;
import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

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
    public String getName() {
        return Objects.requireNonNull(readString("name"));
    }

    @Override
    public @Nullable String getArguments() {
        return readString("arguments");
    }


}
