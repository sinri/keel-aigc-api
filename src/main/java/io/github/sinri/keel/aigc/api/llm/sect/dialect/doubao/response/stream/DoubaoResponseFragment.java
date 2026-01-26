package io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.response.stream;

import io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.doubao.DoubaoResponseFragmentImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;


public interface DoubaoResponseFragment {
    static DoubaoResponseFragment wrap(String s) {
        return new DoubaoResponseFragmentImpl(s);
    }

    @Nullable String getRawData();

    @Nullable JsonObject getData();
}
