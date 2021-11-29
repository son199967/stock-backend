package vn.com.hust.stock.stockapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GroupsStock {


    private static Map<String, List<String>> STOCK_MAP = new HashMap<>();
    private static List<String> STOCK_ARRAYS = new ArrayList<>();

    static {
        STOCK_MAP.put("COMMON", Arrays.asList("VNINDEX", "VN30", "VN30_HOSE", "HNX", "HNX30","UPCOM"));
        STOCK_MAP.put( "GROUPS", Arrays.asList("CONGNGHE", "DAUKHI", "DICHVU", "DUOCPHAM", "XAYDUNG",
                "NANGLUONG", "NGANHANG", "NHUA", "THEP", "THUCPHAM", "THUONGMAI", "THUYSAN", "VANTAI", "VLXD", "HK"));

        for (List<String> a : STOCK_MAP.values()) {
            STOCK_ARRAYS.addAll(a);
        }

    }

    public Map<String, List<String>> STOCK_MAPS(){

        return STOCK_MAP;
    }
     public List<String> STOCK_ARRAYS(){
        return STOCK_ARRAYS;
    }
}
