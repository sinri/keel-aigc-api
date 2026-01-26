package io.github.sinri.keel.aigc.api.vgm.wanx.task;

import org.jspecify.annotations.Nullable;

public interface WanxAsyncTaskResult extends WanxAsyncTaskResultSucceedMixin, WanxAsyncTaskResultFailedMixin {


    /**
     * @return 本次请求的系统唯一码。
     */
    default @Nullable String getRequestId() {
        return readString("request_id");
    }

    /**
     * @return 提交异步任务后的作业状态。
     *         任务状态：
     *         PENDING：排队中
     *         RUNNING：处理中
     *         SUCCEEDED：成功
     *         FAILED：失败
     *         UNKNOWN：作业不存在或状态未知
     */
    default @Nullable WanxTaskStatus getTaskStatus() {
        String s = readString("output", "task_status");
        if (s == null) return null;
        return WanxTaskStatus.valueOf(s);
    }

    /**
     * @return 本次请求的异步任务的作业 id，实际作业结果需要通过异步任务查询接口获取。
     */
    default @Nullable String getTaskId() {
        return readString("output", "task_id");
    }

    /**
     * @return 作业中每个batch任务的状态
     */
    default @Nullable TaskMetrics getTaskMetrics() {
        var x = readJsonObject("output", "task_metrics");
        if (x == null) {
            return null;
        }
        return new TaskMetrics(
                x.getInteger("total"),
                x.getInteger("succeeded"),
                x.getInteger("failed")
        );
    }

    enum WanxTaskStatus {
        PENDING,//排队中
        RUNNING,//处理中
        SUCCEEDED,//成功
        FAILED,//失败
        UNKNOWN,//作业不存在或状态未知
    }

    /**
     * @param total     总共要生成的图片数目
     * @param succeeded 已经生成成功的图片数目
     * @param failed    已经生成失败的图片数目
     */
    record TaskMetrics(int total, int succeeded, int failed) {
    }


}
