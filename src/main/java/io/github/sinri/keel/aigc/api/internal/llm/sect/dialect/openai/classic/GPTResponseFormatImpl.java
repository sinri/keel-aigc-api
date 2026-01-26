package io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.openai.classic;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.request.GPTResponseFormat;
import io.vertx.core.json.JsonObject;

public class GPTResponseFormatImpl extends JsonifiableDataUnitImpl implements GPTResponseFormat {
    public GPTResponseFormatImpl() {
        super();
    }

    public GPTResponseFormatImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

}
