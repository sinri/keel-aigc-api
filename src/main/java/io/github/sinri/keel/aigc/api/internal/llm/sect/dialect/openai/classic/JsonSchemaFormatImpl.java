package io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.openai.classic;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.request.JsonSchemaFormat;
import io.vertx.core.json.JsonObject;

public class JsonSchemaFormatImpl extends JsonifiableDataUnitImpl implements JsonSchemaFormat {
    public JsonSchemaFormatImpl() {
        super();
    }

    public JsonSchemaFormatImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

}
