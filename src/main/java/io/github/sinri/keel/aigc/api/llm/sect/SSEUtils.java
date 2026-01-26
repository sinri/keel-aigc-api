package io.github.sinri.keel.aigc.api.llm.sect;

import io.github.sinri.keel.core.cutter.IntravenouslyCutterOnString;
import io.github.sinri.keel.core.servant.intravenous.Intravenous;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

public class SSEUtils {
    /**
     * 使用流式响应处理器处理HTTP请求。
     *
     * @param requestFunction   用于创建HTTP请求的函数
     * @param cutterProcessFunc 处理流式响应片段的函数
     * @param cutterTimeout     流式响应处理超时时间(毫秒)
     * @return 处理完成的Future
     * @since 2.0.0
     */
    @Deprecated(forRemoval = true)
    public static Future<Void> callStreamWithCutter(
            Vertx vertx,
            HttpClient httpClient,
            Function<HttpClient, Future<HttpClientResponse>> requestFunction,
            Function<String, Future<Void>> cutterProcessFunc,
            long cutterTimeout
    ) {
        return Future.succeededFuture()
                     .compose(v -> requestFunction.apply(httpClient))
                     .compose(httpClientResponse -> {
                         IntravenouslyCutterOnString cutter = new IntravenouslyCutterOnString(
                                 new Intravenous.SingleDropProcessor<String>() {
                                     @Override
                                     public Future<Void> process(String s) {
                                         return cutterProcessFunc.apply(s);
                                     }
                                 },
                                 cutterTimeout
                         );
                         return cutter.deployMe(vertx, new DeploymentOptions())
                                      .compose(deploymentId -> {
                                          httpClientResponse
                                                  .handler(cutter::acceptFromStream)
                                                  .endHandler(ended -> cutter.stopHere())
                                                  .exceptionHandler(cutter::stopHere);
                                          return cutter.waitForAllHandled();
                                      });

                     });
    }

    public static Future<Void> processStreamWithCutter(
            Vertx vertx,
            HttpClientResponse httpClientResponse,
            Function<String, Future<Void>> cutterProcessFunc,
            long cutterTimeout
    ) {
        IntravenouslyCutterOnString cutter = new IntravenouslyCutterOnString(
                cutterProcessFunc::apply,
                cutterTimeout
        );
        return cutter.deployMe(vertx, new DeploymentOptions())
                     .compose(deploymentId -> {
                         httpClientResponse
                                 .handler(cutter::acceptFromStream)
                                 .endHandler(ended -> cutter.stopHere())
                                 .exceptionHandler(cutter::stopHere);
                         return cutter.waitForAllHandled();
                     });
    }

    /**
     * 从流式响应片段中提取数据部分。
     *
     * @param fragment 流式响应的一个片段，通常包含 "data: " 前缀的数据行
     * @return 提取出的数据内容，如果没有找到数据则返回 null
     */
    public static @Nullable String extractFragmentData(String fragment) {
        var lines = fragment.split("[\r\n]+");
        for (var line : lines) {
            var pair = line.split(":\\s*", 2);
            if (pair.length == 2) {
                if (Objects.equals(pair[0], "data")) {
                    return pair[1];
                }
            }
        }
        return null;
    }
}
