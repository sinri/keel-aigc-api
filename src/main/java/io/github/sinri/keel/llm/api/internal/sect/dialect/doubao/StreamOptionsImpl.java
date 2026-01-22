package io.github.sinri.keel.llm.api.internal.sect.dialect.doubao;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.llm.api.sect.dialect.doubao.request.StreamOptions;
import io.vertx.core.json.JsonObject;


public class StreamOptionsImpl extends JsonifiableDataUnitImpl implements StreamOptions {
    public StreamOptionsImpl() {
        super();
    }

    public StreamOptionsImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

}