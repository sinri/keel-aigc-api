package io.github.sinri.keel.aigc.api.provider.volces;

import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.aigc.api.llm.sect.AbnormalResponse;
import io.github.sinri.keel.aigc.api.llm.sect.SSEUtils;
import io.github.sinri.keel.aigc.api.provider.LLMProvider;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

import java.util.function.Function;

public class VolcesProvider implements LLMProvider {
    public static final String pathOfV3ChatCompletions = "/api/v3/chat/completions";
    public static final String hostOfV3ChatCompletions = "ark.cn-beijing.volces.com";
    private final Logger logger;
    private final VolcesConfigElement configElement;

    public VolcesProvider(VolcesConfigElement configElement, Logger logger) {
        this.configElement = configElement;
        this.logger = logger;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public Future<JsonObject> request(WebClient webClient, String chatModel, JsonObject requestPayload, String requestId) {
        String url = "https://" + hostOfV3ChatCompletions + pathOfV3ChatCompletions;

        String apiKey;
        try {
            apiKey = configElement.apiKey();
            requestPayload.put("model", configElement.model(chatModel));
        } catch (NotConfiguredException e) {
            return Future.failedFuture(e);
        }

        getLogger().debug(x -> x
                .message("Start VolcesServiceMeta.request")
                .context(j -> j
                        .put("api", url)
                        .put("requestId", requestId)
                        .put("input", requestPayload)
                )
        );

        return webClient
                .postAbs(url)
                .bearerTokenAuthentication(apiKey)
                .sendJsonObject(requestPayload)
                .compose(bufferHttpResponse -> {
                    if (bufferHttpResponse.statusCode() != 200) {
                        throw new AbnormalResponse(bufferHttpResponse);
                    } else {
                        JsonObject respAsJsonObject = bufferHttpResponse.bodyAsJsonObject();
                        getLogger().debug(x -> x
                                .message("bufferHttpResponse in VolcesServiceMeta.request")
                                .context(j -> j
                                        .put("requestId", requestId)
                                        .put("output", respAsJsonObject))
                        );
                        return Future.succeededFuture(respAsJsonObject);
                    }
                });
    }

    @Override
    public Future<Void> requestStream(Vertx vertx, HttpClient httpClient, String chatModel, JsonObject requestPayload, Function<String, Future<Void>> cutterProcessFunc, long cutterTimeout, String requestId) {
        String apiKey;
        try {
            apiKey = configElement.apiKey();
            requestPayload.put("model", configElement.model(chatModel));
        } catch (NotConfiguredException e) {
            return Future.failedFuture(e);
        }
        return httpClient.request(HttpMethod.POST, 443, hostOfV3ChatCompletions, pathOfV3ChatCompletions)
                         .compose(httpClientRequest -> {
                             httpClientRequest
                                     .putHeader("Content-Type", "application/json")
                                     .putHeader("Authorization", "Bearer " + apiKey);
                             return httpClientRequest
                                     .send(requestPayload.toString());
                         })
                         .compose(httpClientResponse -> {
                             return SSEUtils.processStreamWithCutter(
                                     vertx,
                                     httpClientResponse,
                                     fragment -> {
                                         getLogger().debug("[" + requestId + "] sse fragment: \n" + fragment);
                                         return cutterProcessFunc.apply(fragment);
                                     },
                                     cutterTimeout
                             );
                         });
    }


}
