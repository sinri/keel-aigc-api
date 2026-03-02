package io.github.sinri.keel.aigc.api.provider.dashscope;

import io.github.sinri.keel.aigc.api.llm.catholic.LLMService;
import io.github.sinri.keel.base.configuration.NotConfiguredException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DashscopeMultimodalLargeLanguageModel extends AbstractDashscopeLargeLanguageModel {
    public final static String MODEL_CODE_QWEN3D5_PLUS = "qwen3.5-plus";
    public final static String MODEL_CODE_QWEN3_VL_PLUS = "qwen3-vl-plus";
    public final static String MODEL_CODE_QWEN3_VL_FLASH = "qwen3-vl-flash";
    public final static String MODEL_CODE_QWEN_VL_OCR = "qwen-vl-ocr";
    public final static String MODEL_CODE_QWEN3D5_FLASH = "qwen3.5-flash";

    private static final Map<String, DashscopeMultimodalLargeLanguageModel> CACHE = new ConcurrentHashMap<>();

    public DashscopeMultimodalLargeLanguageModel(String modelCode) {
        super(modelCode);
    }

    public static List<DashscopeMultimodalLargeLanguageModel> createCommonQwenSeriesLLMs() {
        ArrayList<DashscopeMultimodalLargeLanguageModel> list = new ArrayList<>();
        list.add(factory(DashscopeMultimodalLargeLanguageModel.MODEL_CODE_QWEN3D5_PLUS));
        list.add(factory(DashscopeMultimodalLargeLanguageModel.MODEL_CODE_QWEN3_VL_PLUS));
        list.add(factory(DashscopeMultimodalLargeLanguageModel.MODEL_CODE_QWEN3_VL_FLASH));
        list.add(factory(DashscopeMultimodalLargeLanguageModel.MODEL_CODE_QWEN_VL_OCR));
        list.add(factory(DashscopeMultimodalLargeLanguageModel.MODEL_CODE_QWEN3D5_FLASH));
        return list;
    }

    public static DashscopeMultimodalLargeLanguageModel factory(String modelCode) {
        return CACHE.computeIfAbsent(modelCode, DashscopeMultimodalLargeLanguageModel::new);
    }

    @Override
    public LLMService getService() throws NotConfiguredException {
        String apiKey = getApiKey();
        return new DashscopeMultimodalGenerationService(apiKey, getLogger());
    }
}
