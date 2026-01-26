package io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.message.vision;

import io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.openai.classic.GPTVisionMessageContentImageUrlImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.core.OpenAICompatibleVisionContentForImageUrl;
import io.vertx.core.json.JsonObject;

public interface GPTVisionMessageContentImageUrl extends OpenAICompatibleVisionContentForImageUrl<GPTVisionMessageContentImageUrl> {
    static GPTVisionMessageContentImageUrl create() {
        return new GPTVisionMessageContentImageUrlImpl();
    }

    static GPTVisionMessageContentImageUrl wrap(JsonObject jsonObject) {
        return new GPTVisionMessageContentImageUrlImpl(jsonObject);
    }
}
