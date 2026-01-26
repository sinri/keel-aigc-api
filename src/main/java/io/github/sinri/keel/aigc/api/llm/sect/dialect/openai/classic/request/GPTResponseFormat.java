package io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.request;

import io.github.sinri.keel.base.json.JsonifiableDataUnit;
import io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.openai.classic.GPTResponseFormatImpl;
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
