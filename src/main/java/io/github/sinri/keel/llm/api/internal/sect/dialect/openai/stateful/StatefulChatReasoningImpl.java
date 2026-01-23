package io.github.sinri.keel.llm.api.internal.sect.dialect.openai.stateful;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.llm.api.sect.dialect.openai.stateful.StatefulChatReasoning;
import io.vertx.core.json.JsonObject;


/**
 * @since 2.0.1
 */
public class StatefulChatReasoningImpl extends UnmodifiableJsonifiableEntityImpl implements StatefulChatReasoning {
    public StatefulChatReasoningImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
