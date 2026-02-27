package io.github.sinri.keel.aigc.api.llm.rag;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.NullMarked;

@NullMarked
class RagInstructionImpl extends JsonifiableDataUnitImpl implements RagInstruction {
    public RagInstructionImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

    public RagInstructionImpl() {
        super(new JsonObject()
                .put("maximum_rag_reference_count", 5)
        );
    }

    @Override
    public int getMaximumRagReferenceCount() {
        return readIntegerRequired("maximum_rag_reference_count");
    }
}
