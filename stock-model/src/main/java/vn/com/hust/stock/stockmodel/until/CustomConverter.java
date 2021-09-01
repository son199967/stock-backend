package vn.com.hust.stock.stockmodel.until;

import vn.com.hust.stock.stockmodel.exception.BusinessException;
import vn.com.hust.stock.stockmodel.exception.ErrorCode;

import javax.persistence.AttributeConverter;
import java.io.IOException;

public abstract class CustomConverter<T> implements AttributeConverter<Class<T>, String> {


    @Override
    public String convertToDatabaseColumn(Class<T> attribute) {
        return attribute == null ? null : Json.encode(attribute);
    }

    @Override
    public Class<T> convertToEntityAttribute(String dbData) {
        try {
            return dbData == null ? null : Json.decodeValue(dbData,  Class.class);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Can't convert json string to payment object", e);
        }

    }
}
