package io.github.sinri.keel.aigc.api.internal.catholic.response;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponseChunk;
import io.github.sinri.keel.aigc.api.llm.catholic.response.CatholicLLMUsage;
import io.github.sinri.keel.aigc.api.llm.catholic.response.CatholicToolCallChunkDelta;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * CatholicLLMResponseChunk的实现类。
 */
public class CatholicLLMResponseChunkImpl implements CatholicLLMResponseChunk {

    private final String id;
    private final int index;
    private final @Nullable String deltaText;
    private final List<CatholicToolCallChunkDelta> deltaToolCalls;
    private final boolean finished;
    private final CatholicLLMUsage usage;

    public CatholicLLMResponseChunkImpl(
            String id,
            int index,
            @Nullable String deltaText,
            @Nullable List<CatholicToolCallChunkDelta> deltaToolCalls,
            boolean finished,
            @Nullable CatholicLLMUsage usage
    ) {
        this.id = id;
        this.index = index;
        this.deltaText = deltaText;
        this.deltaToolCalls = deltaToolCalls != null ? deltaToolCalls : Collections.emptyList();
        this.finished = finished;
        this.usage = usage != null ? usage : CatholicLLMUsage.empty();
    }

    /**
     * 创建Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public int index() {
        return index;
    }

    @Override
    public @Nullable String deltaText() {
        return deltaText;
    }

    @Override
    public List<CatholicToolCallChunkDelta> deltaToolCalls() {
        return deltaToolCalls;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public CatholicLLMUsage usage() {
        return usage;
    }

    /**
     * 是否有文本增量
     */
    public boolean hasDeltaText() {
        return deltaText != null && !deltaText.isEmpty();
    }

    // === Builder ===

    /**
     * 是否有工具调用增量
     */
    public boolean hasDeltaToolCalls() {
        return !deltaToolCalls.isEmpty();
    }

    /**
     * Builder类
     */
    public static class Builder implements CatholicLLMResponseChunk.Builder {
        private @Nullable String id;
        private @Nullable String deltaText;
        private @Nullable CatholicLLMUsage usage;
        private int index;
        private @Nullable List<CatholicToolCallChunkDelta> deltaToolCalls;
        private boolean finished = false;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder index(int index) {
            this.index = index;
            return this;
        }

        public Builder deltaText(@Nullable String deltaText) {
            this.deltaText = deltaText;
            return this;
        }

        public Builder deltaToolCalls(@Nullable List<CatholicToolCallChunkDelta> deltaToolCalls) {
            this.deltaToolCalls = deltaToolCalls;
            return this;
        }

        public Builder finished(boolean finished) {
            this.finished = finished;
            return this;
        }

        public Builder markFinished() {
            this.finished = true;
            return this;
        }

        public Builder usage(@Nullable CatholicLLMUsage usage) {
            this.usage = usage;
            return this;
        }

        public Builder usage(Integer promptTokens, Integer completionTokens, Integer totalTokens) {
            this.usage = new CatholicLLMUsage(promptTokens, completionTokens, totalTokens);
            return this;
        }

        public CatholicLLMResponseChunkImpl build() {
            if (id == null || id.isBlank()) {
                throw new IllegalStateException("CatholicLLMResponseChunk id must be assigned");
            }
            return new CatholicLLMResponseChunkImpl(id, index, deltaText, deltaToolCalls, finished, usage);
        }
    }
}
