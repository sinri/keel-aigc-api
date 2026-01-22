package io.github.sinri.keel.llm.api.sect.dialect.openai.core.filter;


import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.github.sinri.keel.llm.api.sect.dialect.openai.core.error.OpenAIErrorBase;
import org.jspecify.annotations.Nullable;

public interface OpenAIContentFilterResultsBase extends UnmodifiableJsonifiableEntity {
    @Nullable
    OpenAIContentFilterSeverityResult getSexual();

    @Nullable
    OpenAIContentFilterSeverityResult getViolence();

    @Nullable
    OpenAIContentFilterSeverityResult getHate();

    @Nullable
    OpenAIContentFilterSeverityResult getSelfHarm();

    @Nullable
    OpenAIContentFilterDetectedResult getProfanity();

    @Nullable
    OpenAIErrorBase getError();
}
