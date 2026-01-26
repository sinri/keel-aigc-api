package io.github.sinri.keel.aigc.api.vgm.seedream.request;

import io.github.sinri.keel.aigc.api.internal.vgm.seedream.Seedream4RequestImpl;
import io.github.sinri.keel.base.json.JsonifiableDataUnit;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * @see <a href="https://www.volcengine.com/docs/82379/1541523">图片生成 API（Seedream 4.0 API）</a>
 * @since 5.0.0
 */
public interface Seedream4Request extends SeedreamRequest<Seedream4Request> {
    static Seedream4Request create() {
        return new Seedream4RequestImpl();
    }

    static Seedream4Request wrap(JsonObject jsonObject) {
        return new Seedream4RequestImpl(jsonObject);
    }

    /**
     * doubao-seedream-4.0 支持单图或多图输入（查看多图融合示例）
     *
     * @return 输入的图片信息
     */
    default @Nullable List<String> getImageList() {
        return readStringArray("image");
    }

    /**
     * 输入的图片信息，支持 URL 或 Base64 编码。
     * <p>
     * doubao-seedream-4.0 支持单图或多图输入。
     *
     * @param imageList 一个列表，每个元素参照 {@link SeedreamRequest#setImage(String)} 的参数定义
     * @see <a
     *         href="https://www.volcengine.com/docs/82379/1824121#%E5%A4%9A%E5%9B%BE%E8%9E%8D%E5%90%88%EF%BC%88%E5%A4%9A%E5%9B%BE%E8%BE%93%E5%85%A5%E5%8D%95%E5%9B%BE%E8%BE%93%E5%87%BA%EF%BC%89">多图融合示例</a>
     */
    default Seedream4Request setImage(List<String> imageList) {
        JsonArray jsonArray = ensureJsonArray("image");
        imageList.forEach(jsonArray::add);
        return getImplementation();
    }

    /**
     * 控制是否关闭组图功能。
     * <p>
     * 仅doubao-seedream-4.0支持该参数。
     *
     * @return {@code auto}：自动判断模式，模型会根据用户提供的提示词自主判断是否返回组图以及组图包含的图片数量。<br>
     *         {@code disabled}：关闭组图功能，模型只会生成一张图。
     * @see <a href="https://www.volcengine.com/docs/82379/1824121#%E5%BF%AB%E9%80%9F%E5%BC%80%E5%A7%8B">查看组图输出示例</a>
     */
    default @Nullable SequentialImageGenerationEnum getSequentialImageGeneration() {
        String s = readString("sequential_image_generation");
        if (s == null) return null;
        return SequentialImageGenerationEnum.valueOf(s);
    }

    /**
     * 控制是否关闭组图功能。
     * <p>
     * 仅doubao-seedream-4.0支持该参数。
     *
     * @param sequentialImageGenerationEnum 默认值 disabled。
     *                                      {@code auto}：自动判断模式，模型会根据用户提供的提示词自主判断是否返回组图以及组图包含的图片数量。<br>
     *                                      {@code disabled}：关闭组图功能，模型只会生成一张图。
     */
    default Seedream4Request setSequentialImageGeneration(SequentialImageGenerationEnum sequentialImageGenerationEnum) {
        ensureEntry("sequential_image_generation", sequentialImageGenerationEnum.name());
        return getImplementation();
    }

    /**
     *
     * @return 组图功能的配置。仅当sequential_image_generation为auto时生效。
     */
    default SequentialImageGenerationOptions getSequentialImageGenerationOptions() {
        JsonObject j = readJsonObject("sequential_image_generation_options");
        if (j == null) {
            j = new JsonObject();
        }
        return SequentialImageGenerationOptions.wrap(j);
    }

    /**
     *
     * @param sequentialImageGenerationOptions 组图功能的配置。仅当sequential_image_generation为auto时生效。
     */
    default Seedream4Request setSequentialImageGenerationOptions(SequentialImageGenerationOptions sequentialImageGenerationOptions) {
        ensureEntry("sequential_image_generation_options", sequentialImageGenerationOptions.toJsonObject());
        return getImplementation();
    }

    /**
     * 控制是否开启流式输出模式。
     * <p>
     * 仅doubao-seedream-4.0支持该参数。
     *
     * @return {@code false}：非流式输出模式，等待所有图片全部生成结束后再一次性返回所有信息。
     *         {@code true}：流式输出模式，即时返回每张图片输出的结果。在生成单图和组图的场景下，流式输出模式均生效。
     */
    default @Nullable Boolean getStream() {
        return readBoolean("stream");
    }

    /**
     * 控制是否开启流式输出模式。
     * <p>
     * 仅doubao-seedream-4.0支持该参数。
     *
     * @param stream {@code false}：非流式输出模式，等待所有图片全部生成结束后再一次性返回所有信息。
     *               {@code true}：流式输出模式，即时返回每张图片输出的结果。在生成单图和组图的场景下，流式输出模式均生效。
     */
    default Seedream4Request setStream(boolean stream) {
        ensureEntry("stream", stream);
        return getImplementation();
    }

    enum SequentialImageGenerationEnum {
        /**
         * 自动判断模式，模型会根据用户提供的提示词自主判断是否返回组图以及组图包含的图片数量。
         */
        auto,
        /**
         * 关闭组图功能，模型只会生成一张图
         */
        disabled
    }

    interface SequentialImageGenerationOptions extends JsonifiableDataUnit {
        static SequentialImageGenerationOptions wrap(JsonObject jsonObject) {
            return new Seedream4RequestImpl.SequentialImageGenerationOptionsImpl(jsonObject);
        }

        static SequentialImageGenerationOptions create() {
            return new Seedream4RequestImpl.SequentialImageGenerationOptionsImpl();
        }

        /**
         * @return 指定本次请求，最多可生成的图片数量。取值范围： {@code [1, 15]}
         */
        default @Nullable Integer getMaxImages() {
            return readInteger("max_images");
        }

        /**
         * 指定本次请求，最多可生成的图片数量。
         * <p>
         * 实际可生成的图片数量，除受到 max_images 影响外，还受到输入的参考图数量影响。输入的参考图数量+最终生成的图片数量≤15张。
         *
         * @param maxImages 取值范围： {@code [1, 15]}，默认值 15。
         */
        default SequentialImageGenerationOptions setMaxImages(Integer maxImages) {
            ensureEntry("max_images", maxImages);
            return this;
        }
    }
}
