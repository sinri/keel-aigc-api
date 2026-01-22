package io.github.sinri.keel.llm.api.sect.dialect.openai.core.filter;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.github.sinri.keel.llm.api.internal.sect.dialect.openai.core.ContentFilterDetectedWithCitationResultImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;


public interface OpenAIContentFilterDetectedWithCitationResult extends UnmodifiableJsonifiableEntity {
    static OpenAIContentFilterDetectedWithCitationResult wrap(JsonObject jsonObject) {
        return new ContentFilterDetectedWithCitationResultImpl(jsonObject);
    }

    default @Nullable JsonObject getCitation() {
        // URL as string,
        // license as string
        return readJsonObject("citation");
    }
}
