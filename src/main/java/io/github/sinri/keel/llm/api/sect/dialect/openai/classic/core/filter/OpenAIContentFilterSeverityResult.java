package io.github.sinri.keel.llm.api.sect.dialect.openai.classic.core.filter;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.github.sinri.keel.llm.api.internal.sect.dialect.openai.classic.core.ContentFilterSeverityResultImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

public interface OpenAIContentFilterSeverityResult extends UnmodifiableJsonifiableEntity {
    static OpenAIContentFilterSeverityResult wrap(JsonObject jsonObject) {
        return new ContentFilterSeverityResultImpl(jsonObject);
    }

    default @Nullable Boolean isFiltered() {
        return readBoolean("filtered");
    }

    default @Nullable String getSeverity() {
        return readString("severity");
    }
}
