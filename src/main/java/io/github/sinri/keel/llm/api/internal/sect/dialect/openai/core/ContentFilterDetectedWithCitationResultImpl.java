package io.github.sinri.keel.llm.api.internal.sect.dialect.openai.core;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.llm.api.sect.dialect.openai.core.filter.OpenAIContentFilterDetectedWithCitationResult;
import io.vertx.core.json.JsonObject;


public final class ContentFilterDetectedWithCitationResultImpl extends UnmodifiableJsonifiableEntityImpl implements OpenAIContentFilterDetectedWithCitationResult {
    public ContentFilterDetectedWithCitationResultImpl( JsonObject jsonObject) {
        super(jsonObject);
    }
}
