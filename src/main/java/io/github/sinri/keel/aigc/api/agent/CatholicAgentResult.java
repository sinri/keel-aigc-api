package io.github.sinri.keel.aigc.api.agent;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicChatMessage;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/** 一次用户交互的完整结果，包括终止原因和可审计 transcript。 */
public record CatholicAgentResult(CatholicAgentTermination termination,
                                  List<CatholicChatMessage> transcript,
                                  CatholicLLMResponse lastResponse,
                                  int llmRounds,
                                  int toolRounds,
                                  int maxRounds) {
    public CatholicAgentResult {
        termination = Objects.requireNonNull(termination, "termination");
        transcript = List.copyOf(Objects.requireNonNull(transcript, "transcript"));
        lastResponse = Objects.requireNonNull(lastResponse, "lastResponse");
    }
    public boolean completed() { return termination == CatholicAgentTermination.COMPLETED; }
    /** 返回最后一轮 LLM 给用户的文本；强制中断时可作为未完成结果展示。 */
    public @Nullable String text() { return lastResponse.text(); }

    static CatholicAgentResult completed(List<CatholicChatMessage> transcript, CatholicLLMResponse response,
                                         int llmRounds, int toolRounds, int maxRounds) {
        return new CatholicAgentResult(CatholicAgentTermination.COMPLETED, transcript, response,
            llmRounds, toolRounds, maxRounds);
    }
    static CatholicAgentResult roundLimit(List<CatholicChatMessage> transcript, CatholicLLMResponse response,
                                          int llmRounds, int toolRounds, int maxRounds) {
        return new CatholicAgentResult(CatholicAgentTermination.ROUND_LIMIT_EXCEEDED, transcript, response,
            llmRounds, toolRounds, maxRounds);
    }
}
