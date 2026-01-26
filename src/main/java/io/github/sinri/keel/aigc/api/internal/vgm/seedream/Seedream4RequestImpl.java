package io.github.sinri.keel.aigc.api.internal.vgm.seedream;

import io.github.sinri.keel.aigc.api.vgm.seedream.request.Seedream4Request;
import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.vertx.core.json.JsonObject;


/**
 * @since 5.0.0
 */
public class Seedream4RequestImpl extends JsonifiableDataUnitImpl implements Seedream4Request {
    public Seedream4RequestImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

    public Seedream4RequestImpl() {
        this(new JsonObject());
    }

    @Override
    public Seedream4Request getImplementation() {
        return this;
    }

    public static class SequentialImageGenerationOptionsImpl extends JsonifiableDataUnitImpl implements SequentialImageGenerationOptions {
        public SequentialImageGenerationOptionsImpl(JsonObject jsonObject) {
            super(jsonObject);
        }

        public SequentialImageGenerationOptionsImpl() {
            this(new JsonObject());
        }
    }
}
