package io.github.sinri.keel.aigc.api.provider.dashscope;

import io.github.sinri.keel.aigc.api.llm.sect.AbnormalResponse;
import io.github.sinri.keel.aigc.api.llm.sect.SSEUtils;
import io.github.sinri.keel.aigc.api.provider.LLMProvider;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

import java.util.function.Function;

/**
 * Dashscope（阿里云通义千问）提供者实现。
 * <p>
 * 支持文本生成和多模态生成两种类型的请求。
 *
 * @since 5.0.0
 */
public class DashscopeProvider implements LLMProvider {
    /**
     * 提供者名称。
     */
    public static final String PROVIDER_NAME = "Dashscope";
    /**
     * Dashscope 服务主机名。
     */
    public final static String hostOfDashscope = "dashscope.aliyuncs.com";
    /**
     * 文本生成 API 路径。
     */
    public final static String pathOfDashscopeQwenTextGenerate = "/api/v1/services/aigc/text-generation/generation";
    /**
     * 文本生成 API 端点。
     */
    public final static String endpointOfDashscopeQwenTextGenerate = "https://" + hostOfDashscope + pathOfDashscopeQwenTextGenerate;
    /**
     * 多模态生成 API 路径。
     */
    public final static String pathOfDashscopeQwenMultimodalGenerate = "/api/v1/services/aigc/multimodal-generation/generation";
    /**
     * 多模态生成 API 端点。
     */
    public final static String endpointOfDashscopeQwenMultimodalGenerate = "https://" + hostOfDashscope + pathOfDashscopeQwenMultimodalGenerate;

    private final Logger logger;
    private final String apiKey;

    /**
     * 构造函数。
     *
     * @param apiKey API 密钥
     * @param logger 日志记录器
     */
    public DashscopeProvider(String apiKey, Logger logger) {
        this.apiKey = apiKey;
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
     * 判断是否为视觉相关请求（多模态请求）。
     *
     * @param requestPayload 请求负载
     * @return 如果是视觉相关请求返回 true，否则返回 false
     */
    private boolean isVisionSpecificRequest(JsonObject requestPayload) {
        try {
            JsonArray messages = requestPayload.getJsonObject("input").getJsonArray("messages");
            for (var message : messages) {
                if (message instanceof JsonObject j) {
                    JsonArray jsonArray = j.getJsonArray("content");
                    if (jsonArray != null && !jsonArray.isEmpty()) {
                        return true;
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    /**
     * 发起同步请求。
     *
     * @param webClient      Web 客户端
     * @param chatModel      聊天模型名称
     * @param requestPayload 请求负载
     * @param requestId      请求 ID
     * @return 响应结果的 Future
     */
    @Override
    public Future<JsonObject> request(WebClient webClient, String chatModel, JsonObject requestPayload, String requestId) {
        requestPayload.put("model", chatModel);

        String endpoint = isVisionSpecificRequest(requestPayload) ? endpointOfDashscopeQwenMultimodalGenerate : endpointOfDashscopeQwenTextGenerate;

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

    /**
     * 发起流式请求。
     *
     * @param vertx            Vert.x 实例
     * @param httpClient       HTTP 客户端
     * @param chatModel        聊天模型名称
     * @param requestPayload   请求负载
     * @param cutterProcessFunc SSE 数据处理函数
     * @param cutterTimeout    超时时间（毫秒）
     * @param requestId        请求 ID
     * @return 完成状态的 Future
     */
    @Override
    public Future<Void> requestStream(
            Vertx vertx,
            HttpClient httpClient,
            String chatModel,
            JsonObject requestPayload,
            Function<String, Future<Void>> cutterProcessFunc,
            long cutterTimeout,
            String requestId
    ) {
        requestPayload.put("model", chatModel);

        String path = isVisionSpecificRequest(requestPayload) ? pathOfDashscopeQwenMultimodalGenerate : pathOfDashscopeQwenTextGenerate;

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
