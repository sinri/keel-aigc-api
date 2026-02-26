package io.github.sinri.keel.aigc.api.llm.catholic.tool.call;

import io.vertx.core.json.JsonObject;

/**
 * 工具调用接口。
 * <p>
 * 表示 LLM 对工具的调用，包含工具类型、ID、索引和函数调用信息。
 *
 * @since 5.0.0
 */
public interface ToolCall {

    /**
     * 获取工具类型，固定为{@code function}。
     *
     * @return 工具类型
     */
    String getType();

    /**
     * 获取本次工具响应的ID。
     *
     * @return 工具响应 ID
     */
    String getId();

    /**
     * 获取当前工具调用对象在回复报文里工具调用数组中的索引。
     *
     * @return 索引值
     */
    Integer getIndex();

    /**
     * 以函数调用的形式发起的工具调用，所调用工具的名称，以及输入参数。
     * 当{@link ToolCall#getType()}返回非{@code function}时，本方法返回{@code null}，但这目前应该不可能发生。
     *
     * @return 函数工具调用实例，如果类型不是 function 则返回 null
     * @throws NullPointerException 如果工具类型不是 function
     */
    FunctionToolCall getFunction();

    /**
     * 转换为 JSON 对象。
     *
     * @return JSON 对象
     */
    JsonObject toJsonObject();
}
