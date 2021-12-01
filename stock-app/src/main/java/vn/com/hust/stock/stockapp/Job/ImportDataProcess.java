package vn.com.hust.stock.stockapp.Job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.com.hust.stock.stockapp.config.GroupsStock;
import vn.com.hust.stock.stockapp.repository.PriceHistoryRepository;
import vn.com.hust.stock.stockapp.service.NormalService;
import vn.com.hust.stock.stockapp.service.PriceHistoryService;
import vn.com.hust.stock.stockmodel.entity.PriceHistory;

import javax.transaction.Transactional;
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
    private Map<String, List<String>> STOCK_ARRAYS = new HashMap<>();


    private static int a = 0;
    private GroupsStock groupsStock;
    private static final String COMMA_DELIMITER = "\",\""; // Split by comma
    private PriceHistoryService priceHistoryService;
    private NormalService normalService;


    @Autowired
    public ImportDataProcess(PriceHistoryRepository priceHistoryRepository,
                             PriceHistoryService priceHistoryService,
                             GroupsStock groupsStock, NormalService normalService) {
        this.priceHistoryRepository = priceHistoryRepository;
        this.priceHistoryService = priceHistoryService;
        this.groupsStock = groupsStock;
        this.normalService = normalService;
    }

    public void startImport() {
        priceHistoryRepository.deleteAll();
        importDataFromCsvFile();
        normalService.resetCache();
    }

    @Transactional
    public void importDataFromCsvFile() {
        List<String> stockGr = groupsStock.STOCK_ARRAYS();
        int a = 0;
        BufferedReader br = null;
        String stockExe = "";
        List<PriceHistory> dataInsert = new ArrayList<>();
        double cumulativeLog = 1;
        try {
            String line;
            br = new BufferedReader(new FileReader("/home/ntson6/amibroker_all_data.txt"));
            // How to read file in java line by line?
            while ((line = br.readLine()) != null) {
                String finalLine = line;
                a++;
                log.info("line : {}", line);
                List<String> data = parseCsvLine(finalLine);
                if (data.size() != 7) continue;
                if (!stockGr.contains(data.get(0)))
                    continue;
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
                priceHistory.setPercent((priceHistory.getClose() - priceHistory.getOpen()) * 100 / priceHistory.getOpen());
                if (!data.get(0).equals(stockExe)) {
                    if (dataInsert.size() != 0)
                        priceHistoryRepository.saveAll(dataInsert);
                    stockExe = priceHistory.getSym();
                    dataInsert = new ArrayList<>();
                    cumulativeLog = 1;
                } else {
                    double priceT_1 = dataInsert.get(dataInsert.size() - 1).getClose();
                    double simpleReturn = (priceHistory.getClose() - priceT_1) / priceT_1;
                    double grossReturn = 1 + simpleReturn;
                    double logReturn = Math.log(grossReturn);
                    cumulativeLog = cumulativeLog * (1 + logReturn);
                    priceHistory.setSimpleReturn(simpleReturn * 100);
                    priceHistory.setGrossReturn(grossReturn * 100);
                    priceHistory.setLogReturn(logReturn * 100);
                    priceHistory.setCumulativeLog(cumulativeLog);
                }
                dataInsert.add(priceHistory);

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

    public List<String> parseCsvLine(String csvLine) {
        List<String> result = new ArrayList<String>();
        if (csvLine != null) {
            String[] splitData = csvLine.split(",");
            for (int i = 0; i < splitData.length; i++) {
                result.add(splitData[i].replace("^", ""));
            }
        }
        return result;
    }

}
