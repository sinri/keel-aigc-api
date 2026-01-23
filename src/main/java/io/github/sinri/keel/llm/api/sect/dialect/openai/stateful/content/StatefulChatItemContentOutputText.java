package io.github.sinri.keel.llm.api.sect.dialect.openai.stateful.content;

import io.github.sinri.keel.llm.api.internal.sect.dialect.openai.stateful.StatefulChatItemContentImpl;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

/**
 * Its type is {@code output_text}.
 *
 * @since 2.0.1
 */
public interface StatefulChatItemContentOutputText extends StatefulChatItemContent {
    String TYPE = "output_text";

    static StatefulChatItemContentOutputText wrap(JsonObject jsonObject) {
        return new StatefulChatItemContentImpl(jsonObject);
    }

    /**
     * @return The text output from the model.
     */
    default @Nullable String getText() {
        return this.readString("text");
    }

    /**
     * @return The annotations of the text output.
     */
    default @Nullable JsonArray getAnnotations() {
        return this.readJsonArray("annotations");
    }
}
