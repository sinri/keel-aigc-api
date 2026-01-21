package io.github.sinri.keel.llm.api.catholic.response;


import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.github.sinri.keel.llm.api.catholic.message.MixChatMessage;
import io.github.sinri.keel.llm.api.catholic.tool.call.ToolCall;
import io.github.sinri.keel.llm.api.internal.catholic.response.MixChatResponseImpl;
import io.github.sinri.keel.llm.api.internal.catholic.tool.CommonToolCall;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * MixChatResponse 是一个统一的聊天响应接口，
 * 用于封装来自不同大模型（如 OpenAI GPT、Qwen、Doubao 等）的响应，
 * 并将其标准化为 MixChatMessage 结构，便于上层业务统一处理。
 * <p>
 * 该接口提供了多种静态工厂方法用于将不同厂商的响应对象转换为 MixChatResponse，
 * 并对工具调用（ToolCall）等内容进行适配和处理。
 * <p>
 * 典型用法：
 * <pre>
 *     MixChatResponse response = MixChatResponse.from(gptResponse);
 *     MixChatMessage message = response.getMessage();
 * </pre>
 */
public interface MixChatResponse extends UnmodifiableJsonifiableEntity {

    static MixChatResponse create() {
        return new MixChatResponseImpl();
    }

    /**
     * 将 JsonObject 包装为 MixChatResponse 实例。
     *
     * @param jsonObject 原始 JSON 对象
     * @return MixChatResponse 实例
     */
    static MixChatResponse wrap(JsonObject jsonObject) {
        return new MixChatResponseImpl(jsonObject);
    }

    /**
     * 处理工具调用（ToolCall）列表，并设置到 MixChatMessage。
     *
     * @param toolCalls      工具调用列表
     * @param mixChatMessage 目标 MixChatMessage
     * @param <T>            ToolCall 子类型
     */
    static <T extends ToolCall> void handleToolCalls(List<T> toolCalls, MixChatMessage mixChatMessage) {
        if (!toolCalls.isEmpty()) {
            List<ToolCall> list = new ArrayList<>();

            toolCalls.forEach(tc -> {
                CommonToolCall commonToolCall = new CommonToolCall(tc.getId(), tc.getIndex(), tc.getFunction());
                list.add(commonToolCall);
            });

            mixChatMessage.setToolCalls(list);
        }
    }

    /**
     * 根据 MixChatMessage 构建 MixChatResponse。
     *
     * @param message 标准化的 MixChatMessage
     * @return MixChatResponse 实例
     */
    static MixChatResponse build(MixChatMessage message) {
        return new MixChatResponseImpl(new JsonObject()
                .put(MixChatResponseImpl.KEY_MESSAGE, message.toJsonObject())
        );
    }

    /**
     * 获取标准化后的 MixChatMessage。
     *
     * @return MixChatMessage 实例
     */
    MixChatMessage getMessage();

    MixChatResponse setMessage(MixChatMessage message);
}
