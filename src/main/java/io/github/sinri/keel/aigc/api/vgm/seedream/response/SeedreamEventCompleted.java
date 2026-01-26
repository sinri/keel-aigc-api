package io.github.sinri.keel.aigc.api.vgm.seedream.response;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;


/**
 * @since 5.0.0
 */
public final class SeedreamEventCompleted extends UnmodifiableJsonifiableEntityImpl implements SeedreamEvent {
    public static final String EVENT_TYPE = "image_generation.completed";

    public SeedreamEventCompleted(JsonObject jsonObject) {
        super(jsonObject);
    }
    /*
    data:
    {
    "type":"image_generation.completed",
    "model":"doubao-seedream-4-0-250828",
    "created":1759119942,
    "usage":{"generated_images":1,"output_tokens":16464,"total_tokens":16464}}
     */

    public @Nullable Usage getUsage() {
        JsonObject entries = readJsonObject("data", "usage");
        if(entries==null)return null;
        return Usage.wrap(entries);
    }

}
