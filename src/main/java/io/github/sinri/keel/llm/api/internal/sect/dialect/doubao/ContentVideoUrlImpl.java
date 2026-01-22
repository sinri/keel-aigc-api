package io.github.sinri.keel.llm.api.internal.sect.dialect.doubao;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.llm.api.sect.dialect.doubao.message.vision.ContentVideoUrl;
import io.vertx.core.json.JsonObject;


public class ContentVideoUrlImpl extends JsonifiableDataUnitImpl implements ContentVideoUrl {
    public ContentVideoUrlImpl() {
        super();
    }

    public ContentVideoUrlImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

}
