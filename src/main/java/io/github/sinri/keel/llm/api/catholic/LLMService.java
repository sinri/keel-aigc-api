package io.github.sinri.keel.llm.api.catholic;

import io.github.sinri.keel.base.async.Keel;
import io.github.sinri.keel.llm.api.catholic.request.MixChatRequest;
import io.github.sinri.keel.llm.api.catholic.response.MixChatResponse;
import io.github.sinri.keel.llm.api.catholic.response.stream.MixChatResponseBuffer;
import io.github.sinri.keel.llm.api.catholic.response.stream.MixChatResponseChunk;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

import java.util.function.Function;

/**
 * 大语言模型服务接口，定义了与大语言模型交互的基本操作。
 */
public interface LLMService {
    Future<MixChatResponse> request(MixChatRequest request);

    Future<Void> requestStreamRaw(MixChatRequest request,
                                  Function<JsonObject, Future<Void>> fragmentDataHandler);


    Future<MixChatResponse> requestStreamRaw(MixChatRequest request);

    Future<Void> requestStream(MixChatRequest request, Function<MixChatResponseChunk, Future<Void>> chunkHandler);

    default Future<MixChatResponse> requestStream(MixChatRequest request) {
        MixChatResponseBuffer buffer = new MixChatResponseBuffer();
        return this.requestStream(request, chunk -> {
                       buffer.accept(chunk);
                       return Future.succeededFuture();
                   })
                   .compose(v -> {
                       return Future.succeededFuture(buffer.build());
                   });
    }

    Keel getKeel();

    WebClient getWebClient();

    HttpClient getHttpClient();

    Logger getLogger();
}
