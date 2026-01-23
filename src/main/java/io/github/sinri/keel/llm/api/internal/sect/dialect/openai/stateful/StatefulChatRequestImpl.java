package io.github.sinri.keel.llm.api.internal.sect.dialect.openai.stateful;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.llm.api.sect.dialect.openai.stateful.StatefulChatRequest;
import io.vertx.core.json.JsonObject;


/**
 * @since 2.0.1
 */
public class StatefulChatRequestImpl extends JsonifiableDataUnitImpl implements StatefulChatRequest {
    public StatefulChatRequestImpl() {
        super();
    }

    public StatefulChatRequestImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

}
