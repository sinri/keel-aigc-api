package io.github.sinri.keel.llm.api.sect.dialect.openai.stateful;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.github.sinri.keel.llm.api.internal.sect.dialect.openai.stateful.StatefulChatIncompleteDetailsImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

/**
 * @since 2.0.1
 */
public interface StatefulChatIncompleteDetails extends UnmodifiableJsonifiableEntity {
    static StatefulChatIncompleteDetails wrap(JsonObject jsonObject) {
        return new StatefulChatIncompleteDetailsImpl(jsonObject);
    }

    /**
     * The reason why the response is incomplete.
     *
     * @return Possible values: max_output_tokens, content_filter
     */
    default @Nullable String getReason() {
        return this.readString("reason");
    }
}
