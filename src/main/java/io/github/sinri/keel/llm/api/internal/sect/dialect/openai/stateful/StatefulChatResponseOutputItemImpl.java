package io.github.sinri.keel.llm.api.internal.sect.dialect.openai.stateful;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.llm.api.sect.dialect.openai.stateful.StatefulChatResponseOutputItem;
import io.vertx.core.json.JsonObject;


/**
 * @since 2.0.1
 */
public class StatefulChatResponseOutputItemImpl extends UnmodifiableJsonifiableEntityImpl implements StatefulChatResponseOutputItem {
    public StatefulChatResponseOutputItemImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
