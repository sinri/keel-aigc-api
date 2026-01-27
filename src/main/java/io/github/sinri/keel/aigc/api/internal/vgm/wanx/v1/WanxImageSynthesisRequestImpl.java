package io.github.sinri.keel.aigc.api.internal.vgm.wanx.v1;

import io.github.sinri.keel.aigc.api.vgm.wanx.v1.request.WanxImageSynthesisRequest;
import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.vertx.core.json.JsonObject;


public class WanxImageSynthesisRequestImpl extends JsonifiableDataUnitImpl implements WanxImageSynthesisRequest {
    public WanxImageSynthesisRequestImpl() {
        super();
    }

    public WanxImageSynthesisRequestImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
