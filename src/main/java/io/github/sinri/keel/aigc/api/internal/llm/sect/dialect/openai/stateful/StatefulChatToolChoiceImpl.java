package io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.openai.stateful;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.stateful.StatefulChatToolChoice;
import io.vertx.core.json.JsonObject;


/**
 * @since 5.0.0
 */
public class StatefulChatToolChoiceImpl extends UnmodifiableJsonifiableEntityImpl implements StatefulChatToolChoice {
    public StatefulChatToolChoiceImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
