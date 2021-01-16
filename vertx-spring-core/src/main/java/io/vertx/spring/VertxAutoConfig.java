package io.vertx.spring;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.spi.VerticleFactory;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spring.annotation.SpringVerticle;
import io.vertx.spring.config.VertxConfig;
import io.vertx.spring.factory.SpringVerticleFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author zhongwenjian
 */
@Configuration
@EnableConfigurationProperties({VertxConfig.class})
public class VertxAutoConfig implements ApplicationContextAware {
    private ApplicationContext applicationContext;
    @Bean
    public VerticleFactory verticleFactory(){
        return new SpringVerticleFactory();
    }

    @Bean
    @ConditionalOnMissingBean({ClusterManager.class,Vertx.class})
    public Vertx initVertx(VertxConfig vertxConfig,VerticleFactory factory) {
        Vertx vertx = Vertx.vertx(vertxConfig);
        vertx.registerVerticleFactory(factory);
        deployVerticles(vertx);
        return vertx;
    }
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({ClusterManager.class})
    public Vertx initClusterVertx(VertxConfig vertxConfig,VerticleFactory factory,ClusterManager clusterManager) throws ExecutionException, InterruptedException {
        CompletableFuture<Vertx> vertxFuture = new CompletableFuture<>();
        vertxConfig.setClusterManager(clusterManager);
        Vertx.clusteredVertx(vertxConfig).onSuccess(vertx->{
            vertx.registerVerticleFactory(factory);
            deployVerticles(vertx);
            vertxFuture.complete(vertx);
        });
        return vertxFuture.get();
    }
    private void deployVerticles(Vertx vertx){
        Map<String,Verticle> verticleMap = applicationContext.getBeansOfType(Verticle.class);
        verticleMap.values().forEach(verticle->{
            SpringVerticle springVerticle =  verticle.getClass().getAnnotation(SpringVerticle.class);
            int count = springVerticle == null?1:springVerticle.instances();
            count = Math.max(count, 1);
            vertx.deployVerticle(SpringVerticleFactory.PREFIX+":"+verticle.getClass().getName(),new DeploymentOptions().setInstances(count));
        });
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
