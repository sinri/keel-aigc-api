package io.github.sinri.keel.llm.api.sect.dialect.openai.stateful;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.github.sinri.keel.llm.api.internal.sect.dialect.openai.stateful.StatefulChatToolChoiceImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

/**
 * @since 2.0.1
 */
public interface StatefulChatToolChoice extends UnmodifiableJsonifiableEntity {
    static StatefulChatToolChoice wrap(JsonObject jsonObject) {
        return new StatefulChatToolChoiceImpl(jsonObject);
    }

    /**
     * Indicates that the model should use a built-in tool to generate a response.
     */
    default @Nullable String getType() {
        return this.readString("type");
    }
}
