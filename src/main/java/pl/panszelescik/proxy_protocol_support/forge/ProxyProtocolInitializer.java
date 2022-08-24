package pl.panszelescik.proxy_protocol_support.forge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import pl.panszelescik.proxy_protocol_support.shared.config.Config;
import pl.panszelescik.proxy_protocol_support.shared.config.Configuration;
import pl.panszelescik.proxy_protocol_support.shared.ProxyProtocolSupport;

import java.io.IOException;

/**
 * Forge's main file
 *
 * @author PanSzelescik
 */
@Mod(ProxyProtocolSupport.MODID)
public class ProxyProtocolInitializer {

    public ProxyProtocolInitializer() {
        DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> {
            try {
                Config config = Configuration.loadConfig(FMLPaths.CONFIGDIR.get().toFile());
                ProxyProtocolSupport.initialize(config);
            } catch (IOException e) {
                ProxyProtocolSupport.errorLogger.accept("Error loading config file:");
                throw new RuntimeException(e);
            }
        });
    }
}
