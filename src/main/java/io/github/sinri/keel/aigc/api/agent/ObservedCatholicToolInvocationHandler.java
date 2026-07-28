package io.github.sinri.keel.aigc.api.agent;

import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.CatholicFunctionToolCall;
import io.vertx.core.Future;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * 表示为任意 {@link CatholicToolInvocationHandler} 增加审计观察能力的内部装饰器。
 * 它为每次调用创建 {@link CatholicToolInvocationObservation}，统一捕获同步异常和异步
 * 执行结果，并向观察器发送开始、成功或失败事件；观察器异常会被隔离，不影响委托处理器
 * 的原始结果。
 */
// todo 这个类不对劲，不应该 wrap
final class ObservedCatholicToolInvocationHandler implements CatholicToolInvocationHandler {
    private final CatholicToolInvocationHandler delegate;
    private final CatholicToolInvocationObserver observer;

    ObservedCatholicToolInvocationHandler(
        CatholicToolInvocationHandler delegate, CatholicToolInvocationObserver observer
    ) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.observer = Objects.requireNonNull(observer, "observer");
    }

    @Override
    public Future<String> handle(CatholicFunctionToolCall toolCall) {
        Objects.requireNonNull(toolCall, "toolCall");
        var observation = new CatholicToolInvocationObservation(
            UUID.randomUUID().toString(),
            toolCall.id(),
            toolCall.functionName(),
            toolCall.function().arguments(),
            Instant.now()
        );
        long startedNanos = System.nanoTime();
        safely(() -> observer.onStarted(observation));

        Future<String> invocation;
        try {
            invocation = delegate.handle(toolCall);
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
                safely(() -> observer.onSucceeded(observation, result.result(), elapsedMillis));
            } else {
                safely(() -> observer.onFailed(observation, result.cause(), elapsedMillis));
            }
        });
    }

    private static void safely(Runnable event) {
        try {
            event.run();
        } catch (Throwable ignored) {
            // Tool observability is deliberately fail-open.
        }
    }
}
