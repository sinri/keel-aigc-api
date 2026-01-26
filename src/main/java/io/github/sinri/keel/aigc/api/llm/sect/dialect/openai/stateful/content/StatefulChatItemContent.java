package io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.stateful.content;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.openai.stateful.StatefulChatItemContentImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

/**
 * OpenAI.ItemContent.
 *
 * @see <a
 *         href="https://learn.microsoft.com/en-us/azure/ai-services/openai/reference-preview-latest#discriminator-for-openaiitemcontent">Discriminator
 *         for OpenAI.ItemContent</a>
 * @since 2.0.1
 */
public interface StatefulChatItemContent extends UnmodifiableJsonifiableEntity {
    static StatefulChatItemContent wrap(JsonObject jsonObject) {
        return new StatefulChatItemContentImpl(jsonObject);
    }

    /**
     * @return This component uses the property type to discriminate between different types.
     */
    default @Nullable String getType() {
        return this.readString("type");
    }


}
