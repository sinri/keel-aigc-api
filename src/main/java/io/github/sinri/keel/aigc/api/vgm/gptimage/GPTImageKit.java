package io.github.sinri.keel.aigc.api.vgm.gptimage;

import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.OpenAIModelConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

/**
 * GPT 图像生成工具类。
 * <p>
 * 用于通过 Azure OpenAI 服务生成和编辑图像。
 *
 * @see <a href="https://learn.microsoft.com/en-us/azure/ai-services/openai/how-to/dall-e?tabs=gpt-image-1">How to
 *         use Azure OpenAI image generation models</a>
 * @since 5.0.0
 */
public class GPTImageKit {

    private final OpenAIModelConfigElement openAIConfigElement;
    private final WebClient webClient;

    /**
     * 构造函数。
     *
     * @param openAIConfigElement OpenAI 模型配置元素
     * @param webClient           Web 客户端
     */
    public GPTImageKit(OpenAIModelConfigElement openAIConfigElement, WebClient webClient) {
        this.openAIConfigElement = openAIConfigElement;
        this.webClient = webClient;
    }

    /**
     * 获取图像生成 API 的 URL。
     *
     * @return API URL
     * @throws NotConfiguredException 配置未完成时抛出
     */
    private String getUrlToGenerateImage() throws NotConfiguredException {
        return "https://" + openAIConfigElement.resourceName() + ".cognitiveservices.azure.com/openai/deployments/" + openAIConfigElement.deployment() + "/images/generations?api-version=" + openAIConfigElement.apiVersion();
    }

    /**
     * 获取图像编辑 API 的 URL。
     *
     * @return API URL
     * @throws NotConfiguredException 配置未完成时抛出
     */
    private String getUrlToEditImage() throws NotConfiguredException {
        return "https://" + openAIConfigElement.resourceName() + ".cognitiveservices.azure.com/openai/deployments/" + openAIConfigElement.deployment() + "/images/edits?api-version=" + openAIConfigElement.apiVersion();
    }

    /**
     * 生成图像。
     *
     * @param request 生成图像请求
     * @return 生成图像响应的 Future
     */
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

    /**
     * 编辑图像。
     *
     * @param request 编辑图像请求
     * @return 编辑图像响应的 Future
     */
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
