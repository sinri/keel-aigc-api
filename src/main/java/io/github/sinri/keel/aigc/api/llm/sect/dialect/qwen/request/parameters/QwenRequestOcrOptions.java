package io.github.sinri.keel.aigc.api.llm.sect.dialect.qwen.request.parameters;

import io.github.sinri.keel.base.json.JsonifiableDataUnit;
import io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.qwen.QwenRequestParametersImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

/**
 * @since 5.0.0
 */
public interface QwenRequestOcrOptions extends JsonifiableDataUnit {
    static QwenRequestOcrOptions create() {
        return new QwenRequestParametersImpl.QwenRequestOcrOptionsImpl();
    }

    static QwenRequestOcrOptions wrap(JsonObject jsonObject) {
        return new QwenRequestParametersImpl.QwenRequestOcrOptionsImpl(jsonObject);
    }

    /**
     * 内置任务：通用文字识别
     */
    static QwenRequestOcrOptions createTextRecognitionTask() {
        return new QwenRequestParametersImpl.QwenRequestOcrOptionsImpl("text_recognition", null);
    }

    /**
     * 内置任务：信息抽取
     *
     * @param task_config 表示需要模型抽取的字段，可以是任意形式的JSON结构，最多可嵌套3层JSON 对象。<br/>
     *                    您只需要填写JSON对象的key，value保持为空即可。<br/>
     *                    示例值
     *                    {@code {"result_schema": {"收件人信息": {"收件人姓名": "","收件人电话号码": "","收件人地址": ""}}}}
     */
    static QwenRequestOcrOptions createKeyInformationExtractionTask(JsonObject task_config) {
        return new QwenRequestParametersImpl.QwenRequestOcrOptionsImpl("key_information_extraction", task_config);
    }

    /**
     * 内置任务：文档解析
     */
    static QwenRequestOcrOptions createDocumentParsingTask() {
        return new QwenRequestParametersImpl.QwenRequestOcrOptionsImpl("document_parsing", null);
    }

    /**
     * 内置任务：表格解析
     */
    static QwenRequestOcrOptions createTableParsingTask() {
        return new QwenRequestParametersImpl.QwenRequestOcrOptionsImpl("table_parsing", null);
    }

    /**
     * 内置任务：公式识别
     */
    static QwenRequestOcrOptions createFormulaRecognitionTask() {
        return new QwenRequestParametersImpl.QwenRequestOcrOptionsImpl("formula_recognition", null);
    }

    /**
     * 内置任务：多语言识别
     */
    static QwenRequestOcrOptions createMultiLanTask() {
        return new QwenRequestParametersImpl.QwenRequestOcrOptionsImpl("multi_lan", null);
    }

    /**
     * @return 内置任务的名称
     */
    default @Nullable String getTask() {
        return readString("task");
    }

    /**
     * 当内置任务task为"key_information_extraction"（信息抽取）时使用。
     * 示例值：
     * {@code {
     * "result_schema" : {
     * "收件人信息" : {
     * "收件人姓名" : "",
     * "收件人电话号码" : "",
     * "收件人地址":""
     * }
     * }
     * }}
     */
    default @Nullable JsonObject getTaskConfig() {
        return readJsonObject("task_config");
    }
}
