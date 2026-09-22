package io.github.sinri.keel.aigc.api.llm.catholic;

import io.github.sinri.keel.aigc.api.trace.CatholicTraceContext;

import io.vertx.core.Future;

import java.util.function.Function;

/**
 * 一种通用 LLM 调用类的接口定义，用于调用 LLM 模型并获取回复。
 * <p>
 * 需要（逐步）支持以下协议：<br>
 * - OpenAI: Chat Completions<br>
 * - DashScope<br>
 * - Anthropic<br>
 * - OpenAI: Responses<br>
 */
public interface CatholicLLM {
    /**
     * 以非 stream 方式调用 LLM 获得特定格式的报文，并异步返回封装好的 CatholicLLMResponse 实例
     */
    Future<CatholicLLMResponse> call(CatholicLLMRequest request);

    /**
     * 以 stream 方式调用 LLM 获得片段，每获得一个指定格式的报文片段，异步调用 chunkAsyncProcessor 处理，直到处理结束，最终异步返回。
     */
    Future<Void> callStream(CatholicLLMRequest request, Function<CatholicLLMResponseChunk, Future<Void>> chunkAsyncProcessor);

    /**
     * 以 stream 方式调用 LLM 获得片段，通过获得一系列有序的指定格式的报文片段，组装为封装好的 CatholicLLMResponse 实例后最终异步返回。
     */
    Future<CatholicLLMResponse> callStream(CatholicLLMRequest request);

    /** Whether this implementation captures provider-native evidence. Custom providers default to partial capture. */
    default boolean supportsTrace() { return false; }

    default Future<CatholicLLMResponse> call(CatholicLLMRequest request,
            CatholicTraceContext trace) {
        trace.event("llm_start", java.util.Map.of("model", request.model(),
                "capture_capability", supportsTrace() ? "instrumented" : "partial"));
        return Future.succeededFuture().compose(v -> call(request.withTrace(trace)))
                .andThen(ar -> { if (ar.failed()) trace.failure("LLM_CALL", ar.cause()); });
    }

    default Future<CatholicLLMResponse> callStream(CatholicLLMRequest request,
            CatholicTraceContext trace) {
        trace.event("llm_start", java.util.Map.of("model", request.model(),
                "capture_capability", supportsTrace() ? "instrumented" : "partial"));
        return Future.succeededFuture().compose(v -> callStream(request.withTrace(trace)))
                .andThen(ar -> { if (ar.failed()) trace.failure("LLM_CALL", ar.cause()); });
    }

    default Future<Void> callStream(CatholicLLMRequest request,
            Function<CatholicLLMResponseChunk, Future<Void>> processor,
            CatholicTraceContext trace) {
        trace.event("llm_start", java.util.Map.of("model", request.model(),
                "capture_capability", supportsTrace() ? "instrumented" : "partial"));
        return Future.succeededFuture().compose(v -> callStream(request.withTrace(trace), processor))
                .andThen(ar -> { if (ar.failed()) trace.failure("LLM_CALL", ar.cause()); });
    }
}
