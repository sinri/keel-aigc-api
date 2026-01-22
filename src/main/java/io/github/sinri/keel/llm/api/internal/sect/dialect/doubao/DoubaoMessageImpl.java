package io.github.sinri.keel.llm.api.internal.sect.dialect.doubao;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.llm.api.sect.dialect.doubao.message.DoubaoMessageInChatRequest;
import io.github.sinri.keel.llm.api.sect.dialect.doubao.message.DoubaoMessageInResponse;
import io.github.sinri.keel.llm.api.sect.dialect.doubao.message.DoubaoMessageInVisionRequest;
import io.vertx.core.json.JsonObject;


public class DoubaoMessageImpl extends JsonifiableDataUnitImpl
        implements DoubaoMessageInChatRequest,
        DoubaoMessageInResponse,
        DoubaoMessageInVisionRequest {
    public DoubaoMessageImpl() {
        super();
    }

    public DoubaoMessageImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

}
