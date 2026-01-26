package io.github.sinri.keel.aigc.api.vgm.dalle.v3;

import io.github.sinri.keel.aigc.api.internal.vgm.dalle.DalleContentFilterResultsImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.core.filter.OpenAIContentFilterSeverityResult;
import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;


public interface DalleContentFilterResults extends UnmodifiableJsonifiableEntity {
    static DalleContentFilterResults wrap(JsonObject j) {
        return new DalleContentFilterResultsImpl(j);
    }

    default @Nullable OpenAIContentFilterSeverityResult getSexual() {
        JsonObject entries = readJsonObject("sexual");
        if (entries == null) return null;
        return OpenAIContentFilterSeverityResult.wrap(entries);
    }

    @Nullable
    default OpenAIContentFilterSeverityResult getViolence() {
        JsonObject entries = readJsonObject("violence");
        if (entries == null) return null;
        return OpenAIContentFilterSeverityResult.wrap(entries);
    }

    @Nullable
    default OpenAIContentFilterSeverityResult getHate() {
        JsonObject entries = readJsonObject("hate");
        if (entries == null) return null;
        return OpenAIContentFilterSeverityResult.wrap(entries);
    }

    @Nullable
    default OpenAIContentFilterSeverityResult getSelfHarm() {
        JsonObject entries = readJsonObject("self_harm");
        if (entries == null) return null;
        return OpenAIContentFilterSeverityResult.wrap(entries);
    }
}
