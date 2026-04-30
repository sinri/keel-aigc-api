package io.github.sinri.keel.aigc.api.llm.catholic.response;

import org.jspecify.annotations.Nullable;

/**
 * 流式回复中工具调用的增量片段。
 */
public record CatholicToolCallChunkDelta(
    @Nullable String id,
    @Nullable String type,
    int index,
    @Nullable CatholicToolCallFunctionChunkDelta function
) {
    /**
     * 工具调用函数的增量信息
     */
    public record CatholicToolCallFunctionChunkDelta(
        @Nullable String name,
        @Nullable String argumentsDelta
    ) {
    }
}