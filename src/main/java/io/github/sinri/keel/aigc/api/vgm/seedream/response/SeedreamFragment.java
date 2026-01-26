package io.github.sinri.keel.aigc.api.vgm.seedream.response;

import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * @since 5.0.0
 */
public class SeedreamFragment {
    private @Nullable String event;
    private @Nullable JsonObject data;

    public SeedreamFragment(String fragmentString) {
        // System.out.println("DEBUG:SeedreamFragment accept: " + fragmentString);
        // 不处理 `data: [DONE]`
        String[] lines = fragmentString.split("[\r\n]+");
        for (String line : lines) {
            String[] parts = line.split(":\\s+");
            if (parts.length > 0) {
                var key = parts[0];
                var value = parts.length > 1 ? parts[1] : "";

                // System.out.println("DEBUG:SeedreamFragment > " + key + ": " + value);

                if (Objects.equals("event", key)) {
                    this.event = value;
                    continue;
                } else if (Objects.equals("data", key)) {
                    this.data = new JsonObject(value);
                    continue;
                }
            }
        }
    }

    public @Nullable String getEvent() {
        return event;
    }

    public @Nullable JsonObject getData() {
        return data;
    }

    public SeedreamEvent toSeedreamEvent() {
        if (event == null || data == null) throw new RuntimeException("Invalid Seedream Fragment: " + this);
        var j = new JsonObject()
                .put("timestamp", System.currentTimeMillis())
                .put("event", event)
                .put("data", data);
        return switch (event) {
            case SeedreamEventPartialSucceeded.EVENT_TYPE -> new SeedreamEventPartialSucceeded(j);
            case SeedreamEventPartialFailed.EVENT_TYPE -> new SeedreamEventPartialFailed(j);
            case SeedreamEventCompleted.EVENT_TYPE -> new SeedreamEventCompleted(j);
            default -> throw new RuntimeException("Unexpected event type: " + event);
        };
    }
}
