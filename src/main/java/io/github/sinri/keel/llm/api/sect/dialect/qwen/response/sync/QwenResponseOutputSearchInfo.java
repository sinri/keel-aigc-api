package io.github.sinri.keel.llm.api.sect.dialect.qwen.response.sync;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.github.sinri.keel.llm.api.internal.sect.dialect.qwen.QwenResponseOutputSearchInfoImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.List;
/**
 * @since 2.0.0
 */
public interface QwenResponseOutputSearchInfo extends UnmodifiableJsonifiableEntity {
    static QwenResponseOutputSearchInfo wrap(JsonObject jsonObject) {
        return new QwenResponseOutputSearchInfoImpl(jsonObject);
    }

    default List<SearchResult> getSearchResults() {
        List<JsonObject> list = readJsonObjectArray("search_results");
        if (list == null) {
            return List.of();
        }
        return list.stream().map(SearchResult::wrap).toList();
    }

    interface SearchResult extends UnmodifiableJsonifiableEntity {
        static SearchResult wrap(JsonObject jsonObject) {
            return new QwenResponseOutputSearchInfoImpl.SearchResultImpl(jsonObject);
        }

        /**
         * 获取搜索结果来源的网站名称
         *
         * @return 网站名称
         */
        default @Nullable String getSiteName() {
            return readString("site_name");
        }

        /**
         * 获取来源网站的图标URL
         *
         * @return 图标URL，如果没有图标则为空字符串
         */
        default @Nullable String getIcon() {
            return readString("icon");
        }

        /**
         * 获取搜索结果的序号
         *
         * @return 表示该搜索结果在search_results中的索引
         */
        default @Nullable Integer getIndex() {
            return readInteger("index");
        }

        /**
         * 获取搜索结果的标题
         *
         * @return 标题
         */
        default @Nullable String getTitle() {
            return readString("title");
        }

        /**
         * 获取搜索结果的链接地址
         *
         * @return URL
         */
        default @Nullable String getUrl() {
            return readString("url");
        }
    }
}
