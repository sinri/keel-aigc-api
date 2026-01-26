package io.github.sinri.keel.aigc.api.provider;

import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

import java.util.function.Function;

/**
 * LLM 提供者接口，用于向不同的 LLM 服务发送请求。
 * <p>
 * 实现此接口的类负责处理与特定 LLM 服务的通信，包括同步请求和流式请求。
 *
 * @since 5.0.0
 */
public interface LLMProvider {
    /**
     * 获取日志记录器。
     *
     * @return 日志记录器实例
     */
    Logger getLogger();

    /**
     * 发起同步请求。
     *
     * @param webClient       Web 客户端
     * @param chatModel       聊天模型名称
     * @param requestPayload  请求负载
     * @param requestId       请求 ID
     * @return 响应结果的 Future
     */
    Future<JsonObject> request(WebClient webClient, String chatModel, JsonObject requestPayload, String requestId);

    /**
     * 发起流式请求。
     *
     * @param vertx            Vert.x 实例
     * @param httpClient      HTTP 客户端
     * @param chatModel       聊天模型名称
     * @param requestPayload  请求负载
     * @param cutterProcessFunc SSE 数据处理函数
     * @param cutterTimeout   超时时间（毫秒）
     * @param requestId        请求 ID
     * @return 完成状态的 Future
     */
    Future<Void> requestStream(
            Vertx vertx,
            HttpClient httpClient,
            String chatModel,
            JsonObject requestPayload,
            Function<String, Future<Void>> cutterProcessFunc,
            long cutterTimeout,
            String requestId
    );
}
