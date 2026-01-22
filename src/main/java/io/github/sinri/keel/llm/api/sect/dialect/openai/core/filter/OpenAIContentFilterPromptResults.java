package io.github.sinri.keel.llm.api.sect.dialect.openai.core.filter;

import io.github.sinri.keel.llm.api.internal.sect.dialect.openai.core.ContentFilterPromptResultsImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;


public interface OpenAIContentFilterPromptResults extends OpenAIContentFilterResultsBase {
    static OpenAIContentFilterPromptResults wrap(JsonObject jsonObject) {
        return new ContentFilterPromptResultsImpl(jsonObject);
    }

    @Nullable OpenAIContentFilterDetectedResult getJailbreak();

    default boolean whetherFiltered() {
        var hate = getHate();
        var profanity = getProfanity();
        var sexual = getSexual();
        var jailbreak = getJailbreak();
        var violence = getViolence();
        var selfHarm = getSelfHarm();

        if (hate != null && Boolean.TRUE.equals(hate.isFiltered())) {
            return true;
        }
        if (profanity != null && Boolean.TRUE.equals(profanity.isFiltered())) {
            return true;
        }
        if (sexual != null && Boolean.TRUE.equals(sexual.isFiltered())) {
            return true;
        }
        if (jailbreak != null && Boolean.TRUE.equals(jailbreak.isFiltered())) {
            return true;
        }
        if (violence != null && Boolean.TRUE.equals(violence.isFiltered())) {
            return true;
        }
        return selfHarm != null && Boolean.TRUE.equals(selfHarm.isFiltered());
    }

}
