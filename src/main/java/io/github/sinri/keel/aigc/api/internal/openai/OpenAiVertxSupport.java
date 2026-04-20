package io.github.sinri.keel.aigc.api.internal.openai;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponseChunk;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * OpenAI 兼容 HTTP 客户端（Vert.x）的共用实现：JSON POST、错误体读取、按行解析的 SSE 消费与 chunk 串行处理。
 */
public final class OpenAiVertxSupport {

    private OpenAiVertxSupport() {
    }

    /**
     * {@code POST} JSON，可选 {@code Accept: text/event-stream}。
     */
    public static Future<HttpClientResponse> sendJsonPost(
        HttpClient httpClient,
        String absoluteUri,
        String apiKey,
        JsonObject requestBody,
        boolean stream
    ) {
        RequestOptions options = new RequestOptions()
            .setMethod(HttpMethod.POST)
            .setAbsoluteURI(absoluteUri)
            .putHeader("Content-Type", "application/json")
            .putHeader("Authorization", "Bearer " + apiKey);

        if (stream) {
            options.putHeader("Accept", "text/event-stream");
        }

        return httpClient.request(options)
            .compose(httpClientRequest -> sendJsonBody(httpClientRequest, requestBody));
    }

    private static Future<HttpClientResponse> sendJsonBody(HttpClientRequest httpClientRequest, JsonObject requestBody) {
        return httpClientRequest.send(Buffer.buffer(requestBody.encode()));
    }

    /**
     * 状态码 200 时返回 body，否则失败并附带响应体文本。
     */
    public static Future<Buffer> requireSuccessAndReadBody(HttpClientResponse response, String serviceName) {
        if (response.statusCode() == 200) {
            return response.body();
        }
        return response.body().compose(body -> Future.failedFuture(
            new RuntimeException(serviceName + ": " + response.statusCode() + " - " + body)
        ));
    }

    /**
     * 消费 OpenAI 风格的 SSE（按换行切分，对每行调用 {@code processSseLine}），并对非 null chunk 做串行异步处理。
     */
    public static Future<Void> consumeOpenAiStyleSseStream(
        HttpClientResponse response,
        String serviceName,
        Function<String, CatholicLLMResponseChunk> processSseLine,
        Function<CatholicLLMResponseChunk, Future<Void>> chunkAsyncProcessor
    ) {
        if (response.statusCode() != 200) {
            return requireSuccessAndReadBody(response, serviceName).mapEmpty();
        }

        Promise<Void> promise = Promise.promise();
        AtomicReference<Future<Void>> processingChain = new AtomicReference<>(Future.succeededFuture());
        StringBuilder pendingLine = new StringBuilder();

        response.exceptionHandler(promise::tryFail);
        response.handler(buffer -> {
            pendingLine.append(buffer);
            drainLines(pendingLine, line -> {
                CatholicLLMResponseChunk chunk = processSseLine.apply(line);
                enqueueChunk(processingChain, chunk, chunkAsyncProcessor, promise);
            });
        });
        response.endHandler(v -> {
            if (!promise.future().isComplete() && pendingLine.length() > 0) {
                CatholicLLMResponseChunk chunk = processSseLine.apply(normalizeLine(pendingLine.toString()));
                enqueueChunk(processingChain, chunk, chunkAsyncProcessor, promise);
            }

            processingChain.get().onComplete(ar -> {
                if (ar.failed()) {
                    promise.tryFail(ar.cause());
                } else {
                    promise.tryComplete();
                }
            });
        });
        response.resume();

        return promise.future();
    }

    private static void drainLines(StringBuilder pendingLine, Consumer<String> lineConsumer) {
        int newlineIndex;
        while ((newlineIndex = pendingLine.indexOf("\n")) >= 0) {
            String line = pendingLine.substring(0, newlineIndex);
            pendingLine.delete(0, newlineIndex + 1);
            lineConsumer.accept(normalizeLine(line));
        }
    }

    private static String normalizeLine(String line) {
        if (line.endsWith("\r")) {
            return line.substring(0, line.length() - 1);
        }
        return line;
    }

    private static void enqueueChunk(
        AtomicReference<Future<Void>> processingChain,
        CatholicLLMResponseChunk chunk,
        Function<CatholicLLMResponseChunk, Future<Void>> chunkAsyncProcessor,
        Promise<Void> promise
    ) {
        if (chunk == null || promise.future().isComplete()) {
            return;
        }

        Future<Void> next = processingChain.get().compose(v -> chunkAsyncProcessor.apply(chunk));
        next.onFailure(promise::tryFail);
        processingChain.set(next);
    }
}
