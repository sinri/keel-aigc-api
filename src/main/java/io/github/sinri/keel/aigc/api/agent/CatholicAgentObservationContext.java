package io.github.sinri.keel.aigc.api.agent;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicChatMessage;

import java.util.List;
import java.util.Objects;

/** 当前 LLM 输出及执行进度的只读观察上下文。 */
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
