package io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.doubao;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.response.stream.DoubaoResponseChunkChoice;
import io.vertx.core.json.JsonObject;


public class DoubaoResponseChunkChoiceImpl extends UnmodifiableJsonifiableEntityImpl implements DoubaoResponseChunkChoice {
    public DoubaoResponseChunkChoiceImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
