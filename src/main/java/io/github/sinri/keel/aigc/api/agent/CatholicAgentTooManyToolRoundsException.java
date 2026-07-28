package io.github.sinri.keel.aigc.api.agent;

/**
 * 表示兼容性 {@code chat} API 无法返回完整结果时的轮次超限异常。
 * 当 {@link CatholicAgent} 的工具调用链达到配置的最大轮次、交互未能正常完成时，
 * {@code chat} 方法以此异常暴露失败；需要检查终止原因及 transcript 的调用方应改用
 * {@link CatholicAgent#interact(String)}。
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
