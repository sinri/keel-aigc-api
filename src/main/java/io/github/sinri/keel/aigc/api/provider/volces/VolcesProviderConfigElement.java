package io.github.sinri.keel.aigc.api.provider.volces;

import io.github.sinri.keel.aigc.api.provider.config.ProviderConfigElement;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;

public class VolcesProviderConfigElement extends ProviderConfigElement {
    public VolcesProviderConfigElement(ConfigElement another) {
        super(another);
    }

    @Override
    public String getProviderApiSchema() {
        return "https";
    }

    @Override
    public Integer getProviderApiPort() {
        return 443;
    }

    @Override
    public String getProviderApiHost() {
        return "ark.cn-beijing.volces.com";
    }

    @Override
    public String getProviderApiPath() {
        return "/api/v3/chat/completions";
    }

    protected VolcesModelConfigElement getModelConfigElement(String modelIdentity) throws NotConfiguredException {
        ConfigElement mce = ensureChild("models").getChild(modelIdentity);
        return new VolcesModelConfigElement(mce, this);
    }
}
