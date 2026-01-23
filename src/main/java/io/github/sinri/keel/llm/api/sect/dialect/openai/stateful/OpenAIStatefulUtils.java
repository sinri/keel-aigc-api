package io.github.sinri.keel.llm.api.sect.dialect.openai.stateful;

import io.github.sinri.keel.llm.api.catholic.message.MixChatMessage;
import io.github.sinri.keel.llm.api.catholic.request.MixChatRequest;
import io.github.sinri.keel.llm.api.catholic.response.MixChatResponse;
import io.github.sinri.keel.llm.api.catholic.tool.definition.ToolDefinition;
import io.github.sinri.keel.llm.api.sect.dialect.openai.stateful.content.StatefulChatItemContentOutputText;
import io.vertx.core.json.JsonArray;

import java.util.List;

public class OpenAIStatefulUtils {
    public static StatefulChatRequest toStatefulChatRequest(MixChatRequest mixChatRequest) {
        StatefulChatRequest request = StatefulChatRequest.create();

        List<MixChatMessage> messages = mixChatRequest.getMessages();
        for (MixChatMessage message : messages) {
            if ("system".equals(message.getRole())) {
                String textContent = message.getTextContent();
                if (textContent != null) {
                    request.setInstructions(textContent);
                }
            } else if ("user".equals(message.getRole())) {
                String textContent = message.getTextContent();
                if (textContent != null) {
                    request.setInput(textContent);
                }
            }
        }

        List<ToolDefinition> toolDefinitions = mixChatRequest.getTools();
        JsonArray array = new JsonArray();
        for (ToolDefinition toolDefinition : toolDefinitions) {
            array.add(toolDefinition.toJsonObject());
        }
        request.setTools(array);

        if (mixChatRequest.isStream()) {
            request.setStream(true);
        }

        return request;
    }

    public static MixChatResponse from(StatefulChatResponse response) {
        MixChatResponse mixChatResponse = MixChatResponse.create();
        for (StatefulChatResponseOutputItem outputItem : response.getOutput()) {
            String role = outputItem.getRole();
            String join = String.join("", outputItem.getContent()
                                                    .stream()
                                                    .map(StatefulChatItemContentOutputText::getText)
                                                    .toList()
            );
            mixChatResponse.setMessage(MixChatMessage.create()
                                                     .setRole(role)
                                                     .setTextContent(join)
            );
        }
        return mixChatResponse;
    }
}
