package io.github.sinri.keel.llm.api.internal.sect.dashscope;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.llm.api.sect.dashscope.qwen.response.sync.QwenResponseOutput;
import io.vertx.core.json.JsonObject;


/**
 * @since 2.0.0
 */
public class QwenResponseOutputImpl extends UnmodifiableJsonifiableEntityImpl implements QwenResponseOutput {
    public QwenResponseOutputImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
