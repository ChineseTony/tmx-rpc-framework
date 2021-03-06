package github.tmx.rpc.core.netty.server;

import github.tmx.rpc.core.common.DTO.RpcRequest;
import github.tmx.rpc.core.common.DTO.RpcResponse;
import github.tmx.rpc.core.container.ServiceContainer;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: TangMinXuan
 * @created: 2020/10/02 21:13
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    private static ServiceContainer serviceContainer = NettyServer.getInstance().getServiceContainer();

    private ReflectInvoke reflectInvoke = new ReflectInvoke();

    /**
     * ChannelHandlerContext 是当前 ChannelHandler 与其他 ChannelHandler 和 pipeline 交流的 “信使”
     * @param ctx
     * @param msg
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            // 这里的 msg 已经被前面的 kryoSerializer 反序列化过了，可以直接转为 RpcRequest
            RpcRequest rpcRequest = (RpcRequest) msg;
            logger.debug("服务器收到请求: {}", rpcRequest);

            // 执行具体的接口逻辑
            String interfaceName = rpcRequest.getInterfaceName();
            String group = rpcRequest.getGroup();
            String version = rpcRequest.getVersion();
            Object serviceImpl = serviceContainer.getProvider(interfaceName, group, version);
            Object result = reflectInvoke.invokeTargetMethod(rpcRequest, serviceImpl);
            logger.debug("服务器执行结果: {}", result.toString());

            ChannelFuture channelFuture = ctx.writeAndFlush(RpcResponse.success(result, rpcRequest.getRequestId(), false));
            channelFuture.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        } finally {
            // 避免内存泄漏
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("服务器捕获到异常");
        cause.printStackTrace();
        ctx.close();
    }
}
