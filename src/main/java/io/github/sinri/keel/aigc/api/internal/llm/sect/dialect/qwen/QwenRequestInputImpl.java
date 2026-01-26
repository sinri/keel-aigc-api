package io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.qwen;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.qwen.request.input.QwenRequestInput;
import io.vertx.core.json.JsonObject;


/**
 * @since 5.0.0
 */
public class QwenRequestInputImpl extends JsonifiableDataUnitImpl implements QwenRequestInput {
    public QwenRequestInputImpl() {
        this(new JsonObject());
    }

    public QwenRequestInputImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

}
