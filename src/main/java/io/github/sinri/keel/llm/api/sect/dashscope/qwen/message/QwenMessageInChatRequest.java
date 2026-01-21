package io.github.sinri.keel.llm.api.sect.dashscope.qwen.message;

import io.github.sinri.keel.llm.api.internal.sect.dashscope.QwenMessageImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

/**
 * @since 2.0.0
 */
public interface QwenMessageInChatRequest extends QwenMessage {
    static QwenMessageInChatRequest create() {
        return new QwenMessageImpl();
    }

    static QwenMessageInChatRequest wrap(JsonObject jsonObject) {
        return new QwenMessageImpl(jsonObject);
    }

    /**
     * 模型的目标或角色。
     * 如果设置系统消息，请放在messages列表的第一位。
     * QwQ 模型不建议设置 System Message，QVQ 模型设置System Message不会生效。
     */
    static QwenMessage createAsSystemInRequest(String content) {
        var x = create();
        x.ensureEntry("content", content);
        x.ensureEntry("role", "system");
        return x;
    }

    /**
     * 用户发送给模型的消息。
     * 如果您的输入只有文本，则为string类型；如果您的输入包含图像等多模态数据，则为array类型。
     *
     * @param content 用户消息的内容。
     */
    static QwenMessage createAsUserInRequest(String content) {
        var x= create();
        x.ensureEntry("content", content);
        x.ensureEntry("role", "user");
        return x;
    }

    /**
     * 模型对用户消息的回复。
     *
     * @param content 助手消息的内容。
     * @param partial 是否开启 Partial Mode。
     * @see <a href="https://help.aliyun.com/zh/model-studio/partial-mode">前缀续写</a> Partial Mode的使用方法请参考前缀续写。
     */
    static QwenMessage createAsAssistantInRequest(String content, @Nullable Boolean partial) {
        var x = create();
        x.ensureEntry("content", content);
        x.ensureEntry("role", "assistant");
        if (partial != null) {
            x.ensureEntry("partial", partial);
        }
        return x;
    }

    /**
     * 模型对用户消息的回复。
     *
     * @param content 助手消息的内容。
     */
    static QwenMessage createAsAssistantInRequest(String content) {
        return createAsAssistantInRequest(content, null);
    }

    /**
     * 工具的输出信息。
     *
     * @param content      工具消息的内容，一般为工具函数的输出。
     * @param tool_call_id 发起 Function Calling 后返回的
     *                     id，可以通过{@code response.output.choices[0].message.tool_calls[0]["id"]}获取，用于标记 Tool Message
     *                     对应的工具。
     */
    static QwenMessage createAsToolOutputInRequest(String content, String tool_call_id) {
        var x= create();
        x.ensureEntry("content", content);
        x.ensureEntry("tool_call_id", tool_call_id); // 非流式输出用这个
        x.ensureEntry("id", tool_call_id); // 流式输出用这个
        x.ensureEntry("role", "tool");
        return x;
    }
}
