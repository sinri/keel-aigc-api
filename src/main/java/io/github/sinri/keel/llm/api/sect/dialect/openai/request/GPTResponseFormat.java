package io.github.sinri.keel.llm.api.sect.dialect.openai.request;

import io.github.sinri.keel.base.json.JsonifiableDataUnit;
import io.github.sinri.keel.llm.api.internal.sect.dialect.openai.GPTResponseFormatImpl;
import io.vertx.core.json.JsonObject;

public interface GPTResponseFormat extends JsonifiableDataUnit {
    static GPTResponseFormat wrap(JsonObject jsonObject) {
        return new GPTResponseFormatImpl(jsonObject);
    }

    static GPTResponseFormat createAsText() {
        return new GPTResponseFormatImpl(new JsonObject()
                .put("type", "text"));
    }

    static GPTResponseFormat createAsJsonObject() {
        return new GPTResponseFormatImpl(new JsonObject()
                .put("type", "json_object"));
    }

    static GPTResponseFormat createAsJsonSchema(JsonSchemaFormat jsonSchemaFormat) {
        return new GPTResponseFormatImpl(new JsonObject()
                .put("type", "json_schema")
                .put("json_schema", jsonSchemaFormat.toJsonObject()));
    }

}
