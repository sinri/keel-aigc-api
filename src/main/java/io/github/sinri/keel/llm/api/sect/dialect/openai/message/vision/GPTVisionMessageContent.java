package io.github.sinri.keel.llm.api.sect.dialect.openai.message.vision;

import io.github.sinri.keel.llm.api.internal.sect.dialect.openai.GPTVisionMessageContentImpl;
import io.github.sinri.keel.llm.api.sect.dialect.openai.vision.OpenAICompatibleVisionContent;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

public interface GPTVisionMessageContent extends OpenAICompatibleVisionContent<GPTVisionMessageContent, GPTVisionMessageContentImageUrl> {
    static GPTVisionMessageContent create() {
        return new GPTVisionMessageContentImpl();
    }

    static GPTVisionMessageContent wrap(JsonObject jsonObject) {
        return new GPTVisionMessageContentImpl(jsonObject);
    }

    default GPTVisionMessageContentImageUrl getImageUrl() {
        JsonObject x = readJsonObject("image_url");
        Objects.requireNonNull(x);
        return GPTVisionMessageContentImageUrl.wrap(x);
    }
}
