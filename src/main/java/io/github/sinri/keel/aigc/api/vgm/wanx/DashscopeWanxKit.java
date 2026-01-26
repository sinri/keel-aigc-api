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


public class DashscopeWanxKit {
    private final static String endpointOfDashscopeAsyncTaskQuery = "https://dashscope.aliyuncs.com/api/v1/tasks/";//{task_id}
    private final static String endpointOfDashscopeWanxiangImageSynthesis = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text2image/image-synthesis";

    private final WebClient webClient;
    private final String apiKey;
    private final Logger logger;

    public DashscopeWanxKit(String apiKey, WebClient webClient, Logger logger) {
        this.apiKey = apiKey;
        this.webClient = webClient;
        this.logger = logger;
    }

    public Logger getLogger() {
        return logger;
    }

    public Future<JsonObject> createImageSynthesisTask(JsonObject requestBody, String requestId) {
        return callWanxiangImageSynthesis(requestBody, requestId);
    }

    public Future<WanxImageSynthesisAsyncTaskCreateResult> createImageSynthesisTask(WanxImageSynthesisRequest request, String requestId) {
        return createImageSynthesisTask(request.toJsonObject(), requestId)
                .compose(j -> {
                    var r = WanxImageSynthesisAsyncTaskCreateResult.wrap(j);
                    return Future.succeededFuture(r);
                });
    }

    public Future<WanxImageSynthesisAsyncTaskResult> queryImageSynthesisTaskStatus(String taskId, String requestId) {
        return callAsyncTaskQuery(taskId, requestId)
                .compose(resp -> {
                    var x = new WanxImageSynthesisAsyncTaskResult(resp);
                    return Future.succeededFuture(x);
                });
    }

    /**
     * @since 1.1.6
     */
    private Future<JsonObject> callWanxiangImageSynthesis(JsonObject requestBody, String requestId) {
        return request(
                endpointOfDashscopeWanxiangImageSynthesis,
                Map.of("X-DashScope-Async", "enable"),
                requestBody,
                requestId
        );
    }

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

    private Future<JsonObject> request(String api, JsonObject requestBody, String requestId) {
        return this.request(api, Map.of(), requestBody, requestId);
    }

    /**
     * @since 1.1.6
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
