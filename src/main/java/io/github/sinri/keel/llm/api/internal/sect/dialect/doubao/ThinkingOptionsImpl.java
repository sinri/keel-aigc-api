package io.github.sinri.keel.llm.api.internal.sect.dialect.doubao;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.llm.api.sect.dialect.doubao.request.ThinkingOptions;
import io.vertx.core.json.JsonObject;


public class ThinkingOptionsImpl extends JsonifiableDataUnitImpl implements ThinkingOptions {
    public ThinkingOptionsImpl() {
        super();
    }

    public ThinkingOptionsImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

}