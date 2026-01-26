package io.github.sinri.keel.aigc.api.llm.sect.dialect.qwen.request.parameters;

import io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.qwen.QwenRequestParametersImpl;
import io.vertx.core.json.JsonObject;
/**
 * @since 5.0.0
 */
public interface QwenRequestParameters
        extends QwenRequestParametersThinkMixin<QwenRequestParameters>,
        QwenRequestParametersVLMixin<QwenRequestParameters>,
        QwenRequestParametersOCRMixin<QwenRequestParameters>,
        QwenRequestParametersToolCallMixin<QwenRequestParameters>,
        QwenRequestParametersTranslateMixin<QwenRequestParameters>,
        QwenRequestParametersSearchMixin<QwenRequestParameters> {

    static QwenRequestParameters create() {
        return new QwenRequestParametersImpl();
    }

    static QwenRequestParameters wrap(JsonObject jsonObject) {
        return new QwenRequestParametersImpl(jsonObject);
    }
}
