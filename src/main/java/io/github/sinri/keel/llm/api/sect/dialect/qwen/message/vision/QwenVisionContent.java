package io.github.sinri.keel.llm.api.sect.dialect.qwen.message.vision;

import io.github.sinri.keel.base.json.JsonifiableDataUnit;
import io.github.sinri.keel.llm.api.internal.sect.dialect.qwen.QwenVisionContentImpl;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface QwenVisionContent extends JsonifiableDataUnit {
    static QwenVisionContent create() {
        return new QwenVisionContentImpl();
    }

    static QwenVisionContent wrap(JsonObject jsonObject) {
        return new QwenVisionContentImpl(jsonObject);
    }

    default @Nullable String getText() {
        return readString("text");
    }

    /**
     * @param text 传入的文本信息
     */
    default QwenVisionContent setText(String text) {
        ensureEntry("text", text);
        return this;
    }

    default @Nullable String getImage() {
        return readString("image");
    }

    /**
     * 使用Qwen-VL 模型进行图片理解时，传入的图片文件。可以为图片的URL或本地路径信息。
     * 传入本地文件请参见本地文件（Qwen-VL）或本地文件（QVQ）。
     */
    default QwenVisionContent setImage(String image) {
        ensureEntry("image", image);
        return this;
    }

    default @Nullable Boolean getEnableRotate() {
        return readBoolean("enable_rotate");
    }

    /**
     * 使用通义千问OCR模型进行文字提取前对图像进行自动转正。
     *
     * @param enableRotate 与image参数一起使用，默认值：false。
     */
    default QwenVisionContent setEnableRotate(Boolean enableRotate) {
        ensureEntry("enable_rotate", enableRotate);
        return this;
    }

    default @Nullable String getVideo() {
        return readString("video");
    }

    /**
     * 使用Qwen-VL 模型或QVQ模型进行视频理解时传入的视频文件。
     * 对于Qwen-VL 模型，仅部分模型可直接传入视频文件，详情请参见视频理解（Qwen-VL）；对于QVQ模型，可直接传入视频文件。
     *
     * @param video 视频文件
     */
    default QwenVisionContent setVideoAsFile(String video) {
        ensureEntry("video", video);
        return this;
    }

    /**
     * 使用Qwen-VL 模型或QVQ模型进行视频理解时传入的视频文件
     *
     * @param video 图像列表
     */
    default QwenVisionContent setVideoAsImages(List<String> video) {
        ensureEntry("video", new JsonArray(video));
        return this;
    }

    default @Nullable Float getFps() {
        return readFloat("fps");
    }

    /**
     * @param fps 与video参数一起使用，取值范围为 (0.1, 10)，默认值为2.0
     */
    default QwenVisionContent setFps(float fps) {
        ensureEntry("fps", fps);
        return this;
    }

    default @Nullable String getAudio() {
        return readString("audio");
    }

    /**
     * 模型为音频理解类模型时，是必选参数，如模型为qwen2-audio-instruct等。
     *
     * @param audio 使用音频理解功能时，传入的音频文件。
     */
    default QwenVisionContent setAudio(String audio) {
        ensureEntry("audio", audio);
        return this;
    }

    default @Nullable Integer getMinPixels() {
        return readInteger("min_pixels");
    }

    /**
     * 当通义千问OCR模型限制输入图像的最小像素时需要设置的参数。
     * 当输入图像像素小于min_pixels时，会将图像按原比例放大，直到总像素高于min_pixels。
     *
     * @param minPixels 与image参数一起使用，默认值：3136，最小值：100。
     */
    default QwenVisionContent setMinPixels(int minPixels) {
        ensureEntry("min_pixels", minPixels);
        return this;
    }

    default @Nullable Integer getMaxPixels() {
        return readInteger("max_pixels");
    }

    /**
     * 当通义千问OCR模型限制输入图像的最大像素时需要设置的参数。
     * 当输入图像像素在[min_pixels, max_pixels]区间内时，模型会按原图进行识别。当输入图像像素大于max_pixels时，会将图像按原比例缩小，直到总像素低于max_pixels。
     *
     * @param maxPixels 与image_url参数一起使用，默认值：6422528，最大值：23520000。
     */
    default QwenVisionContent setMaxPixels(int maxPixels) {
        ensureEntry("max_pixels", maxPixels);
        return this;
    }
}
