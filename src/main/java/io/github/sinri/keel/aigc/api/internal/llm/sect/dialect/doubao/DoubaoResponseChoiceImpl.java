package io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.doubao;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.response.sync.DoubaoResponseChoice;
import io.vertx.core.json.JsonObject;


public class DoubaoResponseChoiceImpl extends UnmodifiableJsonifiableEntityImpl implements DoubaoResponseChoice {
    public DoubaoResponseChoiceImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
