package io.github.sinri.keel.aigc.api.vgm.dalle.v3;

import io.github.sinri.keel.aigc.api.llm.sect.AbnormalResponse;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.OpenAIModelConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

/**
 * Dalle3 图像生成工具类。
 * <p>
 * 用于通过 Azure OpenAI 服务生成图像。
 *
 * @since 5.0.0
 */
public class Dalle3Kit {

    private final WebClient webClient;
    private final Logger logger;
    private final OpenAIModelConfigElement openAIConfigElement;

    /**
     * 构造函数。
     *
     * @param openAIConfigElement OpenAI 模型配置元素
     * @param webClient           Web 客户端
     * @param logger              日志记录器
     */
    public Dalle3Kit(
            OpenAIModelConfigElement openAIConfigElement,
            WebClient webClient,
            Logger logger
    ) {
        this.openAIConfigElement = openAIConfigElement;
        this.webClient = webClient;
        this.logger = logger;
    }

    /**
     * 获取日志记录器。
     *
     * @return 日志记录器实例
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * 生成图像（使用 JSON 对象参数）。
     *
     * @param parameters 参数对象
     * @param requestId  请求 ID
     * @return 响应结果的 Future
     */
    public Future<JsonObject> draw(JsonObject parameters, String requestId) {
        return request(parameters, requestId);
    }

    /**
     * 生成图像（使用 Dalle3Request 参数）。
     *
     * @param parameters Dalle3 请求对象
     * @param requestId  请求 ID
     * @return Dalle3 响应的 Future
     */
    public Future<Dalle3Response> draw(Dalle3Request parameters, String requestId) {
        return this.draw(parameters.cloneAsJsonObject(), requestId)
                   .compose(jsonObject -> Future.succeededFuture(Dalle3Response.wrap(jsonObject)));
    }

    /**
     * 生成图像（使用处理器函数）。
     *
     * @param parametersHandler 参数处理器
     * @param requestId         请求 ID
     * @return Dalle3 响应的 Future
     */
    public Future<Dalle3Response> draw(Handler<Dalle3Request> parametersHandler, String requestId) {
        Dalle3Request parameters = Dalle3Request.create();
        parametersHandler.handle(parameters);
        return this.draw(parameters, parameters.getRequestId());
    }

    /**
     * 生成 Azure OpenAI 服务主机名。
     *
     * @return 主机名
     */
    private String generateHost(String resourceName) {
        return resourceName + ".openai.azure.com";
    }

    /**
     * 生成 API 路径。
     *
     * @param api API 路径（以 / 开头）
     * @return 完整 URI
     */
    private String generateUri(String deployment, String api, String apiVersion) {
        return "/openai/deployments/" + deployment + api + "?api-version=" + apiVersion;
    }

    /**
     * 生成完整请求 URL。
     *
     * @param api API 路径（以 / 开头）
     * @return 完整 URL
     */
    private String generateUrl(OpenAIModelConfigElement configElement, String api) throws NotConfiguredException {
        return "https://" + generateHost(configElement.resourceName()) + generateUri(configElement.deployment(), api, configElement.apiVersion());
    }

    /**
     * 发起图像生成请求。
     *
     * @param requestPayload 请求负载
     * @param requestId      请求 ID
     * @return 响应结果的 Future
     */
    private Future<JsonObject> request(JsonObject requestPayload, String requestId) {
        OpenAIModelConfigElement configElement = this.openAIConfigElement;

        String url;
        String apiKey;
        try {
            apiKey = configElement.apiKey();
            url = generateUrl(configElement, "/images/generations");
        } catch (NotConfiguredException e) {
            return Future.failedFuture(e);
        }

        requestPayload.remove("request_id");

        getLogger().debug(x -> x
                .message("Start AzureOpenAIServiceMeta.request")
                .context(
                        j -> j
                                .put("api", url)
                                .put("input", requestPayload)
                                .put("requestId", requestId)
                )
        );


        return webClient
                .postAbs(url)
                .putHeader("Content-Type", "application/json")
                .putHeader("api-key", apiKey)
                .sendJsonObject(requestPayload)
                .compose(bufferHttpResponse -> {
                    JsonObject entries = bufferHttpResponse.bodyAsJsonObject();
                    if (bufferHttpResponse.statusCode() != 200 || entries == null) {
                        throw new AbnormalResponse(bufferHttpResponse);
                    }
                    getLogger().debug(x -> x
                            .message("bufferHttpResponse in AzureOpenAIServiceMeta.request")
                            .context(j -> j
                                    .put("output", entries)
                                    .put("requestId", requestId)
                            )
                    );
                    return Future.succeededFuture(entries);
                });
    }

    /**
     * Dalle3 图像尺寸枚举。
     */
    public enum Dalle3Size {
        /**
         * 横向（1792x1024）。
         */
        LANDSCAPE("1792x1024"),

        /**
         * 正方形（1024x1024）。
         */
        SQUARE("1024x1024"),

        /**
         * 纵向（1024x1792）。
         */
        PORTRAIT("1024x1792");
        private final String size;

        Dalle3Size(String size) {
            this.size = size;
        }

        /**
         * 获取尺寸字符串。
         *
         * @return 尺寸字符串
         */
        public String size() {
            return size;
        }
    }

    /**
     * Dalle3 图像质量枚举。
     */
    public enum Dalle3Quality {
        /**
         * 高清质量。
         */
        hd,
        /**
         * 标准质量。
         */
        standard
    }

    /**
     * Dalle3 图像风格枚举。
     */
    public enum Dalle3Style {
        /**
         * 自然风格。
         */
        natural,
        /**
         * 生动风格。
         */
        vivid
    }

}
