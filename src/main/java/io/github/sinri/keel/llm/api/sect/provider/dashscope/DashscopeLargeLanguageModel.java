package io.github.sinri.keel.llm.api.sect.provider.dashscope;

import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.llm.api.catholic.LLMService;
import io.github.sinri.keel.llm.api.catholic.LLMServiceFacade;
import io.github.sinri.keel.llm.api.catholic.LargeLanguageModel;
import io.github.sinri.keel.llm.api.sect.ProviderConfigElement;
import io.github.sinri.keel.logger.api.logger.Logger;

public class DashscopeLargeLanguageModel extends LargeLanguageModel {

    public final static String MODEL_CODE_QWEN_FLASH = "qwen-flash";
    public final static String MODEL_CODE_QWEN_PLUS = "qwen-plus";
    public final static String MODEL_CODE_QWEN3_MAX = "qwen3-max";
    public final static String MODEL_CODE_QWEN_LONG = "qwen-long";
    public final static String MODEL_CODE_QWEN3_VL_PLUS = "qwen3-vl-plus";
    public final static String MODEL_CODE_QWEN3_VL_FLASH = "qwen3-vl-flash";
    public final static String MODEL_CODE_QWEN_VL_OCR = "qwen-vl-ocr";
    private final String modelCode;

    public DashscopeLargeLanguageModel(String modelCode) {
        this.modelCode = modelCode;
    }

    public String getApiKey() throws NotConfiguredException {
        return ProviderConfigElement.load().dashscope().qwen().apiKey();
    }

    @Override
    public String getCode() {
        return modelCode;
    }

    /**
     * @see <a
     *         href="https://bailian.console.aliyun.com/cn-beijing/?spm=5176.12818093_47.console-base_search-panel.dtab-product_sfm.57f12cc90avKbW&scm=20140722.S_sfm._.ID_sfm-RL_%E7%99%BE%E7%82%BC-LOC_console_console-OR_ser-V_4-P0_0&tab=doc#/doc/?type=model&url=2840914:~:text=%E6%A8%A1%E5%9E%8B-,%E5%88%97%E8%A1%A8,-%E9%99%90%E6%B5%81">模型列表</a>
     */
    @Override
    public LLMService getService() throws NotConfiguredException {
        if (modelCode.startsWith("qwen3-max")
                || modelCode.startsWith("qwen-plus")
                || modelCode.startsWith("qwen-flash")
                || modelCode.startsWith("qwen-turbo")
                || modelCode.startsWith("qwq-plus")
                || modelCode.startsWith("qwen-long")
                || modelCode.startsWith("qwen3-coder")
                || modelCode.startsWith("qwen-mt")
        ) {
            String apiKey = getApiKey();
            return new DashscopeTextGenerationService(apiKey, getLogger());
        } else if (modelCode.startsWith("qwen3-vl")
                || modelCode.startsWith("qwen-vl-ocr")
        ) {
            String apiKey = getApiKey();
            return new DashscopeMultimodalGenerationService(apiKey, getLogger());
        } else {
            // todo
            throw new UnsupportedOperationException();
        }
    }
}
