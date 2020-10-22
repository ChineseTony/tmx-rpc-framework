package github.tmx.netty.server.provider;

import github.tmx.netty.server.NettyServer;
import github.tmx.registry.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultServiceProviderImpl implements ServiceProvider {

    private static final Logger logger = LoggerFactory.getLogger(DefaultServiceProviderImpl.class);

    // key-value: interfaceName-interfaceImplObject
    private static final Map<String, Object> serviceMap = new HashMap<>();

    private static InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 9999);

    /**
     * 注册服务
     * 为何使用 synchronized ？
     * 考虑 2 个 provider 同时进入这个方法，且都是注册对同一个接口的实现
     *
     * @param service
     */
    @Override
    public synchronized void addProvider(Object service) {
        Class[] interfaces = service.getClass().getInterfaces();
        if (interfaces.length == 0) {
            logger.error("服务没有实现任何接口");
        }
        for (Class i : interfaces) {
            if (serviceMap.containsKey(i.getCanonicalName())) {
                logger.info("接口: {} 已经注册过了,不允许重复注册", i.getCanonicalName());
                continue;
            }
            serviceMap.put(i.getCanonicalName(), service);
            logger.info("成功添加接口: {}", i.getCanonicalName());
        }
    }

    @Override
    public Object getProvider(String serviceName) {
        Object service = serviceMap.get(serviceName);
        if (null == service) {
            logger.error("找不到服务: {}", serviceName);
        }
        return service;
    }

    @Override
    public List<String> getAllService() {
        List<String> keysList = new ArrayList<>();
        for (String interfaceName : serviceMap.keySet()) {
            keysList.add(interfaceName);
        }
        return keysList;
    }

    @Override
    public void publishService(Object service) {
        addProvider(service);
        ServiceRegistry serviceRegistry = NettyServer.getServiceRegistry();
        Class<?> interfaceClass = service.getClass().getInterfaces()[0];
        serviceRegistry.registerService(interfaceClass.getCanonicalName(), inetSocketAddress);
    }
}
