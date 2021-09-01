package vn.com.hust.stock.stockmodel.until;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import vn.com.hust.stock.stockmodel.exception.BusinessException;
import vn.com.hust.stock.stockmodel.exception.ErrorCode;
;

import java.io.IOException;
import java.io.InputStream;

public class Json {
    public static ObjectMapper mapper = new ObjectMapper();

    static {
        // Non-standard JSON but we allow C style comments in our JSON
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Encode a POJO to JSON using the underlying Jackson mapper.
     *
     * @param obj a POJO
     * @return a String containing the JSON representation of the given POJO.
     * @throws BusinessException if a property cannot be encoded.
     */
    public static String encode(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.JSON_PROCESSING_ERROR, e);
        }
    }

    /**
     * Encode a POJO to JSON byte[] using the underlying Jackson mapper.
     *
     * @param obj a POJO
     * @return a byte[] containing the JSON representation of the given POJO.
     * @throws JsonProcessingException if a property cannot be encoded.
     */
    public static byte[] encodeValue(Object obj) {
        try {
            return mapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.JSON_PROCESSING_ERROR, e);
        }
    }

    /**
     * Decode a given JSON input stream to a POJO of the given class type.
     *
     * @param inputStream   the JSON input stream.
     * @param clazz the class to map to.
     * @param <T>   the generic type.
     * @return an instance of T
     * @throws IOException when there is a parsing or invalid mapping.
     */
    public static <T> T decodeValue(InputStream inputStream, Class<T> clazz) throws  IOException {
        return mapper.readValue(inputStream, clazz);
    }

    /**
     * Decode a given JSON string to a POJO of the given class type.
     *
     * @param str   the JSON string.
     * @param clazz the class to map to.
     * @param <T>   the generic type.
     * @return an instance of T
     * @throws IOException when there is a parsing or invalid mapping.
     */
    public static <T> T decodeValue(String str, Class<T> clazz) throws  IOException {
        return mapper.readValue(str, clazz);
    }

    /**
     * Decode a given JSON string to a POJO of the given type.
     *
     * @param str  the JSON string.
     * @param type the type to map to.
     * @param <T>  the generic type.
     * @return an instance of T
     * @throws IOException when there is a parsing or invalid mapping.
     */
    public static <T> T decodeValue(String str, TypeReference<T> type) throws IOException {
        return mapper.readValue(str, type);
    }

    /**
     * Decode a given JSON Node to a POJO of the given class type.
     *
     * @param jsonNode  the JSON Node.
     * @param type the type to map to.
     * @param <T>  the generic type.
     * @return an instance of T
     * @throws IllegalArgumentException when there is a parsing or invalid mapping.
     */
    public static <T> T decodeValue(JsonNode jsonNode, TypeReference<T> type) {
        return mapper.convertValue(jsonNode, type);
    }

    /**
     * Decode a given JSON Node to a POJO of the given class type.
     *
     * @param jsonNode   the JSON Node.
     * @param clazz the class to map to.
     * @param <T>   the generic type.
     * @return an instance of T
     * @throws JsonProcessingException when there is a parsing or invalid mapping.
     */
    public static <T> T decodeValue(JsonNode jsonNode, Class<T> clazz) {
        try {
            return mapper.treeToValue(jsonNode, clazz);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.JSON_PROCESSING_ERROR, e);
        }
    }

    public static JsonNode toJsonNode(Object o) {
        try {
            return mapper.valueToTree(o);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.JSON_PROCESSING_ERROR, e);
        }
    }
}

