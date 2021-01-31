package io.vertx.spring.zookeeper;

import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager;
import io.vertx.spring.zookeeper.config.CuratorProperties;
import io.vertx.spring.VertxAutoConfigure;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;


/**
 * @author zhongwenjian
 */
@Configuration
@AutoConfigureBefore({VertxAutoConfigure.class})
@ConditionalOnProperty(value = "vertx.clustered",havingValue = "true",matchIfMissing = true)
@EnableConfigurationProperties({CuratorProperties.class})
public class ZookeeperClusterManagerAutoConfig {
    @Bean
    @ConditionalOnMissingBean
    public ClusterManager clusterManager(CuratorFramework curatorFramework){
        return new ZookeeperClusterManager(curatorFramework);
    }

    @Bean
    @ConditionalOnMissingBean
    public CuratorFramework curator(CuratorProperties properties) throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(
                properties.getBaseSleepTimeMs(),
                properties.getMaxRetries(),
                properties.getMaxSleepTimeMs());
        CuratorFramework curator = CuratorFrameworkFactory.builder().connectString(String.join(",", properties.getHosts()))
                .namespace(properties.getRootPath())
                .sessionTimeoutMs(properties.getSessionTimeout())
                .connectionTimeoutMs(properties.getConnectTimeout()).retryPolicy(retryPolicy).build();
        curator.start();
        if(StringUtils.hasText(properties.getAuth())){
            curator.getZookeeperClient().getZooKeeper().addAuthInfo("digest", properties.getAuth().getBytes());
        }
        return curator;
    }
}
