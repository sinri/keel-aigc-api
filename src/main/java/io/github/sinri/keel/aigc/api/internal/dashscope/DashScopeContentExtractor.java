package io.github.sinri.keel.aigc.api.internal.dashscope;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Extracts textual content from DashScope messages.
 *
 * <p>Text models normally return a string while multimodal models return an
 * array such as {@code [{"text":"..."}]}.</p>
 */
public final class DashScopeContentExtractor {

    private DashScopeContentExtractor() {
    }

    public static String extractText(JsonObject message) {
        if (message == null) {
            return null;
        }

        Object content = message.getValue("content");
        if (content == null) {
            return null;
        }
        if (content instanceof String text) {
            return text;
        }
        if (content instanceof JsonArray contentArray) {
            StringBuilder text = new StringBuilder();
            for (int i = 0; i < contentArray.size(); i++) {
                Object item = contentArray.getValue(i);
                if (item instanceof String stringItem) {
                    text.append(stringItem);
                } else if (item instanceof JsonObject objectItem) {
                    String textItem = objectItem.getString("text");
                    if (textItem != null) {
                        text.append(textItem);
                    }
                }
            }
            return text.toString();
        }
        throw new IllegalArgumentException(
            "Unsupported DashScope message.content type: " + content.getClass().getName()
        );
    }
}
