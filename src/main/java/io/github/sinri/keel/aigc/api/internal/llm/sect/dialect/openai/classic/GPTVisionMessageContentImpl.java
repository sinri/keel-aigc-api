package io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.openai.classic;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.message.vision.GPTVisionMessageContent;
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
