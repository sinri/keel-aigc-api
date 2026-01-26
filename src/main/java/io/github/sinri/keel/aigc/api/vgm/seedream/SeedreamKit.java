package io.github.sinri.keel.aigc.api.vgm.seedream;

import io.github.sinri.keel.aigc.api.provider.volces.VolcesConfigElement;
import io.github.sinri.keel.aigc.api.vgm.seedream.request.Seedream4Request;
import io.github.sinri.keel.aigc.api.vgm.seedream.request.SeedreamRequest;
import io.github.sinri.keel.aigc.api.vgm.seedream.response.SeedreamEvent;
import io.github.sinri.keel.aigc.api.vgm.seedream.response.SeedreamFragment;
import io.github.sinri.keel.aigc.api.vgm.seedream.response.SeedreamResponse;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.core.cutter.IntravenouslyCutterOnString;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;


/**
 * Seedream 图像生成工具类。
 * <p>
 * 用于通过火山引擎的 Seedream 服务生成图像，支持同步和流式请求。
 *
 * @since 5.0.0
 */
public class SeedreamKit {
    /**
     * Seedream API 端点 URL。
     */
    public static final String url = "https://ark.cn-beijing.volces.com/api/v3/images/generations";
    private final VolcesConfigElement volcesConfigElement;

    /**
     * 构造函数。
     *
     * @param volcesConfigElement 火山引擎配置元素
     */
    public SeedreamKit(VolcesConfigElement volcesConfigElement) {
        this.volcesConfigElement = volcesConfigElement;
    }

    /**
     * 生成图像（同步请求）。
     *
     * @param webClient       Web 客户端
     * @param modelCode       模型代码
     * @param seedreamRequest Seedream 请求对象
     * @param requestId       请求 ID
     * @return Seedream 响应的 Future
     */
    public Future<SeedreamResponse> seedream(WebClient webClient, String modelCode, SeedreamRequest<?> seedreamRequest, String requestId) {
        String modelId;
        String apiKey;
        try {
            modelId = this.volcesConfigElement.model(modelCode);
            apiKey = this.volcesConfigElement.apiKey();
        } catch (NotConfiguredException e) {
            return Future.failedFuture(e);
        }

        seedreamRequest.setModel(modelId);

        return webClient.postAbs(url)
                        .bearerTokenAuthentication(apiKey)
                        .sendJsonObject(seedreamRequest.toJsonObject())
                        .compose(bufferHttpResponse -> {
                            int statusCode = bufferHttpResponse.statusCode();
                            if (statusCode != 200)
                                throw new RuntimeException("Seedream API returned status code %d".formatted(statusCode));
                            JsonObject extract = bufferHttpResponse.bodyAsJsonObject();
                            if (extract == null) throw new RuntimeException("Seedream API returned null response");
                            SeedreamResponse seedreamResponse = SeedreamResponse.wrap(extract);
                            return Future.succeededFuture(seedreamResponse);
                        });
    }

    /**
     * 使用 Seedream4 生成图像（使用处理器函数）。
     *
     * @param webClient              Web 客户端
     * @param modelCode              模型代码
     * @param seedream4RequestHandler Seedream4 请求处理器
     * @param requestId              请求 ID
     * @return Seedream 响应的 Future
     */
    public Future<SeedreamResponse> seedream4(WebClient webClient, String modelCode, Handler<Seedream4Request> seedream4RequestHandler, String requestId) {
        Seedream4Request seedream4Request = Seedream4Request.create();
        seedream4RequestHandler.handle(seedream4Request);
        return seedream(webClient, modelCode, seedream4Request, requestId);
    }

    /**
     * 使用 Seedream4 生成图像（流式请求，使用请求对象）。
     *
     * @param httpClient        HTTP 客户端
     * @param modelCode         模型代码
     * @param seedream4Request  Seedream4 请求对象
     * @param fragmentProcessor 片段处理器
     * @param timeout           超时时间（毫秒）
     * @param requestId         请求 ID
     * @return 完成状态的 Future
     */
    public Future<Void> seedream4stream(
            HttpClient httpClient,
            String modelCode,
            Seedream4Request seedream4Request,
            Function<String, Future<Void>> fragmentProcessor,
            long timeout,
            String requestId
    ) {
        String modelId;
        String apiKey;
        try {
            modelId = this.volcesConfigElement.model(modelCode);
            apiKey = this.volcesConfigElement.apiKey();
        } catch (NotConfiguredException e) {
            return Future.failedFuture(e);
        }
        seedream4Request.setModel(modelId);
        seedream4Request.setStream(true);

        IntravenouslyCutterOnString cutter = new IntravenouslyCutterOnString(
                fragment -> {
                    // System.out.println(fragment);
                    fragmentProcessor.apply(fragment);
                    return Future.succeededFuture();
                },
                timeout
        );

        return httpClient.request(new RequestOptions()
                                 .setMethod(HttpMethod.POST)
                                 .setAbsoluteURI(url)
                         )
                         .compose(httpClientRequest -> {
                             httpClientRequest.putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
                             httpClientRequest.putHeader("Authorization", "Bearer " + apiKey);
                             httpClientRequest.setChunked(true);
                             return httpClientRequest.send(seedream4Request.toJsonObject().toBuffer());
                         })
                         .compose(httpClientResponse -> {
                             httpClientResponse.handler(buffer -> {
                                                   if (buffer != null) {
                                                       String s = buffer.toString(StandardCharsets.UTF_8);
                                                       if (!Objects.equals("data: [DONE]", s)) {
                                                           cutter.acceptFromStream(buffer);
                                                       }
                                                   }
                                               })
                                               .endHandler(end -> {
                                                   cutter.stopHere();
                                               })
                                               .exceptionHandler(throwable -> {
                                                   cutter.stopHere(throwable);
                                               });
                             return cutter.waitForAllHandled();
                         });
    }

    /**
     * 使用 Seedream4 生成图像（流式请求，使用处理器函数）。
     *
     * @param httpClient              HTTP 客户端
     * @param modelCode               模型代码
     * @param seedream4RequestHandler Seedream4 请求处理器
     * @param fragmentProcessor      片段处理器
     * @param timeout                超时时间（毫秒）
     * @param requestId              请求 ID
     * @return 完成状态的 Future
     */
    public Future<Void> seedream4stream(
            HttpClient httpClient,
            String modelCode,
            Handler<Seedream4Request> seedream4RequestHandler,
            Function<String, Future<Void>> fragmentProcessor,
            long timeout,
            String requestId
    ) {
        Seedream4Request seedream4Request = Seedream4Request.create();
        seedream4RequestHandler.handle(seedream4Request);
        return this.seedream4stream(httpClient,modelCode, seedream4Request, fragmentProcessor,timeout, requestId);
    }

    /**
     * 使用 Seedream4 生成图像（流式请求，返回事件列表，使用请求对象）。
     *
     * @param httpClient       HTTP 客户端
     * @param modelCode        模型代码
     * @param seedream4Request Seedream4 请求对象
     * @param timeout          超时时间（毫秒）
     * @param requestId        请求 ID
     * @return Seedream 事件列表的 Future
     */
    public Future<List<SeedreamEvent>> seedream4stream(
            HttpClient httpClient,
            String modelCode,
            Seedream4Request seedream4Request,
            long timeout,
            String requestId
    ) {
        List<SeedreamEvent> list = new ArrayList<>();
        return this.seedream4stream(httpClient,modelCode, seedream4Request, s -> {
                       var f = new SeedreamFragment(s);
                       var se = f.toSeedreamEvent();
                       list.add(se);
                       return Future.succeededFuture();
                   },timeout, requestId)
                   .compose(v -> {
                       return Future.succeededFuture(list);
                   });
    }

    /**
     * 使用 Seedream4 生成图像（流式请求，返回事件列表，使用处理器函数）。
     *
     * @param httpClient              HTTP 客户端
     * @param modelCode               模型代码
     * @param seedream4RequestHandler Seedream4 请求处理器
     * @param timeout                超时时间（毫秒）
     * @param requestId              请求 ID
     * @return Seedream 事件列表的 Future
     */
    public Future<List<SeedreamEvent>> seedream4stream(
            HttpClient httpClient,
            String modelCode,
            Handler<Seedream4Request> seedream4RequestHandler,
            long timeout,
            String requestId
    ) {
        Seedream4Request seedream4Request = Seedream4Request.create();
        seedream4RequestHandler.handle(seedream4Request);
        return this.seedream4stream(httpClient,modelCode, seedream4Request, timeout,requestId);
    }
}
