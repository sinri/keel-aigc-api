package io.github.sinri.keel.llm.api.catholic.request;

import io.github.sinri.keel.base.json.JsonifiableDataUnit;
import io.github.sinri.keel.llm.api.catholic.LargeLanguageModel;
import io.github.sinri.keel.llm.api.catholic.message.MixChatMessage;
import io.github.sinri.keel.llm.api.catholic.tool.definition.ToolDefinition;
import io.github.sinri.keel.llm.api.internal.catholic.request.MixChatRequestImpl;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * MixChatRequest 接口定义了混合聊天请求的结构和操作方法，
 * 支持多种大模型（如 OpenAI GPT、Qwen、Doubao）请求的统一封装与转换。
 * 该接口允许设置模型、请求ID、超时时间、流式响应、消息列表和工具列表等参数，
 * 并可根据当前配置生成对应平台的请求对象。
 */
public interface MixChatRequest extends JsonifiableDataUnit {

    static MixChatRequest create() {
        return new MixChatRequestImpl();
    }

    /**
     * 通过 JsonObject 包装生成 MixChatRequest 实例。
     *
     * @param jsonObject 包含请求参数的 JsonObject
     * @return 包装后的 MixChatRequest 实例
     */
    static MixChatRequest wrap(JsonObject jsonObject) {
        return new MixChatRequestImpl(jsonObject);
    }

    /**
     * 定义本请求应运行在哪一个模型服务之上。
     *
     * @return 模型服务规格的 code
     * @see LargeLanguageModel#getCode()
     */
    String getModel();

    /**
     * 定义本请求应运行在哪一个模型服务之上。
     *
     * @param largeLanguageModel 模型服务规格
     * @return 当前 MixChatRequest 实例
     */
    default MixChatRequest setModel(LargeLanguageModel largeLanguageModel) {
        return setModel(largeLanguageModel.getCode());
    }

    MixChatRequest setModel(String largeLanguageModelCode);

    /**
     * 获取请求唯一标识 request_id。
     *
     * @return 请求 ID 字符串
     */
    String getRequestId();

    /**
     * 设置请求唯一标识 request_id。
     *
     * @param requestId 请求 ID 字符串
     * @return 当前 MixChatRequest 实例
     */
    MixChatRequest setRequestId(String requestId);

    /**
     * 获取请求超时时间（毫秒），默认 180_000 ms。
     *
     * @return 超时时间（毫秒）
     */
    long getTimeout();

    /**
     * 设置请求超时时间（毫秒）。
     *
     * @param timeout 超时时间（毫秒）
     * @return 当前 MixChatRequest 实例
     */
    MixChatRequest setTimeout(long timeout);

    boolean isStream();

    /**
     * 设置是否为流式响应。
     *
     * @param stream 是否流式
     * @return 当前 MixChatRequest 实例
     */
    MixChatRequest setStream(boolean stream);

    /**
     * 添加一条聊天消息到消息列表。
     *
     * @param message 聊天消息
     * @return 当前 MixChatRequest 实例
     */
    MixChatRequest addMessage(MixChatMessage message);

    default MixChatRequest addMessage(Handler<MixChatMessage> messageHandler) {
        MixChatMessage mixChatMessage = MixChatMessage.create();
        messageHandler.handle(mixChatMessage);
        return addMessage(mixChatMessage);
    }

    /**
     * 获取所有聊天消息列表。
     *
     * @return 聊天消息列表
     */
    List<MixChatMessage> getMessages();

    /**
     * 添加一个工具定义到工具列表。
     *
     * @param toolDefinition 工具定义
     * @return 当前 MixChatRequest 实例
     */
    MixChatRequest addTool(ToolDefinition toolDefinition);

    /**
     * 获取所有工具定义列表。
     *
     * @return 工具定义列表
     */
    List<ToolDefinition> getTools();

    MixChatRequestExtra getExtra();

    MixChatRequest setExtra(MixChatRequestExtra extra);

}
