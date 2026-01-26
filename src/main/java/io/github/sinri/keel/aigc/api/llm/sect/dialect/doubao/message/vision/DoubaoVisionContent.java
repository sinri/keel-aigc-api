package io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.message.vision;

import io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.doubao.DoubaoVisionContentImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.core.OpenAICompatibleVisionContent;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

public interface DoubaoVisionContent extends OpenAICompatibleVisionContent<DoubaoVisionContent, ContentImageUrl> {
    static DoubaoVisionContent create() {
        return new DoubaoVisionContentImpl();
    }

    static DoubaoVisionContent wrap(JsonObject jsonObject) {
        return new DoubaoVisionContentImpl(jsonObject);
    }

    /**
     * @return 图片消息的内容部分。
     */
    default ContentImageUrl getImageUrl() {
        JsonObject x = readJsonObject("image_url");
        Objects.requireNonNull(x);
        return ContentImageUrl.wrap(x);
    }
}
