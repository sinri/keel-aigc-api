package io.github.sinri.keel.aigc.api.llm.rag;

import org.jspecify.annotations.NullMarked;

@NullMarked
record RagReferenceImpl(
        int index,
        double score,
        String identity,
        String content
) implements RagReference {
}
