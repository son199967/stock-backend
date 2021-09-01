package vn.com.hust.stock.stockmodel.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class VpsResponseMap {
    List<VpsSymbolResponse> data;
}
