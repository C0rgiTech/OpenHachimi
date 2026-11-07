/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.world.entity.player.Player;

public class AntinickModule
extends Module {
    private static final byte[] S_EDBAF86 = new byte[]{42, 39, 59, 50, 43, 58, 39, 46, 120, 49, 112, 33, 39, 52, 39, 44, 54};  // XOR0x42 "heypixel:s2cevent"
    private static final byte[] S_66F8B205 = new byte[]{3, 44, 54, 43, 12, 43, 33, 41};  // XOR0x42 "AntiNick"
    private static final byte[] S_2DAD9907 = new byte[]{(byte)128, (byte)229, 38, 25, 12, 43, 33, 41, 31, 98, (byte)128, (byte)229, 36, 62, 98};
    private static final byte[] S_110ACD = new byte[]{98, (byte)128, (byte)229, 32, 106};
    private static final byte[] S_AE94 = new byte[]{107, (byte)128, (byte)229, 48};
    private static final byte[] S_6294C1B2 = new byte[]{50, 46, 35, 59, 39, 48, 49};  // XOR0x42 "players"
    private static final byte[] S_337A8B = new byte[]{44, 35, 47, 39};  // XOR0x42 "name"
    private static final byte[] S_523E442A = new byte[]{23, 44, 41, 44, 45, 53, 44};  // XOR0x42 "Unknown"
    private static final byte[] S_36F3BB = new byte[]{55, 55, 43, 38};  // XOR0x42 "uuid"
    private static final byte[] S_2EEFAA = new byte[]{38, 35, 54, 35};  // XOR0x42 "data"
    private static final byte[] S_2D = new byte[]{111};
    private static final byte[] S_7B = new byte[]{57};
    private static final byte[] S_5B = new byte[]{25};
    private static final Map<String, String> nickedPlayers = new ConcurrentHashMap<String, String>();
    private final List<PayloadDecoder> decoders = Arrays.asList(new PayloadDecoder(){

        @Override
        public String decode(byte[] data) {
            return AntinickModule.this.decodeLobby(data);
        }
    }, new PayloadDecoder(){

        @Override
        public String decode(byte[] data) {
            return AntinickModule.this.decodeGame(data);
        }
    }, new PayloadDecoder(){

        @Override
        public String decode(byte[] data) {
            return AntinickModule.this.decodeZlib(data);
        }
    }, new PayloadDecoder(){

        @Override
        public String decode(byte[] data) {
            return AntinickModule.this.decodeJsonStart(data);
        }
    }, new PayloadDecoder(){

        @Override
        public String decode(byte[] data) {
            return AntinickModule.this.decodeRaw(data);
        }
    });

    public AntinickModule() {
        super("AntiNick", Category.MISC);
    }

    public static String getRealName(Player player) {
        if (player == null) {
            return null;
        }
        return nickedPlayers.get(player.getGameProfile().getName());
    }

    public static String getRealName(String tabName) {
        return tabName == null ? null : nickedPlayers.get(tabName);
    }

    private boolean isZlibHeaderAt(byte[] data, int offset) {
        if (offset >= data.length - 1) {
            return false;
        }
        int b0 = data[offset] & 0xFF;
        int b1 = data[offset + 1] & 0xFF;
        return b0 == 120 && (b1 == 1 || b1 == 156 || b1 == 218);
    }

    private int findJsonStart(byte[] data) {
        for (int i = 0; i < data.length; ++i) {
            if (data[i] != 123 && data[i] != 91) continue;
            return i;
        }
        return -1;
    }

    private int findZlibHeader(byte[] data) {
        for (int i = 0; i < data.length - 1; ++i) {
            if (!this.isZlibHeaderAt(data, i)) continue;
            return i;
        }
        return -1;
    }

    public static boolean isNickedName(String tabName) {
        if (tabName == null) {
            return false;
        }
        AntinickModule detector = ModuleManager.getModule(AntinickModule.class);
        return detector != null && detector.enabled && nickedPlayers.containsKey(tabName);
    }

    public static boolean isNicked(Player player) {
        if (player == null) {
            return false;
        }
        AntinickModule detector = ModuleManager.getModule(AntinickModule.class);
        return detector != null && detector.enabled && nickedPlayers.containsKey(player.getGameProfile().getName());
    }

    private String tryDecodeUTF8(byte[] bytes, int offset) {
        if (offset >= bytes.length) {
            return null;
        }
        try {
            return new String(bytes, offset, bytes.length - offset, StandardCharsets.UTF_8);
        }
        catch (Exception e) {
            return null;
        }
    }

    private String decodeRaw(byte[] data) {
        return this.tryDecodeUTF8(data, 0);
    }

    private String decodeZlib(byte[] data) {
        int pos = this.findZlibHeader(data);
        if (pos >= 0) {
            return this.decompressZlib(data, pos);
        }
        return null;
    }

    private String decodeGame(byte[] data) {
        if (data.length < 2) {
            return null;
        }
        if ((data[0] & 0xFF) != 233) {
            return null;
        }
        return this.tryDecodeUTF8(data, 1);
    }

    private void outputPlayerInfo(String realName, String uuid) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) {
            return;
        }
        String tabName = null;
        boolean foundInTab = false;
        boolean nameMatches = false;
        for (PlayerInfo info : mc.getConnection().getOnlinePlayers()) {
            String infoUuid = info.getProfile().getId().toString();
            String emptyStr = new String();
            if (!infoUuid.equals(uuid) && !infoUuid.replace("-", emptyStr).equals(uuid.replace("-", emptyStr))) continue;
            tabName = info.getProfile().getName();
            foundInTab = true;
            nameMatches = tabName.equals(realName);
            break;
        }
        if (foundInTab && !nameMatches && tabName != null && !nickedPlayers.containsKey(tabName)) {
            nickedPlayers.put(tabName, realName);
        }
    }

    private String decodeLobby(byte[] data) {
        if (data.length < 6) {
            return null;
        }
        if ((data[0] & 0xFF) != 250) {
            return null;
        }
        if (!this.isZlibHeaderAt(data, 5)) {
            return null;
        }
        return this.decompressZlib(data, 5);
    }

    private boolean isProbablyJson(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return (str = str.trim()).startsWith("{") || str.startsWith("[");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String decompressZlib(byte[] data, int offset) {
        Inflater inflater = new Inflater();
        inflater.setInput(data, offset, data.length - offset);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        try {
            String result;
            int len22;
            while (!inflater.finished() && (len22 = inflater.inflate(buf)) != 0) {
                baos.write(buf, 0, len22);
            }
            String string = result = new String(baos.toByteArray(), StandardCharsets.UTF_8);
            return string;
        }
        catch (DataFormatException e) {
            String string;
            String string2 = string = null;
            return string2;
        }
        finally {
            inflater.end();
        }
    }

    private String decodeJsonStart(byte[] data) {
        int start = this.findJsonStart(data);
        if (start >= 0) {
            return this.tryDecodeUTF8(data, start);
        }
        return null;
    }

    @Override
    public void onEnable() {
        nickedPlayers.clear();
    }

    private void handleCustomPayload(ClientboundCustomPayloadPacket packet) {
        PayloadDecoder decoder;
        FriendlyByteBuf buf = packet.getData();
        if (buf == null) {
            return;
        }
        int readable = buf.readableBytes();
        if (readable == 0) {
            return;
        }
        byte[] rawData = new byte[readable];
        buf.getBytes(buf.readerIndex(), rawData);
        String jsonStr = null;
        Iterator<PayloadDecoder> iterator = this.decoders.iterator();
        while (iterator.hasNext() && !this.isProbablyJson(jsonStr = (decoder = iterator.next()).decode(rawData))) {
        }
        if (!this.isProbablyJson(jsonStr)) {
            return;
        }
        try {
            String uuid;
            String name;
            JsonObject playerObj;
            JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();
            if (json.has("players") && json.get("players").isJsonObject()) {
                JsonObject players = json.getAsJsonObject("players");
                for (Map.Entry entry : players.entrySet()) {
                    playerObj = ((JsonElement)entry.getValue()).getAsJsonObject();
                    name = playerObj.has("name") ? playerObj.get("name").getAsString() : "Unknown";
                    uuid = playerObj.has("uuid") ? playerObj.get("uuid").getAsString() : (String)entry.getKey();
                    this.outputPlayerInfo(name, uuid);
                }
            }
            if (json.has("data") && json.get("data").isJsonObject()) {
                JsonObject data = json.getAsJsonObject("data");
                for (Map.Entry entry : data.entrySet()) {
                    if (!((JsonElement)entry.getValue()).isJsonObject() || !(playerObj = ((JsonElement)entry.getValue()).getAsJsonObject()).has("name") && !playerObj.has("uuid")) continue;
                    name = playerObj.has("name") ? playerObj.get("name").getAsString() : "Unknown";
                    uuid = playerObj.has("uuid") ? playerObj.get("uuid").getAsString() : (String)entry.getKey();
                    this.outputPlayerInfo(name, uuid);
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) {
            return;
        }
        for (PlayerInfo info : mc.getConnection().getOnlinePlayers()) {
            String tabName = info.getProfile().getName();
            if (!nickedPlayers.containsKey(tabName)) continue;
            String realName = nickedPlayers.get(tabName);
            String formatted = "§d[Nick] §f| " + tabName + " §b(" + realName + ")§r";
            info.setTabListDisplayName((Component)Component.literal((String)formatted));
        }
    }

    @Override
    public boolean onIncomingPacket(Packet<?> packet) {
        ClientboundCustomPayloadPacket customPacket;
        if (packet instanceof ClientboundCustomPayloadPacket && (customPacket = (ClientboundCustomPayloadPacket)packet).getIdentifier().toString().equals("heypixel:s2cevent")) {
            this.handleCustomPayload(customPacket);
        }
        return false;
    }

    @FunctionalInterface
    private static interface PayloadDecoder {
        public String decode(byte[] var1);
    }
}

