package io.github.sinri.keel.llm.api.internal.sect.dialect.doubao;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.llm.api.sect.dialect.doubao.response.stream.DoubaoResponseChunkChoice;
import io.vertx.core.json.JsonObject;


public class DoubaoResponseChunkChoiceImpl extends UnmodifiableJsonifiableEntityImpl implements DoubaoResponseChunkChoice {
    public DoubaoResponseChunkChoiceImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
