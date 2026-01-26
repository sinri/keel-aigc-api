package io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.doubao;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.request.ThinkingOptions;
import io.vertx.core.json.JsonObject;


public class ThinkingOptionsImpl extends JsonifiableDataUnitImpl implements ThinkingOptions {
    public ThinkingOptionsImpl() {
        super();
    }

    public ThinkingOptionsImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

}