package pl.panszelescik.proxy_protocol_support.shared;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProxyProtocolDetectorTest {

    // Minecraft handshake-like bytes, long enough for HAProxyMessageDecoder.detectProtocol (needs 12 bytes)
    private static final byte[] DIRECT_PAYLOAD = {0x10, 0x00, (byte) 0xFB, 0x05, 0x09, 'l', 'o', 'c', 'a', 'l', 'h', 'o', 's', 't', 0x63, (byte) 0xDD, 0x02};

    // PROXY protocol v2 signature, version 2 + LOCAL command, UNSPEC family, zero-length address block
    private static final byte[] PROXY_V2_LOCAL_HEADER = {
            0x0D, 0x0A, 0x0D, 0x0A, 0x00, 0x0D, 0x0A, 0x51, 0x55, 0x49, 0x54, 0x0A,
            0x20, 0x00, 0x00, 0x00
    };

    @BeforeAll
    static void setUpLoggers() {
        ProxyProtocolSupport.debugLogger = msg -> {};
        ProxyProtocolSupport.warnLogger = msg -> {};
    }

    private static ByteBuf buf(byte[]... parts) {
        ByteBuf buf = Unpooled.buffer();
        for (byte[] part : parts) {
            buf.writeBytes(part);
        }
        return buf;
    }

    private static byte[] readAllInbound(EmbeddedChannel channel) {
        ByteBuf all = Unpooled.buffer();
        ByteBuf msg;
        while ((msg = channel.readInbound()) != null) {
            all.writeBytes(msg);
            msg.release();
        }
        byte[] bytes = new byte[all.readableBytes()];
        all.readBytes(bytes);
        all.release();
        return bytes;
    }

    // --- Direct connection ---

    @Test
    void directConnectionIsForwardedAndDetectorRemoved() {
        EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast("detector", new ProxyProtocolDetector());

        channel.writeInbound(buf(DIRECT_PAYLOAD));

        assertNull(channel.pipeline().get("detector"));
        assertArrayEquals(DIRECT_PAYLOAD, readAllInbound(channel));
        assertFalse(channel.finish());
    }

    @Test
    void directConnectionSplitAcrossReadsIsBuffered() {
        EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast("detector", new ProxyProtocolDetector());

        byte[] first = new byte[5];
        byte[] rest = new byte[DIRECT_PAYLOAD.length - first.length];
        System.arraycopy(DIRECT_PAYLOAD, 0, first, 0, first.length);
        System.arraycopy(DIRECT_PAYLOAD, first.length, rest, 0, rest.length);

        channel.writeInbound(buf(first));
        assertNotNull(channel.pipeline().get("detector"));
        assertNull(channel.readInbound());

        channel.writeInbound(buf(rest));
        assertNull(channel.pipeline().get("detector"));
        assertArrayEquals(DIRECT_PAYLOAD, readAllInbound(channel));
        assertFalse(channel.finish());
    }

    // --- Later handler rebuilds the pipeline during channelRead (issue #55, AutoModpack) ---

    @Test
    void laterHandlerRemovingAllHandlersDuringReadDoesNotThrow() {
        List<Object> received = new ArrayList<>();
        ChannelHandler pipelineRebuilder = new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                ChannelPipeline pipeline = ctx.pipeline();
                pipeline.toMap().forEach((name, handler) -> pipeline.remove(handler));
                received.add(msg);
            }
        };

        EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast("detector", new ProxyProtocolDetector());
        channel.pipeline().addLast("rebuilder", pipelineRebuilder);

        assertDoesNotThrow(() -> channel.writeInbound(buf(DIRECT_PAYLOAD)));
        assertDoesNotThrow(channel::checkException);

        assertEquals(1, received.size());
        ByteBuf data = (ByteBuf) received.getFirst();
        byte[] bytes = new byte[data.readableBytes()];
        data.readBytes(bytes);
        data.release();
        assertArrayEquals(DIRECT_PAYLOAD, bytes);
        assertTrue(channel.pipeline().toMap().isEmpty());
        channel.finishAndReleaseAll();
    }

    // --- PROXY protocol connection ---

    @Test
    void proxyHeaderSwitchesToProxyModeAndForwardsPayload() {
        EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast("detector", new ProxyProtocolDetector());

        channel.writeInbound(buf(PROXY_V2_LOCAL_HEADER, DIRECT_PAYLOAD));

        assertNull(channel.pipeline().get("detector"));
        assertNotNull(channel.pipeline().get("haproxy-handler"));
        assertArrayEquals(DIRECT_PAYLOAD, readAllInbound(channel));
        assertFalse(channel.finish());
    }
}
