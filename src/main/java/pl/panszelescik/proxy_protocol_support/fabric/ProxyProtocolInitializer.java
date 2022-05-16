package pl.panszelescik.proxy_protocol_support.fabric;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import pl.panszelescik.proxy_protocol_support.shared.config.Config;
import pl.panszelescik.proxy_protocol_support.shared.config.Configuration;
import pl.panszelescik.proxy_protocol_support.shared.ProxyProtocolSupport;

import java.io.IOException;

/**
 * Fabric's main file
 *
 * @author PanSzelescik
 */
@Environment(EnvType.SERVER)
public class ProxyProtocolInitializer implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        try {
            Config config = Configuration.loadConfig(FabricLoader.getInstance().getConfigDir().toFile());
            ProxyProtocolSupport.initialize(config);
        } catch (IOException e) {
            ProxyProtocolSupport.errorLogger.accept("Error loading config file:");
            throw new RuntimeException(e);
        }
    }
}
