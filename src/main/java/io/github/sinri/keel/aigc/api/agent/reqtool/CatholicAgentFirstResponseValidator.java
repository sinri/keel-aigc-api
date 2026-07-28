package io.github.sinri.keel.aigc.api.agent.reqtool;

import io.github.sinri.keel.aigc.api.agent.CatholicAgentObserver;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.vertx.core.Future;

/**
 * 表示一次 Agent 交互的首轮模型响应校验策略。
 * 它在 {@link CatholicAgentObserver} 观察响应和任何工具执行之前异步运行，
 * 用于实现首轮响应必须满足的内部约束，例如确认模型调用了指定工具。
 */
@FunctionalInterface
public interface CatholicAgentFirstResponseValidator {
    Future<Void> validate(CatholicLLMResponse response);
}
