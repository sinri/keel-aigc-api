package io.github.sinri.keel.aigc.api.llm.sect.dialect.qwen.request.parameters;


import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

/**
 * @since 2.0.0
 */
interface QwenRequestParametersOCRMixin<E> extends QwenRequestParametersCore<E> {
    /**
     * 当您使用通义千问OCR模型执行内置任务时需要配置的参数。
     */
    default E ocrOptions(QwenRequestOcrOptions ocrOptions) {
        ensureEntry("ocr_options", ocrOptions.toJsonObject());
        return getImplementation();
    }

    default @Nullable QwenRequestOcrOptions ocrOptions() {
        JsonObject x = readJsonObject("ocr_options");
        if (x == null) {
            return null;
        }
        return QwenRequestOcrOptions.wrap(x);
    }

}
