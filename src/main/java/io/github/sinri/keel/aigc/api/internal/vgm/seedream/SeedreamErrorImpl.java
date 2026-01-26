package io.github.sinri.keel.aigc.api.internal.vgm.seedream;

import io.github.sinri.keel.aigc.api.vgm.seedream.response.SeedreamError;
import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.vertx.core.json.JsonObject;


/**
 * @since 5.0.0
 */
public class SeedreamErrorImpl extends UnmodifiableJsonifiableEntityImpl implements SeedreamError {

    public SeedreamErrorImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
