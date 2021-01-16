package io.vertx.spring;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.spring.annotation.SpringVerticle;

/**
 * @author zhongwenjian
 */
@SpringVerticle(instances = 4)
public class WebServerVerticle extends AbstractVerticle {
    @Override
    public void start(){
        Router router = Router.router(vertx);
        vertx.createHttpServer().requestHandler(router).listen(8888);
        router.get("/hello/:name").handler(context->{
            String name = context.request().getParam("name");
            context.response().end("hello "+name);
        });
    }
}
