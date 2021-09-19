package vn.com.hust.stock.stockjob.config;

import java.util.concurrent.*;

public class CustomThreadPoolExecutor extends ThreadPoolExecutor {
    final static int CORE_POOL_SIZE =4  ;
    final static int MAX_POLL_SIZE =5;
    final  static long KEEP_ALIVE_TIME =4000;
    final static  TimeUnit timeUnit = TimeUnit.SECONDS;


    public CustomThreadPoolExecutor() {
        super(CORE_POOL_SIZE, MAX_POLL_SIZE, KEEP_ALIVE_TIME,timeUnit, new ArrayBlockingQueue<Runnable>(1000));
    }
}
