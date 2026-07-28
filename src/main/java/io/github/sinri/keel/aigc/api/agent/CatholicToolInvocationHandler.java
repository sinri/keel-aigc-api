package io.github.sinri.keel.aigc.api.agent;

import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.CatholicFunctionToolCall;
import io.vertx.core.Future;

import java.util.Objects;

/**
 * 表示模型函数工具调用与应用侧工具实现之间的执行边界。
 * 实现者负责接收模型产生的 {@link CatholicFunctionToolCall}、异步执行对应业务函数，
 * 并返回可封装为 tool 角色消息、写回后续对话的结果字符串。
 */
@FunctionalInterface
public interface CatholicToolInvocationHandler {

    static CatholicToolInvocationHandlerWithNativeFunctionAdapters createWithNativeFunctionAdapters() {
        return new CatholicToolInvocationHandlerWithNativeFunctionAdapters();
    }

    static CatholicToolInvocationHandlerWithNativeFunctionAdapters createWithNativeFunctionAdapters(
        CatholicToolInvocationObserver observer
    ) {
        return new CatholicToolInvocationHandlerWithNativeFunctionAdapters(observer);
    }

    /**
     * Wraps any tool handler with execution observation and audit events.
     */
    static CatholicToolInvocationHandler observed(
        CatholicToolInvocationHandler handler, CatholicToolInvocationObserver observer
    ) {
        return new ObservedCatholicToolInvocationHandler(
            Objects.requireNonNull(handler, "handler"),
            Objects.requireNonNull(observer, "observer")
        );
    }

    /**
     * Returns an observed view of this handler.
     * todo 不应该 wrap
     */
    default CatholicToolInvocationHandler observedBy(CatholicToolInvocationObserver observer) {
        return observed(this, observer);
    }

    /**
     * 执行单次工具调用，返回值将封装为 tool 角色消息进入后续轮次。
     */
    Future<String> handle(CatholicFunctionToolCall toolCall);
}
