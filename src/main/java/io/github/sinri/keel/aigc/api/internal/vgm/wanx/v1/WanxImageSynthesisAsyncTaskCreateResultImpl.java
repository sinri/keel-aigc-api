package io.github.sinri.keel.aigc.api.internal.vgm.wanx.v1;

import io.github.sinri.keel.aigc.api.vgm.wanx.v1.response.WanxImageSynthesisAsyncTaskCreateResult;
import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.vertx.core.json.JsonObject;


public class WanxImageSynthesisAsyncTaskCreateResultImpl extends UnmodifiableJsonifiableEntityImpl implements WanxImageSynthesisAsyncTaskCreateResult {
    public WanxImageSynthesisAsyncTaskCreateResultImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
