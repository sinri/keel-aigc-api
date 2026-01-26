package io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.message.vision;

import io.github.sinri.keel.base.json.JsonifiableDataUnit;
import io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.doubao.ContentVideoUrlImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

public interface ContentVideoUrl extends JsonifiableDataUnit {
    static ContentVideoUrl create() {
        return new ContentVideoUrlImpl();
    }

    static ContentVideoUrl wrap(JsonObject jsonObject) {
        return new ContentVideoUrlImpl(jsonObject);
    }

    default @Nullable String getUrl() {
        return readString("url");
    }

    /**
     * 其他说明：<br>
     * * 视频文件大小：50MB内。<br>
     * * 视频格式：MP4、AVI、MKV、MOV、FLV、WMV、WEBM。<br>
     * * 音频理解：不支持对视频文件中的音频信息进行理解。<br>
     *
     * @param url 支持传入视频链接或视频的Base64编码。<br>
     *            传入视频URL：传入视频的可访问链接，推荐使用 TOS（火山引擎对象存储） 存储视频，并生成视频链接。<br>
     *            传入Base64编码：请遵循格式 {@code data:video/[视频格式];base64,[Base64编码]}。<br>
     */
    default ContentVideoUrl setUrl(String url) {
        ensureEntry("url", url);
        return this;
    }

    default @Nullable Float getFps() {
        return readFloat("fps");
    }

    /**
     * @param fps 取值范围：[0.2, 5]。
     *            每秒钟从视频中抽取指定数量的图像。
     */
    default ContentVideoUrl setFps(float fps) {
        ensureEntry("fps", fps);
        return this;
    }

    default @Nullable String getDetail() {
        return readString("detail");
    }

    /**
     * @param detail 理解视频的细节程度，支持取值 high、low。<br>
     *               high：对视频细节理解较细致，适合高分辨率视频。<br>
     *               low：对视频细节理解较粗，适合低分辨率视频。<br>
     */
    default ContentVideoUrl setDetail(String detail) {
        ensureEntry("detail", detail);
        return this;
    }
}
