/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.settings.ListSetting;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import java.lang.reflect.Field;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;

public class FakelagModule
extends Module {
    private static final byte[] S_240BF9C5 = new byte[]{42, 35, 33, 42, 43, 47, 43, 29, 36, 35, 41, 39, 46, 35, 37};  // XOR0x42 "hachimi_fakelag"
    private static final byte[] S_228D2DDD = new byte[]{4, 35, 41, 39, 14, 35, 37};  // XOR0x42 "FakeLag"
    private static final byte[] S_3EDC963 = new byte[]{6, 39, 46, 35, 59};  // XOR0x42 "Delay"
    private static final byte[] S_2BCE4293 = new byte[]{50, 35, 33, 41, 39, 54, 29, 42, 35, 44, 38, 46, 39, 48};  // XOR0x42 "packet_handler"
    private static final byte[] S_2031851C = new byte[]{39, 44, 33, 45, 38, 39, 48};  // XOR0x42 "encoder"
    private static final byte[] S_2C0B7D03 = new byte[]{33, 42, 35, 44, 44, 39, 46};  // XOR0x42 "channel"
    private static final byte[] S_8CDAC1B = new byte[]{42, 35, 49, 42, 1, 45, 38, 39};  // XOR0x42 "hashCode"
    private static final byte[] S_5A113637 = new byte[]{42, 35, 33, 42, 43, 47, 43, 111, 36, 35, 41, 39, 46, 35, 37};  // XOR0x42 "hachimi-fakelag"
    private final ListSetting delay = new ListSetting("Delay", 250, 50, 2000);
    private final Queue<Delayed> queue = new ConcurrentLinkedQueue<Delayed>();
    private ChannelDuplexHandler handler;
    private Channel channel;
    private Thread worker;
    private volatile boolean running;

    private void drain() {
        Delayed d;
        long now = System.currentTimeMillis();
        while ((d = this.queue.peek()) != null && now >= d.at) {
            this.queue.poll();
            this.send(d);
        }
    }

    public FakelagModule() {
        super("FakeLag", Category.MISC);
        this.values.add(this.delay);
    }

    private void remove() {
        try {
            if (this.channel != null && this.channel.pipeline().get("hachimi_fakelag") != null) {
                this.channel.pipeline().remove("hachimi_fakelag");
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        this.handler = null;
        this.channel = null;
    }

    private void startWorker() {
        this.running = true;
        this.worker = new Thread(new Runnable(){

            @Override
            public void run() {
                while (FakelagModule.this.running) {
                    try {
                        FakelagModule.this.drain();
                        Thread.sleep(5L);
                    }
                    catch (InterruptedException e) {
                        break;
                    }
                    catch (Throwable throwable) {
                    }
                }
            }
        }, "hachimi-fakelag");
        this.worker.setDaemon(true);
        this.worker.start();
    }

    private void stopWorker() {
        this.running = false;
        if (this.worker != null) {
            this.worker.interrupt();
            try {
                this.worker.join(1000L);
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
            this.worker = null;
        }
    }

    private void flushAll() {
        Delayed d;
        while ((d = this.queue.poll()) != null) {
            this.send(d);
        }
    }

    private static Channel channelOf(Connection conn) {
        for (String name : new String[]{"channel", "hashCode"}) {
            try {
                Field f = Connection.class.getDeclaredField(name);
                f.setAccessible(true);
                Object v = f.get(conn);
                if (!(v instanceof Channel)) continue;
                return (Channel)v;
            }
            catch (Throwable f) {
                // empty catch block
            }
        }
        for (Field f : Connection.class.getDeclaredFields()) {
            try {
                f.setAccessible(true);
                Object v = f.get(conn);
                if (!(v instanceof Channel)) continue;
                return (Channel)v;
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return null;
    }

    private void inject() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.getConnection() == null) {
                return;
            }
            Connection conn = mc.getConnection().getConnection();
            if (conn == null) {
                return;
            }
            this.channel = FakelagModule.channelOf(conn);
            if (this.channel == null) {
                return;
            }
            if (this.channel.pipeline().get("hachimi_fakelag") != null) {
                return;
            }
            this.handler = new ChannelDuplexHandler(){

                public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                    if (!FakelagModule.this.enabled || !(msg instanceof Packet)) {
                        super.write(ctx, msg, promise);
                        return;
                    }
                    long at = System.currentTimeMillis() + (long)((Integer)FakelagModule.this.delay.get()).intValue();
                    FakelagModule.this.queue.offer(new Delayed((Packet)msg, at, ctx, promise));
                }
            };
            if (this.channel.pipeline().get("packet_handler") != null) {
                this.channel.pipeline().addBefore("packet_handler", "hachimi_fakelag", (ChannelHandler)this.handler);
            } else if (this.channel.pipeline().get("encoder") != null) {
                this.channel.pipeline().addBefore("encoder", "hachimi_fakelag", (ChannelHandler)this.handler);
            } else {
                this.channel.pipeline().addFirst("hachimi_fakelag", (ChannelHandler)this.handler);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private void send(Delayed d) {
        try {
            if (d.ctx != null && d.ctx.channel().isActive()) {
                d.ctx.write(d.packet, d.promise);
                d.ctx.flush();
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Override
    public void onDisable() {
        this.stopWorker();
        this.flushAll();
        this.remove();
        this.queue.clear();
    }

    @Override
    public void onEnable() {
        this.queue.clear();
        this.inject();
        this.startWorker();
    }

    @Override
    public void onWorldChange() {
        this.queue.clear();
        this.remove();
        if (this.enabled) {
            this.inject();
        }
    }

    private static final class Delayed {
        final Packet<?> packet;
        final long at;
        final ChannelHandlerContext ctx;
        final ChannelPromise promise;

        Delayed(Packet<?> packet, long at, ChannelHandlerContext ctx, ChannelPromise promise) {
            this.packet = packet;
            this.at = at;
            this.ctx = ctx;
            this.promise = promise;
        }
    }
}

