package io.github.sinri.keel.aigc.api.internal.vgm.wanx;

import io.github.sinri.keel.aigc.api.vgm.wanx.ImageSynthesis.request.WanxImageSynthesisParameters;
import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.vertx.core.json.JsonObject;

public class WanxImageSynthesisParametersImpl extends JsonifiableDataUnitImpl implements WanxImageSynthesisParameters {

    public WanxImageSynthesisParametersImpl(){
        super();
    }
    public WanxImageSynthesisParametersImpl(JsonObject jsonObject){
        super(jsonObject);
    }
}
