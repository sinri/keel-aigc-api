package io.github.sinri.keel.aigc.api.vgm.dalle.v3;

import io.github.sinri.keel.aigc.api.internal.vgm.dalle.Dalle3RequestImpl;
import io.github.sinri.keel.base.json.JsonifiableDataUnit;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

public interface Dalle3Request extends JsonifiableDataUnit {
    static Dalle3Request create() {
        return new Dalle3RequestImpl();
    }

    static Dalle3Request wrap(JsonObject jsonObject) {
        return new Dalle3RequestImpl(jsonObject);
    }

    default @Nullable String getPrompt() {
        return readString("prompt");
    }

    default Dalle3Request setPrompt(String prompt) {
        ensureEntry("prompt", prompt);
        return this;
    }

    default Dalle3Kit.@Nullable Dalle3Size getSize() {
        String s = readString("size");
        if (s == null) return null;
        return Dalle3Kit.Dalle3Size.valueOf(s);
    }

    default Dalle3Request setSize(Dalle3Kit.Dalle3Size size) {
        ensureEntry("size", size.size());
        return this;
    }

    default Dalle3Kit.@Nullable Dalle3Quality getQuality() {
        String s = readString("quality");
        if (s == null) return null;
        return Dalle3Kit.Dalle3Quality.valueOf(s);
    }

    default Dalle3Request setQuality(Dalle3Kit.Dalle3Quality quality) {
        ensureEntry("quality", quality.name());
        return this;
    }

    default Dalle3Kit.@Nullable Dalle3Style getStyle() {
        String s = readString("style");
        if (s == null) return null;
        return Dalle3Kit.Dalle3Style.valueOf(s);
    }

    default Dalle3Request setStyle(Dalle3Kit.Dalle3Style style) {
        ensureEntry("style", style.name());
        return this;
    }

    default @Nullable Integer getN() {
        return readInteger("n");
    }

    default Dalle3Request setN(int n) {
        ensureEntry("n", n);
        return this;
    }

    default String getRequestId() {
        return readStringRequired("request_id");
    }

    default Dalle3Request setRequestId(String requestId) {
        ensureEntry("request_id", requestId);
        return this;
    }
}
