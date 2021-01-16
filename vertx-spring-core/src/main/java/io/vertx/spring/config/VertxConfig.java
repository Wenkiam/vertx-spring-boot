package io.vertx.spring.config;

import io.vertx.core.VertxOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author zhongwenjian
 */
@ConfigurationProperties("vertx")
public class VertxConfig extends VertxOptions {

    private boolean clustered;

    public boolean isClustered(){
        return clustered;
    }

    public VertxConfig setClustered(boolean clustered){
        this.clustered = clustered;
        return this;
    }
}
