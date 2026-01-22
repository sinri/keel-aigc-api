package io.github.sinri.keel.llm.api.internal.sect.dialect.doubao;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.llm.api.sect.dialect.doubao.message.vision.ContentImageUrl;
import io.vertx.core.json.JsonObject;


// 包私有实现类
public class ContentImageUrlImpl extends JsonifiableDataUnitImpl implements ContentImageUrl {
    public ContentImageUrlImpl() {
        super();
    }

    public ContentImageUrlImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

    @Override
    public ContentImageUrl getImplementation() {
        return this;
    }
}
