package io.github.sinri.keel.aigc.api.provider.azure;

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

import java.util.Map;
import java.util.function.Function;

/**
 * Azure OpenAI 经典提供者实现。
 * <p>
 * 用于与 Azure OpenAI 服务进行通信，支持同步和流式请求。
 *
 * @since 5.0.0
 */
public class AzureOpenAIClassicProvider implements LLMProvider {
    private final Logger logger;
    private final Map<String, OpenAIChatCompletionsModelConfigElement> modelConfigElementMap;

    /**
     * 构造函数。
     *
     * @param azureOpenAIProviderConfigElement OpenAI 配置元素
     * @param logger              日志记录器
     */
    public AzureOpenAIClassicProvider(AzureOpenAIProviderConfigElement azureOpenAIProviderConfigElement, Logger logger) {
        this.logger = logger;
        this.modelConfigElementMap = azureOpenAIProviderConfigElement.getModelMap();
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
    private String generateUrl(OpenAIChatCompletionsModelConfigElement configElement, String api) throws NotConfiguredException {
        return "https://" + generateHost(configElement.resourceName()) + generateUri(configElement.deployment(), api, configElement.apiVersion());
    }

    /**
     * 发起 ChatGPT 聊天补全请求。
     *
     * @param webClient      Web 客户端
     * @param chatModel      聊天模型
     * @param requestPayload 请求体
     * @param requestId      请求 ID
     * @return 响应结果的 Future
     */
    @Override
    public Future<JsonObject> request(WebClient webClient, String chatModel, JsonObject requestPayload, String requestId) {
        OpenAIChatCompletionsModelConfigElement configElement = modelConfigElementMap.get(chatModel);

        String url;
        String apiKey;
        try {
            apiKey = configElement.apiKey();
            url = generateUrl(configElement, "/chat/completions");
        } catch (NotConfiguredException e) {
            return Future.failedFuture(e);
        }
        getLogger().info(x -> x
                .message("Start AzureOpenAIProvider.request")
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
                    getLogger().info(x -> x
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
     * 发起流式聊天补全请求。
     *
     * @param vertx            Vert.x 实例
     * @param httpClient       HTTP 客户端
     * @param chatModel        聊天模型
     * @param requestPayload   请求参数
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
        OpenAIChatCompletionsModelConfigElement configElement = modelConfigElementMap.get(chatModel);
        String deployment;
        String apiKey;
        String apiVersion;
        String host;
        String uri;
        try {
            apiKey = configElement.apiKey();
            deployment = configElement.deployment();
            apiVersion = configElement.apiVersion();
            host=generateHost(configElement.resourceName());
            uri = generateUri(deployment, "/chat/completions", apiVersion);
//            getLogger().notice("host: "+host);
//            getLogger().notice("uri: "+uri);
//            getLogger().notice("payload: ",x->x.put("payload", requestPayload));
        } catch (NotConfiguredException e) {
            throw new RuntimeException(e);
        }

        return httpClient.request(HttpMethod.POST, 443, host, uri)
                  .compose(httpClientRequest -> {
                      httpClientRequest
                              .putHeader("Content-Type", "application/json")
                              .putHeader("api-key", apiKey);
                      return httpClientRequest
                              .send(requestPayload.toString());
                  })
                  .compose(httpClientResponse -> {
//                      getLogger().notice("httpClientResponse: "+httpClientResponse.statusCode());
//                      httpClientResponse.body()
//                                                .onSuccess(buffer -> {
//                                                    getLogger().notice("buffer: "+buffer.toString());
//                                                });

                      return SSEUtils.processStreamWithCutter(
                              vertx,
                              httpClientResponse,
                              chunk -> {
                                  getLogger().debug("sse chunk", c -> c
                                          .put("chunk", chunk)
                                          .put("request_id", requestId));
                                  return cutterProcessFunc.apply(chunk);
                              },
                              cutterTimeout
                      );
                  });
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
}
