package io.github.sinri.keel.aigc.api.internal.llm.catholic.response;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.aigc.api.llm.catholic.response.stream.MixChatResponseChunkChoice;
import io.vertx.core.json.JsonObject;


public class MixChatResponseChunkChoiceImpl extends JsonifiableDataUnitImpl implements MixChatResponseChunkChoice {
    public MixChatResponseChunkChoiceImpl() {
        super();
    }

    public MixChatResponseChunkChoiceImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

}
