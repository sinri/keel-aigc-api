package io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.stateful;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.openai.stateful.StatefulChatToolChoiceImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

/**
 * @since 5.0.0
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
