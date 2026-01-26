package io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.openai.stateful;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.stateful.StatefulChatIncompleteDetails;
import io.vertx.core.json.JsonObject;


/**
 * @since 2.0.1
 */
public class StatefulChatIncompleteDetailsImpl extends UnmodifiableJsonifiableEntityImpl implements StatefulChatIncompleteDetails {
    public StatefulChatIncompleteDetailsImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
