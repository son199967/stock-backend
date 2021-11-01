package vn.com.hust.stock.stockapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;

@Configuration
public class GroupsStock {


    private static Map<String, List<String>> STOCK_MAP = new HashMap<>();
    private static List<String> STOCK_ARRAYS = new ArrayList<>();

    static {
        STOCK_MAP.put("BDS", Arrays.asList("VIC", "VHM", "VRE", "PRD", "KDH", "REE", "DXG", "HDG", "FLC", "ITA"));
        STOCK_MAP.put("CK", Arrays.asList("SSI", "VND", "VCI", "HCM", "MBS", "FTS", "SHS", "KLB", "AGR", "TVS"));
        STOCK_MAP.put("CONGNGHE", Arrays.asList("FPT", "FOX", "CMG", "SAM", "SGT", "ELC", "VEC", "ITD", "TTN", "CNC"));
        STOCK_MAP.put("DUOCPHAM", Arrays.asList("DGC", "DHG", "DVN", "IMP", "TRA", "DMC", "CSV", "DCL", "VFG", "OPC"));
        STOCK_MAP.put("HK", Arrays.asList("ACV", "VJC", "HVN", "SAS", "SGN", "NCT", "NCS", "MAS", "NAS", "ARM"));
        STOCK_MAP.put("NGANHANG", Arrays.asList("VCB", "TCB", "BID", "CTG", "MBB", "VPB", "ACB", "SHB", "STB", "TPB", "BVH", "VIB", "HDB", "EIB", "LPB", "BAB", "NVB", "ABB", "PVI", "VBB"));
        STOCK_MAP.put("XAYDUNG", Arrays.asList("VCG", "DIG", "DXG", "CTD", "HBC", "ROS", "VCP", "VLB", "TV2", "CC1"));
        STOCK_MAP.put("DAUKHI", Arrays.asList("GAS", "BSR", "PLX", "PVS", "PVD", "PVI", "PVT", "PLC", "PET", "PGS"));
        STOCK_MAP.put("NHUA", Arrays.asList("NTP", "BMP", "AAA", "DNP", "SVI", "INN", "RDP", "HII", "VNP", "MCP"));
        STOCK_MAP.put("COMMON", Arrays.asList("VNINDEX", "VN30", "VN30_HOSE", "HNX", "HNX30","UPCOM"));
        STOCK_MAP.put( "GROUPS", Arrays.asList("CONGNGHE", "DAUKHI", "DICHVU", "DUOCPHAM", "XAYDUNG",
                "NANGLUONG", "NGANHANG", "NHUA", "THEP", "THUCPHAM", "THUONGMAI", "THUYSAN", "VANTAI", "VLXD", "HK"));

        for (List<String> a : STOCK_MAP.values()) {
            STOCK_ARRAYS.addAll(a);
        }

    }

    @Bean
     Map<String, List<String>> STOCK_MAPS(){
        return STOCK_MAP;
    }
    @Bean
     List<String> STOCK_ARRAYS(){
        return STOCK_ARRAYS;
    }
}
