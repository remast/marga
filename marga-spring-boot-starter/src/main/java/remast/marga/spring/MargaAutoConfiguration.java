package remast.marga.spring;

import java.io.IOException;
import java.util.logging.Logger;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;

import remast.marga.Config;
import remast.marga.HttpRouter;
import remast.marga.RequestHandler;

@AutoConfiguration
@EnableConfigurationProperties(MargaProperties.class)
public class MargaAutoConfiguration {
    private static final Logger logger = Logger.getLogger(MargaAutoConfiguration.class.getName());

    @Bean
    @ConditionalOnMissingBean
    HttpRouter margaHttpRouter(MargaProperties properties) {
        var config = Config.builder()
                .host(properties.getHost())
                .port(properties.getPort())
                .readTimeoutMs(properties.getReadTimeoutMs())
                .acceptBacklog(properties.getAcceptBacklog())
                .maxRequestBodyBytes(properties.getMaxRequestBodyBytes())
                .build();
        return new HttpRouter(config);
    }

    @Bean
    SmartInitializingSingleton margaRouteRegistrar(
            ApplicationContext applicationContext,
            HttpRouter router) {
        return () -> applicationContext.getBeansOfType(RequestHandler.class).forEach((beanName, handler) -> {
            var beanType = AopUtils.getTargetClass(handler);
            var routeAnnotations = beanType.getAnnotationsByType(MargaRoute.class);
            for (var route : routeAnnotations) {
                if (route.path().isBlank()) {
                    throw new IllegalStateException("Marga route path cannot be blank on bean '" + beanName + "'");
                }
                var description = route.description().isBlank() ? null : route.description();
                router.addRoute(route.method(), route.path(), handler, description);
                logger.info(() -> "Registered Marga route " + route.method() + " " + route.path()
                        + " on bean '" + beanName + "'");
            }
        });
    }

    @Bean
    @DependsOn("margaRouteRegistrar")
    SmartLifecycle margaServerLifecycle(HttpRouter router) {
        return new SmartLifecycle() {
            private volatile boolean running;
            private volatile Thread serverThread;

            @Override
            public synchronized void start() {
                if (running) {
                    return;
                }
                var thread = Thread.ofPlatform().name("marga-http-server").unstarted(() -> {
                    try {
                        router.run();
                    } catch (IOException e) {
                        if (running) {
                            logger.severe("Marga server stopped unexpectedly: " + e.getMessage());
                        }
                    } finally {
                        running = false;
                    }
                });
                serverThread = thread;
                running = true;
                thread.start();
            }

            @Override
            public synchronized void stop() {
                running = false;
                router.shutdown();
                if (serverThread != null) {
                    try {
                        serverThread.join(5_000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    serverThread = null;
                }
            }

            @Override
            public void stop(Runnable callback) {
                stop();
                callback.run();
            }

            @Override
            public boolean isRunning() {
                return running;
            }

            @Override
            public boolean isAutoStartup() {
                return true;
            }

            @Override
            public int getPhase() {
                return Integer.MAX_VALUE;
            }
        };
    }
}
