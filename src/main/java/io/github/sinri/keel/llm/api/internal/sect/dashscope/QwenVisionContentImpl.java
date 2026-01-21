package io.github.sinri.keel.llm.api.internal.sect.dashscope;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.llm.api.sect.dashscope.qwen.message.vision.QwenVisionContent;
import io.vertx.core.json.JsonObject;


public class QwenVisionContentImpl extends JsonifiableDataUnitImpl implements QwenVisionContent {

    public QwenVisionContentImpl() {
        super();
    }

    public QwenVisionContentImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

}
