package io.github.sinri.keel.aigc.api.llm.sect.dialect.qwen.response.stream;

import io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.qwen.QwenResponseFragmentImpl;
import io.vertx.core.json.JsonObject;

public interface QwenResponseFragment {
    static QwenResponseFragment wrap(String s) {
        return new QwenResponseFragmentImpl(s);
    }

    String getId();

    String getEvent();

    String getRawData();

    JsonObject getData();
}
