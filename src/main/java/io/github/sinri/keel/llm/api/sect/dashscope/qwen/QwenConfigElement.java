package io.github.sinri.keel.llm.api.sect.dashscope.qwen;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;

public class QwenConfigElement extends ConfigElement {
    public QwenConfigElement(ConfigElement another) {
        super(another);
    }

    public String apiKey() throws NotConfiguredException {
        var x = readString("apiKey");
        if (x == null) {
            throw new NotConfiguredException(getAbsoluteKeyChain(), "apiKey");
        }
        return x;
    }
}
