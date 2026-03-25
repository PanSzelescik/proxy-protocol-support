package pl.panszelescik.proxy_protocol_support.forge;

import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import pl.panszelescik.proxy_protocol_support.shared.ProxyProtocolSupport;
import pl.panszelescik.proxy_protocol_support.shared.config.Config;
import pl.panszelescik.proxy_protocol_support.shared.config.Configuration;

import java.io.IOException;

@Mod(ProxyProtocolSupport.MODID)
public class ProxyProtocolForge {

    public ProxyProtocolForge() {
        ServerAboutToStartEvent.BUS.addListener(this::onServerStart);
    }

    private void onServerStart(ServerAboutToStartEvent event) {
        try {
            final Config config = Configuration.loadConfig(FMLPaths.CONFIGDIR.get().toFile());
            ProxyProtocolSupport.initialize(config);
        } catch (IOException e) {
            ProxyProtocolSupport.errorLogger.accept("Error loading config file:");
            throw new RuntimeException(e);
        }
    }
}
