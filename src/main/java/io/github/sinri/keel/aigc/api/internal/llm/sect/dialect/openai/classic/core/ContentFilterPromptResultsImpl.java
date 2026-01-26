package io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.openai.classic.core;

import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.core.filter.OpenAIContentFilterDetectedResult;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.core.filter.OpenAIContentFilterPromptResults;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class ContentFilterPromptResultsImpl extends ContentFilterResultsBaseImpl implements OpenAIContentFilterPromptResults {
    public ContentFilterPromptResultsImpl(JsonObject jsonObject) {
        super(jsonObject);
    }


    @Override
    public @Nullable OpenAIContentFilterDetectedResult getJailbreak() {
        JsonObject x = readJsonObject("jailbreak");
        if (x == null) return null;
        return OpenAIContentFilterDetectedResult.wrap(Objects.requireNonNull(x));
    }
}
