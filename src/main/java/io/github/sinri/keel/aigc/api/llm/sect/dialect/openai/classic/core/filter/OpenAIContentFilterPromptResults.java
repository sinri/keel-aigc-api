package io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.core.filter;

import io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.openai.classic.core.ContentFilterPromptResultsImpl;
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
