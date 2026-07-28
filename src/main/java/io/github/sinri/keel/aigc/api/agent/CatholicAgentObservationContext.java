package io.github.sinri.keel.aigc.api.agent;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicChatMessage;

import java.util.List;
import java.util.Objects;

/**
 * 表示 {@link CatholicAgentObserver} 观察某一轮 Agent 执行时所需的只读上下文。
 * 它汇集当前 LLM 响应、截至当前轮次的完整 transcript，以及 LLM 轮次、工具轮次和
 * 最大轮次等进度信息，使观察器能够在不修改执行状态的前提下判断是否继续。
 */
public record CatholicAgentObservationContext(CatholicLLMResponse response,
                                               List<CatholicChatMessage> transcript,
                                               int llmRound,
                                               int toolRounds,
                                               int maxRounds) {
    public CatholicAgentObservationContext {
        response = Objects.requireNonNull(response, "response");
        transcript = List.copyOf(Objects.requireNonNull(transcript, "transcript"));
    }
}
