package io.github.sinri.keel.llm.api.internal.sect.dialect.openai;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.llm.api.sect.dialect.openai.request.GPTResponseFormat;
import io.vertx.core.json.JsonObject;

public class GPTResponseFormatImpl extends JsonifiableDataUnitImpl implements GPTResponseFormat {
    public GPTResponseFormatImpl() {
        super();
    }

    public GPTResponseFormatImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

}
