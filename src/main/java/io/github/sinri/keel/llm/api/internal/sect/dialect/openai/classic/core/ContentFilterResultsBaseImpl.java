package io.github.sinri.keel.llm.api.internal.sect.dialect.openai.classic.core;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.llm.api.sect.dialect.openai.classic.core.error.OpenAIErrorBase;
import io.github.sinri.keel.llm.api.sect.dialect.openai.classic.core.filter.OpenAIContentFilterDetectedResult;
import io.github.sinri.keel.llm.api.sect.dialect.openai.classic.core.filter.OpenAIContentFilterResultsBase;
import io.github.sinri.keel.llm.api.sect.dialect.openai.classic.core.filter.OpenAIContentFilterSeverityResult;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class ContentFilterResultsBaseImpl extends UnmodifiableJsonifiableEntityImpl implements OpenAIContentFilterResultsBase {
    public ContentFilterResultsBaseImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

    @Override
    @Nullable
    public OpenAIContentFilterSeverityResult getSexual() {
        JsonObject x = readJsonObject("sexual");
        if (x == null) return null;
        return new ContentFilterSeverityResultImpl(Objects.requireNonNull(x));
    }

    @Nullable
    @Override
    public OpenAIContentFilterSeverityResult getViolence() {
        JsonObject x = readJsonObject("violence");
        if (x == null) return null;
        return new ContentFilterSeverityResultImpl(Objects.requireNonNull(x));
    }

    @Nullable
    @Override
    public OpenAIContentFilterSeverityResult getHate() {
        JsonObject x = readJsonObject("hate");
        if (x == null) return null;
        return new ContentFilterSeverityResultImpl(Objects.requireNonNull(x));
    }

    @Nullable
    @Override
    public OpenAIContentFilterSeverityResult getSelfHarm() {
        JsonObject x = readJsonObject("self_harm");
        if (x == null) return null;
        return new ContentFilterSeverityResultImpl(Objects.requireNonNull(x));
    }

    @Nullable
    @Override
    public OpenAIContentFilterDetectedResult getProfanity() {
        JsonObject x = readJsonObject("profanity");
        if (x == null) return null;
        return new ContentFilterDetectedResultImpl(Objects.requireNonNull(x));
    }

    @Nullable
    @Override
    public OpenAIErrorBase getError() {
        JsonObject error = readJsonObject("error");
        if (error != null) {
            return OpenAIErrorBase.wrap(error);
        }
        return null;
    }
}
