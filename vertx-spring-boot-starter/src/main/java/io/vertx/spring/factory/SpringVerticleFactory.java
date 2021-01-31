package io.vertx.spring.factory;

import io.vertx.core.Promise;
import io.vertx.core.Verticle;
import io.vertx.core.spi.VerticleFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.Callable;

/**
 * @author zhongwenjian
 */
public class SpringVerticleFactory implements VerticleFactory{
    public static final String PREFIX = "spring";
    @Autowired
    private ApplicationContext context;
    @Override
    public String prefix() {
        return PREFIX;
    }

    @Override
    public void createVerticle(String verticleName, ClassLoader classLoader, Promise<Callable<Verticle>> promise) {
        String clazz = VerticleFactory.removePrefix(verticleName);
        promise.complete(() -> (Verticle) context.getBean(Class.forName(clazz)));
    }

}
