package io.github.sinri.keel.llm.api.sect.dialect.openai.classic.core.error;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.github.sinri.keel.llm.api.internal.sect.dialect.openai.classic.core.OpenAIErrorBaseImpl;
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
