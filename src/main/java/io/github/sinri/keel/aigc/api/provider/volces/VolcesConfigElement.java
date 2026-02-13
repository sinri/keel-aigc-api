package io.github.sinri.keel.aigc.api.provider.volces;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VolcesConfigElement extends ConfigElement {
    public static final String CONFIG_ELEMENT_NAME = "volces";
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

    public static VolcesConfigElement create(String apiKey, Map<String, String> modelMap) {
        ConfigElement configElement = new ConfigElement(CONFIG_ELEMENT_NAME);
        configElement.ensureChild("apiKey").setElementValue(apiKey);
        modelMap.forEach((k, v) -> configElement.ensureChild("model").ensureChild(k).setElementValue(v));
        return new VolcesConfigElement(configElement);
    }
}
