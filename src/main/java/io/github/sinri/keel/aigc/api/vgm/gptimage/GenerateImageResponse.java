package io.github.sinri.keel.aigc.api.vgm.gptimage;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * @since 5.0.0
 */
public class GenerateImageResponse extends UnmodifiableJsonifiableEntityImpl {
    public GenerateImageResponse( JsonObject jsonObject) {
        super(jsonObject);
    }

    @Nullable
    public Long getCreated() {
        return readLong("created");
    }

    @Nullable
    public List<GptImageResultDatum> getData() {
        List<JsonObject> data = readJsonObjectArray("data");
        if (data == null) return null;
        return data.stream().map(GptImageResultDatum::new).toList();
    }

    @Nullable
    public GptImageApiError getError() {
        JsonObject jsonObject = readJsonObject("error");
        if (jsonObject == null) return null;
        return new GptImageApiError(jsonObject);
    }

    @Nullable
    public GptImageResourceUsage getUsage() {
        JsonObject usage = readJsonObject("usage");
        if (usage == null) return null;
        return new GptImageResourceUsage(usage);
    }

}
