package io.github.sinri.keel.aigc.api.vgm.wanx.task;


import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import org.jspecify.annotations.Nullable;

public interface WanxAsyncTaskResultSucceedMixin extends UnmodifiableJsonifiableEntity {
    default @Nullable Integer getImageCount() {
        return readInteger("usage", "image_count");
    }
}
