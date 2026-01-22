package io.github.sinri.keel.llm.api.internal.sect.dialect.qwen;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.llm.api.sect.dialect.qwen.request.QwenRequest;
import io.vertx.core.json.JsonObject;

public class QwenRequestImpl extends JsonifiableDataUnitImpl implements QwenRequest {
    public QwenRequestImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

    public QwenRequestImpl() {
        super();
    }

}
