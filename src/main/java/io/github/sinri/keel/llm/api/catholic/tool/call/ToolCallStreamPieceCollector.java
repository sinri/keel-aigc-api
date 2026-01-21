package io.github.sinri.keel.llm.api.catholic.tool.call;

import io.github.sinri.keel.core.utils.StringUtils;
import io.github.sinri.keel.llm.api.catholic.response.stream.StreamPieceCollector;
import io.github.sinri.keel.llm.api.internal.catholic.tool.CommonToolCall;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

public class ToolCallStreamPieceCollector implements StreamPieceCollector<ToolCall, ToolCall> {
    private final FunctionToolCallStreamPieceCollector functionToolCallBuffer = new FunctionToolCallStreamPieceCollector();
    ;
    private String toolCallId;
    private Integer index;
    private String type;


    public void accept(ToolCall toolCall) {
        String id = toolCall.getId();
        if (!StringUtils.isNullOrBlank(id)) {
            toolCallId = id;
        }
        index = toolCall.getIndex();
        type = toolCall.getType();
        FunctionToolCall function = toolCall.getFunction();
        Objects.requireNonNull(function);
        functionToolCallBuffer.accept(function);
    }

    @Override
    public ToolCall build() {
        return new CommonToolCall(new JsonObject()
                .put("id", toolCallId)
                .put("index", index)
                .put("type", type)
                .put("function", functionToolCallBuffer.build().toJsonObject())
        );
    }
}
