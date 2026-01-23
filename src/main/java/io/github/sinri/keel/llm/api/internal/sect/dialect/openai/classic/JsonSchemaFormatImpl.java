package io.github.sinri.keel.llm.api.internal.sect.dialect.openai.classic;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.llm.api.sect.dialect.openai.classic.request.JsonSchemaFormat;
import io.vertx.core.json.JsonObject;

public class JsonSchemaFormatImpl extends JsonifiableDataUnitImpl implements JsonSchemaFormat {
    public JsonSchemaFormatImpl() {
        super();
    }

    public JsonSchemaFormatImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

}
