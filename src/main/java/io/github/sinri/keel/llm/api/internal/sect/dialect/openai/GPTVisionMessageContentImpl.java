package io.github.sinri.keel.llm.api.internal.sect.dialect.openai;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.llm.api.sect.dialect.openai.message.vision.GPTVisionMessageContent;
import io.vertx.core.json.JsonObject;

public class GPTVisionMessageContentImpl extends JsonifiableDataUnitImpl implements GPTVisionMessageContent {
    public GPTVisionMessageContentImpl() {
        super();
    }

    public GPTVisionMessageContentImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

    @Override
    public GPTVisionMessageContent getImplementation() {
        return this;
    }
}
