package io.github.sinri.keel.aigc.api.llm.sect;

import io.github.sinri.keel.core.cutter.IntravenouslyCutterOnString;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientResponse;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

public class SSEUtils {
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
