package io.github.sinri.keel.aigc.api.llm.catholic.tool.call;

import io.github.sinri.keel.aigc.api.llm.catholic.response.stream.StreamPieceCollector;
import io.github.sinri.keel.aigc.api.internal.llm.catholic.tool.CommonFunctionToolCall;
import io.vertx.core.json.JsonObject;

public class FunctionToolCallStreamPieceCollector implements StreamPieceCollector<FunctionToolCall, FunctionToolCall> {
    private final StringBuilder nameBuffer = new StringBuilder();
    private final StringBuilder argumentsBuffer = new StringBuilder();

    public void accept(FunctionToolCall functionToolCall) {
        String name = functionToolCall.getName();
        if (name != null) {
            nameBuffer.append(name);
        }
        String arguments = functionToolCall.getArguments();
        if (arguments != null) {
            argumentsBuffer.append(arguments);
        }
    }

    @Override
    public FunctionToolCall build() {
        return new CommonFunctionToolCall(new JsonObject()
                .put("name", nameBuffer.toString())
                .put("arguments", argumentsBuffer.toString())
        );
    }
}
