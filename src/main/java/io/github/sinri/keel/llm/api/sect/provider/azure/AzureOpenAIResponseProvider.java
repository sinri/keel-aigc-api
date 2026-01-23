package io.github.sinri.keel.llm.api.sect.provider.azure;

import io.github.sinri.keel.base.annotations.TechnicalPreview;
import io.github.sinri.keel.llm.api.sect.dialect.openai.OpenAIConfigElement;
import io.github.sinri.keel.llm.api.sect.dialect.openai.OpenAIModelConfigElement;
import io.github.sinri.keel.llm.api.sect.provider.LLMProvider;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@TechnicalPreview
public class AzureOpenAIResponseProvider implements LLMProvider {
    private final Logger logger;
    private final Map<String, OpenAIModelConfigElement> modelConfigElementMap;

    public AzureOpenAIResponseProvider(OpenAIConfigElement openAIConfigElement, Logger logger) {
        this.logger = logger;
        this.modelConfigElementMap = openAIConfigElement.getModelMap();
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public Future<JsonObject> request(WebClient webClient, String chatModel, JsonObject requestPayload, String requestId) {
        String apiVersion = "preview";
        String apiKey;
        String resourceName;
        String deployment;
        try {
            OpenAIModelConfigElement openAIModelConfigElement = modelConfigElementMap.get(Objects.requireNonNull(chatModel));
            apiKey = openAIModelConfigElement.apiKey();
            resourceName = openAIModelConfigElement.resourceName();
            deployment = openAIModelConfigElement.deployment();
        } catch (Throwable e) {
            return Future.failedFuture(e);
        }

        requestPayload.put("model", deployment);

        String url = "https://" + resourceName + ".openai.azure.com/openai/v1/responses?api-version=" + apiVersion;

        requestPayload.put("model", deployment);

        return webClient
                .postAbs(url)
                .putHeader("Content-Type", "application/json")
                .bearerTokenAuthentication(apiKey)
                .sendJsonObject(requestPayload)
                .compose(bufferHttpResponse -> {
                    var output = bufferHttpResponse.bodyAsJsonObject();
                    return Future.succeededFuture(output);
                });
    }

    @Override
    public Future<Void> requestStream(Vertx vertx, HttpClient httpClient, String chatModel, JsonObject requestPayload, Function<String, Future<Void>> cutterProcessFunc, long cutterTimeout, String requestId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
