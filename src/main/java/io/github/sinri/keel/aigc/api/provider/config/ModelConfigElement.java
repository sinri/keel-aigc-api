package io.github.sinri.keel.aigc.api.provider.config;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class ModelConfigElement extends ConfigElement implements ModelPassport {
    private final @Nullable ProviderPassport providerPassport;

    public ModelConfigElement(ConfigElement another, @Nullable ProviderPassport providerPassport) {
        super(another);
        this.providerPassport = providerPassport;
    }

    /**
     *
     * @return the value to identify the model in the configuration scope
     */
    @Override
    public String getModelIdentity() {
        return this.getElementName();
    }

    /**
     *
     * @return the value to be sent to LLM service to identify the model in the provider scope
     */
    @Override
    public String getModelCode() throws NotConfiguredException {
        return readString(List.of("code"));
    }

    @Override
    public String getModelApiKey() throws NotConfiguredException {
        try {
            return readString(List.of("api", "key"));
        } catch (NotConfiguredException e) {
            if (providerPassport != null)
                return providerPassport.getProviderApiKey();
            else throw e;
        }
    }

    @Override
    public String getModelApiSchema() throws NotConfiguredException {
        try {
            return readString(List.of("api", "schema"));
        } catch (NotConfiguredException e) {
            if (providerPassport != null)
                return providerPassport.getProviderApiSchema();
            else throw e;
        }
    }

    @Override
    public Integer getModelApiPort() throws NotConfiguredException {
        try {
            return readInteger(List.of("api", "port"));
        } catch (NotConfiguredException e) {
            if (providerPassport != null)
                return providerPassport.getProviderApiPort();
            else throw e;
        }
    }

    @Override
    public String getModelApiHost() throws NotConfiguredException {
        try {
            return readString(List.of("api", "host"));
        } catch (NotConfiguredException e) {
            if (providerPassport != null)
                return providerPassport.getProviderApiHost();
            else throw e;
        }
    }

    @Override
    public String getModelApiPath() throws NotConfiguredException {
        try {
            return readString(List.of("api", "path"));
        } catch (NotConfiguredException e) {
            if (providerPassport != null)
                return providerPassport.getProviderApiPath();
            else throw e;
        }
    }
}
