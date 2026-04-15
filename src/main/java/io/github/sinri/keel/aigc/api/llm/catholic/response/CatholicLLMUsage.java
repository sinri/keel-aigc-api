package io.github.sinri.keel.aigc.api.llm.catholic.response;

/**
 * Token用量统计。
 */
public record CatholicLLMUsage(
    Integer promptTokens,
    Integer completionTokens,
    Integer totalTokens
) {
    /**
     * 创建空统计
     */
    public static CatholicLLMUsage empty() {
        return new CatholicLLMUsage(null, null, null);
    }

    /**
     * 计算总token数（如果各个值都存在）
     */
    public Integer computeTotalIfMissing() {
        if (totalTokens != null) {
            return totalTokens;
        }
        if (promptTokens != null && completionTokens != null) {
            return promptTokens + completionTokens;
        }
        return null;
    }
}