package io.github.sinri.keel.aigc.api.vgm.seedream.response;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;


/**
 * 当前仅 doubao-seedream-4.0 支持流式响应。
 * <p>
 * 在流式返回模式下，当任意图片生成失败时返回该事件。
 * <p>
 * 若失败原因为审核不通过：仍会继续请求下一个图片生成任务，即不影响同请求内其他图片的生成流程。
 * <p>
 * 若失败原因为内部服务异常（500）：不会继续请求下一个图片生成任务。
 *
 * @since 5.0.0
 */
public final class SeedreamEventPartialFailed extends UnmodifiableJsonifiableEntityImpl implements SeedreamEvent {
    public static final String EVENT_TYPE = "image_generation.partial_failed";

    public SeedreamEventPartialFailed( JsonObject jsonObject) {
        super(jsonObject);
    }

    /**
     *
     * @return 本次生图请求中，本次事件对应图片在请求中的序号。
     *         从0开始累加，不管生图是否成功，即在 image_generation.partial_succeeded、image_generation.partial_failed 事件，均会自动累加 1。
     */
    public @Nullable Integer getImageIndex() {
        return readInteger("data", "image_index");
    }

    public @Nullable SeedreamError getError() {
        var j = readJsonObject("data", "error");
        if (j == null) return null;
        return SeedreamError.wrap(j);
    }
}
