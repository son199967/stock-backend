package vn.com.hust.stock.stockjob.process;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.com.hust.stock.stockjob.config.CustomThreadPoolExecutor;
import vn.com.hust.stock.stockjob.repository.CustomRepository;
import vn.com.hust.stock.stockjob.repository.PriceHistoryRepository;
import vn.com.hust.stock.stockmodel.data.VpsResponseMap;
import vn.com.hust.stock.stockmodel.data.VpsSymbolResponse;
import vn.com.hust.stock.stockmodel.entity.PriceHistory;
import vn.com.hust.stock.stockmodel.exception.BusinessException;
import vn.com.hust.stock.stockmodel.exception.ErrorCode;
import vn.com.hust.stock.stockmodel.until.Json;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

@Component
@Slf4j
public class DataProcess {
    @Autowired
    private CustomRepository<PriceHistory, Long> customRepository;
    Map<String,PriceHistory> priceHistoryMap = new HashMap<>();


    private CustomThreadPoolExecutor threadPoolExecutor;
    private ScheduledExecutorService scheduledExecutor;
    @Autowired
    private HttpClient httpClient;

    public DataProcess() {
        new Thread(() -> {
            threadPoolExecutor = new CustomThreadPoolExecutor();
            threadPoolExecutor.prestartAllCoreThreads();
        }
        ).start();
        new Thread(() -> {
            scheduledExecutor = Executors.newScheduledThreadPool(1);
            scheduledExecutor.scheduleWithFixedDelay(() -> getData(), 5, 10, TimeUnit.SECONDS);
        }).start();

    }

    private void getData() {
        try {
        JsonNode jsonNode = getDataFromVps();
//        log.info("get Data from VPS :{}",jsonNode.toString());

        VpsSymbolResponse[] vpsResponse = new ObjectMapper().readValue(jsonNode.toString(),VpsSymbolResponse[].class);
            log.info("get Data from VPS Map size :{}",vpsResponse);
            List<PriceHistory> priceHistories = new ArrayList<>();
            StreamSupport.stream(Arrays.stream(vpsResponse).spliterator(),true).forEach(v -> {
                log.info("Date :{}",v);
                PriceHistory priceHistory = PriceHistory.builder()
                        .highPrice(v.getHighPrice())
                        .lowPrice(v.getLowPrice())
                        .avePrice(v.getAvePrice())
                        .sym(v.getSym())
                        .g1(Arrays.asList(v.getG1().split("\\|").clone()))
                        .g2(Arrays.asList(v.getG2().split("\\|").clone()))
                        .g3(Arrays.asList(v.getG3().split("\\|").clone()))
                        .g4(Arrays.asList(v.getG4().split("\\|").clone()))
                        .g5(Arrays.asList(v.getG5().split("\\|").clone()))
                        .g6(Arrays.asList(v.getG6().split("\\|").clone()))
                        .g7(Arrays.asList(v.getG7().split("\\|").clone()))
                        .priceCeil(v.getC())
                        .priceFloor(v.getF())
                        .priceTc(v.getR())
                        .time(LocalDateTime.now())
                        .build();
                priceHistoryMap.put(v.getSym(),priceHistory);
                priceHistories.add(priceHistory);
            });
            customRepository.saveAll(priceHistories);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    private JsonNode getDataFromVps(){
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://bgapidatafeed.vps.com.vn/getliststockdata/ACB,BID,BVH,CTG,FPT,GAS,GVR,HDB,HPG,KDH,MBB,MSN,MWG,NVL,PDR,PLX,PNJ,POW,SAB,SSI,STB,TCB,TPB,VCB,VHM,VIC,VJC,VNM,VPB,VRE"))
                .GET()
                .build();

        return  httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(strResp -> handleResponse(strResp, () -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "call error")))
                .thenApply(this::toJsonNode).join();
    }

    private JsonNode toJsonNode(String response) {
        try {
            return Json.decodeValue(response, JsonNode.class);
        } catch (IOException e) {
            throw new CompletionException(e);
        }
    }

    private String handleResponse(HttpResponse<String> response, Supplier<BusinessException> exceptionSupplier) {
        if (response.statusCode() >= 400 && response.statusCode() < 500) {
            log.error(response.body());
            throw exceptionSupplier.get();
        }

        if (response.statusCode() >= 500) {
            log.error(response.body());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return response.body();
    }


}
