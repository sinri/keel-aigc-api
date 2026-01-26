package io.github.sinri.keel.aigc.api.internal.llm.catholic.response;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.aigc.api.llm.catholic.message.MixChatMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.response.MixChatResponse;
import io.vertx.core.json.JsonObject;


public class MixChatResponseImpl extends JsonifiableDataUnitImpl implements MixChatResponse {
    public final static String KEY_MESSAGE = "message";

    public MixChatResponseImpl() {
        super();
    }

    public MixChatResponseImpl(JsonObject jsonObject) {
        super(jsonObject);
    }


    @Override
    public MixChatMessage getMessage() {
        JsonObject message = readJsonObjectRequired(KEY_MESSAGE);
        return MixChatMessage.wrap(message);
    }

    @Override
    public MixChatResponse setMessage(MixChatMessage message) {
        ensureEntry(KEY_MESSAGE, message.toJsonObject());
        return this;
    }
}
