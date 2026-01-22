package io.github.sinri.keel.llm.api.sect.dialect.openai;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;

import java.util.List;

public class OpenAIModelConfigElement extends ConfigElement {
    public OpenAIModelConfigElement(ConfigElement another) {
        super(another);
    }

    public String apiKey() throws NotConfiguredException {
        return readString(List.of("apiKey"));
    }

    public String resourceName() throws NotConfiguredException {
        return readString(List.of("resourceName"));
    }

    public String deployment() throws NotConfiguredException {
        return readString(List.of("deployment"));
    }

    public String apiVersion() throws NotConfiguredException {
        return readString(List.of("apiVersion"));
    }
}
