package io.github.sinri.keel.aigc.api.internal.llm.catholic.request;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.aigc.api.llm.catholic.request.MixChatRequestExtra;
import io.vertx.core.json.JsonObject;


public class MixChatRequestExtraImpl extends JsonifiableDataUnitImpl implements MixChatRequestExtra {
    public MixChatRequestExtraImpl() {
        super();
    }

    public MixChatRequestExtraImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

}
