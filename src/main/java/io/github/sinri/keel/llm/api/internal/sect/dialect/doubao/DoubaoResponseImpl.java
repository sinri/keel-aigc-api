package io.github.sinri.keel.llm.api.internal.sect.dialect.doubao;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.llm.api.sect.dialect.doubao.response.sync.DoubaoResponse;
import io.vertx.core.json.JsonObject;


public class DoubaoResponseImpl extends UnmodifiableJsonifiableEntityImpl implements DoubaoResponse {

    public DoubaoResponseImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

}
