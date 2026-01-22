package io.github.sinri.keel.llm.api.internal.sect.dialect.qwen;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.llm.api.sect.dialect.qwen.request.input.QwenRequestInput;
import io.vertx.core.json.JsonObject;


/**
 * @since 2.0.0
 */
public class QwenRequestInputImpl extends JsonifiableDataUnitImpl implements QwenRequestInput {
    public QwenRequestInputImpl() {
        this(new JsonObject());
    }

    public QwenRequestInputImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

}
