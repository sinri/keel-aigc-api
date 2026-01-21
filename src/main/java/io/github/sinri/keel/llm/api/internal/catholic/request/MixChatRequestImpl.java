package io.github.sinri.keel.llm.api.internal.catholic.request;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.llm.api.catholic.message.MixChatMessage;
import io.github.sinri.keel.llm.api.catholic.request.MixChatRequest;
import io.github.sinri.keel.llm.api.catholic.request.MixChatRequestExtra;
import io.github.sinri.keel.llm.api.catholic.tool.definition.ToolDefinition;
import io.github.sinri.keel.llm.api.internal.catholic.tool.CommonToolDefinition;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Objects;

public class MixChatRequestImpl extends JsonifiableDataUnitImpl implements MixChatRequest {
    public MixChatRequestImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

    public MixChatRequestImpl() {
        super();
        setRequestId(java.util.UUID.randomUUID().toString());
    }

    @Override
    public String getModel() {
        return readStringRequired("model");
        //        JsonObject specificationJsonObject = readJsonObjectRequired("specification");
        //        String className = specificationJsonObject.getString("class_name");
        //        if (className == null) {
        //            throw new IllegalArgumentException("class is required in specification");
        //        }
        //        try {
        //            Object instance = Class.forName(className)
        //                                   .getConstructor(JsonObject.class)
        //                                   .newInstance(specificationJsonObject);
        //            if (instance instanceof LargeLanguageModel) {
        //                return (LargeLanguageModel) instance;
        //            } else {
        //                throw new IllegalArgumentException("Class described by class_name must implement LLMServiceSpecification");
        //            }
        //        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
        //                 NoSuchMethodException e) {
        //            throw new RuntimeException(e);
        //        }
    }


    @Override
    public MixChatRequest setModel(String largeLanguageModelCode) {
        ensureEntry("model", largeLanguageModelCode);
        return this;
    }

    @Override
    public String getRequestId() {
        return readStringRequired("request_id");
    }

    @Override
    public MixChatRequest setRequestId(String requestId) {
        ensureEntry("request_id", requestId);
        return this;
    }

    @Override
    public long getTimeout() {
        return Objects.requireNonNullElse(readLong("timeout"), 180_000L);
    }

    @Override
    public MixChatRequest setTimeout(long timeout) {
        ensureEntry("timeout", timeout);
        return this;
    }

    @Override
    public MixChatRequest setStream(boolean stream) {
        ensureEntry("stream", stream);
        return this;
    }

    public boolean isStream() {
        Boolean stream = readBoolean("stream");
        return Boolean.TRUE.equals(stream);
    }

    @Override
    public MixChatRequest addMessage(MixChatMessage message) {
        ensureJsonArray("messages")
                .add(message.toJsonObject());
        return this;
    }

    @Override
    public List<MixChatMessage> getMessages() {
        List<JsonObject> array = readJsonObjectArray("messages");
        if (array == null) return List.of();
        return array.stream().map(MixChatMessage::wrap).toList();
    }

    @Override
    public MixChatRequest addTool(ToolDefinition toolDefinition) {
        ensureJsonArray("tools")
                .add(toolDefinition.toJsonObject());
        return this;
    }

    @Override
    public List<ToolDefinition> getTools() {
        List<JsonObject> array = readJsonObjectArray("tools");
        if (array == null) return List.of();
        return array.stream().map(CommonToolDefinition::new)
                    .map(x -> (ToolDefinition) x)
                    .toList();
    }

    @Override
    public MixChatRequest setExtra(MixChatRequestExtra extra) {
        ensureEntry("extra", extra.toJsonObject());
        return this;
    }

    @Override
    public MixChatRequestExtra getExtra() {
        var x = readJsonObject("extra");
        if (x == null) x = new JsonObject();
        return MixChatRequestExtra.wrap(x);
    }

}
