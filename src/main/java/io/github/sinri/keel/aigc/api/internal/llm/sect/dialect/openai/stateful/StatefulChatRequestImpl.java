package io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.openai.stateful;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.stateful.StatefulChatRequest;
import io.vertx.core.json.JsonObject;


/**
 * @since 5.0.0
 */
public class StatefulChatRequestImpl extends JsonifiableDataUnitImpl implements StatefulChatRequest {
    public StatefulChatRequestImpl() {
        super();
    }

    public StatefulChatRequestImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

}
