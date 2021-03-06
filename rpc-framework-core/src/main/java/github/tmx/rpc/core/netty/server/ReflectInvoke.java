package github.tmx.rpc.core.netty.server;

import github.tmx.rpc.core.common.DTO.RpcRequest;
import github.tmx.rpc.core.common.DTO.RpcResponse;
import github.tmx.rpc.core.common.enumeration.RpcResponseEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author: TangMinXuan
 */
public class ReflectInvoke {

    private static final Logger logger = LoggerFactory.getLogger(ReflectInvoke.class);

    public Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            if (null == method) {
                return RpcResponse.fail(rpcRequest.getRequestId(), RpcResponseEnum.NOT_FOUND_METHOD);
            }
            return method.invoke(service, rpcRequest.getParameters());
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            logger.error("反射调用时发生错误: {}", e);
        }
        return null;
    }
}
