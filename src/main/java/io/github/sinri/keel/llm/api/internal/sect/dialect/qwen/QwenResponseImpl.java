package io.github.sinri.keel.llm.api.internal.sect.dialect.qwen;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.llm.api.sect.dialect.qwen.response.sync.QwenResponse;
import io.vertx.core.json.JsonObject;

/**
 * @since 2.0.0
 */
public class QwenResponseImpl extends UnmodifiableJsonifiableEntityImpl implements QwenResponse {
    public QwenResponseImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
