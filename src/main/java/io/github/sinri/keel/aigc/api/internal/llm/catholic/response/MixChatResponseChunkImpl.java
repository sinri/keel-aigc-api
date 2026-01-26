package io.github.sinri.keel.aigc.api.internal.llm.catholic.response;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.aigc.api.llm.catholic.response.stream.MixChatResponseChunk;
import io.vertx.core.json.JsonObject;

public class MixChatResponseChunkImpl extends JsonifiableDataUnitImpl implements MixChatResponseChunk {
    public MixChatResponseChunkImpl() {
        super();
    }

    public MixChatResponseChunkImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

}
