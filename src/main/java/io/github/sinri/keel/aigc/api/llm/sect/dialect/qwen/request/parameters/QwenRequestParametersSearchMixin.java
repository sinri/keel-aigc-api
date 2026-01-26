package io.github.sinri.keel.aigc.api.llm.sect.dialect.qwen.request.parameters;

import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

/**
 * @since 2.0.0
 */
interface QwenRequestParametersSearchMixin<E> extends QwenRequestParametersCore<E> {
    /**
     * 模型在生成文本时是否使用互联网搜索结果进行参考。
     * 启用互联网搜索功能可能会增加 Token 的消耗。
     *
     * @param enableSearch 取值如下：<br/>
     *                     true：启用互联网搜索，模型会将搜索结果作为文本生成过程中的参考信息，但模型会基于其内部逻辑判断是否使用互联网搜索结果。如果模型没有搜索互联网，建议优化Prompt，或设置search_options中的forced_search参数开启强制搜索。<br/>
     *                     false（默认）：关闭互联网搜索。<br/>
     */
    default E enableSearch(boolean enableSearch) {
        ensureEntry("enable_search", enableSearch);
        return getImplementation();
    }

    default @Nullable Boolean enableSearch() {
        return readBoolean("enable_search");
    }

    /**
     * 联网搜索的策略。仅当enable_search为true时生效。
     */
    default E searchOptions(QwenRequestSearchOptions searchOptions) {
        ensureEntry("search_options", searchOptions.toJsonObject());
        return getImplementation();
    }

    default @Nullable QwenRequestSearchOptions searchOptions() {
        JsonObject x = readJsonObject("search_options");
        if (x == null) return null;
        return QwenRequestSearchOptions.wrap(x);
    }

}
