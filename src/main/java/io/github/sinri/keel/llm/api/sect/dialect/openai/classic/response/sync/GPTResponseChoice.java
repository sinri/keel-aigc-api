package io.github.sinri.keel.llm.api.sect.dialect.openai.classic.response.sync;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.github.sinri.keel.llm.api.internal.sect.dialect.openai.classic.GPTResponseChoiceImpl;
import io.github.sinri.keel.llm.api.sect.dialect.openai.classic.core.filter.OpenAIContentFilterChoiceResults;
import io.github.sinri.keel.llm.api.sect.dialect.openai.classic.message.GPTMessageInResponse;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

public interface GPTResponseChoice extends UnmodifiableJsonifiableEntity {
    static GPTResponseChoice wrap(JsonObject jsonObject) {
        return new GPTResponseChoiceImpl(jsonObject);
    }

    default @Nullable OpenAIContentFilterChoiceResults getContentFilterResults() {
        var x = readJsonObject("content_filter_results");
        if (x == null) return null;
        return OpenAIContentFilterChoiceResults.wrap(x);
    }

    default @Nullable String getFinishReason() {
        return readString("finish_reason");
    }

    default @Nullable Integer getIndex() {
        return readInteger("index");
    }

    default @Nullable JsonObject getLogprobs() {
        return readJsonObject("logprobs");
    }

    default @Nullable GPTMessageInResponse getMessage() {
        JsonObject jsonObject = readJsonObject("message");
        if (jsonObject == null) return null;
        return GPTMessageInResponse.wrap(jsonObject);
    }
}
