package io.github.sinri.keel.aigc.api.agent.tool;

import io.github.sinri.keel.aigc.api.trace.CatholicTraceContext;

import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.CatholicFunctionToolCall;
import io.vertx.core.Future;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

/**
 * 表示模型函数工具调用与应用侧工具实现之间的执行边界。
 * 实现者负责接收模型产生的 {@link CatholicFunctionToolCall}、异步执行对应业务函数，
 * 并返回可封装为 tool 角色消息、写回后续对话的结果字符串。
 */
public abstract class CatholicToolInvocationHandler {
    private volatile CatholicToolInvocationObserver observer = CatholicToolInvocationObserver.noop();

    public static CatholicToolInvocationHandlerWithNativeFunctionAdapters createWithNativeFunctionAdapters() {
        return new CatholicToolInvocationHandlerWithNativeFunctionAdapters();
    }

    public static CatholicToolInvocationHandlerWithNativeFunctionAdapters createWithNativeFunctionAdapters(
        CatholicToolInvocationObserver observer
    ) {
        return new CatholicToolInvocationHandlerWithNativeFunctionAdapters(observer);
    }

    /**
     * Configures execution observation and audit events on a tool handler.
     */
    public static CatholicToolInvocationHandler observed(
        CatholicToolInvocationHandler handler, CatholicToolInvocationObserver observer
    ) {
        return Objects.requireNonNull(handler, "handler").observedBy(observer);
    }

    /**
     * Creates a tool invocation handler from a function.
     */
    public static CatholicToolInvocationHandler of(
        Function<CatholicFunctionToolCall, Future<String>> handler
    ) {
        Objects.requireNonNull(handler, "handler");
        return new CatholicToolInvocationHandler() {
            @Override
            protected Future<String> handleToolCall(CatholicFunctionToolCall toolCall) {
                return handler.apply(toolCall);
            }
        };
    }

    /**
     * Configures the observer of this handler and returns this handler.
     */
    public final CatholicToolInvocationHandler observedBy(CatholicToolInvocationObserver observer) {
        this.observer = Objects.requireNonNull(observer, "observer");
        return this;
    }

    /**
     * 执行单次工具调用，返回值将封装为 tool 角色消息进入后续轮次。
     */
    public final Future<String> handle(CatholicFunctionToolCall toolCall) {
        return handle(toolCall, CatholicTraceContext.none());
    }

    public final Future<String> handle(CatholicFunctionToolCall toolCall,
            CatholicTraceContext parentTrace) {
        Objects.requireNonNull(toolCall, "toolCall");
        var observation = new CatholicToolInvocationObservation(
            UUID.randomUUID().toString(),
            toolCall.id(),
            toolCall.functionName(),
            toolCall.function().arguments(),
            Instant.now()
        );
        var trace = parentTrace.child("invocation_id", observation.invocationId())
                .child("tool_call_id", toolCall.id()).child("function_name", toolCall.functionName());
        trace.payload("tool_arguments_ready", java.util.Map.of(), toolCall.function().arguments(), true);
        CatholicToolInvocationObserver invocationObserver = observer;
        long startedNanos = System.nanoTime();
        safely(() -> invocationObserver.onStarted(observation));

        Future<String> invocation;
        try {
            invocation = handleToolCall(toolCall, trace);
            if (invocation == null) {
                invocation = Future.failedFuture(
                    new NullPointerException("tool invocation handler returned null Future")
                );
            }
        } catch (Throwable cause) {
            invocation = Future.failedFuture(cause);
        }

        return invocation.andThen(result -> {
            long elapsedMillis = (System.nanoTime() - startedNanos) / 1_000_000L;
            if (result.succeeded()) {
                trace.payload("tool_succeeded", java.util.Map.of("elapsed_ms", elapsedMillis), result.result(), false);
                safely(() -> invocationObserver.onSucceeded(observation, result.result(), elapsedMillis));
            } else {
                trace.failure("TOOL_EXECUTION", result.cause());
                safely(() -> invocationObserver.onFailed(observation, result.cause(), elapsedMillis));
            }
        });
    }

    protected Future<String> handleToolCall(CatholicFunctionToolCall toolCall,
            CatholicTraceContext trace) {
        return handleToolCall(toolCall);
    }

    public static CatholicToolInvocationHandler ofTraced(java.util.function.BiFunction<CatholicFunctionToolCall,
            CatholicTraceContext, Future<String>> handler) {
        return new CatholicToolInvocationHandler() {
            protected Future<String> handleToolCall(CatholicFunctionToolCall call) {
                return handler.apply(call, CatholicTraceContext.none());
            }
            protected Future<String> handleToolCall(CatholicFunctionToolCall call,
                    CatholicTraceContext trace) {
                return handler.apply(call, trace);
            }
        };
    }

    protected abstract Future<String> handleToolCall(CatholicFunctionToolCall toolCall);

    private static void safely(Runnable event) {
        try {
            event.run();
        } catch (Throwable ignored) {
            // Tool observability is deliberately fail-open.
        }
    }
}
