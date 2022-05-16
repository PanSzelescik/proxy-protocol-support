package pl.panszelescik.proxy_protocol_support.shared.mixin;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import net.minecraft.server.network.ServerConnectionListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import pl.panszelescik.proxy_protocol_support.shared.ProxyProtocolChannelInitializer;

/**
 * Replaces anonymous ChannelInitializer with ProxyProtocolChannelInitializer
 *
 * @author PanSzelescik
 * @see pl.panszelescik.proxy_protocol_support.shared.ProxyProtocolChannelInitializer
 */
@Mixin(ServerConnectionListener.class)
public class ProxyProtocolImplementation {

    // TODO: Mixin into anonymous class?
    @Redirect(method = "startTcpServerListener", at = @At(value = "INVOKE", target = "Lio/netty/bootstrap/ServerBootstrap;childHandler(Lio/netty/channel/ChannelHandler;)Lio/netty/bootstrap/ServerBootstrap;", remap = false))
    private ServerBootstrap addProxyProtocolSupport(ServerBootstrap bootstrap, ChannelHandler childHandler) {
        return bootstrap.childHandler(new ProxyProtocolChannelInitializer(((ChannelInitializerInvoker) childHandler)));
    }
}
