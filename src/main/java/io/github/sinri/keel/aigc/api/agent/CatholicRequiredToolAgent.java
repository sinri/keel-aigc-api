package io.github.sinri.keel.aigc.api.agent;

import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicUserMessage;
import io.vertx.core.Future;

import java.util.List;
import java.util.Objects;

/** “首次 LLM 分析必须选择指定工具”的特殊 Agent；该策略不污染通用 Agent。 */
public final class CatholicRequiredToolAgent {
    private final CatholicAgent delegate;
    private final String functionName;

    public CatholicRequiredToolAgent(CatholicAgent delegate, String functionName) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.functionName = Objects.requireNonNull(functionName, "functionName");
        if (functionName.isBlank()) throw new IllegalArgumentException("functionName must not be blank");
        if (!delegate.supportsFunction(functionName)) {
            throw new IllegalArgumentException("Required function is not configured: " + functionName);
        }
    }

    public Future<CatholicAgentResult> interact(String userText) {
        return interact(CatholicUserMessage.ofText(userText));
    }

    public Future<CatholicAgentResult> interact(CatholicUserMessage userMessage) {
        return delegate.interact(List.of(), userMessage, functionName, response -> {
            boolean requiredToolCalled = response.message().hasToolCalls()
                && response.message().toolCalls().stream()
                    .anyMatch(call -> functionName.equals(call.functionName()));
            return requiredToolCalled
                ? Future.succeededFuture()
                : Future.failedFuture(new CatholicRequiredToolNotCalledException(functionName));
        });
    }
}
