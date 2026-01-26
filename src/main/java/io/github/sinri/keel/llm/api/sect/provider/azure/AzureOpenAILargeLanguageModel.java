package io.github.sinri.keel.llm.api.sect.provider.azure;

import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.llm.api.catholic.LLMService;
import io.github.sinri.keel.llm.api.catholic.LargeLanguageModel;
import io.github.sinri.keel.llm.api.sect.ProviderConfigElement;
import io.github.sinri.keel.llm.api.sect.dialect.openai.OpenAIConfigElement;

public class AzureOpenAILargeLanguageModel extends LargeLanguageModel {
    public static final String MODEL_CODE_GPT_5_CHAT = "gpt-5-chat";

    private final String code;

    public AzureOpenAILargeLanguageModel(String code) {
        this.code = code;
    }

    @Override
    public String getRegisterCode() {
        return code;
    }

    private OpenAIConfigElement getOpenAIConfigElement() throws NotConfiguredException {
        return ProviderConfigElement.load().azure().openai();
    }

    @Override
    public LLMService getService() throws NotConfiguredException {
        return new AzureOpenAITextGenerationService(getOpenAIConfigElement(), getLogger());
    }
}
