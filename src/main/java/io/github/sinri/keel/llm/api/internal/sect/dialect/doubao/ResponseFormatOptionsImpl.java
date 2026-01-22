package io.github.sinri.keel.llm.api.internal.sect.dialect.doubao;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.llm.api.sect.dialect.doubao.request.ResponseFormatOptions;
import io.vertx.core.json.JsonObject;


public class ResponseFormatOptionsImpl extends JsonifiableDataUnitImpl
        implements ResponseFormatOptions {
    public ResponseFormatOptionsImpl() {
        super();
    }

    public ResponseFormatOptionsImpl(JsonObject jsonObject) {
        super(jsonObject);
    }


    public static class JsonSchemaDefImpl extends JsonifiableDataUnitImpl implements JsonSchemaDef {
        public JsonSchemaDefImpl() {
            super();
        }

        public JsonSchemaDefImpl(JsonObject jsonObject) {
            super(jsonObject);
        }

    }
}
