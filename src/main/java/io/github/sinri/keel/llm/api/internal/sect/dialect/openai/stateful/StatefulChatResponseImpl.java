package io.github.sinri.keel.llm.api.internal.sect.dialect.openai.stateful;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.llm.api.sect.dialect.openai.stateful.StatefulChatResponse;
import io.vertx.core.json.JsonObject;


/**
 * @since 2.0.1
 */
public class StatefulChatResponseImpl extends UnmodifiableJsonifiableEntityImpl implements StatefulChatResponse {
    public StatefulChatResponseImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
