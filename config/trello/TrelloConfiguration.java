package com.sharkdom.config.trello;

import com.sharkdom.config.trello.threading.TrelloExecutor;
import com.sharkdom.config.trello.threading.TrelloForkJoinPool;
import com.sharkdom.model.trello.TrelloAuthParams;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

@Configuration
@EnableAsync
public class TrelloConfiguration {

    @Value("${trello.api.key}")
    private String apiKey;

    @Value("${trello.api.token}")
    private String apiToken;

    @Value("${trello.api.base-url}")
    private String baseUrl;

    @Value("${trello.async.pool.parallelism-factor:2}")
    private int parallelismFactor;

    @Value("${trello.async.pool.max-threads:32}")
    private int maxThreads;

    @Value("${trello.async.pool.min-threads:8}")
    private int minThreads;

    @Bean
    public TrelloAuthParams trelloAuthParams() {
        return new TrelloAuthParams(apiKey, apiToken, baseUrl);
    }

    @Bean(name = "trelloTaskExecutor")
    public Executor trelloTaskExecutor() {
        return new TrelloExecutor(trelloForkJoinPool());
    }

    final int parallelism = calculateOptimalParallelism();

    private TrelloForkJoinPool trelloForkJoinPool() {
        return new TrelloForkJoinPool(
                parallelism,
                pool -> {
                    final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                    worker.setName("trello-sync-" + worker.getPoolIndex());
                    worker.setContextClassLoader(Thread.currentThread().getContextClassLoader());
                    return worker;
                },
                null,
                true);
    }

    private int calculateOptimalParallelism() {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int calculated = Math.min(maxThreads, Math.max(minThreads, availableProcessors * parallelismFactor));
        if (calculated <= 0) {
            calculated = 10;
        }
        return calculated;
    }

}
