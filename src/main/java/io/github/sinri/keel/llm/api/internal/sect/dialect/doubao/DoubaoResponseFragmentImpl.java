package io.github.sinri.keel.llm.api.internal.sect.dialect.doubao;

import io.github.sinri.keel.llm.api.sect.SSEUtils;
import io.github.sinri.keel.llm.api.sect.dialect.doubao.response.stream.DoubaoResponseFragment;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

public class DoubaoResponseFragmentImpl implements DoubaoResponseFragment {
    private final @Nullable String data;

    public DoubaoResponseFragmentImpl(String s) {
        //        String[] lines = s.split("[\r\n]+");
        //        for (var line : lines) {
        //            String[] pair = line.split(":\\s*", 2);
        //            if (pair.length == 2) {
        //                if (Objects.equals("data", pair[0])) {
        //                    data = pair[1];
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
            String rawData = getRawData();
            if (rawData == null) return null;
            return new JsonObject(rawData);
        } catch (Throwable throwable) {
            return null;
        }
    }
}
