package io.github.sinri.keel.llm.api.catholic.request;

import io.github.sinri.keel.base.json.JsonifiableDataUnit;
import io.github.sinri.keel.llm.api.internal.catholic.request.MixChatRequestExtraImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

public interface MixChatRequestExtra extends JsonifiableDataUnit {
    static MixChatRequestExtra create() {
        return new MixChatRequestExtraImpl();
    }

    static MixChatRequestExtra wrap(JsonObject jsonObject) {
        return new MixChatRequestExtraImpl(jsonObject);
    }

    default @Nullable Float getTemperature() {
        return readFloat("temperature");
    }

    default MixChatRequestExtra setTemperature(float temperature) {
        ensureEntry("temperature", temperature);
        return this;
    }


    default @Nullable Integer getSeed() {
        return readInteger("seed");
    }


    default MixChatRequestExtra setSeed(Integer seed) {
        ensureEntry("seed", seed);
        return this;
    }

    default @Nullable JsonObject getResponseFormat() {
        return readJsonObject("response_format");
    }

    /**
     * Let the response format of this chat model request be as the ordered, plain
     * text, a json object, even follow the json schema.
     * Its effect differs with different chat model.
     */
    default MixChatRequestExtra setResponseFormat(JsonObject responseFormat) {
        ensureEntry("response_format", responseFormat);
        return this;
    }
}
