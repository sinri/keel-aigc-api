package io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.doubao;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.message.vision.ContentImageUrl;
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
