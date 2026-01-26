package io.github.sinri.keel.aigc.api.llm.sect;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import org.jspecify.annotations.Nullable;


/**
 * 大语言模型服务适配器运作过程中出现的回复报文异常。
 * 封装了状态码和响应体内容，支持JSON解析。
 *
 * @since 5.0.0
 */
public class AbnormalResponse extends RuntimeException {
    /**
     * HTTP状态码。
     */
    private final int statusCode;
    /**
     * 响应体内容。
     */
    private final String responseBody;

    /**
     * 通过HttpResponse构造异常。
     *
     * @param httpResponse Vert.x HTTP响应
     */
    public AbnormalResponse(HttpResponse<Buffer> httpResponse) {
        this(httpResponse.statusCode(), httpResponse.bodyAsString());
    }

    /**
     * 通过状态码和响应体构造异常。
     *
     * @param statusCode   HTTP状态码
     * @param responseBody 响应体内容
     */
    private AbnormalResponse(int statusCode, String responseBody) {
        super("[Abnormal Response] STATUS: " + statusCode + "; BODY: " + responseBody);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    /**
     * 获取HTTP状态码。
     *
     * @return 状态码
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * 获取响应体内容。
     *
     * @return 响应体
     */
    public String getResponseBody() {
        return responseBody;
    }

    /**
     * 获取响应体的JSON对象表示。
     *
     * @return JSON对象，若解析失败则为null
     */
    public @Nullable JsonObject getResponseBodyAsJson() {
        try {
            return new JsonObject(responseBody);
        } catch (Throwable e) {
            return null;
        }
    }
}
