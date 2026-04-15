package io.github.sinri.keel.aigc.api.provider.vertex;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;

import java.util.ArrayList;
import java.util.List;

public class VertexConfigElement extends ConfigElement {
    public static final String CONFIG_ELEMENT_NAME = "vertex";

    public VertexConfigElement(ConfigElement another) {
        super(another);
    }

    public List<VertexProjectConfigElement> getProjects() {
        List<VertexProjectConfigElement> list = new ArrayList<>();
        for (var n : getChildNames()) {
            ConfigElement item = null;
            try {
                item = getChild(n);
                list.add(new VertexProjectConfigElement(item));
            } catch (NotConfiguredException ignored) {
            }
        }
        return list;
    }

    public VertexProjectConfigElement getProject(String projectCode) throws NotConfiguredException {
        ConfigElement child = getChild(projectCode);
        return new VertexProjectConfigElement(child);
    }
}
