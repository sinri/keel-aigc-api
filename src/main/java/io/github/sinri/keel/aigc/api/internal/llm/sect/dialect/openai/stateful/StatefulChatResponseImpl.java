package io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.openai.stateful;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.stateful.StatefulChatResponse;
import io.vertx.core.json.JsonObject;


/**
 * @since 5.0.0
 */
public class StatefulChatResponseImpl extends UnmodifiableJsonifiableEntityImpl implements StatefulChatResponse {
    public StatefulChatResponseImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
