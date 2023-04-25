package pl.panszelescik.proxy_protocol_support.shared.mixin;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import pl.panszelescik.proxy_protocol_support.shared.impl.IChannelInitializer;

/**
 * Adds Invoker for initChannel
 *
 * @author PanSzelescik
 * @see io.netty.channel.ChannelInitializer#initChannel(Channel)
 */
@Mixin(ChannelInitializer.class)
public interface ChannelInitializerInvoker extends IChannelInitializer {

    @Invoker(value = "initChannel", remap = false)
    void invokeInitChannel(Channel ch) throws Exception;
}
