package io.github.sinri.keel.aigc.api.llm.sect.dialect.qwen.request.parameters;

import io.github.sinri.keel.base.json.JsonifiableDataUnit;
import io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.qwen.QwenRequestParametersImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

/**
 * @since 5.0.0
 */
public interface QwenRequestSearchOptions extends JsonifiableDataUnit {
    static QwenRequestSearchOptions create() {
        return new QwenRequestParametersImpl.QwenRequestSearchOptionsImpl();
    }

    static QwenRequestSearchOptions wrap(JsonObject jsonObject) {
        return new QwenRequestParametersImpl.QwenRequestSearchOptionsImpl(jsonObject);
    }

    /**
     * 是否在返回结果中展示搜索来源信息。
     */
    default QwenRequestSearchOptions enableSource(boolean enableSource) {
        ensureEntry("enable_source", enableSource);
        return this;
    }

    default @Nullable Boolean enableSource() {
        return readBoolean("enable_source");
    }

    /**
     * 是否在返回结果中展示引用信息。
     * 是否开启[1]或[ref_1]样式的角标标注功能。在enable_source为true时生效。
     */
    default QwenRequestSearchOptions enableCitation(boolean enableCitation) {
        ensureEntry("enable_citation", enableCitation);
        return this;
    }

    default @Nullable Boolean enableCitation() {
        return readBoolean("enable_citation");
    }

    /**
     * 角标样式。在enable_citation为true时生效。
     * 参数值：
     * {@code [<number>]}：角标形式为{@code [1]}；
     * {@code [ref_<number>]}：角标形式为{@code [ref_1]}。
     */
    default QwenRequestSearchOptions citationFormat(String citationFormat) {
        ensureEntry("citation_format", citationFormat);
        return this;
    }

    default @Nullable String citationFormat() {
        return readString("citation_format");
    }

    /**
     * 是否强制开启搜索。
     */
    default QwenRequestSearchOptions forcedSearch(boolean forcedSearch) {
        ensureEntry("forced_search", forcedSearch);
        return this;
    }

    default @Nullable Boolean forcedSearch() {
        return readBoolean("forced_search");
    }

    /**
     * 搜索互联网信息的数量。
     * <p>
     * 可选值：<br>
     * - "standard": 搜索5条互联网信息<br>
     * - "pro": 搜索10条互联网信息<br>
     * </p>
     */
    default QwenRequestSearchOptions searchStrategy(String searchStrategy) {
        ensureEntry("search_strategy", searchStrategy);
        return this;
    }

    default @Nullable String searchStrategy() {
        return readString("search_strategy");
    }
}
