package io.github.sinri.keel.llm.api.sect.provider.volces;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VolcesConfigElement extends ConfigElement {
    public VolcesConfigElement(ConfigElement another) {
        super(another);
    }

    public String apiKey() throws NotConfiguredException {
        return readString(List.of("apiKey"));
    }

    public String model(String code) throws NotConfiguredException {
        return readString(List.of("model", code));
    }

    public Map<String, String> getModelMap() throws NotConfiguredException {
        Map<String, String> modelDeploymentMap = new HashMap<>();
        var models = this.extract("model");
        models.getChildNames().forEach(k -> {
            try {
                var v = models.getChild(k);
                modelDeploymentMap.put(k, v.getElementValue());
            } catch (NotConfiguredException ignored) {
            }
        });
        return modelDeploymentMap;
    }
}
