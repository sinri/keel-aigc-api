package io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.stateful;

import io.github.sinri.keel.base.json.JsonifiableDataUnit;
import io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.openai.stateful.StatefulChatRequestImpl;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

/**
 * @see <a
 *         href="https://learn.microsoft.com/en-us/azure/ai-services/openai/reference-preview-latest#create-response">Create
 *         response</a>
 * @since 2.0.1
 */
public interface StatefulChatRequest extends JsonifiableDataUnit {
    static StatefulChatRequest create() {
        return new StatefulChatRequestImpl();
    }

    static StatefulChatRequest wrap(JsonObject jsonObject) {
        return new StatefulChatRequestImpl(jsonObject);
    }

    default @Nullable Boolean getBackground() {
        return this.readBoolean("background");
    }

    /**
     * @param background Whether to run the model response in the background.
     */
    default StatefulChatRequest setBackground(Boolean background) {
        ensureEntry("background", background);
        return this;
    }

    default @Nullable JsonArray getInclude() {
        return this.readJsonArray("include");
    }

    /**
     * Specify additional output data to include in the model response. Currently
     * supported values are:
     * <p>
     * - file_search_call.results: Include the search results of the file search tool call.<p>
     * - message.input_image.image_url: Include image urls from the input message.<p>
     * - computer_call_output.output.image_url: Include image urls from the computer call output.<p>
     * - reasoning.encrypted_content: Includes an encrypted version of reasoning
     * tokens in reasoning item outputs. This enables reasoning items to be used in
     * multi-turn conversations when using the Responses API statelessly (like
     * when the store parameter is set to false, or when an organization is
     * enrolled in the zero data retention program).
     */
    default StatefulChatRequest setInclude(JsonArray include) {
        ensureEntry("include", include);
        return this;
    }

    default @Nullable String getInput() {
        return this.readString("input");
    }

    /**
     * string (or array but not supported yet)
     */
    default StatefulChatRequest setInput(String input) {
        ensureEntry("input", input);
        return this;
    }

    default @Nullable String getInstructions() {
        return this.readString("instructions");
    }

    /**
     * Inserts a system (or developer) message as the first item in the model's context.
     * <p>
     * When using along with previous_response_id, the instructions from a previous
     * response will not be carried over to the next response. This makes it simple
     * to swap out system (or developer) messages in new responses.
     */
    default StatefulChatRequest setInstructions(String instructions) {
        ensureEntry("instructions", instructions);
        return this;
    }

    default @Nullable Integer getMaxOutputTokens() {
        return this.readInteger("max_output_tokens");
    }

    /**
     * An upper bound for the number of tokens that can be generated for a response,
     * including visible output tokens and {@code .}
     */
    default StatefulChatRequest setMaxOutputTokens(Integer maxOutputTokens) {
        ensureEntry("max_output_tokens", maxOutputTokens);
        return this;
    }

    default @Nullable JsonObject getMetadata() {
        return this.readJsonObject("metadata");
    }

    /**
     * Set of 16 key-value pairs that can be attached to an object. This can be
     * useful for storing additional information about the object in a structured
     * format, and querying for objects via API or the dashboard.
     * <p>
     * Keys are strings with a maximum length of 64 characters. Values are strings
     * with a maximum length of 512 characters.
     */
    default StatefulChatRequest setMetadata(JsonObject metadata) {
        ensureEntry("metadata", metadata);
        return this;
    }

    default @Nullable String getModel() {
        return this.readString("model");
    }

    /**
     * The model deployment to use for the creation of this response.
     */
    default StatefulChatRequest setModel(String model) {
        ensureEntry("model", model);
        return this;
    }

    default @Nullable Boolean getParallelToolCalls() {
        return this.readBoolean("parallel_tool_calls");
    }

    /**
     * Whether to allow the model to run tool calls in parallel.
     */
    default StatefulChatRequest setParallelToolCalls(Boolean parallelToolCalls) {
        ensureEntry("parallel_tool_calls", parallelToolCalls);
        return this;
    }

    default @Nullable String getPreviousResponseId() {
        return this.readString("previous_response_id");
    }

    /**
     * The unique ID of the previous response to the model. Use this to
     * create multi-turn conversations. reasoning models.
     */
    default StatefulChatRequest setPreviousResponseId(String previousResponseId) {
        ensureEntry("previous_response_id", previousResponseId);
        return this;
    }

    default @Nullable JsonObject getReasoning() {
        return this.readJsonObject("reasoning");
    }

    /**
     * o-series models only.
     * <p>
     * Configuration options for reasoning models.
     */
    default StatefulChatRequest setReasoning(JsonObject reasoning) {
        ensureEntry("reasoning", reasoning);
        return this;
    }

    default @Nullable Boolean getStore() {
        return this.readBoolean("store");
    }

    /**
     * Whether to store the generated model response for later retrieval via API.
     */
    default StatefulChatRequest setStore(Boolean store) {
        ensureEntry("store", store);
        return this;
    }

    default @Nullable Boolean getStream() {
        return this.readBoolean("stream");
    }

    /**
     * If set to true, the model response data will be streamed to the client
     * as it is generated using server-sent events.
     *
     * @see <a
     *         href="https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events#Event_stream_format">server-sent
     *         events</a>
     */
    default StatefulChatRequest setStream(Boolean stream) {
        ensureEntry("stream", stream);
        return this;
    }

    default @Nullable Float getTemperature() {
        return this.readFloat("temperature");
    }

    /**
     * What sampling temperature to use, between 0 and 2. Higher values like 0.8 will make the output more random, while
     * lower values like 0.2 will make it more focused and deterministic.
     * We generally recommend altering this or top_p but not both.
     */
    default StatefulChatRequest setTemperature(Float temperature) {
        ensureEntry("temperature", temperature);
        return this;
    }

    default @Nullable JsonObject getText() {
        return this.readJsonObject("text");
    }

    /**
     * Configuration options for a text response from the model.
     * Can be plain text or structured JSON data.
     */
    default StatefulChatRequest setText(JsonObject text) {
        ensureEntry("text", text);
        return this;
    }

    default @Nullable JsonObject getToolChoice() {
        return this.readJsonObject("tool_choice");
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
    default StatefulChatRequest setToolChoice(JsonObject toolChoice) {
        ensureEntry("tool_choice", toolChoice);
        return this;
    }

    default @Nullable JsonArray getTools() {
        return this.readJsonArray("tools");
    }

    /**
     * An array of tools the model may call while generating a response. You
     * can specify which tool to use by setting the tool_choice parameter.
     * <p>
     * The two categories of tools you can provide the model are:
     * <p>
     * - Built-in tools: Tools that are provided by OpenAI that extend the
     * model's capabilities, like file search.
     * <p>
     * - Function calls (custom tools): Functions that are defined by you,
     * enabling the model to call your own code.
     */
    default StatefulChatRequest setTools(JsonArray tools) {
        ensureEntry("tools", tools);
        return this;
    }

    default @Nullable Float getTopP() {
        return this.readFloat("top_p");
    }

    /**
     * An alternative to sampling with temperature, called nucleus sampling,
     * where the model considers the results of the tokens with top_p probability
     * mass. So 0.1 means only the tokens comprising the top 10% probability mass
     * are considered.
     * <p>
     * We generally recommend altering this or temperature but not both.
     */
    default StatefulChatRequest setTopP(Float topP) {
        ensureEntry("top_p", topP);
        return this;
    }

    default @Nullable String getTruncation() {
        return this.readString("truncation");
    }

    /**
     * The truncation strategy to use for the model response.
     * <p>
     * - auto: If the context of this response and previous ones exceeds
     * the model's context window size, the model will truncate the
     * response to fit the context window by dropping input items in the
     * middle of the conversation.
     * <p>
     * - disabled (default): If a model response will exceed the context window
     * size for a model, the request will fail with a 400 error.
     *
     * @param truncation Possible values: auto, disabled
     */
    default StatefulChatRequest setTruncation(String truncation) {
        ensureEntry("truncation", truncation);
        return this;
    }

    default @Nullable String getUser() {
        return this.readString("user");
    }

    /**
     * A unique identifier representing your end-user, which can help OpenAI to monitor and detect abuse.
     */
    default StatefulChatRequest setUser(String user) {
        ensureEntry("user", user);
        return this;
    }
}
