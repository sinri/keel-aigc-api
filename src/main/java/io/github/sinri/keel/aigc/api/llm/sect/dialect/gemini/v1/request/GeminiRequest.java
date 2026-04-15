package io.github.sinri.keel.aigc.api.llm.sect.dialect.gemini.v1.request;

import io.github.sinri.keel.base.json.JsonifiableDataUnit;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * @see <a
 *         href="https://docs.cloud.google.com/vertex-ai/generative-ai/docs/reference/express-mode/rest/v1/publishers.models/generateContent#request-body">Vertex
 *         AI - Gemini - Request</a>
 */
public interface GeminiRequest extends JsonifiableDataUnit {
    @Nullable GeminiRequestContent getSystemInstruction();

    /**
     * The user provided system instructions for the model.
     * <p>Optional.
     * <p>Note: only text should be used in parts and content in each part will be in a separate paragraph.
     */
    GeminiRequest setSystemInstruction(@Nullable GeminiRequestContent systemInstruction);

    List<GeminiRequestContent> getContents();

    /**
     * The content of the current conversation with the model.
     * <p>
     * Required.
     * <p>
     * For single-turn queries, this is a single instance.
     * For multi-turn queries, this is a repeated field that
     * contains conversation history + latest request.
     */
    GeminiRequest setContents(List<GeminiRequestContent> contents);

    List<GeminiRequestTool> getTools();

    /**
     * A list of Tools the model may use to generate the next response.
     * <p>
     * Optional.
     * <p>
     * A Tool is a piece of code that enables the system to interact with external systems to perform an action,
     * or set of actions, outside of knowledge and scope of the model.
     */
    GeminiRequest setTools(List<GeminiRequestTool> tools);

    @Nullable GeminiRequestToolConfig getToolConfig();

    /**
     * Tool config.
     * <p>Optional.
     * <p>This config is shared for all tools provided in the request.
     */
    GeminiRequest setToolConfig(@Nullable GeminiRequestToolConfig toolConfig);

    Map<String, String> getLabels();

    /**
     *
     * The labels with user-defined metadata for the request. It is used for billing and reporting only.
     * <p>
     * Optional.
     * <p>
     * label keys and values can be no longer than 63 characters (Unicode codepoints)
     * and can only contain lowercase letters, numeric characters, underscores, and dashes.
     * International characters are allowed.
     * <p>
     * label values are optional.
     * <p>
     * label keys must start with a letter.
     */
    GeminiRequest setLabels(Map<String, String> labels);

    List<SafetySetting> getSafetySettings();

    /**
     * Per request settings for blocking unsafe content.
     * Enforced on GenerateContentResponse.candidates.
     * <p>Optional.
     * <p>
     */
    GeminiRequest setSafetySettings(List<SafetySetting> safetySettings);

    @Nullable GenerationConfig getGenerationConfig();

    /**
     * Generation config.<p>Optional.
     */
    GeminiRequest setGenerationConfig(@Nullable GenerationConfig generationConfig);

    /**
     * @see <a
     *         href="https://docs.cloud.google.com/vertex-ai/generative-ai/docs/reference/rest/v1/Content">Content</a>
     */
    interface GeminiRequestContent extends JsonifiableDataUnit {
        @Nullable Role getRole();

        /**
         * The producer of the content. Must be either 'user' or 'model'.
         * <p>Optional.
         * <p>
         * If not set, the service will default to 'user'.
         */
        GeminiRequestContent setRole(@Nullable Role role);

        List<Part> getParts();

        /**
         * A list of Part objects that make up a single message. Parts of a message can have different MIME types.
         * <p>Required.
         * <p>
         * A Content message must have at least one Part.
         */
        GeminiRequestContent setParts(List<Part> parts);

        enum Role {
            user, model
        }

        interface Part extends JsonifiableDataUnit {
            @Nullable String getThought();

            /**
             * Indicates whether the part represents the model's thought process or reasoning.
             * <p>Optional.
             */
            Part setThought(@Nullable String thought);

            @Nullable String getThoughtSignature();

            /**
             * An opaque signature for the thought so it can be reused in subsequent requests.
             * <p>Optional.
             * <p>
             * A base64-encoded string.
             */
            Part setThoughtSignature(@Nullable String thoughtSignature);
            // todo optional mediaResolution https://docs.cloud.google.com/vertex-ai/generative-ai/docs/reference/rest/v1/Content#MediaResolution

            @Nullable String getDataAsText();

            Part setDataAsText(@Nullable String dataAsText);

            // todo data as inlineData
            // todo data as fileData

            @Nullable FunctionCall getDataAsFunctionCall();

            Part setDataAsFunctionCall(@Nullable FunctionCall functionCall);

            @Nullable FunctionResponse getDataAsFunctionResponse();

            Part setDataAsFunctionResponse(@Nullable FunctionResponse functionResponse);

            // todo data as executableCode
            // todo data as codeExecutionResult

            // metadata
        }

        /**
         * @see <a
         *         href="https://docs.cloud.google.com/vertex-ai/generative-ai/docs/reference/rest/v1/Content#FunctionCall">FunctionCall</a>
         */
        interface FunctionCall extends JsonifiableDataUnit {
            @Nullable String getName();

            /**
             * The name of the function to call. Matches FunctionDeclaration.name.
             * <p>Optional.
             */
            FunctionCall setName(@Nullable String name);
            @Nullable JsonObject getArgs();
            FunctionCall setArgs(@Nullable JsonObject args);
            //List<partialArgs>
/*

args
object (Struct format)
Optional. The function parameters and values in JSON object format. See FunctionDeclaration.parameters for parameter details.

partialArgs[]
object (PartialArg)
Optional. The partial argument value of the function call. If provided, represents the arguments/fields that are streamed incrementally.

willContinue
boolean
Optional. Whether this is the last part of the FunctionCall. If true, another partial message for the current FunctionCall is expected to follow.
 */

            interface PartialArg extends JsonifiableDataUnit {
                @Nullable String getName();
                PartialArg setName(@Nullable String name);
                @Nullable String getValue();
                PartialArg setValue(@Nullable String value);
            }
        }

        /**
         * @see <a
         *         href="https://docs.cloud.google.com/vertex-ai/generative-ai/docs/reference/rest/v1/Content#FunctionResponse">FunctionResponse</a>
         */
        interface FunctionResponse extends JsonifiableDataUnit {
        }
    }

    /**
     * @see <a
     *         href="https://docs.cloud.google.com/vertex-ai/generative-ai/docs/reference/rest/v1/projects.locations.cachedContents#Tool">Tool</a>
     */
    interface GeminiRequestTool extends JsonifiableDataUnit {

    }

    /**
     * @see <a
     *         href="https://docs.cloud.google.com/vertex-ai/generative-ai/docs/reference/rest/v1/projects.locations.cachedContents#ToolConfig">ToolConfig</a>
     */
    interface GeminiRequestToolConfig extends JsonifiableDataUnit {
    }

    /**
     * @see <a href="https://docs.cloud.google.com/vertex-ai/generative-ai/docs/reference/rest/v1/SafetySetting">
     *         SafetySetting</a>
     */
    interface SafetySetting extends JsonifiableDataUnit {
    }

    /**
     * @see <a
     *         href="https://docs.cloud.google.com/vertex-ai/generative-ai/docs/reference/rest/v1/GenerationConfig">GenerationConfig</a>
     */
    interface GenerationConfig extends JsonifiableDataUnit {
    }
}
