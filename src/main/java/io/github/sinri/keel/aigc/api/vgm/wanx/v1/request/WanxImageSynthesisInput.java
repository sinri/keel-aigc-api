package io.github.sinri.keel.aigc.api.vgm.wanx.v1.request;

import io.github.sinri.keel.aigc.api.internal.vgm.wanx.v1.WanxImageSynthesisInputImpl;
import io.github.sinri.keel.base.json.JsonifiableDataUnit;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;


public interface WanxImageSynthesisInput extends JsonifiableDataUnit {
    static WanxImageSynthesisInput create() {
        return new WanxImageSynthesisInputImpl();
    }

    static WanxImageSynthesisInput wrap(JsonObject jsonObject) {
        return new WanxImageSynthesisInputImpl(jsonObject);
    }

    @Nullable
    default String getPrompt() {
        return this.readString("prompt");
    }

    /**
     * @param prompt 描述画面的提示词信息。支持中英文，长度不超过500个字符，超过部分会自动截断。
     */
    default WanxImageSynthesisInput setPrompt(String prompt) {
        ensureEntry("prompt", prompt);
        return this;
    }

    @Nullable
    default String getNegativePrompt() {
        return this.readString("negative_prompt");
    }

    /**
     * @param prompt 画面中不想出现的内容描述词信息。支持中英文，长度不超过500个字符，超过部分会自动截断。 Optional.
     */
    default WanxImageSynthesisInput setNegativePrompt(String prompt) {
        ensureEntry("negative_prompt", prompt);
        return this;
    }

    @Nullable
    default String getRefImg() {
        return this.readString("ref_img");
    }

    /**
     * @param url 输入参考图像的URL；图片格式可为 jpg，png，tiff，webp等常见位图格式。默认为空。 Optional.
     */
    default WanxImageSynthesisInput setRefImg(String url) {
        ensureEntry("ref_img", url);
        return this;
    }
}
