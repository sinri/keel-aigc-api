package io.github.sinri.keel.llm.api.sect.dialect.openai.classic.core.filter;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.github.sinri.keel.llm.api.internal.sect.dialect.openai.classic.core.ContentFilterDetectedResultImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

public interface OpenAIContentFilterDetectedResult extends UnmodifiableJsonifiableEntity {
    static OpenAIContentFilterDetectedResult wrap(JsonObject jsonObject) {
        return new ContentFilterDetectedResultImpl(jsonObject);
    }

    default @Nullable Boolean isFiltered() {
        return readBoolean("filtered");
    }

    default @Nullable Boolean isDetected() {
        return readBoolean("detected");
    }
}
