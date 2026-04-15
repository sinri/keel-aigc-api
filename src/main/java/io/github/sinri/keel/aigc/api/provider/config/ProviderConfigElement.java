package io.github.sinri.keel.aigc.api.provider.config;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class ProviderConfigElement extends ConfigElement implements ProviderPassport {
    private @Nullable ProviderPassport parent;

    public ProviderConfigElement(ConfigElement another) {
        super(another);
    }

    public ProviderConfigElement(ConfigElement another,@Nullable ProviderPassport parent) {
        super(another);
        this.parent = parent;
    }

    @Override
    public String getProviderIdentity() {
        return this.getElementName();
    }

    @Override
    public String getProviderApiKey() throws NotConfiguredException {
        try {
            return readString(List.of("api", "key"));
        } catch (NotConfiguredException e) {
            if (parent != null) return parent.getProviderApiKey();
            else throw e;
        }
    }

    @Override
    public String getProviderApiSchema() throws NotConfiguredException {
        try {
            return readString(List.of("api", "schema"));
        } catch (NotConfiguredException e) {
            if (parent != null) return parent.getProviderApiSchema();
            else throw e;
        }
    }

    @Override
    public Integer getProviderApiPort() throws NotConfiguredException {
        try {
            return readInteger(List.of("api", "port"));
        } catch (NotConfiguredException e) {
            if (parent != null) return parent.getProviderApiPort();
            else throw e;
        }
    }

    @Override
    public String getProviderApiHost() throws NotConfiguredException {
        try {
            return readString(List.of("api", "host"));
        } catch (NotConfiguredException e) {
            if (parent != null) return parent.getProviderApiHost();
            else throw e;
        }
    }

    @Override
    public String getProviderApiPath() throws NotConfiguredException {
        try {
            return readString(List.of("api", "path"));
        } catch (NotConfiguredException e) {
            if (parent != null) return parent.getProviderApiPath();
            else throw e;
        }
    }

    protected ModelConfigElement getModelConfigElement(String modelIdentity) throws NotConfiguredException {
        ConfigElement mce = ensureChild("models").getChild(modelIdentity);
        return new ModelConfigElement(mce, this);
    }
}
