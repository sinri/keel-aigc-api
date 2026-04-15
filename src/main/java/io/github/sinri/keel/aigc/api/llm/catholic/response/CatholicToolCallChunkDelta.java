package io.github.sinri.keel.aigc.api.llm.catholic.response;

/**
 * 流式回复中工具调用的增量片段。
 */
public record CatholicToolCallChunkDelta(
    String id,
    String type,
    int index,
    CatholicToolCallFunctionChunkDelta function
) {
    /**
     * 工具调用函数的增量信息
     */
    public record CatholicToolCallFunctionChunkDelta(
        String name,
        String argumentsDelta
    ) {
    }
}