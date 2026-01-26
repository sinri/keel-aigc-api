package io.github.sinri.keel.aigc.api.llm.sect.dialect.qwen;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;

import java.util.List;

public class QwenConfigElement extends ConfigElement {
    public QwenConfigElement(ConfigElement another) {
        super(another);
    }

    public String apiKey() throws NotConfiguredException {
        return readString(List.of("apiKey"));
    }
}
