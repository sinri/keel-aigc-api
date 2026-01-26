package io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.response.stream;

import io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.openai.classic.GPTResponseFragmentImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

public interface GPTResponseFragment {
    static GPTResponseFragment wrap(String s) {
        return new GPTResponseFragmentImpl(s);
    }

    @Nullable String getRawData();

    @Nullable JsonObject getData();
}
