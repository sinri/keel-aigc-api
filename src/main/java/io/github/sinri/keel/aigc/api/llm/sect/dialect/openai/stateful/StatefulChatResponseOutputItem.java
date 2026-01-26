package io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.stateful;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.openai.stateful.StatefulChatResponseOutputItemImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.stateful.content.StatefulChatItemContentOutputText;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * @since 2.0.1
 */
public interface StatefulChatResponseOutputItem extends UnmodifiableJsonifiableEntity {
    static StatefulChatResponseOutputItem wrap(JsonObject jsonObject) {
        return new StatefulChatResponseOutputItemImpl(jsonObject);
    }

    default @Nullable String getId() {
        return this.readString("id");
    }

    default @Nullable String getType() {
        return this.readString("type");
    }

    default @Nullable String getStatus() {
        return this.readString("status");
    }

    default List<StatefulChatItemContentOutputText> getContent() {
        var a = this.readJsonObjectArray("content");
        if (a == null) {
            return List.of();
        }
        return a.stream().map(StatefulChatItemContentOutputText::wrap).toList();
    }

    default @Nullable String getRole() {
        return this.readString("role");
    }
}
