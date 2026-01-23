package io.github.sinri.keel.llm.api.sect.dialect.openai.classic.response.stream;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.github.sinri.keel.llm.api.internal.sect.dialect.openai.classic.GPTResponseChunkChoiceImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

public interface GPTResponseChunkChoice extends UnmodifiableJsonifiableEntity {
    static GPTResponseChunkChoice wrap(JsonObject jsonObject) {
        return new GPTResponseChunkChoiceImpl(jsonObject);
    }

    default @Nullable JsonObject getContentFilterResults() {
        return readJsonObject("content_filter_results");
    }

    default @Nullable String getFinishReason() {
        return readString("finish_reason");
    }

    default @Nullable Integer getIndex() {
        return readInteger("index");
    }

    default @Nullable GPTResponseChunkChoiceDelta getDelta() {
        JsonObject x = readJsonObject("delta");
        if (x == null) {
            return null;
        }
        return GPTResponseChunkChoiceDelta.wrap(x);
    }

    // logprobs is not supported yet
}
