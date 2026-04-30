module io.github.sinri.keel.integration.llm.api {
    requires transitive io.github.sinri.keel.base;
    requires transitive io.github.sinri.keel.core;
    requires transitive io.github.sinri.keel.logger.api;

    requires transitive io.vertx.core;
    requires transitive io.vertx.config;
    requires transitive org.commonmark;
    requires transitive org.commonmark.ext.gfm.tables;

    requires static org.jspecify;
    requires io.vertx.jsonschema;
    requires org.yaml.snakeyaml;

    // io.github.sinri.keel.aigc.api.internal 及其下子包均不对外透出（含 OpenAI / DashScope / Anthropic 协议与 Vert.x 共用实现）

    // Catholic LLM 通用格式
    exports io.github.sinri.keel.aigc.api.llm.catholic;
    exports io.github.sinri.keel.aigc.api.llm.catholic.message;
    exports io.github.sinri.keel.aigc.api.llm.catholic.tool;
    exports io.github.sinri.keel.aigc.api.llm.catholic.tool.definition;
    exports io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.function;
    exports io.github.sinri.keel.aigc.api.llm.catholic.tool.call;
    exports io.github.sinri.keel.aigc.api.llm.catholic.request;
    exports io.github.sinri.keel.aigc.api.llm.catholic.response;

    // OpenAI Chat Completions API
    exports io.github.sinri.keel.aigc.api.llm.openai.chatcompletions;

    // OpenAI Responses API
    exports io.github.sinri.keel.aigc.api.llm.openai.responses;

    // DashScope API（根包无公开类型，仅导出子包）
    exports io.github.sinri.keel.aigc.api.llm.dashscope.textgeneration;
    exports io.github.sinri.keel.aigc.api.llm.dashscope.multimodalgeneration;

    // Anthropic Messages API
    exports io.github.sinri.keel.aigc.api.llm.anthropic;

    // Agent（Catholic LLM 工具循环编排）
    exports io.github.sinri.keel.aigc.api.agent;
}
