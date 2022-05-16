package pl.panszelescik.proxy_protocol_support.shared.mixin;

import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.net.SocketAddress;

/**
 * Adds Accessor for address
 *
 * @author PanSzelescik
 * @see net.minecraft.network.Connection#address
 */
@Mixin(Connection.class)
public interface ProxyProtocolAddressSetter {

    @Accessor
    void setAddress(SocketAddress address);
}
