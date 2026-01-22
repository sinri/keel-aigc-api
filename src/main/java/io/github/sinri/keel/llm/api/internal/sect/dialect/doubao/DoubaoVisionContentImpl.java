package io.github.sinri.keel.llm.api.internal.sect.dialect.doubao;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.llm.api.sect.dialect.doubao.message.vision.DoubaoVisionContent;
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
