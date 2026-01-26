package io.github.sinri.keel.aigc.api.internal.vgm.dalle;

import io.github.sinri.keel.aigc.api.vgm.dalle.v3.Dalle3Response;
import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.vertx.core.json.JsonObject;


public class Dalle3ResponseImpl extends UnmodifiableJsonifiableEntityImpl implements Dalle3Response {
    public Dalle3ResponseImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
