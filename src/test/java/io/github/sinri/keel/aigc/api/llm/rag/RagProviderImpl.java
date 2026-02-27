package io.github.sinri.keel.aigc.api.llm.rag;

import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;

import java.util.List;
@NullMarked
class RagProviderImpl implements RagProvider<RagInstruction, RagReference> {
    private final RagInstruction ragInstruction;

    public RagProviderImpl(RagInstruction ragInstruction) {
        this.ragInstruction = ragInstruction;
    }

    @Override
    public Future<List<RagReference>> getRagReferences(String userPrompt) {
        RagReferenceImpl ref_1 = new RagReferenceImpl(1, 0.92, "alpha", "Machine Alpha marked as deprecated");
        RagReferenceImpl ref_2 = new RagReferenceImpl(2, 0.90, "beta", "Machine Beta is now working");
        RagReferenceImpl ref_3 = new RagReferenceImpl(3, 0.85, "gamma", "Machine Gamma is in plan");

        return Future.succeededFuture(List.of(ref_1, ref_2, ref_3));
    }

    @Override
    public RagInstruction getInstruction() {
        return ragInstruction;
    }
}
