package io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.openai.stateful;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.stateful.content.StatefulChatItemContent;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.stateful.content.StatefulChatItemContentOutputText;
import io.vertx.core.json.JsonObject;


/**
 * @since 2.0.1
 */
public class StatefulChatItemContentImpl extends UnmodifiableJsonifiableEntityImpl
        implements StatefulChatItemContent, StatefulChatItemContentOutputText {
    public StatefulChatItemContentImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
