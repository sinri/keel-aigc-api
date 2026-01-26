package io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.core.error;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.openai.classic.core.OpenAIErrorBaseImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

public interface OpenAIErrorBase extends UnmodifiableJsonifiableEntity {
    static OpenAIErrorBase wrap(JsonObject json) {
        return new OpenAIErrorBaseImpl(json);
    }

    default @Nullable String getCode() {
        return readString("code");
    }

    default @Nullable String getMessage() {
        return readString("message");
    }
}
