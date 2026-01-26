package io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.message;

import io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.openai.classic.GPTMessageImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.message.vision.GPTVisionMessageContent;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

public interface GPTMessageInVisionRequest extends GPTMessage {
    static GPTMessageInVisionRequest create() {
        return new GPTMessageImpl();
    }

    static GPTMessageInVisionRequest createAsUser(List<GPTVisionMessageContent> contentList) {
        var j = new JsonObject()
                .put("role", "user");

        JsonArray content = new JsonArray();
        for (var c : contentList) {
            content.add(c.toJsonObject());
        }
        j.put("content", content);
        return new GPTMessageImpl(j);
    }
}
