package io.github.sinri.keel.aigc.api.agent;

import io.vertx.core.Future;

/**
 * 表示 Agent 每轮 LLM 响应的异步决策策略。
 * 实现者检查 {@link CatholicAgentObservationContext} 并返回
 * {@link CatholicAgentDirective}，用于判断任务已经完成，还是需要执行工具或追加
 * 机制观察消息后继续下一轮。默认实现以响应中是否存在工具调用作为继续条件。
 */
@FunctionalInterface
public interface CatholicAgentObserver {
    Future<CatholicAgentDirective> observe(CatholicAgentObservationContext context);

    static CatholicAgentObserver defaultObserver() {
        return context -> Future.succeededFuture(context.response().hasToolCalls()
            ? CatholicAgentDirective.continueExecution()
            : CatholicAgentDirective.complete());
    }
}
