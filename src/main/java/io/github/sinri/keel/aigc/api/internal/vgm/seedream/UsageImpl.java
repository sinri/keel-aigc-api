package io.github.sinri.keel.aigc.api.internal.vgm.seedream;

import io.github.sinri.keel.aigc.api.vgm.seedream.response.Usage;
import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.vertx.core.json.JsonObject;


/**
 * @since 5.0.0
 */
public class UsageImpl extends UnmodifiableJsonifiableEntityImpl implements Usage {

    public UsageImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
