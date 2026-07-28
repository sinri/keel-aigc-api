package io.github.sinri.keel.aigc.api.agent;

/**
 * 表示一次 {@link CatholicAgent} 交互的终止原因。
 * 该枚举是 {@link CatholicAgentResult} 的状态实体，用于区分任务正常完成和因达到
 * 配置的最大轮次而被强制中止。
 */
public enum CatholicAgentTermination {
    COMPLETED,
    ROUND_LIMIT_EXCEEDED
}
