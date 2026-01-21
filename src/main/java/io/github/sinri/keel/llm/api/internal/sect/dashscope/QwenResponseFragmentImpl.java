package io.github.sinri.keel.llm.api.internal.sect.dashscope;

import io.github.sinri.keel.llm.api.sect.dashscope.qwen.response.stream.QwenResponseFragment;
import io.github.sinri.keel.logger.api.LateObject;
import io.vertx.core.json.JsonObject;


public class QwenResponseFragmentImpl implements QwenResponseFragment {
    private final LateObject<String> lateId = new LateObject<>();
    private final LateObject<String> lateEvent = new LateObject<>();
    private final LateObject<String> lateRawData = new LateObject<>();

    public QwenResponseFragmentImpl(String s) {
        for (String p : s.split("[\r\n]+")) {
            String[] pair = p.split(":", 2);
            if (pair.length == 2) {
                String k = pair[0].trim();
                String v = pair[1].trim();
                switch (k) {
                    case "id":
                        this.lateId.set(v);
                        break;
                    case "event":
                        this.lateEvent.set(v);
                        break;
                    case "data":
                        this.lateRawData.set(v);
                        break;
                }
            }
        }

    }

    @Override
    public String getId() {
        return lateId.get();
    }

    @Override
    public String getEvent() {
        return lateEvent.get();
    }

    @Override
    public String getRawData() {
        return lateRawData.get();
    }

    @Override
    public JsonObject getData() {
        return new JsonObject(lateRawData.get());
    }
}
