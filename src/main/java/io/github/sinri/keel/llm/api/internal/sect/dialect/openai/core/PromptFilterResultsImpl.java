package io.github.sinri.keel.llm.api.internal.sect.dialect.openai.core;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.llm.api.sect.dialect.openai.core.filter.OpenAIContentFilterPromptResults;
import io.github.sinri.keel.llm.api.sect.dialect.openai.core.filter.OpenAIPromptFilterResults;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;


public final class PromptFilterResultsImpl extends UnmodifiableJsonifiableEntityImpl implements OpenAIPromptFilterResults {
    public PromptFilterResultsImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

    @Override

    public @Nullable OpenAIContentFilterPromptResults getContentFilterResults() {
        JsonObject cfr = readJsonObject("content_filter_results");
        if (cfr == null) return null;
        return new ContentFilterPromptResultsImpl(cfr);
    }
}
