package io.github.sinri.keel.llm.api.sect.dialect.openai.core.filter;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.github.sinri.keel.llm.api.internal.sect.dialect.openai.core.PromptFilterResultsImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;


public interface OpenAIPromptFilterResults extends UnmodifiableJsonifiableEntity {
    static OpenAIPromptFilterResults wrap(JsonObject jsonObject) {
        return new PromptFilterResultsImpl(jsonObject);
    }

    default @Nullable Integer getPromptIndex() {
        return readInteger("prompt_index");
    }

    /**
     * Information about the content filtering category (hate, sexual, violence, self_harm),
     * if it has been detected,
     * as well as the severity level (very_low, low, medium, high-scale that determines the intensity and risk level of
     * harmful content)
     * and if it has been filtered or not.
     * Information about jailbreak content and profanity, if it has been detected, and if it has been filtered or not.
     * And information about customer blocklist, if it has been filtered and its id.
     */
    @Nullable OpenAIContentFilterPromptResults getContentFilterResults();

}
