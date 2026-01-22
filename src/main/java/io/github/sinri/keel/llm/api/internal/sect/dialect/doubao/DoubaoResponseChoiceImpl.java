package io.github.sinri.keel.llm.api.internal.sect.dialect.doubao;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.llm.api.sect.dialect.doubao.response.sync.DoubaoResponseChoice;
import io.vertx.core.json.JsonObject;


public class DoubaoResponseChoiceImpl extends UnmodifiableJsonifiableEntityImpl implements DoubaoResponseChoice {
    public DoubaoResponseChoiceImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
