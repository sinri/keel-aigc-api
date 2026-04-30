package io.github.sinri.keel.aigc.api.llm.catholic;

import io.github.sinri.keel.aigc.api.internal.catholic.response.CatholicLLMResponseChunkImpl;
import io.github.sinri.keel.aigc.api.llm.catholic.response.CatholicLLMUsage;
import io.github.sinri.keel.aigc.api.llm.catholic.response.CatholicToolCallChunkDelta;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * 一种通用 LLM 回复格式（{@link CatholicLLMResponse}）在 stream 方式调用时的输出片段的接口定义。
 * <p>
 * 在 stream 调用下,通过特定 LLM 回复报文片段转化而来。
 */
public interface CatholicLLMResponseChunk {
    /**
     * 回复ID（同一流式回复的所有片段ID相同）
     */
    String id();

    /**
     * 片段序号（从0开始）
     */
    int index();

    /**
     * 增量文本内容（可能为空）
     */
    @Nullable String deltaText();

    /**
     * 增量工具调用（可能为空）
     */
    @Nullable List<CatholicToolCallChunkDelta> deltaToolCalls();

    /**
     * 是否为结束片段
     */
    boolean isFinished();

    /**
     * 结束时的usage统计（仅在结束片段有效）
     */
    @Nullable CatholicLLMUsage usage();

    /**
     * 是否有文本增量
     */
    default boolean hasDeltaText() {
        return deltaText() != null && !deltaText().isEmpty();
    }

    /**
     * 是否有工具调用增量
     */
    default boolean hasDeltaToolCalls() {
        return deltaToolCalls() != null && !deltaToolCalls().isEmpty();
    }

    /**
     * 创建片段Builder
     */
    static Builder builder() {
        return CatholicLLMResponseChunkImpl.builder();
    }

    /**
     * Builder接口，将构造逻辑暴露给外部模块。
     */
    interface Builder {
        Builder id(String id);

        Builder index(int index);

        Builder deltaText(String deltaText);

        Builder deltaToolCalls(List<CatholicToolCallChunkDelta> deltaToolCalls);

        Builder finished(boolean finished);

        Builder markFinished();

        Builder usage(CatholicLLMUsage usage);

        Builder usage(Integer promptTokens, Integer completionTokens, Integer totalTokens);

        CatholicLLMResponseChunk build();
    }
}