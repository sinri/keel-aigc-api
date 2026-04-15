package io.github.sinri.keel.aigc.api.llm.sect.dialect.qwen;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;

import java.util.List;

/**
 * @deprecated let `qwen` be a key identity
 */
@Deprecated
public class QwenConfigElement extends ConfigElement {
    public static final String CONFIG_ELEMENT_NAME = "qwen";

    public QwenConfigElement(ConfigElement another) {
        super(another);
    }

    public static QwenConfigElement create(String apiKey) {
        ConfigElement x = new ConfigElement(CONFIG_ELEMENT_NAME);
        x.ensureChild("apiKey").setElementValue(apiKey);
        return new QwenConfigElement(x);
    }

    public String apiKey() throws NotConfiguredException {
        return readString(List.of("apiKey"));
    }
}
