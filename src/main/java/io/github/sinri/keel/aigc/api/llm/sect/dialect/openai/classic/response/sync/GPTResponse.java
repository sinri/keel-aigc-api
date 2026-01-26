package io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.response.sync;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.openai.classic.GPTResponseImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.core.filter.OpenAIPromptFilterResults;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface GPTResponse extends UnmodifiableJsonifiableEntity {
    static GPTResponse wrap(JsonObject jsonObject) {
        return new GPTResponseImpl(jsonObject);
    }

    /**
     * @return A unique identifier for the completion.
     */
    default @Nullable String getId() {
        return readString("id");
    }

    /**
     * @return The Unix timestamp (in seconds) of when the completion was created.
     */
    default @Nullable Integer getCreated() {
        return readInteger("created");
    }

    /**
     * @return The object type, which is always "text_completion"
     *         Possible values: text_completion
     */
    default @Nullable String getObject() {
        return readString("object");
    }

    /**
     * @return The model used for completion.
     */
    default @Nullable String getModel() {
        return readString("model");
    }

    /**
     * This fingerprint represents the backend configuration that the model runs
     * with.
     * <p>
     * Can be used in conjunction with the seed request parameter to understand when
     * backend changes have been made that might impact determinism.
     */
    default @Nullable String getSystemFingerprint() {
        return readString("system_fingerprint");
    }

    default List<GPTResponseChoice> getChoices() {
        List<JsonObject> array = readJsonObjectArray("choices");
        if (array == null) {
            return List.of();
        }
        return array.stream().map(GPTResponseChoice::wrap).toList();
    }

    /**
     * Usage statistics for the completion request.
     */
    default @Nullable JsonObject getUsage() {
        return readJsonObject("usage");
    }

    /**
     * Content filtering results for zero or more prompts in the request. In a
     * streaming request, results for different prompts may arrive at different
     * times or in different orders.
     */
    default List<OpenAIPromptFilterResults> getPromptFilterResults() {
        var a = readJsonObjectArray("prompt_filter_results");
        if (a == null) return List.of();
        return a.stream().map(OpenAIPromptFilterResults::wrap).toList();
    }

}
