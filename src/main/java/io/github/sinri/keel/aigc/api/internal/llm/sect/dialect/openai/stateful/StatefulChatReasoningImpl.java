package io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.openai.stateful;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.stateful.StatefulChatReasoning;
import io.vertx.core.json.JsonObject;


/**
 * @since 5.0.0
 */
public class StatefulChatReasoningImpl extends UnmodifiableJsonifiableEntityImpl implements StatefulChatReasoning {
    public StatefulChatReasoningImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
