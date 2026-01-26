package io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.core.filter;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.openai.classic.core.ContentFilterDetectedWithCitationResultImpl;
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
