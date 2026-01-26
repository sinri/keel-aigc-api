package io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.doubao;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.message.vision.ContentImageUrl;
import io.vertx.core.json.JsonObject;


public class ImagePixelLimitImpl extends JsonifiableDataUnitImpl implements ContentImageUrl.ImagePixelLimit {
    public ImagePixelLimitImpl() {
        super();
    }

    public ImagePixelLimitImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

}
