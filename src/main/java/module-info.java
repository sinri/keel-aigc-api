module io.github.sinri.keel.integration.llm.api {
    requires transitive io.github.sinri.keel.base;
    requires transitive io.github.sinri.keel.core;
    requires transitive io.github.sinri.keel.logger.api;

    requires transitive io.vertx.core;
    requires transitive io.vertx.web.client;
    requires transitive io.vertx.config;
    requires transitive org.commonmark;
    requires transitive org.commonmark.ext.gfm.tables;

    requires static org.jspecify;
    requires io.vertx.jsonschema;
    requires io.vertx.web.common;
    requires org.yaml.snakeyaml;

    // io.github.sinri.keel.llm.api.internal 及其下子包均不对外透出
    exports io.github.sinri.keel.aigc.api.llm.catholic;
    exports io.github.sinri.keel.aigc.api.llm.catholic.message;
    exports io.github.sinri.keel.aigc.api.llm.catholic.request;
    exports io.github.sinri.keel.aigc.api.llm.catholic.response;
    exports io.github.sinri.keel.aigc.api.llm.catholic.response.stream;
    exports io.github.sinri.keel.aigc.api.llm.catholic.tool;
    exports io.github.sinri.keel.aigc.api.llm.catholic.tool.call;
    exports io.github.sinri.keel.aigc.api.llm.catholic.tool.definition;
    exports io.github.sinri.keel.aigc.api.llm.sect;
    exports io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao;
    exports io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.message;
    exports io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.message.vision;
    exports io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.request;
    exports io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.response.stream;
    exports io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.response.sync;
    exports io.github.sinri.keel.aigc.api.llm.sect.dialect.openai;
    exports io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic;
    exports io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.core;
    exports io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.core.error;
    exports io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.core.filter;
    exports io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.message;
    exports io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.message.vision;
    exports io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.request;
    exports io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.response.stream;
    exports io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.response.sync;
    exports io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.stateful;
    exports io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.stateful.content;
    exports io.github.sinri.keel.aigc.api.llm.sect.dialect.qwen;
    exports io.github.sinri.keel.aigc.api.llm.sect.dialect.qwen.message;
    exports io.github.sinri.keel.aigc.api.llm.sect.dialect.qwen.message.vision;
    exports io.github.sinri.keel.aigc.api.llm.sect.dialect.qwen.request;
    exports io.github.sinri.keel.aigc.api.llm.sect.dialect.qwen.request.input;
    exports io.github.sinri.keel.aigc.api.llm.sect.dialect.qwen.request.parameters;
    exports io.github.sinri.keel.aigc.api.llm.sect.dialect.qwen.response.stream;
    exports io.github.sinri.keel.aigc.api.llm.sect.dialect.qwen.response.sync;
    exports io.github.sinri.keel.aigc.api.llm.skill;
    exports io.github.sinri.keel.aigc.api.llm.rag;
    exports io.github.sinri.keel.aigc.api.provider;
    exports io.github.sinri.keel.aigc.api.provider.azure;
    exports io.github.sinri.keel.aigc.api.provider.dashscope;
    exports io.github.sinri.keel.aigc.api.provider.volces;


}