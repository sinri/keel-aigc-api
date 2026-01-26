package io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.doubao;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.response.sync.DoubaoResponse;
import io.vertx.core.json.JsonObject;


public class DoubaoResponseImpl extends UnmodifiableJsonifiableEntityImpl implements DoubaoResponse {

    public DoubaoResponseImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

}
