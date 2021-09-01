package vn.com.hust.stock.stockmodel.until;
import com.fasterxml.jackson.databind.JsonNode;
import vn.com.hust.stock.stockmodel.exception.BusinessException;
import vn.com.hust.stock.stockmodel.exception.ErrorCode;

import javax.persistence.AttributeConverter;
import java.io.IOException;

public class JsonNodeConverter implements AttributeConverter<JsonNode, String> {
    @Override
    public String convertToDatabaseColumn(JsonNode attribute) {
        return attribute == null ? null : Json.encode(attribute);
    }

    @Override
    public JsonNode convertToEntityAttribute(String dbData) {
        try {
            return dbData == null ? null : Json.decodeValue(dbData, JsonNode.class);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Can't convert json string to JsonNode object", e);
        }
    }
}
