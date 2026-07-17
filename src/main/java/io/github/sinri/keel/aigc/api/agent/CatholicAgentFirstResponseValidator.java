package io.github.sinri.keel.aigc.api.agent;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.vertx.core.Future;

/** 一次交互首轮模型响应的异步校验器，在 Observer 和工具执行前运行。 */
@FunctionalInterface
interface CatholicAgentFirstResponseValidator {
    Future<Void> validate(CatholicLLMResponse response);
}
