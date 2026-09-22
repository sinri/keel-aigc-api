package io.github.sinri.keel.aigc.api.internal;

import io.github.sinri.keel.aigc.api.internal.dashscope.DashScopeStreamHandler;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponseChunk;
import io.github.sinri.keel.aigc.api.llm.catholic.observation.CatholicLLMObserver;
import io.github.sinri.keel.aigc.api.llm.catholic.observation.CatholicLLMObservationStage;
import io.github.sinri.keel.aigc.api.internal.catholic.observation.CatholicLLMObservationSupport;
import io.github.sinri.keel.aigc.api.internal.catholic.observation.CatholicLLMObservationSupport.Exchange;
import io.github.sinri.keel.base.async.Keel;
import io.github.sinri.keel.core.cutter.IntravenouslyCutterOnString;
import io.github.sinri.keel.core.servant.intravenous.Intravenous;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 工具类，用于处理 LLM 的流式调用产生的 SSE 事件。
 *
 * @see <a href="https://html.spec.whatwg.org/multipage/server-sent-events.html#parsing-an-event-stream">
 *     WHATWG HTML: Parsing an event stream</a>
 */
public class SSE2Chunk {

    private SSE2Chunk() {
    }

    /**
     * 仅在流处理正常完成后组装最终响应。
     *
     * @param streamFuture 流处理结果
     * @param responseBuilder 最终响应组装器
     * @return 流失败时保留原始失败，成功时返回组装结果
     */
    public static <T> Future<T> buildResponseOnSuccess(
        Future<Void> streamFuture,
        Supplier<T> responseBuilder
    ) {
        return streamFuture.map(v -> responseBuilder.get());
    }

    /** Validate protocol completion before exposing an aggregated response. */
    public static Future<io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse> buildCollectedResponse(
        Future<Void> streamFuture,
        io.github.sinri.keel.aigc.api.internal.catholic.response.CatholicResponseChunkCollector collector,
        String terminalEvent, CatholicLLMObserver observer, Exchange exchange
    ) {
        return SSE2Chunk.<io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse>buildResponseOnSuccess(streamFuture, () -> {
            diagnostic(observer, exchange, "response_build", collector.diagnosticSnapshot());
            if (!collector.isIdInitialized()) {
                throw new IllegalStateException("No valid response chunks collected; exchange=" + exchange.id());
            }
            if (!collector.isFinished()) {
                throw new IllegalStateException("Stream ended before " + terminalEvent + "; exchange=" + exchange.id());
            }
            var result = collector.build();
            exchange.trace().event("response_built", java.util.Map.of("response_id", result.id()));
            return result;
        }).andThen(ar -> {
            if (ar.failed() && streamFuture.succeeded()) CatholicLLMObservationSupport.failure(
                observer, exchange, CatholicLLMObservationStage.RESPONSE_CONVERSION, ar.cause());
        });
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

    public static Future<Buffer> requireSuccessAndReadBody(
        HttpClientResponse response, String serviceName, CatholicLLMObserver observer, Exchange exchange
    ) {
        CatholicLLMObservationSupport.responseStarted(observer, exchange, response);
        return response.body()
            .andThen(ar -> {
                if (ar.succeeded()) {
                    CatholicLLMObservationSupport.response(observer, exchange, response, ar.result());
                } else {
                    CatholicLLMObservationSupport.failure(
                        observer, exchange, CatholicLLMObservationStage.RESPONSE_BODY, ar.cause()
                    );
                }
            })
            .compose(body -> response.statusCode() == 200
                ? Future.succeededFuture(body)
                : Future.failedFuture(serviceName + ": " + response.statusCode() + " - " + body));
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
        Function<String, @Nullable CatholicLLMResponseChunk> processSseLine,
        Function<CatholicLLMResponseChunk, Future<Void>> chunkAsyncProcessor
    ) {
        return processOpenAiStyleSSEStream(keel, httpClientResponse, serviceName, processSseLine,
            chunkAsyncProcessor, CatholicLLMObserver.noop(),
            CatholicLLMObservationSupport.exchange(serviceName, "", true));
    }

    public static Future<Void> processOpenAiStyleSSEStream(
        Keel keel,
        HttpClientResponse httpClientResponse,
        String serviceName,
        Function<String, @Nullable CatholicLLMResponseChunk> processSseLine,
        Function<CatholicLLMResponseChunk, Future<Void>> chunkAsyncProcessor,
        CatholicLLMObserver observer,
        Exchange exchange
    ) {
        return processProtocolStream(keel, httpClientResponse, serviceName, processSseLine,
            chunkAsyncProcessor, observer, exchange, false);
    }

    private static Future<Void> processProtocolStream(
        Keel keel, HttpClientResponse httpClientResponse, String serviceName,
        Function<String, @Nullable CatholicLLMResponseChunk> processSseLine,
        Function<CatholicLLMResponseChunk, Future<Void>> chunkAsyncProcessor,
        CatholicLLMObserver observer, Exchange exchange, boolean dispatchEventBoundary
    ) {
        if (httpClientResponse.statusCode() != 200) {
            return requireSuccessAndReadBody(httpClientResponse, serviceName, observer, exchange).mapEmpty();
        }
        CatholicLLMObservationSupport.responseStarted(observer, exchange, httpClientResponse);
        AtomicLong events = new AtomicLong();
        AtomicLong parsed = new AtomicLong();
        AtomicLong processed = new AtomicLong();
        AtomicLong terminalChunks = new AtomicLong();
        java.util.concurrent.atomic.AtomicBoolean done = new java.util.concurrent.atomic.AtomicBoolean();
        Intravenous.SingleDropProcessor<String> dropProcessor = drop -> {
            long sequence = events.getAndIncrement();
            exchange.trace().payload("sse_event", java.util.Map.of("event_sequence", sequence), drop, false);
            CatholicLLMObservationSupport.safely(() -> observer.onStreamEvent(
                exchange.id(), exchange.provider(), sequence, drop, exchange.elapsedMillis()));
            Future<Void> chain = Future.succeededFuture();
            var lines = new java.util.ArrayList<>(java.util.Arrays.asList(drop.split("\n")));
            // DashScope assembles an event across data lines and dispatches on the empty line.
            if (dispatchEventBoundary) lines.add("");
            for (String rawLine : lines) {
                String line = rawLine.endsWith("\r") ? rawLine.substring(0, rawLine.length() - 1) : rawLine;
                // Parse inside the chain: a failed callback must prevent subsequent parsing.
                chain = chain.compose(v -> {
                    if (line.equals("data: [DONE]")) done.set(true);
                    CatholicLLMResponseChunk chunk;
                    try {
                        chunk = processSseLine.apply(line);
                    } catch (Exception e) {
                        diagnostic(observer, exchange, "parse_failed", java.util.Map.of("sequence", sequence));
                        return Future.failedFuture(e);
                    }
                    if (chunk == null) return Future.succeededFuture();
                    long chunkSequence = parsed.getAndIncrement();
                    exchange.trace().event("converted_chunk", java.util.Map.of("event_sequence", sequence,
                            "chunk_sequence", chunkSequence, "finished", chunk.isFinished(), "choice_index", chunk.index()));
                    if (chunk.deltaToolCalls() != null) for (var delta : chunk.deltaToolCalls()) {
                        if (delta.function() != null) exchange.trace().payload("converted_arguments_delta",
                                java.util.Map.of("event_sequence", sequence, "chunk_sequence", chunkSequence,
                                        "tool_index", delta.index(), "tool_call_id", delta.id() == null ? "" : delta.id()),
                                delta.function().argumentsDelta(), true);
                    }
                    if (chunk.isFinished()) terminalChunks.incrementAndGet();
                    return Future.succeededFuture().compose(ignored -> chunkAsyncProcessor.apply(chunk))
                        .andThen(ar -> {
                            if (ar.succeeded()) processed.incrementAndGet();
                            else diagnostic(observer, exchange, "processor_failed", java.util.Map.of("sequence", sequence));
                        });
                });
            }
            return chain;
        };
        return processSSEStream(keel, httpClientResponse, dropProcessor, 0L,
            details -> diagnostic(observer, exchange, "transport_completed", details), exchange)
            .andThen(ar -> {
                diagnostic(observer, exchange, "stream_completed", java.util.Map.of(
                    "events", events.get(), "parsed_chunks", parsed.get(), "processed_chunks", processed.get(),
                    "done_seen", done.get(), "terminal_chunks", terminalChunks.get(), "success", ar.succeeded()));
                if (ar.failed()) CatholicLLMObservationSupport.failure(
                    observer, exchange, CatholicLLMObservationStage.STREAM_READING, ar.cause());
            });
    }

    public static void diagnostic(CatholicLLMObserver observer, Exchange exchange,
                                  String phase, java.util.Map<String, Object> details) {
        exchange.trace().event(phase, details);
        CatholicLLMObservationSupport.safely(() -> observer.onStreamDiagnostic(
            exchange.id(), exchange.provider(), phase, details, exchange.elapsedMillis()));
    }

    /**
     * 消费 DashScope 风格的 SSE 流：每个 {@code \n\n} 分隔的事件块被逐行传给 {@code streamHandler.processSseLine}，
     * 并在块末尾模拟空行边界以触发事件完成。未形成事件边界的底层残留数据仅在诊断中报告。
     * 非 null chunk 串行传给 {@code chunkAsyncProcessor}。
     */
    public static Future<Void> processDashScopeSSEStream(
        Keel keel,
        HttpClientResponse httpClientResponse,
        String serviceName,
        DashScopeStreamHandler streamHandler,
        Function<CatholicLLMResponseChunk, Future<Void>> chunkAsyncProcessor
    ) {
        return processDashScopeSSEStream(keel, httpClientResponse, serviceName, streamHandler,
            chunkAsyncProcessor, CatholicLLMObserver.noop(),
            CatholicLLMObservationSupport.exchange(serviceName, "", true));
    }

    public static Future<Void> processDashScopeSSEStream(
        Keel keel,
        HttpClientResponse httpClientResponse,
        String serviceName,
        DashScopeStreamHandler streamHandler,
        Function<CatholicLLMResponseChunk, Future<Void>> chunkAsyncProcessor,
        CatholicLLMObserver observer,
        Exchange exchange
    ) {
        return processProtocolStream(keel, httpClientResponse, serviceName, streamHandler::processSseLine,
            chunkAsyncProcessor, observer, exchange, true);
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
     * @see <a href="https://html.spec.whatwg.org/multipage/server-sent-events.html#parsing-an-event-stream">
     *     WHATWG HTML: Parsing an event stream</a>
     */
    public static Future<Void> processSSEStream(
        Keel keel,
        HttpClientResponse httpClientResponse,
        Intravenous.SingleDropProcessor<String> dropProcessor,
        long timeout
    ) {
        return processSSEStream(keel, httpClientResponse, dropProcessor, timeout, details -> {}, null);
    }

    private static Future<Void> processSSEStream(
        Keel keel, HttpClientResponse httpClientResponse,
        Intravenous.SingleDropProcessor<String> dropProcessor, long timeout,
        java.util.function.Consumer<java.util.Map<String, Object>> diagnostic, @Nullable Exchange exchange
    ) {
        // Pause before asynchronous deployment; a fast HTTP body can otherwise end unobserved.
        httpClientResponse.pause();
        var processingFailure = new java.util.concurrent.atomic.AtomicReference<Throwable>();
        Intravenous.SingleDropProcessor<String> guarded = drop -> {
            if (processingFailure.get() != null) return Future.succeededFuture();
            return Future.succeededFuture().compose(v -> dropProcessor.process(drop))
                .andThen(ar -> {
                    if (ar.failed()) processingFailure.compareAndSet(null, ar.cause());
                });
        };
        AtomicLong bytes = new AtomicLong();
        AtomicLong buffers = new AtomicLong();
        java.util.concurrent.atomic.AtomicBoolean eof = new java.util.concurrent.atomic.AtomicBoolean();
        var cutter = new IntravenouslyCutterOnString(guarded, timeout) {
            void capturePending() {
                if (exchange != null && exchange.trace().enabled()) synchronized (getBufferRef()) {
                    CatholicLLMObservationSupport.captureBuffer(exchange, "transport_pending", getBufferRef().get(), buffers.get());
                }
            }
            java.util.Map<String, Object> snapshot() {
                synchronized (getBufferRef()) {
                    Buffer pending = getBufferRef().get();
                    return java.util.Map.of("received_bytes", bytes.get(), "http_eof", eof.get(),
                        "pending_bytes", pending.length(), "pending_has_crlf", pending.toString().contains("\r\n"));
                }
            }
        };
        return cutter.deployMe(keel, new DeploymentOptions())
                     .compose(deploymentId -> {
                         httpClientResponse.pause();
                         httpClientResponse.handler(buffer -> {
                             bytes.addAndGet(buffer.length());
                             if (exchange != null) CatholicLLMObservationSupport.captureBuffer(
                                     exchange, "transport_buffer", buffer, buffers.getAndIncrement());
                             cutter.acceptFromStream(buffer);
                         });
                         httpClientResponse.exceptionHandler(cutter::stopHere);
                         httpClientResponse.endHandler(v -> {
                             eof.set(true);
                             cutter.stopHere(null);
                         });
                         httpClientResponse.resume();

                         return cutter.waitForAllHandled().compose(v -> processingFailure.get() == null
                             ? Future.<Void>succeededFuture() : Future.<Void>failedFuture(processingFailure.get()));
                     })
                     .andThen(ar -> {
                         CatholicLLMObservationSupport.safely(cutter::capturePending);
                         CatholicLLMObservationSupport.safely(() -> diagnostic.accept(cutter.snapshot()));
                         cutter.undeployMe();
                     });
    }
}
