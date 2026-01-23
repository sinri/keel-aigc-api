package io.github.sinri.keel.llm.api.sect.dialect.openai.classic.message.vision;

import io.github.sinri.keel.llm.api.internal.sect.dialect.openai.classic.GPTVisionMessageContentImageUrlImpl;
import io.github.sinri.keel.llm.api.sect.dialect.openai.classic.core.OpenAICompatibleVisionContentForImageUrl;
import io.vertx.core.json.JsonObject;

public interface GPTVisionMessageContentImageUrl extends OpenAICompatibleVisionContentForImageUrl<GPTVisionMessageContentImageUrl> {
    static GPTVisionMessageContentImageUrl create() {
        return new GPTVisionMessageContentImageUrlImpl();
    }

    static GPTVisionMessageContentImageUrl wrap(JsonObject jsonObject) {
        return new GPTVisionMessageContentImageUrlImpl(jsonObject);
    }
}
