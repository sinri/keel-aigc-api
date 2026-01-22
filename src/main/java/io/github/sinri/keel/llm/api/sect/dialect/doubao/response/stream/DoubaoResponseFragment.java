package io.github.sinri.keel.llm.api.sect.dialect.doubao.response.stream;

import io.github.sinri.keel.llm.api.internal.sect.dialect.doubao.DoubaoResponseFragmentImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;


public interface DoubaoResponseFragment {
    static DoubaoResponseFragment wrap(String s) {
        return new DoubaoResponseFragmentImpl(s);
    }

    @Nullable String getRawData();

    @Nullable JsonObject getData();
}
