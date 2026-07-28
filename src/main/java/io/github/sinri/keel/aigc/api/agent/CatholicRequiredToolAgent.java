package io.github.sinri.keel.aigc.api.agent;

import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicUserMessage;
import io.vertx.core.Future;

import java.util.List;
import java.util.Objects;

/**
 * 表示“首轮 LLM 响应必须调用指定函数工具”的策略型 Agent。
 * 它包装一个已配置该函数的 {@link CatholicAgent}，在请求中声明强制工具选择，并校验
 * 首轮响应确实包含目标函数调用；这样可复用通用 Agent 的执行循环而不向其引入特定
 * 业务约束。
 */
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
