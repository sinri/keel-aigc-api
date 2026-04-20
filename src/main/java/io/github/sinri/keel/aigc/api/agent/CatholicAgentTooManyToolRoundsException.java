package io.github.sinri.keel.aigc.api.agent;

/**
 * 工具调用链超过 {@link CatholicAgent} 配置的 {@code maxToolRounds} 时抛出。
 */
public final class CatholicAgentTooManyToolRoundsException extends RuntimeException {

    private final int maxRounds;

    public CatholicAgentTooManyToolRoundsException(int maxRounds) {
        super("Tool invocation exceeded max rounds: " + maxRounds);
        this.maxRounds = maxRounds;
    }

    public int maxRounds() {
        return maxRounds;
    }
}
