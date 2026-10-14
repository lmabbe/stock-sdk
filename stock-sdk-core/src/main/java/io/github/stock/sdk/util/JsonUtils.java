package io.github.stock.sdk.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.stock.sdk.exception.StockException;

import java.io.IOException;

/**
 * JSON 序列化与反序列化工具类
 */
public class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static ObjectMapper getMapper() {
        return MAPPER;
    }

    public static JsonNode readTree(String json) {
        try {
            return MAPPER.readTree(json);
        } catch (IOException e) {
            throw new StockException("JSON_PARSE_ERROR", "Failed to parse json tree: " + json, e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            throw new StockException("JSON_PARSE_ERROR", "Failed to parse json to " + clazz.getSimpleName(), e);
        }
    }

    public static <T> T fromJson(String json, TypeReference<T> typeRef) {
        try {
            return MAPPER.readValue(json, typeRef);
        } catch (IOException e) {
            throw new StockException("JSON_PARSE_ERROR", "Failed to parse json with type reference", e);
        }
    }

    public static String toJson(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (IOException e) {
            throw new StockException("JSON_SERIALIZE_ERROR", "Failed to serialize object to json", e);
        }
    }
}
