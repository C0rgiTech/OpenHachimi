/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.world.entity.LivingEntity;

public class HealthbypassModule
extends Module {
    private static final byte[] S_BF17A84 = new byte[]{10, 39, 35, 46, 54, 42, 0, 59, 50, 35, 49, 49};  // XOR0x42 "HealthBypass"
    private static final byte[] S_FA6D1ED = new byte[]{32, 39, 46, 45, 53, 10, 39, 35, 46, 54, 42};  // XOR0x42 "belowHealth"
    private static final byte[] S_3735025C = new byte[]{42, 39, 35, 46, 54, 42};  // XOR0x42 "health"
    private static final byte[] S_D08 = new byte[]{42, 50};  // XOR0x42 "hp"
    private static final byte[] S_4641E5CD = new byte[]{32, 39, 46, 45, 53, 42, 39, 35, 46, 54, 42};  // XOR0x42 "belowhealth"
    private static final byte[] S_27B1AC0B = new byte[]{47, 29, 115, 113, 113, 112, 122, 123, 29};  // XOR0x42 "m_133289_"
    private static final byte[] S_7B855274 = new byte[]{37, 39, 54, 17, 46, 45, 54};  // XOR0x42 "getSlot"
    private static final byte[] S_40048CAA = new byte[]{37, 39, 54, 6, 43, 49, 50, 46, 35, 59, 17, 46, 45, 54};  // XOR0x42 "getDisplaySlot"
    private static final byte[] S_35E9FE = new byte[]{49, 46, 45, 54};  // XOR0x42 "slot"
    private static final byte[] S_27B1AEB5 = new byte[]{47, 29, 115, 113, 113, 112, 123, 114, 29};  // XOR0x42 "m_133290_"
    private static final byte[] S_BDA1FAE = new byte[]{37, 39, 54, 13, 32, 40, 39, 33, 54, 43, 52, 39, 12, 35, 47, 39};  // XOR0x42 "getObjectiveName"
    private static final byte[] S_4E7A6364 = new byte[]{45, 32, 40, 39, 33, 54, 43, 52, 39, 12, 35, 47, 39};  // XOR0x42 "objectiveName"
    private static final byte[] S_7B82E301 = new byte[]{37, 39, 54, 12, 35, 47, 39};  // XOR0x42 "getName"
    private static final byte[] S_337A8B = new byte[]{44, 35, 47, 39};  // XOR0x42 "name"
    private static final Map<String, Integer> HEALTHS = new ConcurrentHashMap<String, Integer>();
    private static final Set<String> HEALTH_OBJECTIVES = ConcurrentHashMap.newKeySet();
    private Object lastLevelRef;

    public HealthbypassModule() {
        super("HealthBypass", Category.MISC);
    }

    private static Integer readInt(Object obj, String ... methodNames) {
        Object v = HealthbypassModule.readAny(obj, methodNames);
        if (v instanceof Integer) {
            Integer i = (Integer)v;
            return i;
        }
        return null;
    }

    private static String readString(Object obj, String ... methodNames) {
        Object v = HealthbypassModule.readAny(obj, methodNames);
        if (v instanceof String) {
            String s = (String)v;
            return s;
        }
        return null;
    }

    private static boolean isHealthObjective(String objectiveName) {
        if (objectiveName == null || objectiveName.isEmpty()) {
            return false;
        }
        if (HEALTH_OBJECTIVES.contains(objectiveName)) {
            return true;
        }
        if ("belowHealth".equals(objectiveName) || "health".equals(objectiveName)) {
            return true;
        }
        String n = objectiveName.toLowerCase(Locale.ROOT);
        return n.contains("health") || n.equals("hp") || n.contains("belowhealth");
    }

    private static Object readAny(Object obj, String ... methodNames) {
        if (obj == null || methodNames == null) {
            return null;
        }
        for (String name : methodNames) {
            if (name == null || name.isEmpty()) continue;
            try {
                Method m = obj.getClass().getMethod(name, new Class[0]);
                m.setAccessible(true);
                return m.invoke(obj, new Object[0]);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return null;
    }

    public static float getTrackedHealth(LivingEntity entity) {
        if (!(entity instanceof AbstractClientPlayer)) {
            return entity.getHealth();
        }
        AbstractClientPlayer player = (AbstractClientPlayer)entity;
        if (player.getGameProfile() == null) {
            return entity.getHealth();
        }
        String name = player.getGameProfile().getName();
        if (name == null || name.isEmpty()) {
            return entity.getHealth();
        }
        Integer score = HEALTHS.get(name);
        if (score == null) {
            return entity.getHealth();
        }
        float max = Math.max(1.0f, entity.getMaxHealth());
        float v = score.floatValue();
        if (v > max + 0.5f && v <= max * 2.0f + 0.5f) {
            v *= 0.5f;
        }
        if (v < 0.0f) {
            v = 0.0f;
        }
        return Math.min(v, max);
    }

    @Override
    public void onDisable() {
        HEALTHS.clear();
        HEALTH_OBJECTIVES.clear();
        this.lastLevelRef = null;
    }

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null || mc.player == null) {
            return;
        }
        if (this.lastLevelRef != mc.level) {
            HEALTHS.clear();
            HEALTH_OBJECTIVES.clear();
            this.lastLevelRef = mc.level;
        }
        for (AbstractClientPlayer player : mc.level.players()) {
            Integer score;
            String name;
            if (player == null || player == mc.player || player.getGameProfile() == null || (name = player.getGameProfile().getName()) == null || name.isEmpty() || (score = HEALTHS.get(name)) == null) continue;
            float tracked = HealthbypassModule.getTrackedHealth((LivingEntity)player);
            player.setHealth(Math.max(1.0f, tracked));
        }
    }

    @Override
    public boolean onIncomingPacket(Packet<?> packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null || mc.player == null || mc.player.getGameProfile() == null) {
            return false;
        }
        if (packet instanceof ClientboundSetDisplayObjectivePacket) {
            ClientboundSetDisplayObjectivePacket objPacket = (ClientboundSetDisplayObjectivePacket)packet;
            Integer slot = HealthbypassModule.readInt(objPacket, "m_133289_", "getSlot", "getDisplaySlot", "slot");
            String obj = HealthbypassModule.readString(objPacket, "m_133290_", "getObjectiveName", "objectiveName", "getName", "name");
            if (slot != null && slot == 2 && obj != null && !obj.isEmpty()) {
                HEALTH_OBJECTIVES.add(obj);
            }
            return false;
        }
        if (packet instanceof ClientboundSetScorePacket) {
            ClientboundSetScorePacket scorePacket = (ClientboundSetScorePacket)packet;
            String objective = scorePacket.getObjectiveName();
            if (!HealthbypassModule.isHealthObjective(objective)) {
                return false;
            }
            String owner = scorePacket.getOwner();
            if (owner == null || owner.isEmpty()) {
                return false;
            }
            if (owner.equals(mc.player.getGameProfile().getName())) {
                return false;
            }
            HEALTHS.put(owner, scorePacket.getScore());
        }
        return false;
    }
}

