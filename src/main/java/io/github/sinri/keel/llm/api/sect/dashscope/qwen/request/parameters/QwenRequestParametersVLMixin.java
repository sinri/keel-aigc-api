package io.github.sinri.keel.llm.api.sect.dashscope.qwen.request.parameters;

import org.jspecify.annotations.Nullable;

/**
 * @since 2.0.0
 */
interface QwenRequestParametersVLMixin<E> extends QwenRequestParametersCore<E> {
    /**
     * 是否提高输入图片的默认Token上限。输入图片的默认Token上限为1280，配置为true时输入图片的Token上限为16384。
     */
    default E vlHighResolutionImages(boolean vl_high_resolution_images) {
        ensureEntry("vl_high_resolution_images", vl_high_resolution_images);
        return getImplementation();
    }

    default @Nullable Boolean vlHighResolutionImages() {
        return readBoolean("vl_high_resolution_images");
    }
}
