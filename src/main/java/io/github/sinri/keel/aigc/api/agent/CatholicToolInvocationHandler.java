package io.github.sinri.keel.aigc.api.agent;

import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.CatholicFunctionToolCall;
import io.vertx.core.Future;

/**
 * 将模型产生的 {@link CatholicFunctionToolCall} 转为可写回对话的工具结果字符串。
 */
@FunctionalInterface
public interface CatholicToolInvocationHandler {

    static CatholicToolInvocationHandlerWithNativeFunctionAdapters createWithNativeFunctionAdapters() {
        return new CatholicToolInvocationHandlerWithNativeFunctionAdapters();
    }

    /**
     * 执行单次工具调用，返回值将封装为 tool 角色消息进入后续轮次。
     */
    Future<String> handle(CatholicFunctionToolCall toolCall);
}
