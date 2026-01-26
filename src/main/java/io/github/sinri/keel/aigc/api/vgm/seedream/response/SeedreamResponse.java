package io.github.sinri.keel.aigc.api.vgm.seedream.response;

import io.github.sinri.keel.aigc.api.internal.vgm.seedream.SeedreamResponseImpl;
import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * @since 5.0.0
 */
public interface SeedreamResponse extends UnmodifiableJsonifiableEntity {
    static SeedreamResponse wrap(JsonObject jsonObject) {
        return new SeedreamResponseImpl(jsonObject);
    }

    /**
     *
     * @return 本次请求使用的模型 ID （模型名称-版本）。
     */
    default @Nullable String getModel() {
        return readString("model");
    }

    /**
     *
     * @return 本次请求创建时间的 Unix 时间戳（秒）。
     */
    default @Nullable Integer getCreated() {
        return readInteger("created");
    }

    /**
     * 输出图像的信息。
     * <p>
     * 说明 <br>
     * doubao-seedream-4.0模型生成组图场景下，组图生成过程中某张图生成失败时：<br>
     * 若失败原因为审核不通过：仍会继续请求下一个图片生成任务，即不影响同请求内其他图片的生成流程。<br>
     * 若失败原因为内部服务异常（500）：不会继续请求下一个图片生成任务。
     */
    default @Nullable List<Datum> getData() {
        List<JsonObject> data = readJsonObjectArray("data");
        if (data == null) return null;
        return data.stream().map(Datum::wrap).toList();
    }

    default @Nullable Usage getUsage() {
        JsonObject jsonObject = readJsonObject("usage");
        if (jsonObject == null) {
            return null;
        }
        return Usage.wrap(jsonObject);
    }

    default @Nullable SeedreamError getError() {
        JsonObject jsonObject = readJsonObject("error");
        if (jsonObject == null) {
            return null;
        }
        return SeedreamError.wrap(jsonObject);
    }

    interface Datum extends DatumDone, DatumFailed {
        static Datum wrap(JsonObject jsonObject) {
            return new SeedreamResponseImpl.DatumImpl(jsonObject);
        }
    }

    /**
     * 生成成功的图片信息。
     */
    interface DatumDone extends UnmodifiableJsonifiableEntity {
        /**
         * 图片的 url 信息，当 response_format 指定为 url 时返回。该链接将在生成后 24 小时内失效，请务必及时保存图像。
         */
        default @Nullable String getUrl() {
            return readString("url");
        }

        /**
         *
         * @return 图片的 base64 信息，当 response_format 指定为 b64_json 时返回。
         */
        default @Nullable String getB64Json() {
            return readString("b64_json");
        }

        /**
         * 仅 doubao-seedream-4.0 支持该字段。
         *
         * @return 图像的宽高像素值，格式 宽像素x高像素，如2048×2048。
         */
        default @Nullable String getSize() {
            return readString("size");
        }
    }

    interface DatumFailed extends UnmodifiableJsonifiableEntity {

        /**
         *
         * @return 某张图片生成失败，错误信息。
         */
        default @Nullable SeedreamError getError() {
            JsonObject entries = readJsonObject("error");
            if (entries == null) return null;
            return SeedreamError.wrap(entries);
        }
    }

}
