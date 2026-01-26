package io.github.sinri.keel.aigc.api.vgm.gptimage;

import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.OpenAIModelConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

/**
 * @see <a href="https://learn.microsoft.com/en-us/azure/ai-services/openai/how-to/dall-e?tabs=gpt-image-1">How to
 *         use Azure OpenAI image generation models</a>
 * @since 1.3.1
 */
public class GPTImageKit {

    private final OpenAIModelConfigElement openAIConfigElement;
    private final WebClient webClient;

    public GPTImageKit(OpenAIModelConfigElement openAIConfigElement, WebClient webClient) {
        this.openAIConfigElement = openAIConfigElement;
        this.webClient = webClient;
    }

    private String getUrlToGenerateImage() throws NotConfiguredException {
        return "https://" + openAIConfigElement.resourceName() + ".cognitiveservices.azure.com/openai/deployments/" + openAIConfigElement.deployment() + "/images/generations?api-version=" + openAIConfigElement.apiVersion();
    }

    private String getUrlToEditImage() throws NotConfiguredException {
        return "https://" + openAIConfigElement.resourceName() + ".cognitiveservices.azure.com/openai/deployments/" + openAIConfigElement.deployment() + "/images/edits?api-version=" + openAIConfigElement.apiVersion();
    }

    public Future<GenerateImageResponse> generateImage(GenerateImageRequest request) {
        String url;
        String apiKey;
        try {
            url = getUrlToGenerateImage();
            apiKey = openAIConfigElement.apiKey();
        } catch (NotConfiguredException e) {
            return Future.failedFuture(e);
        }
        return webClient.postAbs(url)
                        .putHeader("api-key", apiKey)
                        .sendJsonObject(request.toJsonObject())
                        .compose(bufferHttpResponse -> {
                            JsonObject body = bufferHttpResponse.bodyAsJsonObject();
                            GenerateImageResponse response = new GenerateImageResponse(body);
                            return Future.succeededFuture(response);
                        });
    }

    public Future<EditImageResponse> editImage(EditImageRequest request) {
        String url;
        String apiKey;
        try {
            url = getUrlToEditImage();
            apiKey = openAIConfigElement.apiKey();
        } catch (NotConfiguredException e) {
            return Future.failedFuture(e);
        }
        return webClient
                .postAbs(url)
                .putHeader("api-key", apiKey)
                .sendMultipartForm(request.toMultipartForm())
                .compose(bufferHttpResponse -> {
                    EditImageResponse response = new EditImageResponse(bufferHttpResponse.bodyAsJsonObject());
                    return Future.succeededFuture(response);
                });
    }

}
