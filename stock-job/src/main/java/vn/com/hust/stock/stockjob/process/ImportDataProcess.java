package vn.com.hust.stock.stockjob.process;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.com.hust.stock.stockjob.repository.PriceHistoryRepository;
import vn.com.hust.stock.stockmodel.entity.PriceHistory;
import vn.com.hust.stock.stockmodel.enumm.Floor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Component
public class ImportDataProcess {


    private PriceHistoryRepository priceHistoryRepository;
    private List<String> STOCK_ARRAYS = new ArrayList<>(Arrays.asList("ACB","BID",
            "BVH","CTG","FPT","GAS","GVR","HDB","HPG","KDH","MBB","MSN","MWG","NVL","PDR","PLX"
            ,"PNJ","POW","SAB","SSI","STB","TCB","TPB","VCB","VHM","VIC","VJC","VNM","VPB","VRE","REE"));

    private static int a =0;

    private static final String COMMA_DELIMITER = ","; // Split by comma
    private ScheduledExecutorService scheduledExecutor;


    @Autowired
    public ImportDataProcess(PriceHistoryRepository priceHistoryRepository) {
       this.priceHistoryRepository = priceHistoryRepository;
        new Thread(() -> {
            scheduledExecutor = Executors.newScheduledThreadPool(10);
        }).start();
//        importDataFromCsvFile();
        importDataFPTFromCsvFile();
        System.out.println("+++++++++++++++> done");
    }

    private void importDataFromCsvFile(){
        int a =0;
        BufferedReader br = null;
        try {
            String line;
            br = new BufferedReader(new FileReader("/Users/sonnguyen/Documents/code/stock-backend/stock-job/data/CafeF.HSX.Upto15.09.2021.csv"));

            // How to read file in java line by line?
            while ((line = br.readLine()) != null) {
                String finalLine = line;
                a++;
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
    }
    public  List<String> parseCsvLine(String csvLine) {
        List<String> result = new ArrayList<String>();
        if (csvLine != null) {
            String[] splitData = csvLine.split(COMMA_DELIMITER);
            for (int i = 0; i < splitData.length; i++) {
                result.add(splitData[i]);
            }
        }
        return result;
    }
    private  void addToDataBase(List<String> data) {
        if (!STOCK_ARRAYS.contains(data.get(0))) return;
        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate datetime = LocalDate.parse(data.get(1), pattern);
       PriceHistory priceHistory = new PriceHistory();
       priceHistory.setSym(data.get(0));
       priceHistory.setOpen(Double.parseDouble(data.get(2)));
       priceHistory.setTime(datetime);
       priceHistory.setHigh(Double.parseDouble(data.get(3)));
       priceHistory.setLow(Double.parseDouble(data.get(4)));
       priceHistory.setClose(Double.parseDouble(data.get(5)));
       priceHistory.setVolume(Double.parseDouble(data.get(6)));
       priceHistory.setFloor(Floor.HSX);
       priceHistoryRepository.save(priceHistory);
       System.out.println(priceHistory.toString()+ "done");
    }
    private void importDataFPTFromCsvFile(){
        int a =0;
        BufferedReader br = null;
        try {
            String line;
            br = new BufferedReader(new FileReader("/Users/sonnguyen/Documents/code/stock-backend/stock-job/data/Price_BID_Daily.csv"));

            // How to read file in java line by line?
            while ((line = br.readLine()) != null) {
                String finalLine = line;
                a++;
                scheduledExecutor.execute(()-> addToDataBaseFPT(parseCsvLineFPT(finalLine)));
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
    }
    public  List<String> parseCsvLineFPT(String csvLine) {
        List<String> result = new ArrayList<String>();
        if (csvLine != null) {
            String[] splitData = csvLine.split(COMMA_DELIMITER);
            for (int i = 0; i < splitData.length; i++) {
                result.add(splitData[i]);
            }
        }
        return result;
    }
    private  void addToDataBaseFPT(List<String> data) {
        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate datetime = LocalDate.parse(data.get(0), pattern);
        PriceHistory priceHistory = new PriceHistory();
        priceHistory.setSym("BID");
        priceHistory.setOpen(Double.parseDouble(data.get(1)));
        priceHistory.setTime(datetime);
        priceHistory.setHigh(Double.parseDouble(data.get(2)));
        priceHistory.setLow(Double.parseDouble(data.get(3)));
        priceHistory.setClose(Double.parseDouble(data.get(4)));
        priceHistory.setVolume(Double.parseDouble(data.get(5)));
        priceHistory.setAnnualisedStandardDeviation(0);
        priceHistory.setNumberOfSharesWithEquity(0);
        priceHistory.setConstrainedWeightsLeverage(0);
        priceHistory.setTargetWeights(0);
        priceHistory.setFloor(Floor.HSX);
        priceHistoryRepository.save(priceHistory);
        System.out.println(priceHistory.toString()+ "done");
    }
}
