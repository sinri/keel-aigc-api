package io.github.sinri.keel.llm.api.internal.sect.dialect.openai.classic;

import io.github.sinri.keel.llm.api.sect.SSEUtils;
import io.github.sinri.keel.llm.api.sect.dialect.openai.classic.response.stream.GPTResponseFragment;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

public class GPTResponseFragmentImpl implements GPTResponseFragment {
    private final @Nullable String data;

    public GPTResponseFragmentImpl(String s) {
        //        var lines = s.split("[\r\n]+");
        //        for (var line : lines) {
        //            var pair = line.split(":\\s*", 2);
        //            if (pair.length == 2) {
        //                if (Objects.equals(pair[0], "data")) {
        //                    this.data = pair[1];
        //                    break;
        //                }
        //            }
        //        }

        this.data = SSEUtils.extractFragmentData(s);
    }

    @Override
    public @Nullable String getRawData() {
        return data;
    }

    @Override
    public @Nullable JsonObject getData() {
        try {
            if (data == null) return null;
            return new JsonObject(data);
        } catch (Throwable e) {
            return null;
        }
    }
}
