package io.github.sinri.keel.aigc.api.agent;

import java.util.Objects;

/**
 * 表示模型未遵循 {@link CatholicRequiredToolAgent} 首轮强制工具调用约束。
 * 当首轮 LLM 响应未调用指定函数时抛出，并保留目标函数名，便于调用方诊断模型服务
 * 是否支持或正确执行了强制工具选择参数。
 */
public final class CatholicRequiredToolNotCalledException extends RuntimeException {
    private final String functionName;

    public CatholicRequiredToolNotCalledException(String functionName) {
        super("Model did not call the required tool in its first response: "
            + Objects.requireNonNull(functionName, "functionName"));
        this.functionName = functionName;
    }

    public String functionName() {
        return functionName;
    }
}
