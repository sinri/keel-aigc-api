package io.github.sinri.keel.llm.api.sect.dialect.doubao.request;

import io.github.sinri.keel.base.json.JsonifiableDataUnit;
import io.github.sinri.keel.llm.api.catholic.tool.call.ToolCall;
import io.github.sinri.keel.llm.api.catholic.tool.definition.ToolDefinition;
import io.github.sinri.keel.llm.api.internal.catholic.tool.CommonToolDefinition;
import io.github.sinri.keel.llm.api.internal.sect.dialect.doubao.DoubaoRequestImpl;
import io.github.sinri.keel.llm.api.sect.dialect.doubao.message.DoubaoMessage;
import io.github.sinri.keel.llm.api.sect.dialect.doubao.message.DoubaoMessageInChatRequest;
import io.github.sinri.keel.llm.api.sect.dialect.doubao.message.DoubaoMessageInVisionRequest;
import io.github.sinri.keel.llm.api.sect.dialect.doubao.message.vision.DoubaoVisionContent;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface DoubaoRequest extends JsonifiableDataUnit {
    static DoubaoRequest create() {
        return new DoubaoRequestImpl();
    }

    static DoubaoRequest wrap(JsonObject jsonObject) {
        return new DoubaoRequestImpl(jsonObject);
    }

    /**
     * @param model 指定需要调用的模型，以模型ID或自定义推理接入点ID
     * @see <a href="https://www.volcengine.com/docs/82379/1330310">模型列表</a>
     * @see <a href="https://www.volcengine.com/docs/82379/1099522">获取 Endpoint
     *         ID（创建自定义推理接入点）</a>
     */
    default DoubaoRequest model(String model) {
        ensureEntry("model", model);
        return this;
    }

    /**
     * 指定需要调用的模型（以模型ID或自定义推理接入点ID形式）。
     */
    default @Nullable String model() {
        return readString("model");
    }

    default DoubaoRequest addMessage(DoubaoMessage message) {
        this.ensureJsonArray("messages")
            .add(message.toJsonObject());
        return this;
    }

    default DoubaoRequest addSystemChatMessage(String content) {
        return addMessage(DoubaoMessageInChatRequest.createAsSystemMessage(content));
    }

    default DoubaoRequest addUserChatMessage(String content) {
        return addMessage(DoubaoMessageInChatRequest.createAsUserMessage(content));
    }

    default DoubaoRequest addUserVisionMessage(List<DoubaoVisionContent> contentList) {
        return addMessage(DoubaoMessageInVisionRequest.createAsUserMessage(contentList));
    }

    default DoubaoRequest addAssistantChatMessage(String content) {
        return addMessage(DoubaoMessageInChatRequest.createAsAssistantMessage(content));
    }

    default DoubaoRequest addToolCallChatMessage(@Nullable String content, List<ToolCall> toolCalls) {
        return addMessage(DoubaoMessageInChatRequest.createAsToolCallMessage(content, toolCalls));
    }

    default DoubaoRequest addToolOutputChatMessage(String content, String tool_call_id) {
        return addMessage(DoubaoMessageInChatRequest.createAsToolOutputMessage(content, tool_call_id));
    }

    /**
     * 不同模型支持不同类型的消息，如文本、图片、视频（需工单申请）等。
     *
     * @return 到目前为止的对话组成的消息列表。
     */
    default List<DoubaoMessageInChatRequest> messages() {
        List<JsonObject> l = readJsonObjectArray("messages");
        if (l == null)
            return List.of();
        return l.stream().map(DoubaoMessageInChatRequest::wrap).toList();
    }

    /**
     * 控制模型是否开启深度思考模式。默认开启深度思考模式，可以手动关闭。
     * 支持此字段的模型为
     * doubao-1.5-thinking-vision-pro-250428，doubao-1-5-thinking-pro-m-250428。
     */
    default DoubaoRequest thinking(ThinkingOptions thinkingOptions) {
        ensureEntry("thinking", thinkingOptions.toJsonObject());
        return this;
    }

    default ThinkingOptions thinking() {
        JsonObject x = readJsonObject("thinking");
        if (x == null) {
            x = new JsonObject();
        }
        return ThinkingOptions.wrap(x);
    }

    /**
     * 响应内容是否流式返回
     *
     * @param b {@code false}：模型生成完所有内容后一次性返回结果。 {@code true}：按 SSE
     *          协议逐块返回模型生成内容，并以一条 data: [DONE] 消息结束。
     */
    default DoubaoRequest stream(boolean b) {
        ensureEntry("stream", b);
        return this;
    }

    default @Nullable Boolean stream() {
        return this.readBoolean("stream");
    }

    /**
     * 流式响应的选项。当 stream 为 true 时可设置 stream_options 字段。
     */
    default DoubaoRequest streamOptions(StreamOptions streamOptions) {
        ensureEntry("stream_options", streamOptions.toJsonObject());
        return this;
    }

    default StreamOptions streamOptions() {
        var x = this.readJsonObject("stream_options");
        if (x == null) {
            x = new JsonObject();
        }
        return StreamOptions.wrap(x);
    }

    /**
     * 模型回复最大长度（单位 token），取值范围各个模型不同，详细见模型列表。
     * 输入 token 和输出 token 的总长度还受模型的上下文长度限制。
     *
     * @param max_tokens 默认值 4096
     * @see <a href="https://www.volcengine.com/docs/82379/1330310">模型列表</a>
     */
    default DoubaoRequest maxTokens(int max_tokens) {
        ensureEntry("max_tokens", max_tokens);
        return this;
    }

    default @Nullable Integer maxTokens() {
        return readInteger("max_tokens");
    }

    /**
     * 指定是否使用TPM保障包。生效对象为购买了保障包推理接入点。
     *
     * @param service_tier 默认值 auto。取值范围
     *                     {@code auto}：优先使用TPM保障包。
     *                     如果有TPM保障包额度的推理接入点，本次请求将会使用TPM保障包用量，获得更高限流以及响应速度。
     *                     否则不使用，使用默认的限流和普通的服务响应速度。
     *                     {@code default}：本次请求，不使用TPM保障包，使用默认的限流和普通的服务响应速度，即使请求的是有TPM保障包额度的推理接入点。
     */
    default DoubaoRequest serviceTier(String service_tier) {
        ensureEntry("service_tier", service_tier);
        return this;
    }

    default @Nullable String serviceTier() {
        return readString("service_tier");
    }

    /**
     * 模型遇到 stop 字段所指定的字符串时将停止继续生成，这个词语本身不会输出。
     */
    default DoubaoRequest stop(String stop) {
        ensureEntry("stop", stop);
        return this;
    }

    /**
     * 模型遇到 stop 字段所指定的字符串时将停止继续生成，这个词语本身不会输出。
     *
     * @param stop 最多支持 4 个字符串。
     */
    default DoubaoRequest stop(List<String> stop) {
        ensureEntry("stop", new JsonArray(stop));
        return this;
    }

    default List<String> stop() {
        String s = readString("stop");
        if (s == null) {
            JsonArray array = readJsonArray("stop");
            if (array == null) {
                return List.of();
            }
            List<String> l = new ArrayList<>();
            array.forEach(x -> l.add(x.toString()));
            return Collections.unmodifiableList(l);
        }
        return List.of(s);
    }

    /**
     * 模型输出内容须遵循此处指定的格式。
     */
    default DoubaoRequest responseFormat(ResponseFormatOptions responseFormatOptions) {
        ensureEntry("response_format", responseFormatOptions.toJsonObject());
        return this;
    }

    default ResponseFormatOptions responseFormat() {
        var x = readJsonObject("response_format");
        if (x == null) {
            x = new JsonObject();
        }
        return ResponseFormatOptions.wrap(x);
    }

    /**
     * 频率惩罚系数。如果值为正，会根据新 token 在文本中的出现频率对其进行惩罚，从而降低模型逐字重复的可能性。
     *
     * @param frequency_penalty 默认值 0，取值范围为 [-2.0, 2.0]
     */
    default DoubaoRequest frequencyPenalty(float frequency_penalty) {
        ensureEntry("frequency_penalty", frequency_penalty);
        return this;
    }

    default @Nullable Float frequencyPenalty() {
        return readFloat("frequency_penalty");
    }

    /**
     * 存在惩罚系数。如果值为正，会根据新 token 到目前为止是否出现在文本中对其进行惩罚，从而增加模型谈论新主题的可能性。
     *
     * @param presence_penalty 默认值 0，取值范围为 [-2.0, 2.0]
     */
    default DoubaoRequest presencePenalty(float presence_penalty) {
        ensureEntry("presence_penalty", presence_penalty);
        return this;
    }

    default @Nullable Float presencePenalty() {
        return readFloat("presence_penalty");
    }

    /**
     * 采样温度。控制生成文本时对每个候选词的概率分布进行平滑的程度。
     *
     * @param temperature 默认值 1，取值范围为 [0, 2]
     */
    default DoubaoRequest temperature(float temperature) {
        ensureEntry("temperature", temperature);
        return this;
    }

    default @Nullable Float temperature() {
        return readFloat("temperature");
    }

    /**
     * 核采样概率阈值。模型会考虑概率质量在 top_p 内的 token 结果。
     *
     * @param top_p 默认值 0.7，取值范围为 [0, 1]
     */
    default DoubaoRequest topP(float top_p) {
        ensureEntry("top_p", top_p);
        return this;
    }

    default @Nullable Float topP() {
        return readFloat("top_p");
    }

    /**
     * 是否返回输出 tokens 的对数概率。
     *
     * @param logprobs 默认值 false
     */
    default DoubaoRequest logprobs(boolean logprobs) {
        ensureEntry("logprobs", logprobs);
        return this;
    }

    default @Nullable Boolean logprobs() {
        return readBoolean("logprobs");
    }

    /**
     * 指定每个输出 token 位置最有可能返回的 token 数量，仅当 logprobs为true 时可设置。
     *
     * @param top_logprobs 默认值 0，取值范围为 [0, 20]
     */
    default DoubaoRequest topLogprobs(int top_logprobs) {
        ensureEntry("top_logprobs", top_logprobs);
        return this;
    }

    default @Nullable Integer topLogprobs() {
        return readInteger("top_logprobs");
    }

    /**
     * 模型可以调用的工具列表。
     * 目前仅函数作为工具被支持。用这个来提供模型可能为其生成 JSON 输入的函数列表。
     *
     * @see <a href=
     *         "https://www.volcengine.com/docs/82379/1262342#8c325d45">拥有Function
     *         Calling能力的模型列表</a>
     *         支持该字段的模型请参见文档。
     */
    default DoubaoRequest addTool(ToolDefinition toolDefinition) {
        ensureJsonArray("tools")
                .add(toolDefinition.toJsonObject());
        return this;
    }

    default List<ToolDefinition> tools() {
        List<JsonObject> tools = readJsonObjectArray("tools");
        if (tools == null)
            return List.of();
        return tools.stream().map(CommonToolDefinition::new)
                    .map(x -> (ToolDefinition) x)
                    .toList();
    }
}
