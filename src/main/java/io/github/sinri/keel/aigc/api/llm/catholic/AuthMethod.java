package io.github.sinri.keel.aigc.api.llm.catholic;

/**
 * LLM API 认证方式。
 */
public enum AuthMethod {
    Bearer, // OpenAI Classic Style
    ApiKey, // Azure Style
}