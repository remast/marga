package remast.marga.spring;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
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
import remast.marga.Request;
import remast.marga.RequestHandler;
import remast.marga.Response;

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
        return () -> {
            var requestHandlers = applicationContext.getBeansOfType(RequestHandler.class);
            var inspectedBeanNames = new HashSet<>(requestHandlers.keySet());
            requestHandlers.forEach((beanName, handler) -> {
                var beanType = AopUtils.getTargetClass(handler);
                registerTypeRoutes(beanName, beanType, handler, router);
                registerMethodRoutes(beanName, beanType, handler, router);
            });

            for (var beanName : applicationContext.getBeanDefinitionNames()) {
                if (inspectedBeanNames.contains(beanName)) {
                    continue;
                }
                var beanType = applicationContext.getType(beanName);
                if (beanType == null || !hasMargaRouteMethods(beanType)) {
                    continue;
                }
                var bean = applicationContext.getBean(beanName);
                registerMethodRoutes(beanName, AopUtils.getTargetClass(bean), bean, router);
            }
        };
    }

    private void registerTypeRoutes(String beanName, Class<?> beanType, RequestHandler handler, HttpRouter router) {
        for (var route : beanType.getAnnotationsByType(MargaRoute.class)) {
            validateRoutePath(beanName, route.path());
            var description = route.description().isBlank() ? null : route.description();
            router.addRoute(route.method(), route.path(), handler, description);
            logRoute(beanName, route.method(), route.path());
        }
    }

    private void registerMethodRoutes(String beanName, Class<?> beanType, Object bean, HttpRouter router) {
        for (var method : beanType.getMethods()) {
            for (var route : method.getAnnotationsByType(MargaRoute.class)) {
                validateRoutePath(beanName, route.path());
                validateRouteMethod(beanName, method);
                var description = route.description().isBlank() ? null : route.description();
                router.addRoute(route.method(), route.path(), request -> invokeRouteMethod(beanName, bean, method, request), description);
                logRoute(beanName, route.method(), route.path());
            }
        }
    }

    private boolean hasMargaRouteMethods(Class<?> beanType) {
        return Arrays.stream(beanType.getMethods()).anyMatch(method -> method.isAnnotationPresent(MargaRoute.class)
                || method.isAnnotationPresent(MargaRoutes.class));
    }

    private void validateRoutePath(String beanName, String path) {
        if (path.isBlank()) {
            throw new IllegalStateException("Marga route path cannot be blank on bean '" + beanName + "'");
        }
    }

    private void validateRouteMethod(String beanName, Method method) {
        if (method.getParameterCount() != 1 || method.getParameterTypes()[0] != Request.class
                || !Response.class.isAssignableFrom(method.getReturnType())) {
            throw new IllegalStateException("Marga route method '" + method.getName() + "' on bean '" + beanName
                    + "' must accept one Request and return a Response");
        }
    }

    private Response invokeRouteMethod(String beanName, Object bean, Method method, Request request) {
        try {
            return (Response) method.invoke(bean, request);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot invoke Marga route method '" + method.getName()
                    + "' on bean '" + beanName + "'", e);
        } catch (InvocationTargetException e) {
            var cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException("Marga route method '" + method.getName() + "' on bean '"
                    + beanName + "' failed", cause);
        }
    }

    private void logRoute(String beanName, String method, String path) {
        logger.info(() -> "Registered Marga route " + method + " " + path + " on bean '" + beanName + "'");
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
