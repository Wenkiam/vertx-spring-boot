package io.vertx.spring;

import io.vertx.core.*;
import io.vertx.core.spi.VerticleFactory;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spring.annotation.SpringVerticle;
import io.vertx.spring.factory.SpringVerticleFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author zhongwenjian
 */
@Configuration
public class VertxAutoConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(VertxAutoConfig.class);

    @Bean
    public VerticleFactory verticleFactory() {
        return new SpringVerticleFactory();
    }

    @Bean
    @ConditionalOnMissingBean({ClusterManager.class, Vertx.class})
    public Vertx initVertx(VertxOptions options, VerticleFactory factory) {
        Vertx vertx = Vertx.vertx(options);
        vertx.registerVerticleFactory(factory);
        return vertx;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({ClusterManager.class})
    public Vertx initClusterVertx(VertxOptions options, VerticleFactory factory, ClusterManager clusterManager) throws ExecutionException, InterruptedException {
        CompletableFuture<Vertx> vertxFuture = new CompletableFuture<>();
        options.setClusterManager(clusterManager);
        Vertx.clusteredVertx(options).onSuccess(vertx -> {
            vertx.registerVerticleFactory(factory);
            vertxFuture.complete(vertx);
        });
        return vertxFuture.get();
    }

    @EventListener
    public void deployVerticles(ApplicationReadyEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        Vertx vertx = applicationContext.getBean(Vertx.class);
        Map<String, Verticle> verticleMap = applicationContext.getBeansOfType(Verticle.class);
        verticleMap.values().forEach(verticle -> {
            SpringVerticle springVerticle = verticle.getClass().getAnnotation(SpringVerticle.class);
            int count = springVerticle == null ? 1 : springVerticle.instances();
            count = Math.max(count, 1);
            vertx.deployVerticle(SpringVerticleFactory.PREFIX + ":" + verticle.getClass().getName(),
                    new DeploymentOptions().setInstances(count))
                    .onSuccess(deploymentId -> {
                        LOGGER.info("deploy {} success,deployment id:{}", verticle.getClass().getName(), deploymentId);
                    });
        });
    }

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = "vertx")
    public VertxOptions vertxOptions() {
        return new VertxOptions();
    }

}
