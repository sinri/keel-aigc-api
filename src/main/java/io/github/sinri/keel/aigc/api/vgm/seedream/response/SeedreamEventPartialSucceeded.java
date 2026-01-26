package io.github.sinri.keel.aigc.api.vgm.seedream.response;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;


/**
 * 当前仅 doubao-seedream-4.0 支持流式响应。
 * 在流式响应模式下，当任意图片生成成功时返回该事件。
 *
 * @since 5.0.0
 */
public final class SeedreamEventPartialSucceeded extends UnmodifiableJsonifiableEntityImpl implements SeedreamEvent {
    public static final String EVENT_TYPE = "image_generation.partial_succeeded";

    public SeedreamEventPartialSucceeded(JsonObject jsonObject) {
        super(jsonObject);
    }

    /*
    data:
    {
    "type":"image_generation.partial_succeeded",
    "model":"doubao-seedream-4-0-250828",
    "created":1759119942,
    "image_index":0,
    "url":"",
    "size":"3136x1344"
    }
     */

    /**
     *
     * @return 本次生图请求中，本次事件对应图片在请求中的序号。
     *         从0开始累加，不管生图是否成功，即在 image_generation.partial_succeeded、image_generation.partial_failed 事件，均会自动累加 1。
     */
    public @Nullable Integer getImageIndex() {
        return readInteger("data", "image_index");
    }

    /**
     *
     * @return 本次事件对应图片的下载 URL。当请求中配置字段 response_format 为 url 时返回。
     */
    public @Nullable String getUrl() {
        return readString("data", "url");
    }

    /**
     *
     * @return 本次事件对应图片的 Base64 编码。当请求中配置字段 response_format 为 b64_json 时返回。
     */
    public @Nullable String getB64Json() {
        return readString("data", "b64_json");
    }

    /**
     *
     * @return 图像的宽高像素值，格式 宽像素×高像素，如 2048×2048。
     */
    public @Nullable String getSize() {
        return readString("data", "size");
    }

}
