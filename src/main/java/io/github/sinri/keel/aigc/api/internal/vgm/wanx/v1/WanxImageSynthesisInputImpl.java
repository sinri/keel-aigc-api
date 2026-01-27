package io.github.sinri.keel.aigc.api.internal.vgm.wanx.v1;

import io.github.sinri.keel.aigc.api.vgm.wanx.v1.request.WanxImageSynthesisInput;
import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.vertx.core.json.JsonObject;


public class WanxImageSynthesisInputImpl extends JsonifiableDataUnitImpl implements WanxImageSynthesisInput {
    public WanxImageSynthesisInputImpl(){
        super();
    }
    public WanxImageSynthesisInputImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
