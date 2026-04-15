package io.github.sinri.keel.aigc.api.provider.vertex;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;

import java.util.List;

public class VertexProjectConfigElement extends ConfigElement {
    public VertexProjectConfigElement(ConfigElement another) {
        super(another);
    }

    public String projectNumber() throws NotConfiguredException {
        return readString(List.of("projectNumber"));
    }

    public String projectId() throws NotConfiguredException {
        return readString(List.of("projectId"));
    }

    public String location() throws NotConfiguredException {
        return readString(List.of("location"));
    }

    public String apiKey() throws NotConfiguredException {
        return readString(List.of("apiKey"));
    }

}
