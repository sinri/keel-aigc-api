package io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.qwen;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.qwen.message.vision.QwenVisionContent;
import io.vertx.core.json.JsonObject;


public class QwenVisionContentImpl extends JsonifiableDataUnitImpl implements QwenVisionContent {

    public QwenVisionContentImpl() {
        super();
    }

    public QwenVisionContentImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

}
