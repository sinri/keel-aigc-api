package io.github.sinri.keel.aigc.api.provider.dashscope;

import io.github.sinri.keel.aigc.api.llm.catholic.LLMService;
import io.github.sinri.keel.base.configuration.NotConfiguredException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DashscopeTextLargeLanguageModel extends AbstractDashscopeLargeLanguageModel {

    public final static String MODEL_CODE_QWEN_FLASH = "qwen-flash";
    public final static String MODEL_CODE_QWEN3D5_FLASH = "qwen3.5-flash";
    public final static String MODEL_CODE_QWEN_PLUS = "qwen-plus";
    public final static String MODEL_CODE_QWEN3_MAX = "qwen3-max";
    public final static String MODEL_CODE_QWEN_LONG = "qwen-long";

    private static final Map<String, DashscopeTextLargeLanguageModel> CACHE = new ConcurrentHashMap<>();


    protected DashscopeTextLargeLanguageModel(String modelCode) {
        super(modelCode);
    }

    public static List<DashscopeTextLargeLanguageModel> createCommonQwenSeriesLLMs() {
        ArrayList<DashscopeTextLargeLanguageModel> list = new ArrayList<>();
        list.add(factory(DashscopeTextLargeLanguageModel.MODEL_CODE_QWEN3_MAX));
        list.add(factory(DashscopeTextLargeLanguageModel.MODEL_CODE_QWEN_PLUS));
        list.add(factory(DashscopeTextLargeLanguageModel.MODEL_CODE_QWEN_FLASH));
        list.add(factory(DashscopeTextLargeLanguageModel.MODEL_CODE_QWEN3D5_FLASH));
        list.add(factory(DashscopeTextLargeLanguageModel.MODEL_CODE_QWEN_LONG));
        return list;
    }

    public static DashscopeTextLargeLanguageModel factory(String modelCode) {
        return CACHE.computeIfAbsent(modelCode, DashscopeTextLargeLanguageModel::new);
    }

    /**
     * @see <a
     *         href="https://bailian.console.aliyun.com/cn-beijing/?spm=5176.12818093_47.console-base_search-panel.dtab-product_sfm.57f12cc90avKbW&scm=20140722.S_sfm._.ID_sfm-RL_%E7%99%BE%E7%82%BC-LOC_console_console-OR_ser-V_4-P0_0&tab=doc#/doc/?type=model&url=2840914:~:text=%E6%A8%A1%E5%9E%8B-,%E5%88%97%E8%A1%A8,-%E9%99%90%E6%B5%81">模型列表</a>
     */
    @Override
    public LLMService getService() throws NotConfiguredException {
        String apiKey = getApiKey();
        return new DashscopeTextGenerationService(apiKey, getLogger());
    }
}
