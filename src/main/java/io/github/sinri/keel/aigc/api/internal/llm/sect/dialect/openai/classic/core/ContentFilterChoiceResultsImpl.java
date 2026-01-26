package io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.openai.classic.core;

import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.core.filter.OpenAIContentFilterChoiceResults;
import io.vertx.core.json.JsonObject;

public final class ContentFilterChoiceResultsImpl extends ContentFilterResultsBaseImpl implements OpenAIContentFilterChoiceResults {
    public ContentFilterChoiceResultsImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
