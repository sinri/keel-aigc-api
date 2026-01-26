package io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.response.stream;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.doubao.DoubaoResponseChunkChoiceImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

public interface DoubaoResponseChunkChoice extends UnmodifiableJsonifiableEntity {
    static DoubaoResponseChunkChoice wrap(JsonObject jsonObject) {
        return new DoubaoResponseChunkChoiceImpl(jsonObject);
    }

    /**
     * @return 当前元素在 choices 列表的索引。
     */
    default @Nullable Integer getIndex() {
        return readInteger("index");
    }

    /**
     * @return 模型停止生成 token 的原因。取值范围：
     *         stop：模型输出自然结束，或因命中请求参数 stop 中指定的字段而被截断。
     *         length：模型输出因达到模型输出限制而被截断，有以下原因：
     *         触发max_token限制（回答内容的长度限制）。
     *         触发max_completion_tokens限制（思维链内容+回答内容的长度限制）。
     *         触发context_window限制（输入内容+思维链内容+回答内容的长度限制）。
     *         content_filter：模型输出被内容审核拦截。
     *         tool_calls：模型调用了工具。
     */
    default @Nullable String getFinishReason() {
        return readString("finish_reason");
    }

    /**
     * @return 模型输出的增量内容。
     */
    default DoubaoResponseChunkChoiceDelta getDelta() {
        JsonObject delta = readJsonObject("delta");
        if (delta == null) {
            delta = new JsonObject();
        }
        return DoubaoResponseChunkChoiceDelta.wrap(delta);
    }

}
