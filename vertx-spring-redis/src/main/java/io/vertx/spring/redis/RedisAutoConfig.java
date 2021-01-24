package io.vertx.spring.redis;

import io.vertx.core.Vertx;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisOptions;
import io.vertx.redis.client.impl.RedisClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisAutoConfig {
    @Bean
    @ConditionalOnMissingBean
    public RedisAPI redisAPI(RedisClient redisClient){
        return RedisAPI.api(redisClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisClient redisClient(Vertx vertx,RedisOptions options){
        return new RedisClient(vertx,options);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = "vertx.redis")
    public RedisOptions redisOptions(){
        return new RedisOptions();
    }
}
