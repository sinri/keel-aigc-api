package io.github.sinri.keel.aigc.api.agent;

import io.vertx.core.Future;

/** 判断 Agent 已完成任务，还是需要执行工具或加入机制观察后继续。 */
@FunctionalInterface
public interface CatholicAgentObserver {
    Future<CatholicAgentDirective> observe(CatholicAgentObservationContext context);

    static CatholicAgentObserver defaultObserver() {
        return context -> Future.succeededFuture(context.response().hasToolCalls()
            ? CatholicAgentDirective.continueExecution()
            : CatholicAgentDirective.complete());
    }
}
