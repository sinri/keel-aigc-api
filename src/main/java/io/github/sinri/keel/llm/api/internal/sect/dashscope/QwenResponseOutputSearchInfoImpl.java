package io.github.sinri.keel.llm.api.internal.sect.dashscope;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.llm.api.sect.dashscope.qwen.response.sync.QwenResponseOutputSearchInfo;
import io.vertx.core.json.JsonObject;

/**
 * @since 2.0.0
 */
public class QwenResponseOutputSearchInfoImpl extends UnmodifiableJsonifiableEntityImpl implements QwenResponseOutputSearchInfo {
    public QwenResponseOutputSearchInfoImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

    public static class SearchResultImpl extends UnmodifiableJsonifiableEntityImpl implements SearchResult {

        public SearchResultImpl(JsonObject jsonObject) {
            super(jsonObject);
        }
    }
}
