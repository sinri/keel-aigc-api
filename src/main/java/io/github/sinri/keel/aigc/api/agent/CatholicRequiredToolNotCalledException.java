package io.github.sinri.keel.aigc.api.agent;

import java.util.Objects;

/** 模型服务未遵循强制工具选择参数、首轮没有调用指定函数时抛出。 */
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
