package io.github.sinri.keel.aigc.api.provider.vertex;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.sinri.keel.tesuto.KeelInstantRunner;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.parsetools.JsonParser;

public class JsonParserTest extends KeelInstantRunner {
    @Override
    protected Future<Void> run() throws Exception {
        return useJackson();

        //return Future.succeededFuture();
    }

    private Future<Void> useJackson() {
        ObjectMapper mapper = new ObjectMapper();
        io.github.sinri.keel.aigc.api.provider.WritableInputStream inputStream = new io.github.sinri.keel.aigc.api.provider.WritableInputStream();
        io.vertx.core.Promise<Void> promise = io.vertx.core.Promise.promise();

        Thread thread = new Thread(() -> {
            try {
                // 创建解析器
                try (var parser = mapper.getFactory().createParser(inputStream)) {
                    // 移动到数组的开始符号 '['
                    if (parser.nextToken() != JsonToken.START_ARRAY) {
                        throw new IllegalStateException("Expected content to be an array");
                    }
                    
                    // 读取数组中的每个元素
                    JsonToken token = parser.nextToken();
                    while (token != JsonToken.END_ARRAY && token != null) {
                        if (token == JsonToken.START_OBJECT) {
                            // 读取为 Jackson 的 JsonNode，然后转换为 Vertx 的 JsonObject
                            com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(parser);
                            JsonObject obj = new JsonObject(mapper.writeValueAsString(node));
                            getLogger().info("OBJECT FOUND: "+obj.toString());
                        } else if (token == JsonToken.VALUE_NULL) {
                            parser.skipChildren();
                        }
                        token = parser.nextToken();
                    }
                }
                promise.complete();
            } catch (Exception e) {
                promise.fail(e);
            }
        });
        thread.start();

        // 模拟流式输入数据
        inputStream.accept(Buffer.buffer("[{}"));
        getKeel().setTimer(1000L,t1->{
            inputStream.accept(Buffer.buffer(",\n" + new JsonObject()
                    .put("a", 1)
                    .encodePrettily()));
        });
        getKeel().setTimer(2000L,t1->{
            inputStream.accept(Buffer.buffer(",\n" + new JsonObject()
                    .put("b", new JsonArray()
                            .add(new JsonObject().put("b", "c"))
                    )
                    .encodePrettily()));
        });
        getKeel().setTimer(3000L,t1->{
            inputStream.accept(Buffer.buffer("]"));
            inputStream.end();
        });

        return promise.future();
    }

    private void useVertxJsonParser() {
        JsonParser jsonParser = JsonParser.newParser();
        jsonParser.handler(event -> {
            getLogger().info(event.type().toString(), x -> {
                        x.put("filed", event.fieldName());
                        if (event.fieldName() != null) {
                            x.put("field_class", event.fieldName().getClass().getName());
                        }
                        x.put("value", event.value());
                        if (event.value() != null) {
                            x.put("value_class", event.value().getClass().getName());
                        }
                    }
            );
        });

        jsonParser.handle(Buffer.buffer("[{}"));
        jsonParser.handle(Buffer.buffer(",\n" + new JsonObject()
                .put("a", 1)
                .encodePrettily()));
        jsonParser.handle(Buffer.buffer(",\n" + new JsonObject()
                .put("b", new JsonArray()
                        .add(new JsonObject().put("b", "c"))
                )
                .encodePrettily()));
        jsonParser.handle(Buffer.buffer("]"));
        jsonParser.end();
    }

}
