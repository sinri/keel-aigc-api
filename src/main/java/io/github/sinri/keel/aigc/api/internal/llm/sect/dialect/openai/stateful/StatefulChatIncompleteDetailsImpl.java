package io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.openai.stateful;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.stateful.StatefulChatIncompleteDetails;
import io.vertx.core.json.JsonObject;


/**
 * @since 5.0.0
 */
public class StatefulChatIncompleteDetailsImpl extends UnmodifiableJsonifiableEntityImpl implements StatefulChatIncompleteDetails {
    public StatefulChatIncompleteDetailsImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
