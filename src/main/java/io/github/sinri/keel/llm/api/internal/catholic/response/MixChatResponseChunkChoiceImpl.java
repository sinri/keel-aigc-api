package io.github.sinri.keel.llm.api.internal.catholic.response;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.llm.api.catholic.response.stream.MixChatResponseChunkChoice;
import io.vertx.core.json.JsonObject;


public class MixChatResponseChunkChoiceImpl extends JsonifiableDataUnitImpl implements MixChatResponseChunkChoice {
    public MixChatResponseChunkChoiceImpl() {
        super();
    }

    public MixChatResponseChunkChoiceImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

}
