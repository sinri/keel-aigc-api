package io.github.sinri.keel.aigc.api.internal.vgm.seedream;

import io.github.sinri.keel.aigc.api.vgm.seedream.response.SeedreamResponse;
import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.vertx.core.json.JsonObject;


/**
 * @since 5.0.0
 */
public class SeedreamResponseImpl extends UnmodifiableJsonifiableEntityImpl implements SeedreamResponse {
    public SeedreamResponseImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

    public static class DatumImpl extends UnmodifiableJsonifiableEntityImpl implements SeedreamResponse.Datum {

        public DatumImpl(JsonObject jsonObject) {
            super(jsonObject);
        }
    }

}
