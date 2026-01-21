package io.github.sinri.keel.llm.api.catholic.tool.call;

import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

public interface ToolCall {

    /**
     * 工具类型，固定为{@code function}。
     */
    String getType();

    /**
     * 本次工具响应的ID。
     */
    String getId();

    /**
     * 当前工具调用对象在回复报文里工具调用数组中的索引。
     */
    Integer getIndex();

    /**
     * 以函数调用的形式发起的工具调用，所调用工具的名称，以及输入参数。
     * 当{@link ToolCall#getType()}返回非{@code function}时，本方法返回{@code null}，但这目前应该不可能发生。
     */
    @Nullable FunctionToolCall getFunction();

    JsonObject toJsonObject();
}
