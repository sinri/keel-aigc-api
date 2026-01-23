package io.github.sinri.keel.llm.api.internal.sect.dialect.openai.classic;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.llm.api.sect.dialect.openai.classic.request.GPTRequest;
import io.vertx.core.json.JsonObject;

public class GPTRequestImpl extends JsonifiableDataUnitImpl implements GPTRequest {
    public GPTRequestImpl() {
        super();
    }

    public GPTRequestImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

}
