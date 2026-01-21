package io.github.sinri.keel.llm.api.internal.catholic.response;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.llm.api.catholic.response.stream.MixChatResponseChunk;
import io.vertx.core.json.JsonObject;

public class MixChatResponseChunkImpl extends JsonifiableDataUnitImpl implements MixChatResponseChunk {
    public MixChatResponseChunkImpl() {
        super();
    }

    public MixChatResponseChunkImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

}
