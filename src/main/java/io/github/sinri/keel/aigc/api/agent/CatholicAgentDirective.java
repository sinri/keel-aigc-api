package io.github.sinri.keel.aigc.api.agent;

import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicChatMessage;

import java.util.List;
import java.util.Objects;

/** 观察器对当前交互的控制指令。 */
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
