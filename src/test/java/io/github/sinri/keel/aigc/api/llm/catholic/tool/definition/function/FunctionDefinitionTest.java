package io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.function;

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FunctionDefinitionTest {

    @Test
    void testBuildFunctionDefinitionWithParameters() {
        FunctionDefinition functionDefinition = FunctionDefinition.builder()
            .name("get_weather")
            .comment("Get weather info")
            .addParameter("location", "string", "City name", true)
            .addParameter("unit", new JsonObject()
                .put("type", "string")
                .put("enum", java.util.List.of("celsius", "fahrenheit"))
                .put("description", "Temperature unit"))
            .build();

        assertEquals("get_weather", functionDefinition.name());
        assertEquals("Get weather info", functionDefinition.description());

        JsonObject parameters = functionDefinition.parameters();
        assertEquals("object", parameters.getString("type"));
        assertEquals("string", parameters.getJsonObject("properties")
            .getJsonObject("location")
            .getString("type"));
        assertEquals("location", parameters.getJsonArray("required").getString(0));
    }

    @Test
    void testBuildRequiresNameAndDescription() {
        assertThrows(IllegalArgumentException.class, () -> FunctionDefinition.builder()
            .description("No name")
            .build());
        assertThrows(IllegalArgumentException.class, () -> FunctionDefinition.builder()
            .name("no_description")
            .build());
    }
}
