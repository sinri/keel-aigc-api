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

/**
 * 火山引擎（Volces）提供者实现。
 * <p>
 * 用于与火山引擎的 LLM 服务进行通信。
 *
 * @since 5.0.0
 */
public class VolcesProvider implements LLMProvider {
    /**
     * V3 聊天补全 API 路径。
     */
    public static final String pathOfV3ChatCompletions = "/api/v3/chat/completions";
    /**
     * V3 聊天补全服务主机名。
     */
    public static final String hostOfV3ChatCompletions = "ark.cn-beijing.volces.com";
    private final Logger logger;
    private final VolcesConfigElement configElement;

    /**
     * 构造函数。
     *
     * @param configElement 配置元素
     * @param logger        日志记录器
     */
    public VolcesProvider(VolcesConfigElement configElement, Logger logger) {
        this.configElement = configElement;
        this.logger = logger;
    }

    /**
     * 获取日志记录器。
     *
     * @return 日志记录器实例
     */
    @Override
    public Logger getLogger() {
        return logger;
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
