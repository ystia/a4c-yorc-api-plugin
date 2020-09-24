package alien4cloud.yorc.api.plugin.context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.inject.Inject;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Plugin spring context configuration.
 */
@Configuration
@ComponentScan(basePackages = { "alien4cloud.yorc.api.plugin.context" })
@Slf4j
public class ContextConfiguration {

    @Bean
    Scheduler scheduler() {
        return Schedulers.from(executorService());
    }

    @Inject
    private OrchestratorConfiguration orchestratorConfiguration = null;

    /*
     * @return the executor service for our Runnable
     */
    @Bean
    ExecutorService executorService() {
        log.info("Executor will use {} threads.", orchestratorConfiguration.getExecutorThreadPoolSize());
        ExecutorService svc = Executors.newFixedThreadPool(orchestratorConfiguration.getExecutorThreadPoolSize(),
                taskThreadFactory());
        return svc;
    }

    @Bean
    ThreadFactory taskThreadFactory() {
        BasicThreadFactory.Builder builder = new BasicThreadFactory.Builder();
        return builder.namingPattern(contextName() + "-task-%d").build();
    }

    private String contextName() {
        return "YorcContext-" + orchestratorConfiguration.getOrchestratorName().replaceAll("\\W", "_");
    }

    /**
     * Thread factory for io threads
     *
     * @return
     */
    @Bean("http-thread-factory")
    ThreadFactory httpThreadFactory() {
        BasicThreadFactory.Builder builder = new BasicThreadFactory.Builder();
        return builder.namingPattern(contextName() + "-http-%d").build();
    }

    @Bean
    @SneakyThrows(IOReactorException.class)
    ConnectingIOReactor ioReactor() {

        IOReactorConfig config = IOReactorConfig.custom()
                .setConnectTimeout(orchestratorConfiguration.getConnectionTimeout() * 1000)
                .setSoTimeout(orchestratorConfiguration.getSocketTimeout() * 1000)
                .setIoThreadCount(orchestratorConfiguration.getIOThreadCount()).build();

        log.info("IOReactor will be configured using : " + config);

        return new DefaultConnectingIOReactor(config, httpThreadFactory());
    }
}