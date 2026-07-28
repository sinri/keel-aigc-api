package io.github.sinri.keel.aigc.api.agent.tool;

/**
 * 表示 {@link CatholicToolInvocationHandler} 执行过程的审计观察器。
 * 它接收同一次工具调用的开始、成功和失败事件，可用于日志、指标或链路追踪；
 * 每个事件都通过 {@link CatholicToolInvocationObservation} 关联。
 * <p>
 * 观察逻辑与业务执行结果相互隔离：观察器自身抛出的异常不会改变工具调用结果。
 */
public interface CatholicToolInvocationObserver {
    default void onStarted(CatholicToolInvocationObservation observation) {
    }

    default void onSucceeded(
        CatholicToolInvocationObservation observation, String result, long elapsedMillis
    ) {
    }

    default void onFailed(
        CatholicToolInvocationObservation observation, Throwable cause, long elapsedMillis
    ) {
    }

    static CatholicToolInvocationObserver noop() {
        return NoopCatholicToolInvocationObserver.INSTANCE;
    }

    enum NoopCatholicToolInvocationObserver implements CatholicToolInvocationObserver {
        INSTANCE
    }
}
