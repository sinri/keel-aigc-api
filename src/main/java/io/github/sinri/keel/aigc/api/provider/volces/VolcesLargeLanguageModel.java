package io.github.sinri.keel.aigc.api.provider.volces;

import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.aigc.api.llm.catholic.LLMService;
import io.github.sinri.keel.aigc.api.llm.catholic.LargeLanguageModel;

public class VolcesLargeLanguageModel extends LargeLanguageModel {
    public static final String MODEL_CODE_DOUBAO_PRO_32K = "doubao-pro-32k";
    private final String code;

    public VolcesLargeLanguageModel(String code) {
        this.code = code;
    }

    @Override
    public String getRegisterCode() {
        return code;
    }

    @Override
    public LLMService getService() throws NotConfiguredException {
        return null;
    }
}
