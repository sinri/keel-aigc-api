package io.github.sinri.keel.aigc.api.internal.vgm.wanx.v1;

import io.github.sinri.keel.aigc.api.vgm.wanx.v1.task.WanxAsyncTaskResult;
import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class WanxImageSynthesisAsyncTaskResult extends UnmodifiableJsonifiableEntityImpl implements WanxAsyncTaskResult {
    public WanxImageSynthesisAsyncTaskResult(JsonObject jsonObject) {
        super(jsonObject);
    }

    /**
     * For Task Status `SUCCEEDED`.
     * 如果作业执行完成并成功之后，再次查询作业状态，接口将在告知作业状态的同时，一并将作业的结果返回。
     * 对于本模型，作业在结束之后的状态会持续保留24小时以备客户随时查询，24小时之后，作业将从系统中清除，相关的结果也将一并清除；
     * 对应的，作业生成的结果为图像的URL地址，出于安全考虑，该URL的下载有效期也是24小时，需要用户在获取作业结果后根据需要及时使用或者转存。
     * 在一次提交中，本模型可以根据客户的需求生成多张图片，
     * 只要其中一张图片生成成功，作业将被设置为成功状态，
     * 并且对应的作业结果会在查询的时候返回，
     * 对于失败的batch，结果中也会返回对应的失败原因；
     * 同时在usage计量中，只会对成功的结果计数。
     */
    public @Nullable List<Result> getResults() {
        List<JsonObject> array = readJsonObjectArray("output", "results");
        if (array == null) {
            return null;
        }
        List<Result> list = new ArrayList<>();
        array.forEach(item -> {
            var r = new Result(item.getString("url"), item.getString("code"), item.getString("message"));
            list.add(r);
        });
        return list;
    }


    /**
     * @param url     成功时：下载图片的地址
     * @param code    失败时：代码
     * @param message 失败时：信息
     */
    public record Result(String url, String code, String message) {
    }
}
