package io.github.sinri.keel.llm.api.sect.dashscope.qwen.request.parameters;

import io.github.sinri.keel.base.annotations.SelfInterface;
import io.github.sinri.keel.base.json.JsonifiableDataUnit;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

/**
 * @since 2.0.0
 */
interface QwenRequestParametersCore<E> extends JsonifiableDataUnit, SelfInterface<E> {
    /**
     * 是否流式输出回复。
     * 通过HTTP实现流式输出请在Header中指定X-DashScope-SSE为enable。
     * Qwen3商业版（思考模式）、Qwen3开源版、QwQ、QVQ只支持流式输出。
     *
     * @param stream 是否流式输出。
     *               {@code false}：模型生成完所有内容后一次性返回结果。
     *               {@code true}：边生成边输出，即每生成一部分内容就立即输出一个片段（chunk），您需要实时地逐个读取这些片段以获得完整的结果。
     * @return this
     */
    default E stream(boolean stream) {
        ensureEntry("stream", stream);
        return getImplementation();
    }

    default @Nullable Boolean stream() {
        return this.readBoolean("stream");
    }

    /**
     * 默认为false（Qwen3 开源版、QwQ 、QVQ模型默认值为 true）
     * 在流式输出模式下是否开启增量输出。
     * QwQ 模型与思考模式下的 Qwen3 模型只支持设置为 true。由于 Qwen3 商业版模型默认值为false，您需要在思考模式下手动设置为 true。
     * Qwen3 开源版模型不支持设置为 false。
     */
    default E incrementalOutput(boolean incremental_output) {
        ensureEntry("incremental_output", incremental_output);
        return getImplementation();
    }

    default @Nullable Boolean incrementalOutput() {
        return readBoolean("incremental_output");
    }

    /**
     * 采样温度，控制模型生成文本的多样性。
     * temperature越高，生成的文本更多样，反之，生成的文本更确定。
     * 取值范围： [0, 2)
     *
     * @param temperature 取值范围： [0, 2)
     */
    default E temperature(float temperature) {
        ensureEntry("temperature", temperature);
        return this.getImplementation();
    }

    default @Nullable Float temperature() {
        return this.readFloat("temperature");
    }

    /**
     * 核采样的概率阈值，控制模型生成文本的多样性。
     * top_p越高，生成的文本更多样。反之，生成的文本更确定。
     * 取值范围：（0,1.0]。
     * 不建议修改QVQ模型的默认 top_p 值。
     *
     * @param topP 取值范围：（0,1.0]
     */
    default E topP(float topP) {
        ensureEntry("top_p", topP);
        return this.getImplementation();
    }

    /**
     * 获取 top_p 参数。
     */
    default @Nullable Float topP() {
        return this.readFloat("top_p");
    }

    /**
     * 生成过程中采样候选集的大小。例如，取值为50时，仅将单次生成中得分最高的50个Token组成随机采样的候选集。
     * 取值越大，生成的随机性越高；取值越小，生成的确定性越高。取值为None或当top_k大于100时，表示不启用top_k策略，此时仅有top_p策略生效。
     * 取值需要大于或等于0。
     * 不建议修改QVQ模型的默认 top_k 值。
     *
     * @param topK 取值需要大于或等于0。
     */
    default E topK(int topK) {
        ensureEntry("top_k", topK);
        return this.getImplementation();
    }

    /**
     * 获取 top_k 参数。
     */
    default @Nullable Integer topK() {
        return this.readInteger("top_k");
    }

    /**
     * 控制模型生成文本时的内容重复度。
     * 取值范围：[-2.0, 2.0]。正数会减少重复度，负数会增加重复度。
     * 适用场景：
     * 较高的presence_penalty适用于要求多样性、趣味性或创造性的场景，如创意写作或头脑风暴。
     * 较低的presence_penalty适用于要求一致性或专业术语的场景，如技术文档或其他正式文档。
     * 不建议修改QVQ模型的默认presence_penalty值。
     *
     * @param presencePenalty 取值范围：[-2.0, 2.0]。正数会减少重复度，负数会增加重复度。
     */
    default E presencePenalty(float presencePenalty) {
        ensureEntry("presence_penalty", presencePenalty);
        return this.getImplementation();
    }

    /**
     * 获取 presence_penalty 参数。
     */
    default @Nullable Float presencePenalty() {
        return this.readFloat("presence_penalty");
    }

    /**
     * 模型生成时连续序列中的重复度。提高repetition_penalty时可以降低模型生成的重复度，1.0表示不做惩罚。没有严格的取值范围，只要大于0即可。
     * 不建议修改QVQ模型的默认 repetition_penalty 值。
     */
    default E repetitionPenalty(float repetition_penalty) {
        ensureEntry("repetition_penalty", repetition_penalty);
        return this.getImplementation();
    }

    /**
     * 获取 repetition_penalty 参数。
     */
    default @Nullable Float repetitionPenalty() {
        return this.readFloat("repetition_penalty");
    }

    /**
     * 本次请求返回的最大 Token 数。
     * max_tokens 的设置不会影响大模型的生成过程，如果模型生成的 Token 数超过max_tokens，本次请求会返回截断后的内容。
     * 默认值和最大值都是模型的最大输出长度。关于各模型的最大输出长度，请参见模型列表。
     */
    default E maxTokens(int maxTokens) {
        ensureEntry("max_tokens", maxTokens);
        return this.getImplementation();
    }

    /**
     * 获取 max_tokens 参数。
     */
    default @Nullable Integer maxTokens() {
        return this.readInteger("max_tokens");
    }

    /**
     * 返回内容的格式。
     */
    default E responseFormatTypeAsText() {
        return responseFormat(new JsonObject().put("type", "text"));
    }

    /**
     * 返回内容的格式。
     * 输出标准格式的JSON字符串。
     * 您需要在System Message或User Message中指引模型输出JSON格式，如："请按照json格式输出。"
     *
     * @see <a href="https://help.aliyun.com/zh/model-studio/json-mode">结构化输出</a>
     */
    default E responseFormatTypeAsJsonObject() {
        return responseFormat(new JsonObject().put("type", "json_object"));
    }

    /**
     * 返回内容的格式。
     *
     * @see <a href="https://help.aliyun.com/zh/model-studio/json-mode">结构化输出</a>
     */
    default E responseFormat(JsonObject responseFormat) {
        ensureEntry("response_format", responseFormat);
        return this.getImplementation();
    }

    /**
     * 返回内容的格式。
     */
    default @Nullable JsonObject responseFormat() {
        return this.readJsonObject("response_format");
    }

    /**
     * 对于需要生成多个响应的场景（如创意写作、广告文案等），可以设置较大的 n 值。
     * 当前仅支持 qwen-plus 与 Qwen3（非思考模式） 模型，且在传入 tools 参数时固定为1。
     * 设置较大的 n 值不会增加输入 Token 消耗，会增加输出 Token 的消耗。
     *
     * @param n 生成响应的个数，取值范围是1-4。
     */
    default E n(int n) {
        ensureEntry("n", n);
        return this.getImplementation();
    }

    /**
     * 获取 n 参数。
     */
    default @Nullable Integer n() {
        return this.readInteger("n");
    }

    /**
     * 设置seed参数会使文本生成过程更具有确定性，通常用于使模型每次运行的结果一致。
     * 在每次模型调用时传入相同的seed值（由您指定），并保持其他参数不变，模型将尽可能返回相同的结果。
     * 取值范围：0到2^31−1。
     *
     * @param seed 随机种子值，取值范围：0到2^31−1。
     */
    default E seed(int seed) {
        ensureEntry("seed", seed);
        return getImplementation();
    }

    /**
     * 获取 seed 参数。
     */
    default @Nullable Integer seed() {
        return this.readInteger("seed");
    }

    /**
     * 使用stop参数后，当模型生成的文本即将包含指定的字符串或token_id时，将自动停止生成。
     * 您可以在stop参数中传入敏感词来控制模型的输出。
     */
    default E stop(String stopWord) {
        ensureEntry("stop", stopWord);
        return getImplementation();
    }

    /**
     * 使用stop参数后，当模型生成的文本即将包含指定的字符串或token_id时，将自动停止生成。
     * 您可以在stop参数中传入敏感词来控制模型的输出。
     * <p>
     * stop为array类型时，不可以将token_id和字符串同时作为元素输入，比如不可以指定stop为["你好",104307]。
     * </p>
     */
    default E stop(JsonArray stop) {
       ensureEntry("stop", stop);
        return getImplementation();
    }

    /**
     * 获取 stop 参数（字符串类型）。
     */
    default @Nullable String stopString() {
        return this.readString("stop");
    }

    /**
     * 获取 stop 参数（数组类型）。
     */
    default @Nullable JsonArray stopArray() {
        return this.readJsonArray("stop");
    }

    /**
     * 返回数据的格式。
     * 默认为"text"（QwQ 模型、Qwen3 开源模型与 Qwen-Long 模型默认值为 "message"）
     * 推荐您优先设置为"message"，可以更方便地进行多轮对话。
     * 平台后续将统一将默认值调整为"message"。
     * 模型为通义千问VL/QVQ/OCR/Audio/ASR时，设置"text"不生效。
     * 思考模式下的 Qwen3 模型只能设置为"message"，由于 Qwen3 商业版模型默认值为"text"，您需要将其设置为"message"。
     */
    default E resultFormat(String result_format) {
        ensureEntry("result_format", result_format);
        return this.getImplementation();
    }

    /**
     * 获取 result_format 参数。
     */
    default @Nullable String resultFormat() {
        return this.readString("result_format");
    }
}
