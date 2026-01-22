package io.github.sinri.keel.llm.api.sect.dialect.openai.response.stream;

import io.github.sinri.keel.llm.api.internal.sect.dialect.openai.GPTResponseFragmentImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

public interface GPTResponseFragment {
    static GPTResponseFragment wrap(String s) {
        return new GPTResponseFragmentImpl(s);
    }

    @Nullable String getRawData();

    @Nullable JsonObject getData();
}
