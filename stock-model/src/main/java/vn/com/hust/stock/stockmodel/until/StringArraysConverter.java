package vn.com.hust.stock.stockmodel.until;

import vn.com.hust.stock.stockmodel.exception.BusinessException;
import vn.com.hust.stock.stockmodel.exception.ErrorCode;

import javax.persistence.AttributeConverter;
import java.io.IOException;
import java.util.List;

public class StringArraysConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        try {
            return attribute == null ? null : Json.encode(attribute);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.JSON_PROCESSING_ERROR, "Can't convert payment object to json string", e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        try {
            return dbData == null ? null : Json.decodeValue(dbData,List.class);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.JSON_PROCESSING_ERROR, "Can't convert json string to payment object", e);
        }

    }
}
