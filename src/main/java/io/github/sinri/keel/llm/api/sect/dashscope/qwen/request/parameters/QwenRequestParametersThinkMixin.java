package io.github.sinri.keel.llm.api.sect.dashscope.qwen.request.parameters;

import org.jspecify.annotations.Nullable;

/**
 * @since 2.0.0
 */
interface QwenRequestParametersThinkMixin<E> extends QwenRequestParametersCore<E> {
    /**
     * 是否开启思考模式，适用于 Qwen3 模型。
     * Qwen3 商业版模型默认值为 False，Qwen3 开源版模型默认值为 True。
     */
    default E enableThinking(boolean enableThinking) {
        ensureEntry("enable_thinking", enableThinking);
        return getImplementation();
    }

    default @Nullable Boolean enableThinking() {
        return readBoolean("enable_thinking");
    }

    /**
     * 思考过程的最大长度，只在enable_thinking为true时生效。
     * 适用于 Qwen3 的商业版与开源版模型。
     *
     * @param thinkingBudget 思考过程的最大长度
     * @see <a
     *         href=
     *         "https://help.aliyun.com/zh/model-studio/deep-thinking?spm=a2c4g.11186623.0.0.714a47bbLaiIAQ#e7c0002fe4meu">限制思考长度</a>
     */
    default E thinkingBudget(int thinkingBudget) {
        ensureEntry("thinking_budget", thinkingBudget);
        return getImplementation();
    }

    default @Nullable Integer thinkingBudget() {
        return readInteger("thinking_budget");
    }
}
