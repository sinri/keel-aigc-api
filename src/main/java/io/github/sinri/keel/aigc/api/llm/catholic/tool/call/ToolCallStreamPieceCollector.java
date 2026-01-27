package io.github.sinri.keel.aigc.api.llm.catholic.tool.call;

import io.github.sinri.keel.core.utils.StringUtils;
import io.github.sinri.keel.aigc.api.llm.catholic.response.stream.StreamPieceCollector;
import io.github.sinri.keel.aigc.api.internal.llm.catholic.tool.CommonToolCall;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

/**
 * 工具调用流式数据碎片聚合器。
 * <p>
 * 用于收集和聚合流式响应中的工具调用数据碎片，最终构建完整的 ToolCall 对象。
 *
 * @since 5.0.0
 */
public class ToolCallStreamPieceCollector implements StreamPieceCollector<ToolCall, ToolCall> {
    private final FunctionToolCallStreamPieceCollector functionToolCallBuffer = new FunctionToolCallStreamPieceCollector();
    private @Nullable String toolCallId;
    private @Nullable Integer index;
    private @Nullable String type;


    public void accept(ToolCall toolCall) {
        String id = toolCall.getId();
        if (!StringUtils.isNullOrBlank(id)) {
            toolCallId = id;
        }
        index = toolCall.getIndex();
        type = toolCall.getType();
        FunctionToolCall function = toolCall.getFunction();
        if (function != null) {
            functionToolCallBuffer.accept(function);
        }
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
