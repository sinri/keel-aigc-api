package io.github.sinri.keel.llm.api.internal.sect.dialect.openai.classic;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.llm.api.sect.dialect.openai.classic.message.GPTMessageInResponse;
import io.github.sinri.keel.llm.api.sect.dialect.openai.classic.message.GPTMessageInTextRequest;
import io.github.sinri.keel.llm.api.sect.dialect.openai.classic.message.GPTMessageInVisionRequest;
import io.vertx.core.json.JsonObject;

public class GPTMessageImpl extends JsonifiableDataUnitImpl implements GPTMessageInTextRequest, GPTMessageInVisionRequest, GPTMessageInResponse {
    public GPTMessageImpl() {
        super();
    }

    public GPTMessageImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
