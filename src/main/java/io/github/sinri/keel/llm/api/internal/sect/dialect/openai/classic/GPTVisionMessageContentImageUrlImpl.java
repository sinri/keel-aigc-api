package io.github.sinri.keel.llm.api.internal.sect.dialect.openai.classic;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.llm.api.sect.dialect.openai.classic.message.vision.GPTVisionMessageContentImageUrl;
import io.vertx.core.json.JsonObject;


public class GPTVisionMessageContentImageUrlImpl extends JsonifiableDataUnitImpl implements GPTVisionMessageContentImageUrl {
    public GPTVisionMessageContentImageUrlImpl() {
        super();
    }

    public GPTVisionMessageContentImageUrlImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

    @Override
    public GPTVisionMessageContentImageUrl getImplementation() {
        return this;
    }
}
