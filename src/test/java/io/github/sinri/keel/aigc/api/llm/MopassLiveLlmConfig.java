package io.github.sinri.keel.aigc.api.llm;

import io.github.sinri.keel.aigc.api.llm.catholic.AuthMethod;
import io.github.sinri.keel.base.configuration.ConfigElement;

import java.util.Objects;

/**
 * 实际调用 LLM 的协议测试统一读取 {@code llm.mopass} 网关配置。
 * <p>
 * 必填：{@code llm.mopass.endpoint}、{@code llm.mopass.apiKey}、{@code llm.mopass.model}。
 * <p>
 * 可选：{@code llm.mopass.authMethod}（默认 Bearer）；各协议可用
 * {@code llm.mopass.openai.model}、{@code llm.mopass.anthropic.model}、
 * {@code llm.mopass.dashscope.text.model}、{@code llm.mopass.dashscope.multimodal.model}
 * 覆盖默认模型名。
 */
public final class MopassLiveLlmConfig {

    private MopassLiveLlmConfig() {
    }

    public static String endpoint() {
        return required("llm.mopass.endpoint");
    }

    public static String apiKey() {
        return required("llm.mopass.apiKey");
    }

    public static String model() {
        return required("llm.mopass.model");
    }

    public static String openaiModel() {
        return firstNonBlank("llm.mopass.openai.model", model());
    }

    public static String anthropicModel() {
        return firstNonBlank("llm.mopass.anthropic.model", model());
    }

    public static String dashscopeTextModel() {
        return firstNonBlank("llm.mopass.dashscope.text.model", model());
    }

    public static String dashscopeMultimodalModel() {
        return firstNonBlank("llm.mopass.dashscope.multimodal.model", model());
    }

    public static AuthMethod authMethod() {
        String raw = ConfigElement.root().readProperty("llm.mopass.authMethod");
        if (raw == null || raw.isBlank()) {
            return AuthMethod.Bearer;
        }
        return AuthMethod.valueOf(raw);
    }

    private static String required(String key) {
        return Objects.requireNonNull(ConfigElement.root().readProperty(key), key + " must be set");
    }

    private static String firstNonBlank(String overrideKey, String fallback) {
        String override = ConfigElement.root().readProperty(overrideKey);
        if (override == null || override.isBlank()) {
            return fallback;
        }
        return override;
    }
}
