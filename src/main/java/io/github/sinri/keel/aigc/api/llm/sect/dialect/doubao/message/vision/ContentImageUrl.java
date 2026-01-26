package io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.message.vision;

import io.github.sinri.keel.base.json.JsonifiableDataUnit;
import io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.doubao.ContentImageUrlImpl;
import io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.doubao.ImagePixelLimitImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.core.OpenAICompatibleVisionContentForImageUrl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

public interface ContentImageUrl extends OpenAICompatibleVisionContentForImageUrl<ContentImageUrl> {

    static ContentImageUrl create() {
        return new ContentImageUrlImpl();
    }

    static ContentImageUrl wrap(JsonObject jsonObject) {
        return new ContentImageUrlImpl(jsonObject);
    }

    default @Nullable String getDetail() {
        return readString("detail");
    }

    /**
     * @param detail 默认值 low <br>
     *               取值范围：high、low、auto。<br>
     *               支持手动设置图片的质量。<br>
     *               high：高细节模式，适用于需要理解图像细节信息的场景，如对图像的多个局部信息/特征提取、复杂/丰富细节的图像理解等场景，理解更全面。此时 min_pixels 取值3136、max_pixels
     *               取值4014080。<br>
     *               low：低细节模式，适用于简单的图像分类/识别、整体内容理解/描述等场景，理解更快速。此时 min_pixels 取值3136、max_pixels 取值1048576。<br>
     *               auto：默认模式，不同模型选择的模式略有不同，具体请参见理解图像的深度控制。<br>
     * @see <a href="https://www.volcengine.com/docs/82379/1362931#bf4d9224">理解图像的深度控制</a>
     */
    default ContentImageUrl setDetail(String detail) {
        ensureEntry("detail", detail);
        return this;
    }

    default @Nullable ImagePixelLimit getImagePixelLimit() {
        JsonObject x = readJsonObject("image_pixel_limit");
        if (x == null) return null;
        return ImagePixelLimit.wrap(x);
    }

    /**
     * 允许设置图片的像素大小限制，如果不在此范围，则会等比例放大或者缩小至该范围内。<br>
     * 生效优先级：高于 detail 字段，即同时配置 detail 与 image_pixel_limit 字段时，生效 image_pixel_limit 字段配置。<br>
     * 若 min_pixels / max_pixels 字段未设置，使用 detail 设置配置的值对应的min_pixels / max_pixels 值。<br>
     * 子字段取值逻辑：3136 ≤ min_pixels ≤ max_pixels ≤ 4014080
     */
    default ContentImageUrl setImagePixelLimit(@Nullable ImagePixelLimit imagePixelLimit) {
        ensureEntry("image_pixel_limit", imagePixelLimit == null ? null : imagePixelLimit.toJsonObject());
        return this;
    }

    interface ImagePixelLimit extends JsonifiableDataUnit {
        static ImagePixelLimit create() {
            return new ImagePixelLimitImpl();
        }

        static ImagePixelLimit wrap(JsonObject jsonObject) {
            return new ImagePixelLimitImpl(jsonObject);
        }

        default @Nullable Integer getMaxPixels() {
            return readInteger("max_pixels");
        }

        /**
         * @param maxPixels 取值范围：(min_pixels,  4014080]。
         *                  传入图片最大像素限制，大于此像素则等比例缩小至 max_pixels 字段取值以下。
         *                  若未设置，则取值为 detail 设置配置的值对应的 max_pixels 值。
         */
        default ImagePixelLimit setMaxPixels(int maxPixels) {
            ensureEntry("max_pixels", maxPixels);
            return this;
        }

        default @Nullable Integer getMinPixels() {
            return readInteger("min_pixels");
        }

        /**
         * @param minPixels 取值范围：[3136,  max_pixels)。
         *                  传入图片最小像素限制，小于此像素则等比例放大至 min_pixels 字段取值以上。
         *                  若未设置，则取值为 detail 设置配置的值对应的 min_pixels 值（3136）。
         */
        default ImagePixelLimit setMinPixels(int minPixels) {
            ensureEntry("min_pixels", minPixels);
            return this;
        }
    }
}

