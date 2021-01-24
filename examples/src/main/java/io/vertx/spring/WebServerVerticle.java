package io.vertx.spring;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.Response;
import io.vertx.spring.annotation.SpringVerticle;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 * @author zhongwenjian
 */
@SpringVerticle(instances = 4)
public class WebServerVerticle extends AbstractVerticle {

    @Resource
    private RedisAPI redisAPI;

    @Override
    public void start(){
        Router router = Router.router(vertx);
        vertx.createHttpServer().requestHandler(router).listen(8888);
        router.get("/hello/:name").handler(context->{
            String name = context.request().getParam("name");
            context.response().end("hello "+name);
        });
        redisAPI.set(Arrays.asList("testKey","testValue","EX","300"))
                .map(Response::toString)
                .onSuccess(System.out::println)
                .onFailure(Throwable::printStackTrace);
    }
}
