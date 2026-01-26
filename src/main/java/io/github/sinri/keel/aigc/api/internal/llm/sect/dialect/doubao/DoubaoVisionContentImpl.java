package io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.doubao;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.message.vision.DoubaoVisionContent;
import io.vertx.core.json.JsonObject;


public class DoubaoVisionContentImpl extends JsonifiableDataUnitImpl implements DoubaoVisionContent {
    public DoubaoVisionContentImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

    public DoubaoVisionContentImpl() {
        super();
    }

    @Override
    public DoubaoVisionContent getImplementation() {
        return this;
    }
}
