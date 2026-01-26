package io.github.sinri.keel.aigc.api.vgm.wanx.ImageSynthesis.response;

import io.github.sinri.keel.aigc.api.internal.vgm.wanx.WanxImageSynthesisAsyncTaskCreateResultImpl;
import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

public interface WanxImageSynthesisAsyncTaskCreateResult extends UnmodifiableJsonifiableEntity {
    static WanxImageSynthesisAsyncTaskCreateResult wrap(JsonObject jsonObject) {
        return new WanxImageSynthesisAsyncTaskCreateResultImpl(jsonObject);
    }

    /**
     * @return 本次请求的系统唯一码。
     */
    default @Nullable String getRequestId() {
        return readString("request_id");
    }

    /**
     * @return 提交异步任务后的作业状态。
     */
    default @Nullable String getTaskStatus() {
        return readString("output", "task_status");
    }

    /**
     * @return 本次请求的异步任务的作业 id，实际作业结果需要通过异步任务查询接口获取。
     */
    default @Nullable String getTaskId() {
        return readString("output", "task_id");
    }
}
