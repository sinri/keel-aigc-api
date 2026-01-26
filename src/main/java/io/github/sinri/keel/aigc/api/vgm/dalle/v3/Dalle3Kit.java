package io.github.sinri.keel.aigc.api.vgm.dalle.v3;

import io.github.sinri.keel.aigc.api.llm.sect.AbnormalResponse;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.OpenAIModelConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

public class Dalle3Kit {

    private final WebClient webClient;
    private final Logger logger;
    private final OpenAIModelConfigElement openAIConfigElement;

    public Dalle3Kit(
            OpenAIModelConfigElement openAIConfigElement,
            WebClient webClient,
            Logger logger
    ) {
        this.openAIConfigElement = openAIConfigElement;
        this.webClient = webClient;
        this.logger = logger;
    }

    public Logger getLogger() {
        return logger;
    }

    public Future<JsonObject> draw(JsonObject parameters, String requestId) {
        return request(parameters, requestId);
    }

    public Future<Dalle3Response> draw(Dalle3Request parameters, String requestId) {
        return this.draw(parameters.cloneAsJsonObject(), requestId)
                   .compose(jsonObject -> Future.succeededFuture(Dalle3Response.wrap(jsonObject)));
    }

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

    public enum Dalle3Size {
        LANDSCAPE("1792x1024"),

        SQUARE("1024x1024"),

        PORTRAIT("1024x1792");
        private final String size;

        Dalle3Size(String size) {
            this.size = size;
        }

        public String size() {
            return size;
        }
    }

    public enum Dalle3Quality {
        hd, standard
    }

    public enum Dalle3Style {
        natural, vivid
    }

}
