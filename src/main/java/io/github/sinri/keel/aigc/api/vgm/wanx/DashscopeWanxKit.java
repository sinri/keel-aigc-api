package io.github.sinri.keel.aigc.api.vgm.wanx;

import io.github.sinri.keel.aigc.api.internal.vgm.wanx.WanxImageSynthesisAsyncTaskResult;
import io.github.sinri.keel.aigc.api.llm.sect.AbnormalResponse;
import io.github.sinri.keel.aigc.api.vgm.wanx.ImageSynthesis.request.WanxImageSynthesisRequest;
import io.github.sinri.keel.aigc.api.vgm.wanx.ImageSynthesis.response.WanxImageSynthesisAsyncTaskCreateResult;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

import java.util.Map;

/**
 * Dashscope 万相图像合成工具类。
 * <p>
 * 用于通过阿里云 Dashscope 服务进行图像合成，支持异步任务创建和查询。
 *
 * @since 5.0.0
 */
public class DashscopeWanxKit {
    /**
     * Dashscope 异步任务查询端点。
     */
    private final static String endpointOfDashscopeAsyncTaskQuery = "https://dashscope.aliyuncs.com/api/v1/tasks/";//{task_id}
    /**
     * Dashscope 万相图像合成端点。
     */
    private final static String endpointOfDashscopeWanxiangImageSynthesis = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text2image/image-synthesis";

    private final WebClient webClient;
    private final String apiKey;
    private final Logger logger;

    /**
     * 构造函数。
     *
     * @param apiKey   API 密钥
     * @param webClient Web 客户端
     * @param logger   日志记录器
     */
    public DashscopeWanxKit(String apiKey, WebClient webClient, Logger logger) {
        this.apiKey = apiKey;
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
     * 创建图像合成任务（使用 JSON 对象）。
     *
     * @param requestBody 请求体
     * @param requestId   请求 ID
     * @return 响应结果的 Future
     */
    public Future<JsonObject> createImageSynthesisTask(JsonObject requestBody, String requestId) {
        return callWanxiangImageSynthesis(requestBody, requestId);
    }

    /**
     * 创建图像合成任务（使用请求对象）。
     *
     * @param request   万相图像合成请求对象
     * @param requestId 请求 ID
     * @return 万相图像合成异步任务创建结果的 Future
     */
    public Future<WanxImageSynthesisAsyncTaskCreateResult> createImageSynthesisTask(WanxImageSynthesisRequest request, String requestId) {
        return createImageSynthesisTask(request.toJsonObject(), requestId)
                .compose(j -> {
                    var r = WanxImageSynthesisAsyncTaskCreateResult.wrap(j);
                    return Future.succeededFuture(r);
                });
    }

    /**
     * 查询图像合成任务状态。
     *
     * @param taskId    任务 ID
     * @param requestId 请求 ID
     * @return 万相图像合成异步任务结果的 Future
     */
    public Future<WanxImageSynthesisAsyncTaskResult> queryImageSynthesisTaskStatus(String taskId, String requestId) {
        return callAsyncTaskQuery(taskId, requestId)
                .compose(resp -> {
                    var x = new WanxImageSynthesisAsyncTaskResult(resp);
                    return Future.succeededFuture(x);
                });
    }

    /**
     * 调用万相图像合成 API。
     *
     * @param requestBody 请求体
     * @param requestId   请求 ID
     * @return 响应结果的 Future
     */
    private Future<JsonObject> callWanxiangImageSynthesis(JsonObject requestBody, String requestId) {
        return request(
                endpointOfDashscopeWanxiangImageSynthesis,
                Map.of("X-DashScope-Async", "enable"),
                requestBody,
                requestId
        );
    }

    /**
     * 调用异步任务查询 API。
     *
     * @param taskId    任务 ID
     * @param requestId 请求 ID
     * @return 响应结果的 Future
     */
    private Future<JsonObject> callAsyncTaskQuery(String taskId, String requestId) {
        return webClient
                .getAbs(endpointOfDashscopeAsyncTaskQuery + taskId)
                .putHeader("Authorization", "Bearer " + apiKey)
                .send()
                .compose(resp -> {
                    var r = resp.bodyAsJsonObject();
                    return Future.succeededFuture(r);
                });
    }

    /**
     * 发起请求（无额外请求头）。
     *
     * @param api         API 端点
     * @param requestBody 请求体
     * @param requestId   请求 ID
     * @return 响应结果的 Future
     */
    private Future<JsonObject> request(String api, JsonObject requestBody, String requestId) {
        return this.request(api, Map.of(), requestBody, requestId);
    }

    /**
     * 发起请求（带额外请求头）。
     *
     * @param api         API 端点
     * @param headers     额外请求头
     * @param requestBody 请求体
     * @param requestId   请求 ID
     * @return 响应结果的 Future
     */
    private Future<JsonObject> request(String api, Map<String, String> headers, JsonObject requestBody, String requestId) {
        getLogger().debug(x -> x
                .message("Start DashscopeWanxKit.request")
                .context(j -> j
                        .put("api", api)
                        .put("requestId", requestId)
                        .put("input", requestBody)
                )
        );

        var req = webClient.postAbs(api)
                           .putHeader("Content-Type", "application/json")
                           .putHeader("Authorization", "Bearer " + apiKey);

        headers.forEach(req::putHeader);

        return req.sendJsonObject(requestBody)
                  .compose(bufferHttpResponse -> {
                      int statusCode = bufferHttpResponse.statusCode();
                      if (statusCode != 200) {
                          getLogger().error(x -> x
                                  .message("Unexpected bufferHttpResponse in DashscopeServiceMeta.request")
                                  .context(
                                          j -> j
                                                  .put("requestId", requestId)
                                                  .put("status_code", statusCode)
                                                  .put("detail", bufferHttpResponse.bodyAsString())
                                  )
                          );

                          return Future.failedFuture(new AbnormalResponse(bufferHttpResponse));
                      } else {
                          JsonObject entries = bufferHttpResponse.bodyAsJsonObject();

                          getLogger().debug(x -> x
                                  .message("bufferHttpResponse in DashscopeServiceMeta.request")
                                  .context(j -> j
                                          .put("requestId", requestId)
                                          .put("output", entries))
                          );

                          return Future.succeededFuture(entries);
                      }
                  });
    }

}
