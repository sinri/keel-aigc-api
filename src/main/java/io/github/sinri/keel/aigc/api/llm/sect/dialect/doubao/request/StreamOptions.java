package io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.request;

import io.github.sinri.keel.base.json.JsonifiableDataUnit;
import io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.doubao.StreamOptionsImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

public interface StreamOptions extends JsonifiableDataUnit {

    static StreamOptions create() {
        return new StreamOptionsImpl();
    }

    static StreamOptions wrap(JsonObject x) {
        return new StreamOptionsImpl(x);
    }

    /**
     * 是否包含本次请求的 token 用量统计信息。
     *
     * @param b {@code false}：不返回 token 用量信息。
     *          {@code true}：在 data: [DONE] 消息之前会返回一个额外的块，此块上的 usage 字段代表整个请求的 token 用量，choices 字段为空数组。所有其他块也将包含
     *          usage 字段，但值为 null。
     */
    default StreamOptions includeUsage(boolean b) {
        ensureEntry("include_usage", b);
        return this;
    }

    default @Nullable Boolean includeUsage() {
        return readBoolean("include_usage");
    }
}
