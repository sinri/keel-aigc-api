package io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.doubao;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.request.DoubaoRequest;
import io.vertx.core.json.JsonObject;


public class DoubaoRequestImpl extends JsonifiableDataUnitImpl implements DoubaoRequest {
    public DoubaoRequestImpl() {
        super();
    }

    public DoubaoRequestImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

}
