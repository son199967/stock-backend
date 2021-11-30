package vn.com.hust.stock.stockapp.Job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.com.hust.stock.stockapp.repository.PriceHistoryRepository;
import vn.com.hust.stock.stockapp.service.PriceHistoryService;
import vn.com.hust.stock.stockmodel.entity.PriceHistory;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Component
@Slf4j
public class ImportDataProcess {



    private PriceHistoryRepository priceHistoryRepository;
    private Map<String,List<String>> STOCK_ARRAYS = new HashMap<>();


    private static int a = 0;

    private static final String COMMA_DELIMITER = "\",\""; // Split by comma
    private ScheduledExecutorService scheduledExecutor;
    private PriceHistoryService priceHistoryService;


    @Autowired
    public ImportDataProcess(PriceHistoryRepository priceHistoryRepository ,PriceHistoryService priceHistoryService) {
       this.priceHistoryRepository = priceHistoryRepository;
       this.priceHistoryService = priceHistoryService;
        scheduledExecutor = Executors.newScheduledThreadPool(10);
    }

    public void startImport(){
       CompletableFuture.runAsync(()-> importDataFromCsvFile());


    }

    private void importDataFromCsvFile(){
        priceHistoryRepository.deleteAll();
        STOCK_ARRAYS.put("bds",Arrays.asList("VIC","VHM","VRE","PRD","KDH","REE","DXG","HDG","FLC","ITA"));
        STOCK_ARRAYS.put("ck",Arrays.asList("SSI","VND","VCI","HCM","MBS","FTS","SHS","KLB","AGR","TVS"));
        STOCK_ARRAYS.put("cn",Arrays.asList("FPT","FOX","CMG","SAM","SGT","ELC","VEC","ITD","TTN","CNC"));
        STOCK_ARRAYS.put("dp",Arrays.asList("DGC","DHG","DVN","IMP","TRA","DMC","CSV","DCL","VFG","OPC"));
        STOCK_ARRAYS.put("hk",Arrays.asList("ACV","VJC","HVN","SAS","SGN","NCT","NCS","MAS","NAS","ARM"));
        STOCK_ARRAYS.put("bank",Arrays.asList("VCB","TCB","BID","CTG","MBB","VPB","ACB","SHB","STB","TPB","BVH","VIB","HDB","EIB","LPB","BAB","NVB","ABB","PVI","VBB"));
        STOCK_ARRAYS.put("xd",Arrays.asList("VCG","DIG","DXG","CTD","HBC","ROS","VCP","VLB","TV2","CC1"));
        STOCK_ARRAYS.put("dk",Arrays.asList("GAS","BSR","PLX","PVS","PVD","PVI","PVT","PLC","PET","PGS","BMI"));
        STOCK_ARRAYS.put("nhua",Arrays.asList("NTP","BMP","AAA","DNP","SVI","INN","RDP","HII","VNP","MCP"));
        STOCK_ARRAYS.put("common",Arrays.asList("VNINDEX","VN30","VN30_HOSE","HNX","HNX30","CONGNGHE","DAUKHI","DICHVU","DUOCPHAM","XAYDUNG",
                "NANGLUONG","NGANHANG","NHUA","THEP","THUCPHAM","THUONGMAI","THUYSAN","UPCOM","VANTAI","VLXD"));
        int a =0;
        BufferedReader br = null;
        try {
            String line;
            br = new BufferedReader(new FileReader("/home/ntson6/amibroker_all_data.txt"));
            // How to read file in java line by line?
            while ((line = br.readLine()) != null) {
                String finalLine = line;
                a++;
                log.info("line : {}", line);
                scheduledExecutor.execute(()-> addToDataBase(parseCsvLine(finalLine)));
                System.out.println("aaaaaa------------>:"+a);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException crunchifyException) {
                crunchifyException.printStackTrace();
            }
        }
        priceHistoryService.updateData();
    }
    public  List<String> parseCsvLine(String csvLine) {
        List<String> result = new ArrayList<String>();
        if (csvLine != null) {
            String[] splitData = csvLine.split(",");
            for (int i = 0; i < splitData.length; i++) {
                result.add(splitData[i].replace("^",""));
            }
        }
        return result;
    }
    private  void addToDataBase(List<String> data) {
        if (data.get(0).toCharArray().length!=3
                && !Arrays.asList("VNINDEX","VN30","VN30_HOSE","HNX","HNX30","CONGNGHE","DAUKHI","DICHVU","DUOCPHAM","XAYDUNG",
                "NANGLUONG","NGANHANG","NHUA","THEP","THUCPHAM","THUONGMAI","THUYSAN","UPCOM","VANTAI","VLXD")
                .contains(data.get(0)))
            return;
        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate datetime = LocalDate.parse(data.get(1), pattern);
       PriceHistory priceHistory = new PriceHistory();
       priceHistory.setSym(data.get(0));
       priceHistory.setOpen(Double.parseDouble(data.get(2)));
       priceHistory.setTime(datetime);
       priceHistory.setHigh(Double.parseDouble(data.get(3)));
       priceHistory.setLow(Double.parseDouble(data.get(4)));
       priceHistory.setClose(Double.parseDouble(data.get(5)));
       if (priceHistory.getClose()==0d) return;
       priceHistory.setVolume(Double.parseDouble(data.get(6)));
       priceHistory.setPercent((priceHistory.getClose()-priceHistory.getOpen())*100/priceHistory.getOpen());
       priceHistoryRepository.save(priceHistory);
       System.out.println(priceHistory.toString()+ "done");
    }
}
