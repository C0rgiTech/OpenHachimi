/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.settings.BooleanSetting;
import dev.openhachimi.settings.ModeSetting;
import dev.openhachimi.settings.NumberSetting;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraftforge.client.event.ComputeFovModifierEvent;

public class NofovModule
extends Module {
    private static final byte[] S_47EB32C = new byte[]{12, 45, 4, 45, 52};  // XOR0x42 "NoFov"
    private static final byte[] S_24ADC3 = new byte[]{15, 45, 38, 39};  // XOR0x42 "Mode"
    private static final byte[] S_1401EE3A = new byte[]{17, 50, 48, 43, 44, 54};  // XOR0x42 "Sprint"
    private static final byte[] S_2905E9 = new byte[]{21, 35, 46, 41};  // XOR0x42 "Walk"
    private static final byte[] S_78FB7791 = new byte[]{1, 55, 49, 54, 45, 47};  // XOR0x42 "Custom"
    private static final byte[] S_4605B9B = new byte[]{14, 43, 47, 43, 54};  // XOR0x42 "Limit"
    private static final byte[] S_2EE8D3CA = new byte[]{17, 50, 39, 39, 38, 98, 7, 36, 36, 39, 33, 54};  // XOR0x42 "Speed Effect"
    private static final byte[] S_7D9DBEAF = new byte[]{4, 46, 59, 43, 44, 37};  // XOR0x42 "Flying"
    private static final byte[] S_4D5861A = new byte[]{0, 45, 53, 98, 6, 48, 35, 53};  // XOR0x42 "Bow Draw"
    private static final byte[] S_11E413D5 = new byte[]{16, 43, 38, 43, 44, 37};  // XOR0x42 "Riding"
    private static final float WALK_RATIO = 1.0f;
    private static final float SPRINT_RATIO = 1.3f;
    private static NofovModule instance;
    private final ModeSetting mode = new ModeSetting("Mode", new String[]{"Sprint", "Walk", "Custom"}, "Sprint");
    private final NumberSetting limit = new NumberSetting("Limit", 1.3f, 1.0f, 2.0f);
    private final BooleanSetting speedEffect = new BooleanSetting("Speed Effect", false);
    private final BooleanSetting sprintFov = new BooleanSetting("Sprint", true);
    private final BooleanSetting flyFov = new BooleanSetting("Flying", true);
    private final BooleanSetting bowFov = new BooleanSetting("Bow Draw", true);
    private final BooleanSetting ridingFov = new BooleanSetting("Riding", true);

    public NofovModule() {
        super("NoFov", Category.RENDER);
        this.values.add(this.mode);
        this.values.add(this.limit);
        this.values.add(this.speedEffect);
        this.values.add(this.sprintFov);
        this.values.add(this.flyFov);
        this.values.add(this.bowFov);
        this.values.add(this.ridingFov);
        this.limit.setVisible(() -> this.mode.isMode("Custom"));
        instance = this;
    }

    public static void apply(ComputeFovModifierEvent event) {
        float out;
        float flySpeed;
        NofovModule module = instance;
        if (module == null || !module.enabled) {
            return;
        }
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        float walkingSpeed = player.getAbilities().getWalkingSpeed();
        if (!(walkingSpeed > 0.0f)) {
            return;
        }
        float raw = event.getFovModifier();
        if (!(raw > 0.0f) || Float.isNaN(raw) || Float.isInfinite(raw)) {
            return;
        }
        if (!module.anyScene()) {
            return;
        }
        float ratio = (float)(player.getAttributeValue(Attributes.MOVEMENT_SPEED) / (double)walkingSpeed);
        if (Float.isNaN(ratio) || Float.isInfinite(ratio) || ratio <= 0.0f) {
            return;
        }
        float speedPart = (ratio + 1.0f) * 0.5f;
        float flyPart = 1.0f;
        if ((player.getAbilities().flying || player.isFallFlying()) && (flySpeed = player.getAbilities().getFlyingSpeed()) > 0.0f) {
            flyPart = (flySpeed / 0.1f + 1.0f) * 0.5f;
        }
        float bowPart = 1.0f;
        if (player.isUsingItem() && player.getUseItem().getItem() instanceof BowItem) {
            int ticks = player.getTicksUsingItem();
            bowPart = 1.0f - 0.15f * Math.min(1.0f, (float)ticks / 20.0f);
        }
        float ridePart = 1.0f;
        if (module.isRiding(player)) {
            ridePart = Mth.clamp((float)((ratio + 1.0f) * 0.5f), (float)0.85f, (float)1.15f);
        }
        float rest = raw / Math.max(1.0E-4f, speedPart) / Math.max(1.0E-4f, flyPart) / Math.max(1.0E-4f, bowPart) / Math.max(1.0E-4f, ridePart);
        float cap; // declaration lost by CFR
        if (!((Boolean)module.speedEffect.get()).booleanValue() && module.hasSpeedEffect(player)) {
            cap = module.ratioLimit();
            float cappedPart = (cap + 1.0f) * 0.5f;
            out = rest * cappedPart;
            if (!((Boolean)module.sprintFov.get()).booleanValue()) {
                out = rest * 1.0f;
            }
        } else if (((Boolean)module.sprintFov.get()).booleanValue()) {
            float capped;
            cap = module.ratioLimit();
            out = capped = raw * ((cap + 1.0f) / (ratio + 1.0f));
        } else {
            out = rest * flyPart * bowPart * ridePart;
        }
        if (!((Boolean)module.flyFov.get()).booleanValue()) {
            out /= Math.max(1.0E-4f, flyPart);
        }
        if (!((Boolean)module.bowFov.get()).booleanValue()) {
            out /= Math.max(1.0E-4f, bowPart);
        }
        if (!((Boolean)module.ridingFov.get()).booleanValue()) {
            out /= Math.max(1.0E-4f, ridePart);
        }
        Minecraft mc = Minecraft.getInstance();
        double effectScale = 1.0;
        if (mc != null && mc.options != null) {
            effectScale = (Double)mc.options.fovEffectScale().get();
        }
        event.setNewFovModifier((float)Mth.lerp((double)effectScale, (double)1.0, (double)out));
    }

    public static boolean active() {
        NofovModule module = instance;
        return module != null && module.enabled;
    }

    private boolean hasSpeedEffect(Player player) {
        return player.hasEffect(MobEffects.MOVEMENT_SPEED);
    }

    private boolean isRiding(Player player) {
        return player.getVehicle() != null;
    }

    private float ratioLimit() {
        if (this.mode.isMode("Walk")) {
            return 1.0f;
        }
        if (this.mode.isMode("Custom")) {
            Float custom = (Float)this.limit.get();
            float value = custom == null ? 1.3f : custom.floatValue();
            return value < 1.0f ? 1.0f : value;
        }
        return 1.3f;
    }

    private boolean anyScene() {
        return (Boolean)this.sprintFov.get() != false || (Boolean)this.flyFov.get() != false || (Boolean)this.bowFov.get() != false || (Boolean)this.ridingFov.get() != false || (Boolean)this.speedEffect.get() != false;
    }
}

