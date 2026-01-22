package io.github.sinri.keel.llm.api.sect.dialect.openai.request;

import io.github.sinri.keel.base.json.JsonifiableDataUnit;
import io.github.sinri.keel.llm.api.internal.sect.dialect.openai.JsonSchemaFormatImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

public interface JsonSchemaFormat extends JsonifiableDataUnit {

    static JsonSchemaFormat create() {
        return new JsonSchemaFormatImpl();
    }

    static JsonSchemaFormat wrap(JsonObject jsonObject) {
        return new JsonSchemaFormatImpl(jsonObject);
    }

    default JsonSchemaFormat description(String description) {
        ensureEntry("description", description);
        return this;
    }

    default @Nullable String description() {
        return readString("description");
    }

    default JsonSchemaFormat name(String name) {
        ensureEntry(name, new JsonObject());
        return this;
    }

    default @Nullable String name() {
        return readString("name");
    }

    default JsonSchemaFormat schema(JsonObject schema) {
        ensureEntry("schema", schema);
        return this;
    }

    default @Nullable JsonObject schema() {
        return readJsonObject("schema");
    }

    default JsonSchemaFormat strict(boolean strict) {
        ensureEntry("strict", strict);
        return this;
    }

    default @Nullable Boolean strict() {
        return readBoolean("strict");
    }
}
