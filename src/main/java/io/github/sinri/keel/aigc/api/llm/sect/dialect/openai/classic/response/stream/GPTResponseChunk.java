package io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.response.stream;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.openai.classic.GPTResponseChunkImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.core.filter.OpenAIPromptFilterResults;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface GPTResponseChunk extends UnmodifiableJsonifiableEntity {
    static GPTResponseChunk wrap(JsonObject jsonObject) {
        return new GPTResponseChunkImpl(jsonObject);
    }

    default @Nullable Integer getCreated() {
        return this.readInteger("created");
    }

    default @Nullable String getId() {
        return this.readString("id");
    }

    default @Nullable String getModel() {
        return this.readString("model");
    }

    default @Nullable String getObject() {
        return this.readString("object");
    }

    default List<OpenAIPromptFilterResults> getPromptFilterResults() {
        var a = readJsonObjectArray("prompt_filter_results");
        if (a == null) return List.of();
        return a.stream().map(OpenAIPromptFilterResults::wrap).toList();
    }

    default List<GPTResponseChunkChoice> getChoices() {
        List<JsonObject> a = readJsonObjectArray("choices");
        if (a == null) return List.of();
        return a.stream().map(GPTResponseChunkChoice::wrap).toList();
    }

}
