package io.github.sinri.keel.llm.api.sect.provider.dashscope;

import io.github.sinri.keel.llm.api.sect.AbnormalResponse;
import io.github.sinri.keel.llm.api.sect.SSEUtils;
import io.github.sinri.keel.llm.api.sect.provider.LLMProvider;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

import java.util.function.Function;

public class DashscopeProvider implements LLMProvider {
    public static final String PROVIDER_NAME = "Dashscope";
    public final static String hostOfDashscope = "dashscope.aliyuncs.com";
    public final static String pathOfDashscopeQwenTextGenerate = "/api/v1/services/aigc/text-generation/generation";
    public final static String endpointOfDashscopeQwenTextGenerate = "https://" + hostOfDashscope + pathOfDashscopeQwenTextGenerate;
    public final static String pathOfDashscopeQwenMultimodalGenerate = "/api/v1/services/aigc/multimodal-generation/generation";
    public final static String endpointOfDashscopeQwenMultimodalGenerate = "https://" + hostOfDashscope + pathOfDashscopeQwenMultimodalGenerate;

    private final Logger logger;
    private final String apiKey;

    public DashscopeProvider(String apiKey, Logger logger) {
        this.apiKey = apiKey;
        this.logger = logger;
    }

    public Logger getLogger() {
        return logger;
    }

    public Future<JsonObject> requestTextGeneration(WebClient webClient, String chatModel, JsonObject requestPayload, String requestId) {
        return request(webClient, endpointOfDashscopeQwenTextGenerate, chatModel, requestPayload, requestId);
    }

    public Future<JsonObject> requestMultimodalGeneration(WebClient webClient, String chatModel, JsonObject requestPayload, String requestId) {
        return request(webClient, endpointOfDashscopeQwenMultimodalGenerate, chatModel, requestPayload, requestId);
    }

    public Future<JsonObject> request(WebClient webClient, String endpoint, String chatModel, JsonObject requestPayload, String requestId) {
        requestPayload.put("model", chatModel);
        getLogger().debug(x -> x
                .message("Start DashscopeServiceMeta.request")
                .context(j -> j
                        .put("api", endpoint)
                        .put("requestId", requestId)
                        .put("input", requestPayload)
                )
        );

        return webClient
                .postAbs(endpoint)
                .putHeader("Content-Type", "application/json")
                .putHeader("Authorization", "Bearer " + apiKey)
                .sendJsonObject(requestPayload)
                .compose(bufferHttpResponse -> {
                    int statusCode = bufferHttpResponse.statusCode();
                    if (statusCode != 200) {
                        throw new AbnormalResponse(bufferHttpResponse);
                    }
                    JsonObject entries = bufferHttpResponse.bodyAsJsonObject();
                    getLogger().debug(x -> x
                            .message("bufferHttpResponse in DashscopeServiceMeta.request")
                            .context(j -> j
                                    .put("requestId", requestId)
                                    .put("output", entries))
                    );
                    return Future.succeededFuture(entries);
                });
    }

    public Future<Void> requestTextGenerationStream(Vertx vertx,
                                                    HttpClient httpClient,
                                                    String chatModel, JsonObject requestPayload, Function<String, Future<Void>> cutterProcessFunc, long cutterTimeout, String requestId) {
        return requestStream(vertx, httpClient, pathOfDashscopeQwenTextGenerate, chatModel, requestPayload, cutterProcessFunc, cutterTimeout, requestId);
    }

    public Future<Void> requestMultimodalGenerationStream(Vertx vertx,
                                                          HttpClient httpClient,
                                                          String chatModel, JsonObject requestPayload, Function<String, Future<Void>> cutterProcessFunc, long cutterTimeout, String requestId) {
        return requestStream(vertx, httpClient, pathOfDashscopeQwenMultimodalGenerate, chatModel, requestPayload, cutterProcessFunc, cutterTimeout, requestId);
    }

    public Future<Void> requestStream(
            Vertx vertx,
            HttpClient httpClient,
            String path,
            String chatModel,
            JsonObject requestPayload,
            Function<String, Future<Void>> cutterProcessFunc,
            long cutterTimeout,
            String requestId
    ) {
        requestPayload.put("model", chatModel);
        getLogger().info("Start DashscopeServiceMeta.requestStream", j -> j
                .put("payload", requestPayload)
                .put("requestId", requestId));

        return httpClient.request(HttpMethod.POST, 443, hostOfDashscope, path)
                         .compose(httpClientRequest -> {
                             httpClientRequest
                                     .putHeader("Content-Type", "application/json")
                                     .putHeader("Authorization", "Bearer " + apiKey)
                                     .putHeader("X-DashScope-SSE", "enable");
                             return httpClientRequest.send(requestPayload.toString());
                         })
                         .compose(httpClientResponse -> {
                             int statusCode = httpClientResponse.statusCode();
                             getLogger().debug("stream resp code: " + statusCode);
                             return SSEUtils.processStreamWithCutter(
                                     vertx,
                                     httpClientResponse,
                                     chunk -> {
                                         getLogger().debug("sse chunk:", c -> c
                                                 .put("chunk", chunk)
                                                 .put("request_id", requestId));
                                         return cutterProcessFunc.apply(chunk);
                                     },
                                     cutterTimeout
                             );
                         });
    }

}
