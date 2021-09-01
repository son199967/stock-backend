package vn.com.hust.stock.stockmodel.until;

import vn.com.hust.stock.stockmodel.exception.BusinessException;
import vn.com.hust.stock.stockmodel.exception.ErrorCode;

import javax.persistence.AttributeConverter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

public class MapConvert  implements AttributeConverter<Map, String> {
    @Override
    public String convertToDatabaseColumn(Map attribute) {
        return attribute == null ? null : Json.encode(attribute);
    }

    @Override
    public Map<LocalDate,String> convertToEntityAttribute(String dbData) {
        try {
            return dbData == null ? null : Json.decodeValue(dbData, Map.class );
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Can't convert json string to payment object", e);
        }

    }
}
