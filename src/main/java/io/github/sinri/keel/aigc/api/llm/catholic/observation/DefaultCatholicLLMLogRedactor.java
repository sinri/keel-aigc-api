package io.github.sinri.keel.aigc.api.llm.catholic.observation;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Conservative default redactor for headers, JSON payloads, SSE data and free text.
 */
public final class DefaultCatholicLLMLogRedactor implements CatholicLLMLogRedactor {
    public static final String REDACTED = "***REDACTED***";

    private static final Set<String> SENSITIVE_HEADERS = Set.of(
        "authorization", "proxy-authorization", "x-api-key", "api-key", "cookie", "set-cookie"
    );
    private static final Set<String> SENSITIVE_JSON_KEYS = Set.of(
        "apikey", "accesstoken", "refreshtoken", "authorization", "password", "passwd",
        "secret", "clientsecret", "privatekey", "session", "cookie", "signature"
    );
    private static final Pattern BEARER = Pattern.compile("(?i)\\bBearer\\s+[^\\s,;\"']+");
    private static final Pattern OPENAI_KEY = Pattern.compile("\\bsk-[A-Za-z0-9_-]{12,}\\b");
    private static final Pattern JWT = Pattern.compile("\\beyJ[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\b");
    private static final Pattern PEM = Pattern.compile(
        "-----BEGIN [^-]*PRIVATE KEY-----[\\s\\S]*?-----END [^-]*PRIVATE KEY-----"
    );
    private static final Pattern QUERY_SECRET = Pattern.compile(
        "(?i)([?&](?:api[_-]?key|access[_-]?token|token|signature|secret)=)[^&#\\s]+"
    );
    private static final Pattern JSON_SECRET = Pattern.compile(
        "(?i)(\"(?:api[_-]?key|access[_-]?token|refresh[_-]?token|authorization|password|passwd|secret|"
            + "client[_-]?secret|private[_-]?key|session|cookie|signature)\"\\s*:\\s*\")([^\"]*)(\")"
    );
    private static final Pattern LONG_BASE64 = Pattern.compile("(?<![A-Za-z0-9+/=])[A-Za-z0-9+/]{512,}={0,2}");

    @Override
    public String redactHeader(String name, String value) {
        if (SENSITIVE_HEADERS.contains(name.toLowerCase(Locale.ROOT))) {
            return REDACTED;
        }
        return redactText(value);
    }

    @Override
    public String redactBody(String body) {
        if (body == null || body.isEmpty()) {
            return body;
        }
        try {
            Object json = body.stripLeading().startsWith("[") ? new JsonArray(body) : new JsonObject(body);
            redactJson(json);
            return json instanceof JsonObject object ? object.encode() : ((JsonArray) json).encode();
        } catch (RuntimeException ignored) {
            StringBuilder result = new StringBuilder();
            String[] lines = body.split("\\n", -1);
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (line.startsWith("data:")) {
                    String data = line.substring(5).stripLeading();
                    result.append("data: ").append(redactBody(data));
                } else {
                    result.append(redactText(line));
                }
                if (i + 1 < lines.length) result.append('\n');
            }
            return result.toString();
        }
    }

    @Override
    public String redactText(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String redacted = PEM.matcher(text).replaceAll(REDACTED);
        redacted = BEARER.matcher(redacted).replaceAll("Bearer " + REDACTED);
        redacted = OPENAI_KEY.matcher(redacted).replaceAll(REDACTED);
        redacted = JWT.matcher(redacted).replaceAll(REDACTED);
        redacted = QUERY_SECRET.matcher(redacted).replaceAll("$1" + REDACTED);
        redacted = JSON_SECRET.matcher(redacted).replaceAll("$1" + REDACTED + "$3");
        return LONG_BASE64.matcher(redacted).replaceAll(match ->
            "<binary-like-data omitted,length=" + match.group().length()
                + ",sha256=" + sha256(match.group()) + ">"
        );
    }

    private void redactJson(Object value) {
        if (value instanceof JsonObject object) {
            for (String key : Set.copyOf(object.fieldNames())) {
                Object child = object.getValue(key);
                if (SENSITIVE_JSON_KEYS.contains(normalizeKey(key))) {
                    object.put(key, REDACTED);
                } else if (child instanceof JsonObject || child instanceof JsonArray) {
                    redactJson(child);
                } else if (child instanceof String text) {
                    object.put(key, redactText(text));
                }
            }
        } else if (value instanceof JsonArray array) {
            for (int i = 0; i < array.size(); i++) {
                Object child = array.getValue(i);
                if (child instanceof JsonObject || child instanceof JsonArray) {
                    redactJson(child);
                } else if (child instanceof String text) {
                    array.set(i, redactText(text));
                }
            }
        }
    }

    private String normalizeKey(String key) {
        return key.toLowerCase(Locale.ROOT).replace("_", "").replace("-", "");
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }
}
