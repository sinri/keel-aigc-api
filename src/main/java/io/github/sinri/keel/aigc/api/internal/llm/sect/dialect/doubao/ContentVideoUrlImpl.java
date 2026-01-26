package io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.doubao;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.message.vision.ContentVideoUrl;
import io.vertx.core.json.JsonObject;


public class ContentVideoUrlImpl extends JsonifiableDataUnitImpl implements ContentVideoUrl {
    public ContentVideoUrlImpl() {
        super();
    }

    public ContentVideoUrlImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

}
