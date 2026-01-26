package io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.qwen;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.qwen.request.QwenRequest;
import io.vertx.core.json.JsonObject;

public class QwenRequestImpl extends JsonifiableDataUnitImpl implements QwenRequest {
    public QwenRequestImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

    public QwenRequestImpl() {
        super();
    }

}
