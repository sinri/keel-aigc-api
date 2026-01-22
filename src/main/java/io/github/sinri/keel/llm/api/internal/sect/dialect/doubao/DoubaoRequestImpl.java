package io.github.sinri.keel.llm.api.internal.sect.dialect.doubao;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.llm.api.sect.dialect.doubao.request.DoubaoRequest;
import io.vertx.core.json.JsonObject;


public class DoubaoRequestImpl extends JsonifiableDataUnitImpl implements DoubaoRequest {
    public DoubaoRequestImpl() {
        super();
    }

    public DoubaoRequestImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

}
