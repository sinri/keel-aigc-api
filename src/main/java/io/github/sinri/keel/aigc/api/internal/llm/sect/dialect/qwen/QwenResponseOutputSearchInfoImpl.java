package io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.qwen;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.qwen.response.sync.QwenResponseOutputSearchInfo;
import io.vertx.core.json.JsonObject;

/**
 * @since 5.0.0
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
