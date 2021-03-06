package github.tmx.rpc.core.spring.component;

import github.tmx.rpc.core.netty.client.NettyRpcClientProxy;
import github.tmx.rpc.core.spring.annotation.RpcReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * 为 Bean 中标记 @RpcReference 的引用生成代理对象
 * @author: TangMinXuan
 * @created: 2020/10/23 10:41
 */
@Component
public class RpcReferenceProcessor implements BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(RpcReferenceProcessor.class);

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        Field[] declaredFields = targetClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            RpcReference rpcReference = declaredField.getAnnotation(RpcReference.class);
            // 只有存在 @RpcReference 注解, 才去启动 NettyClient
            if (rpcReference != null) {
                NettyRpcClientProxy nettyRpcClientProxy = new NettyRpcClientProxy(rpcReference.group(), rpcReference.version());
                Object proxyObject = nettyRpcClientProxy.getProxyInstance(declaredField.getType());
                declaredField.setAccessible(true);
                try {
                    declaredField.set(bean, proxyObject);
                } catch (IllegalAccessException e) {
                    logger.error("为 RpcReference 生成代理对象时发生异常: ", e);
                }
            }

        }
        return bean;
    }
}
