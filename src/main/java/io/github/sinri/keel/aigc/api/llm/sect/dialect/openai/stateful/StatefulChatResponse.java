package io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.stateful;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.openai.stateful.StatefulChatResponseImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.core.error.OpenAIErrorBase;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * @see <a
 *         href="https://learn.microsoft.com/en-us/azure/ai-services/openai/reference-preview-latest#azureresponse">AzureResponse</a>
 * @since 5.0.0
 */
public interface StatefulChatResponse extends UnmodifiableJsonifiableEntity {
    static StatefulChatResponse wrap(JsonObject jsonObject) {
        return new StatefulChatResponseImpl(jsonObject);
    }

    /**
     * @return Whether to run the model response in the background.
     */
    default @Nullable Boolean getBackground() {
        return this.readBoolean("background");
    }

    /**
     * @return Unix timestamp (in seconds) of when this Response was created.
     */
    default @Nullable Integer getCreatedAt() {
        return this.readInteger("created_at");
    }

    /**
     * @return An error object returned when the model fails to generate a Response.
     */
    default @Nullable OpenAIErrorBase getError() {
        var x = this.readJsonObject("error");
        if (x == null) return null;
        return OpenAIErrorBase.wrap(x);
    }

    /**
     * @return Details about why the response is incomplete.
     */
    default @Nullable StatefulChatIncompleteDetails getIncompleteDetails() {
        var x = this.readJsonObject("incomplete_details");
        if (x == null) {
            return null;
        }
        return StatefulChatIncompleteDetails.wrap(x);
    }

    /**
     * Inserts a system (or developer) message as the first item in the model's context.
     * <p>
     * When using along with previous_response_id, the instructions from a previous
     * response will not be carried over to the next response. This makes it simple
     * to swap out system (or developer) messages in new responses.
     */
    default @Nullable String getInstructions() {
        return this.readString("instructions");
    }

    /**
     * An upper bound for the number of tokens that can be generated for a response,
     * including visible output tokens and {@literal .}
     */
    default @Nullable Integer getMaxOutputTokens() {
        return this.readInteger("max_output_tokens");
    }

    /**
     * Set of 16 key-value pairs that can be attached to an object. This can be
     * useful for storing additional information about the object in a structured
     * format, and querying for objects via API or the dashboard.
     * <p>
     * Keys are strings with a maximum length of 64 characters. Values are strings
     * with a maximum length of 512 characters.
     */
    default @Nullable JsonObject getMetadata() {
        return this.readJsonObject("metadata");
    }

    /**
     * The model used to generate this response.
     */
    default @Nullable String getModel() {
        return this.readString("model");
    }

    /**
     * The object type of this resource - always set to response.
     *
     * @return Possible values: response
     */
    default @Nullable String getObject() {
        return this.readString("object");
    }

    /**
     * Whether to allow the model to run tool calls in parallel.
     */
    default @Nullable Boolean getParallelToolCalls() {
        return this.readBoolean("parallel_tool_calls");
    }

    /**
     * o-series models only.
     * <p>
     * Configuration options for reasoning models.
     */
    default @Nullable StatefulChatReasoning getReasoning() {
        var x = this.readJsonObject("reasoning");
        if (x == null) return null;
        return StatefulChatReasoning.wrap(x);
    }

    /**
     * What sampling temperature to use, between 0 and 2. Higher values like 0.8 will make the output more random, while
     * lower values like 0.2 will make it more focused and deterministic.
     * We generally recommend altering this or top_p but not both.
     */
    default @Nullable Float getTemperature() {
        return this.readFloat("temperature");
    }

    /**
     * Configuration options for a text response from the model.
     * Can be plain text or structured JSON data.
     */
    default @Nullable JsonObject getText() {
        return this.readJsonObject("text");
    }

    /**
     * Controls which (if any) tool is called by the model.
     * <p>
     * none means the model will not call any tool and instead generates a message.
     * <p>
     * auto means the model can pick between generating a message or calling one or
     * more tools.
     * <p>
     * required means the model must call one or more tools.
     */
    default @Nullable StatefulChatToolChoice getToolChoice() {
        var x = this.readJsonObject("tool_choice");
        if (x == null) return null;
        return StatefulChatToolChoice.wrap(x);
    }

    /**
     * An array of tools the model may call while generating a response. You
     * can specify which tool to use by setting the tool_choice parameter.
     * <p>
     * The two categories of tools you can provide the model are:
     * <p>
     * - Built-in tools: Tools that are provided by OpenAI that extend the
     * model's capabilities.
     * <p>
     * - Function calls (custom tools): Functions that are defined by you, enabling the model to call
     * your own code
     */
    default @Nullable JsonArray getTools() {
        return this.readJsonArray("tools");
    }

    /**
     * An alternative to sampling with temperature, called nucleus sampling,
     * where the model considers the results of the tokens with top_p probability
     * mass. So 0.1 means only the tokens comprising the top 10% probability mass
     * are considered.
     * <p>
     * We generally recommend altering this or temperature but not both.
     */
    default @Nullable Float getTopP() {
        return this.readFloat("top_p");
    }

    /**
     * The truncation strategy to use for the model response.<p>
     * - auto: If the context of this response and previous ones exceeds
     * the model's context window size, the model will truncate the
     * response to fit the context window by dropping input items in the
     * middle of the conversation.<p>
     * - disabled (default): If a model response will exceed the context window
     * size for a model, the request will fail with a 400 error.<p>
     *
     * @return Possible values: auto, disabled
     */
    default @Nullable String getTruncation() {
        return this.readString("truncation");
    }

    /**
     * Represents token usage details including input tokens, output tokens,
     * a breakdown of output tokens, and the total tokens used.
     */
    default @Nullable JsonObject getUsage() {
        return this.readJsonObject("usage");
    }

    /**
     * A unique identifier representing your end-user, which can help OpenAI to monitor and detect abuse.
     */
    default @Nullable String getUser() {
        return this.readString("user");
    }

    /**
     * @return Unique identifier for this Response.
     */
    default @Nullable String getId() {
        return this.readString("id");
    }

    /**
     * The status of the response generation. One of completed, failed,
     * in_progress, cancelled, queued, or incomplete.
     *
     * @return Possible values: completed, failed, in_progress, cancelled, queued, incomplete
     */
    default @Nullable String getStatus() {
        return this.readString("status");
    }

    /**
     * An array of content items generated by the model.
     * <p>
     * - The length and order of items in the output array is dependent
     * on the model's response.<p>
     * - Rather than accessing the first item in the output array and
     * assuming it's an assistant message with the content generated by
     * the model, you might consider using the output_text property where
     * supported in SDKs.<p>
     */
    default List<StatefulChatResponseOutputItem> getOutput() {
        var a = this.readJsonObjectArray("output");
        if (a == null) return List.of();
        return a.stream().map(StatefulChatResponseOutputItem::wrap).toList();
    }

    /**
     * The unique ID of the previous response to the model. Use this to
     * create multi-turn conversations. reasoning models.
     */
    default @Nullable String getPreviousResponseId() {
        return this.readString("previous_response_id");
    }

}
