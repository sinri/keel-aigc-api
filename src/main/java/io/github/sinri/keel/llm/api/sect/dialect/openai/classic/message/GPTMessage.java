package io.github.sinri.keel.llm.api.sect.dialect.openai.classic.message;

import io.github.sinri.keel.base.json.JsonifiableDataUnit;
import io.github.sinri.keel.llm.api.sect.dialect.openai.classic.message.vision.GPTVisionMessageContent;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface GPTMessage extends JsonifiableDataUnit {
    default @Nullable String getRole() {
        return readString("role");
    }

    default GPTMessage setRole(String role) {
        ensureEntry("role", role);
        return this;
    }

    default @Nullable String getContent() {
        return readString("content");
    }

    default List<GPTVisionMessageContent> getContents() {
        List<JsonObject> array = readJsonObjectArray("content");
        if (array == null) return List.of();
        return array.stream().map(GPTVisionMessageContent::wrap).toList();
    }

    default GPTMessage setTextContent(String content) {
        ensureEntry("content", content);
        return this;
    }

    default GPTMessage setVisionContent(List<GPTVisionMessageContent> contentList) {
        JsonArray array = new JsonArray();
        contentList.forEach(c -> array.add(c.toJsonObject()));
        ensureEntry("content", array);
        return this;
    }
}
