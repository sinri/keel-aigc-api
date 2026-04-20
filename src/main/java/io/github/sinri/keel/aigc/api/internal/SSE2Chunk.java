package io.github.sinri.keel.aigc.api.internal;

import io.github.sinri.keel.aigc.api.internal.dashscope.DashScopeStreamHandler;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponseChunk;
import io.github.sinri.keel.base.async.Keel;
import io.github.sinri.keel.core.cutter.IntravenouslyCutterOnString;
import io.github.sinri.keel.core.servant.intravenous.Intravenous;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;

import java.util.function.Function;

/**
 * 工具类，用于处理 LLM 的流式调用产生的 SSE 事件。
 */
public class SSE2Chunk {

    private SSE2Chunk() {
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
     * 消费 OpenAI/Anthropic 风格的 SSE 流：每个 {@code \n\n} 分隔的事件块被逐行传给 {@code processSseLine}，
     * 非 null chunk 串行传给 {@code chunkAsyncProcessor}。
     * <p>
     * 对于 DashScope 风格的 SSE，请使用 {@link #processDashScopeSSEStream}。
     */
    public static Future<Void> processOpenAiStyleSSEStream(
        Keel keel,
        HttpClientResponse httpClientResponse,
        String serviceName,
        Function<String, CatholicLLMResponseChunk> processSseLine,
        Function<CatholicLLMResponseChunk, Future<Void>> chunkAsyncProcessor
    ) {
        if (httpClientResponse.statusCode() != 200) {
            return requireSuccessAndReadBody(httpClientResponse, serviceName).mapEmpty();
        }

        Intravenous.SingleDropProcessor<String> dropProcessor = drop -> {
            Future<Void> chain = Future.succeededFuture();
            for (String rawLine : drop.split("\n")) {
                String line = rawLine.endsWith("\r")
                    ? rawLine.substring(0, rawLine.length() - 1)
                    : rawLine;
                CatholicLLMResponseChunk chunk;
                try {
                    chunk = processSseLine.apply(line);
                } catch (Exception e) {
                    return Future.failedFuture(e);
                }
                if (chunk != null) {
                    chain = chain.compose(v -> chunkAsyncProcessor.apply(chunk));
                }
            }
            return chain;
        };

        return processSSEStream(keel, httpClientResponse, dropProcessor, 0L);
    }

    /**
     * 消费 DashScope 风格的 SSE 流：每个 {@code \n\n} 分隔的事件块被逐行传给 {@code streamHandler.processSseLine}，
     * 并在块末尾模拟空行边界以触发事件完成。流结束后调用 {@code streamHandler.flush()} 处理残留数据。
     * 非 null chunk 串行传给 {@code chunkAsyncProcessor}。
     */
    public static Future<Void> processDashScopeSSEStream(
        Keel keel,
        HttpClientResponse httpClientResponse,
        String serviceName,
        DashScopeStreamHandler streamHandler,
        Function<CatholicLLMResponseChunk, Future<Void>> chunkAsyncProcessor
    ) {
        if (httpClientResponse.statusCode() != 200) {
            return requireSuccessAndReadBody(httpClientResponse, serviceName).mapEmpty();
        }

        Intravenous.SingleDropProcessor<String> dropProcessor = drop -> {
            Future<Void> chain = Future.succeededFuture();
            for (String rawLine : drop.split("\n")) {
                String line = rawLine.endsWith("\r")
                    ? rawLine.substring(0, rawLine.length() - 1)
                    : rawLine;
                CatholicLLMResponseChunk chunk;
                try {
                    chunk = streamHandler.processSseLine(line);
                } catch (Exception e) {
                    return Future.failedFuture(e);
                }
                if (chunk != null) {
                    chain = chain.compose(v -> chunkAsyncProcessor.apply(chunk));
                }
            }
            // \n\n 分隔消耗了空行边界，模拟它以触发 DashScope 事件块完成
            CatholicLLMResponseChunk flushChunk;
            try {
                flushChunk = streamHandler.processSseLine("");
            } catch (Exception e) {
                return Future.failedFuture(e);
            }
            if (flushChunk != null) {
                chain = chain.compose(v -> chunkAsyncProcessor.apply(flushChunk));
            }
            return chain;
        };

        return processSSEStream(keel, httpClientResponse, dropProcessor, 0L)
            .compose(v -> {
                CatholicLLMResponseChunk finalChunk = streamHandler.flush();
                if (finalChunk != null) {
                    return chunkAsyncProcessor.apply(finalChunk);
                }
                return Future.succeededFuture();
            });
    }

    /**
     * 处理 SSE 流，使用 {@link IntravenouslyCutterOnString} 按 {@code \n\n} 切分事件块，
     * 每个块作为 drop 由 {@code dropProcessor} 串行处理。
     *
     * @param keel               Keel 实例，用于部署 Verticle。
     * @param httpClientResponse HTTP 响应流。
     * @param dropProcessor      逐块处理器。
     * @param timeout            每块处理超时（毫秒），0 表示不限时。
     * @return 所有块处理完成后的 Future。
     */
    public static Future<Void> processSSEStream(
        Keel keel,
        HttpClientResponse httpClientResponse,
        Intravenous.SingleDropProcessor<String> dropProcessor,
        long timeout
    ) {
        var cutter = new IntravenouslyCutterOnString(dropProcessor, timeout);
        return cutter.deployMe(keel, new DeploymentOptions())
                     .compose(deploymentId -> {
                         httpClientResponse.pause();
                         httpClientResponse.handler(cutter::acceptFromStream);
                         httpClientResponse.exceptionHandler(cutter::stopHere);
                         httpClientResponse.endHandler(v -> cutter.stopHere(null));
                         httpClientResponse.resume();

                         return cutter.waitForAllHandled();
                     })
                     .andThen(ar -> {
                         cutter.undeployMe();
                     });
    }
}