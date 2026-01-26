package io.github.sinri.keel.aigc.api.llm.catholic.response.stream;

import io.github.sinri.keel.base.json.JsonifiableDataUnit;
import io.github.sinri.keel.aigc.api.internal.llm.catholic.response.MixChatResponseChunkImpl;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface MixChatResponseChunk extends JsonifiableDataUnit {
    static MixChatResponseChunk create() {
        return new MixChatResponseChunkImpl();
    }

    static MixChatResponseChunk wrap(JsonObject x) {
        return new MixChatResponseChunkImpl(x);
    }

    default @Nullable Integer getCreated() {
        return readInteger("created");
    }

    default MixChatResponseChunk setCreated(@Nullable Integer created) {
        ensureEntry("created", created);
        return this;
    }

    default @Nullable String getId() {
        return this.readString("id");
    }

    default MixChatResponseChunk setId(@Nullable String id) {
        ensureEntry("id", id);
        return this;
    }

    default @Nullable String getModel() {
        return this.readString("model");
    }

    default MixChatResponseChunk setModel(@Nullable String model) {
        ensureEntry("model", model);
        return this;
    }

    default @Nullable String getObject() {
        return this.readString("object");
    }

    default MixChatResponseChunk setObject(@Nullable String object) {
        ensureEntry("object", object);
        return this;
    }

    default List<MixChatResponseChunkChoice> getChoices() {
        List<JsonObject> array = readJsonObjectArray("choices");
        if (array == null)
            return List.of();
        return array.stream().map(MixChatResponseChunkChoice::wrap).toList();
    }

    default MixChatResponseChunk setChoices(List<MixChatResponseChunkChoice> choices) {
        JsonArray array=new JsonArray();
        choices.forEach(x->array.add(x.toJsonObject()));
        ensureEntry("choices", array);
        return this;
    }
}
