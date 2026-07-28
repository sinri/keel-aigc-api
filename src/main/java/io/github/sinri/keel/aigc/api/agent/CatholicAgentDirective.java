package io.github.sinri.keel.aigc.api.agent;

import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicChatMessage;

import java.util.List;
import java.util.Objects;

/**
 * 表示 {@link CatholicAgentObserver} 对当前 Agent 轮次作出的控制指令。
 * 它同时描述交互是否已经完成，以及继续执行前需要追加到 transcript 的机制观察消息，
 * 由 {@link CatholicAgent} 据此决定结束交互、执行模型请求的工具或发起下一轮 LLM 调用。
 */
public record CatholicAgentDirective(boolean completed, List<CatholicChatMessage> messagesToAppend) {
    public CatholicAgentDirective {
        messagesToAppend = List.copyOf(Objects.requireNonNull(messagesToAppend, "messagesToAppend"));
    }

    public static CatholicAgentDirective complete() { return new CatholicAgentDirective(true, List.of()); }
    public static CatholicAgentDirective continueExecution() { return new CatholicAgentDirective(false, List.of()); }
    public static CatholicAgentDirective continueWith(CatholicChatMessage observation) {
        return new CatholicAgentDirective(false, List.of(Objects.requireNonNull(observation, "observation")));
    }
}
