package io.github.sinri.keel.llm.api.internal.sect.dialect.openai;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.llm.api.sect.dialect.openai.message.GPTMessageInResponse;
import io.github.sinri.keel.llm.api.sect.dialect.openai.message.GPTMessageInTextRequest;
import io.github.sinri.keel.llm.api.sect.dialect.openai.message.GPTMessageInVisionRequest;
import io.vertx.core.json.JsonObject;

public class GPTMessageImpl extends JsonifiableDataUnitImpl implements GPTMessageInTextRequest, GPTMessageInVisionRequest, GPTMessageInResponse {
    public GPTMessageImpl() {
        super();
    }

    public GPTMessageImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
