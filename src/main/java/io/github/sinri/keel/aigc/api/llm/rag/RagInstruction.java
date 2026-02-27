package io.github.sinri.keel.aigc.api.llm.rag;

import io.github.sinri.keel.base.json.JsonObjectConvertible;
import io.github.sinri.keel.base.json.JsonObjectReloadable;

/**
 * 对 RAG 机制的运作指示，包含必要的配置。
 * @since 5.0.0
 */
public interface RagInstruction extends JsonObjectConvertible, JsonObjectReloadable {
    int getMaximumRagReferenceCount();
}
