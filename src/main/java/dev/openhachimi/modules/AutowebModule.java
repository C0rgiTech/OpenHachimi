/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.utils.rotation.RotationManager;
import dev.openhachimi.settings.BooleanSetting;
import dev.openhachimi.settings.ListSetting;
import dev.openhachimi.settings.NumberSetting;
import dev.openhachimi.utils.rotation.Rotation;
import dev.openhachimi.utils.rotation.RotationUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AutowebModule
extends Module {
    private static final byte[] S_3CB2F325 = new byte[]{3, 55, 54, 45, 53, 39, 32};  // XOR0x42 "Autoweb"
    private static final byte[] S_4B1491D = new byte[]{16, 35, 44, 37, 39};  // XOR0x42 "Range"
    private static final byte[] S_3EDC963 = new byte[]{6, 39, 46, 35, 59};  // XOR0x42 "Delay"
    private static final byte[] S_110AD = new byte[]{4, 13, 20};  // XOR0x42 "FOV"
    private static final byte[] S_11B02DB6 = new byte[]{16, 39, 44, 38, 39, 48};  // XOR0x42 "Render"
    private static final byte[] S_3FF9793B = new byte[]{5, 48, 45, 55, 44, 38, 98, 21, 39, 32};  // XOR0x42 "Ground Web"
    private static final byte[] S_240700 = new byte[]{14, 35, 52, 35};  // XOR0x42 "Lava"
    private static final byte[] S_6E8564DE = new byte[]{14, 35, 52, 35, 98, 21, 35, 43, 54, 98, 21, 35, 54, 39, 48, 98, 0, 55, 33, 41, 39, 54};  // XOR0x42 "Lava Wait Water Bucket"
    private static final byte[] S_29FB20C5 = new byte[]{17, 41, 43, 50, 98, 33, 45, 32, 53, 39, 32, 98, 32, 39, 33, 35, 55, 49, 39, 98, 54, 35, 48, 37, 39, 54, 98, 49, 54, 35, 59, 49, 98, 43, 44, 98, 33, 45, 32, 53, 39, 32, 120, 98};  // XOR0x42 "Skip cobweb because target stays in cobweb: "
    private static final byte[] S_7841C307 = new byte[]{5, 48, 45, 55, 44, 38, 98, 53, 39, 32, 98, 54, 35, 48, 37, 39, 54, 127};  // XOR0x42 "Ground web target="
    private static final byte[] S_3C6C956F = new byte[]{98, 36, 45, 45, 54, 127};  // XOR0x42 " foot="
    private static final byte[] S_3C84023D = new byte[]{98, 42, 39, 35, 38, 127};  // XOR0x42 " head="
    private static final byte[] S_73CD78EA = new byte[]{1, 45, 32, 53, 39, 32, 98, 54, 35, 48, 37, 39, 54, 127};  // XOR0x42 "Cobweb target="
    private static final byte[] S_6205B036 = new byte[]{98, 50, 46, 35, 33, 39, 127};  // XOR0x42 " place="
    private static final byte[] S_256B5A6E = new byte[]{98, 49, 55, 50, 50, 45, 48, 54, 127};  // XOR0x42 " support="
    private static final byte[] S_3C660960 = new byte[]{98, 36, 35, 33, 39, 127};  // XOR0x42 " face="
    private static final byte[] S_1F09FB8 = new byte[]{98, 35, 43, 47, 127};  // XOR0x42 " aim="
    private static final byte[] S_1BA263AC = new byte[]{14, 35, 52, 35, 98, 54, 35, 48, 37, 39, 54, 127};  // XOR0x42 "Lava target="
    private static final byte[] S_48B7E91E = new byte[]{18, 48, 39, 38, 43, 33, 54, 39, 38, 98, 53, 39, 32, 98, 39, 58, 43, 54, 98, 54, 35, 48, 37, 39, 54, 127};  // XOR0x42 "Predicted web exit target="
    private static final byte[] S_68AFD4A7 = new byte[]{98, 54, 43, 33, 41, 49, 127};  // XOR0x42 " ticks="
    private static final byte[] S_3CCF526C = new byte[]{98, 47, 45, 52, 39, 127};  // XOR0x42 " move="
    private static final byte[] S_66036252 = new byte[]{50, 48, 39, 38, 43, 33, 54, 39, 38, 98, 53, 39, 32, 98, 39, 58, 43, 54};  // XOR0x42 "predicted web exit"
    private static final byte[] S_4DD3D755 = new byte[]{18, 48, 39, 38, 43, 33, 54, 39, 38, 98, 53, 39, 32, 98, 39, 58, 43, 54, 98, 37, 48, 45, 55, 44, 38, 98, 53, 39, 32, 98, 54, 35, 48, 37, 39, 54, 127};  // XOR0x42 "Predicted web exit ground web target="
    private static final byte[] S_4AEADDE7 = new byte[]{1, 42, 35, 43, 44, 98, 46, 35, 52, 35, 98, 49, 41, 43, 50, 50, 39, 38, 120, 98, 44, 45, 98, 50, 46, 35, 33, 39, 47, 39, 44, 54, 98, 54, 35, 48, 37, 39, 54, 127};  // XOR0x42 "Chain lava skipped: no placement target="
    private static final byte[] S_EEF80F = new byte[]{1, 42, 35, 43, 44, 98, 46, 35, 52, 35, 98, 35, 36, 54, 39, 48, 98, 33, 45, 32, 53, 39, 32, 98, 54, 35, 48, 37, 39, 54, 127};  // XOR0x42 "Chain lava after cobweb target="
    private static final byte[] S_1B76368E = new byte[]{5, 48, 45, 55, 44, 38, 98, 53, 39, 32, 98, 32, 48, 39, 35, 41, 98, 49, 41, 43, 50, 50, 39, 38, 120, 98, 44, 45, 98, 49, 53, 45, 48, 38};  // XOR0x42 "Ground web break skipped: no sword"
    private static final byte[] S_60CBBBFD = new byte[]{17, 41, 43, 50, 98, 46, 35, 52, 35, 98, 32, 39, 33, 35, 55, 49, 39, 98, 54, 35, 48, 37, 39, 54, 98, 43, 49, 98, 45, 44, 98, 36, 43, 48, 39, 120, 98};  // XOR0x42 "Skip lava because target is on fire: "
    private static final byte[] S_6E84F6B9 = new byte[]{17, 41, 43, 50, 98, 46, 35, 52, 35, 98, 32, 39, 33, 35, 55, 49, 39, 98, 54, 35, 48, 37, 39, 54, 98, 43, 49, 98, 44, 45, 54, 98, 37, 48, 45, 55, 44, 38, 39, 38, 120, 98};  // XOR0x42 "Skip lava because target is not grounded: "
    private static final byte[] S_8704 = new byte[]{98, 59, 127};  // XOR0x42 " y="
    private static final byte[] S_1184DD = new byte[]{103, 108, 112, 36};  // XOR0x42 "%.2f"
    private static final byte[] S_462AD327 = new byte[]{98, 47, 45, 54, 43, 45, 44, 127};  // XOR0x42 " motion="
    private static final byte[] S_391FD934 = new byte[]{17, 41, 43, 50, 98, 46, 35, 52, 35, 98, 32, 39, 33, 35, 55, 49, 39, 98, 54, 35, 48, 37, 39, 54, 98, 43, 49, 98, 47, 45, 52, 43, 44, 37, 120, 98};  // XOR0x42 "Skip lava because target is moving: "
    private static final byte[] S_1486C3AF = new byte[]{5, 48, 45, 55, 44, 38, 98, 53, 39, 32, 98, 49, 41, 43, 50, 50, 39, 38, 120, 98, 44, 45, 98, 49, 53, 45, 48, 38};  // XOR0x42 "Ground web skipped: no sword"
    private static final byte[] S_4928153B = new byte[]{37, 48, 45, 55, 44, 38, 98, 53, 39, 32};  // XOR0x42 "ground web"
    private static final byte[] S_1F2FC8E = new byte[]{98, 36, 45, 45, 54};  // XOR0x42 " foot"
    private static final byte[] S_1DF4807E = new byte[]{17, 41, 43, 50, 98, 37, 48, 45, 55, 44, 38, 98, 53, 39, 32, 98, 32, 39, 33, 35, 55, 49, 39, 98, 54, 35, 48, 37, 39, 54, 98, 43, 49, 98, 44, 45, 54, 98, 37, 48, 45, 55, 44, 38, 39, 38, 120, 98};  // XOR0x42 "Skip ground web because target is not grounded: "
    private static final byte[] S_6E575AB9 = new byte[]{17, 41, 43, 50, 98, 37, 48, 45, 55, 44, 38, 98, 53, 39, 32, 98, 32, 39, 33, 35, 55, 49, 39, 98, 54, 35, 48, 37, 39, 54, 98, 43, 49, 98, 47, 45, 52, 43, 44, 37, 120, 98};  // XOR0x42 "Skip ground web because target is moving: "
    private static final byte[] S_432BBD79 = new byte[]{33, 55, 48, 48, 39, 44, 54};  // XOR0x42 "current"
    private static final byte[] S_654D7BEF = new byte[]{14, 35, 52, 35, 98, 36, 35, 43, 46, 120, 98, 44, 45, 98, 46, 35, 52, 35, 98, 50, 46, 35, 33, 39, 47, 39, 44, 54, 98, 36, 45, 45, 54, 127};  // XOR0x42 "Lava fail: no lava placement foot="
    private static final byte[] S_47F40FE4 = new byte[]{14, 35, 52, 35, 98, 36, 35, 43, 46, 120, 98};  // XOR0x42 "Lava fail: "
    private static final byte[] S_444DFB62 = new byte[]{98, 37, 48, 45, 55, 44, 38, 98, 50, 48, 45, 32, 39, 98, 36, 35, 33, 39, 98, 44, 45, 54, 98, 55, 50, 98, 36, 35, 33, 39, 127};  // XOR0x42 " ground probe face not up face="
    private static final byte[] S_1F190C7F = new byte[]{18, 46, 35, 33, 39, 47, 39, 44, 54, 98, 36, 35, 43, 46, 120, 98};  // XOR0x42 "Placement fail: "
    private static final byte[] S_4829A4C4 = new byte[]{98, 49, 55, 50, 50, 45, 48, 54, 98, 47, 43, 49, 47, 35, 54, 33, 42, 98, 39, 58, 50, 39, 33, 54, 39, 38, 127};  // XOR0x42 " support mismatch expected="
    private static final byte[] S_1F3CF2A = new byte[]{98, 42, 43, 54, 127};  // XOR0x42 " hit="
    private static final byte[] S_2560AF5C = new byte[]{98, 48, 35, 59, 98, 47, 43, 49, 49, 98, 49, 55, 50, 50, 45, 48, 54, 127};  // XOR0x42 " ray miss support="
    private static final byte[] S_4D48672B = new byte[]{98, 36, 35, 33, 39, 98, 44, 45, 54, 98, 55, 50, 98, 36, 35, 33, 39, 127};  // XOR0x42 " face not up face="
    private static final byte[] S_542B00BE = new byte[]{14, 35, 52, 35, 98, 36, 35, 43, 46, 120, 98, 49, 55, 50, 50, 45, 48, 54, 98, 48, 35, 59, 98, 47, 43, 49, 47, 35, 54, 33, 42, 98, 39, 58, 50, 39, 33, 54, 39, 38, 127};  // XOR0x42 "Lava fail: support ray mismatch expected="
    private static final byte[] S_77F2453C = new byte[]{98, 50, 46, 35, 33, 39, 98, 43, 49, 98, 53, 35, 54, 39, 48, 98, 50, 46, 35, 33, 39, 127};  // XOR0x42 " place is water place="
    private static final byte[] S_4F8C3C2E = new byte[]{98, 50, 46, 35, 33, 39, 98, 44, 45, 54, 98, 48, 39, 50, 46, 35, 33, 39, 35, 32, 46, 39, 98, 50, 46, 35, 33, 39, 127};  // XOR0x42 " place not replaceable place="
    private static final byte[] S_7628B7D7 = new byte[]{98, 50, 46, 35, 33, 39, 98, 43, 44, 54, 39, 48, 49, 39, 33, 54, 49, 98, 49, 39, 46, 36, 98, 50, 46, 35, 33, 39, 127};  // XOR0x42 " place intersects self place="
    private static final byte[] S_2484E50B = new byte[]{98, 50, 46, 35, 33, 39, 98, 42, 35, 49, 98, 36, 46, 55, 43, 38, 98, 50, 46, 35, 33, 39, 127};  // XOR0x42 " place has fluid place="
    private static final byte[] S_2D0D46A8 = new byte[]{98, 49, 55, 50, 50, 45, 48, 54, 98, 43, 44, 52, 35, 46, 43, 38, 98, 49, 55, 50, 50, 45, 48, 54, 127};  // XOR0x42 " support invalid support="
    private static final byte[] S_4F023A1E = new byte[]{98, 35, 43, 47, 98, 54, 45, 45, 98, 36, 35, 48, 98, 49, 55, 50, 50, 45, 48, 54, 127};  // XOR0x42 " aim too far support="
    private static final byte[] S_16C8ADC5 = new byte[]{7, 58, 39, 33, 55, 54, 39, 98, 36, 35, 43, 46, 120, 98, 43, 54, 39, 47, 98, 49, 46, 45, 54, 98, 47, 43, 49, 49, 43, 44, 37, 98, 54, 59, 50, 39, 127};  // XOR0x42 "Execute fail: item slot missing type="
    private static final byte[] S_671115B = new byte[]{14, 35, 52, 35, 98, 50, 46, 35, 33, 39, 98, 55, 49, 39, 98, 32, 55, 33, 41, 39, 54, 98, 49, 46, 45, 54, 127};  // XOR0x42 "Lava place use bucket slot="
    private static final byte[] S_1854911 = new byte[]{0, 55, 33, 41, 39, 54, 98, 55, 49, 39, 98, 48, 39, 49, 55, 46, 54, 127};  // XOR0x42 "Bucket use result="
    private static final byte[] S_1FB6BCE = new byte[]{98, 59, 35, 53, 127};  // XOR0x42 " yaw="
    private static final byte[] S_61E40D1D = new byte[]{98, 50, 43, 54, 33, 42, 127};  // XOR0x42 " pitch="
    private static final byte[] S_7E059957 = new byte[]{5, 48, 45, 55, 44, 38, 98, 53, 39, 32, 98, 32, 48, 39, 35, 41, 98, 38, 45, 44, 39, 98, 36, 45, 45, 54, 127};  // XOR0x42 "Ground web break done foot="
    private static final byte[] S_17B613FD = new byte[]{5, 48, 45, 55, 44, 38, 98, 53, 39, 32, 98, 32, 48, 39, 35, 41, 98, 35, 32, 45, 48, 54, 120, 98, 44, 45, 98, 49, 53, 45, 48, 38, 98, 36, 45, 45, 54, 127};  // XOR0x42 "Ground web break abort: no sword foot="
    private static final byte[] S_6FCB55E0 = new byte[]{5, 48, 45, 55, 44, 38, 98, 53, 39, 32, 98, 32, 48, 39, 35, 41, 98, 35, 32, 45, 48, 54, 120, 98, 54, 43, 47, 39, 45, 55, 54, 98, 36, 45, 45, 54, 127};  // XOR0x42 "Ground web break abort: timeout foot="
    private static final byte[] S_314F6475 = new byte[]{5, 48, 45, 55, 44, 38, 98, 53, 39, 32, 98, 46, 35, 52, 35, 98, 49, 41, 43, 50, 50, 39, 38, 120, 98, 44, 45, 98, 46, 35, 52, 35, 98, 50, 46, 35, 33, 39, 47, 39, 44, 54, 98, 54, 35, 48, 37, 39, 54, 127};  // XOR0x42 "Ground web lava skipped: no lava placement target="
    private static final byte[] S_4EE65E1A = new byte[]{5, 48, 45, 55, 44, 38, 98, 53, 39, 32, 98, 46, 35, 52, 35, 98, 51, 55, 39, 55, 39, 38, 98, 54, 35, 48, 37, 39, 54, 127};  // XOR0x42 "Ground web lava queued target="
    private static final byte[] S_298EC9EC = new byte[]{14, 35, 52, 35, 98, 48, 39, 54, 48, 43, 39, 52, 39, 98, 53, 35, 43, 54, 98, 54, 35, 48, 37, 39, 54, 98, 53, 35, 54, 39, 48, 98, 32, 55, 33, 41, 39, 54, 98, 54, 43, 33, 41, 49, 127};  // XOR0x42 "Lava retrieve wait target water bucket ticks="
    private static final byte[] S_2F = new byte[]{109};
    private static final byte[] S_3A23762 = new byte[]{98, 49, 45, 55, 48, 33, 39, 127};  // XOR0x42 " source="
    private static final byte[] S_3C504FAC = new byte[]{14, 35, 52, 35, 98, 48, 39, 54, 48, 43, 39, 52, 39, 98, 35, 32, 45, 48, 54, 120, 98, 44, 45, 98, 39, 47, 50, 54, 59, 98, 32, 55, 33, 41, 39, 54, 98, 49, 45, 55, 48, 33, 39, 127};  // XOR0x42 "Lava retrieve abort: no empty bucket source="
    private static final byte[] S_7270D01F = new byte[]{14, 35, 52, 35, 98, 48, 39, 54, 48, 43, 39, 52, 39, 98, 53, 35, 43, 54, 98, 32, 55, 33, 41, 39, 54, 98, 35, 54, 54, 39, 47, 50, 54, 127};  // XOR0x42 "Lava retrieve wait bucket attempt="
    private static final byte[] S_7E320315 = new byte[]{98, 46, 35, 52, 35, 0, 55, 33, 41, 39, 54, 17, 46, 45, 54, 127};  // XOR0x42 " lavaBucketSlot="
    private static final byte[] S_21DBB729 = new byte[]{14, 35, 52, 35, 98, 48, 39, 54, 48, 43, 39, 52, 39, 98, 55, 49, 39, 98, 32, 55, 33, 41, 39, 54, 98, 35, 54, 54, 39, 47, 50, 54, 127};  // XOR0x42 "Lava retrieve use bucket attempt="
    private static final byte[] S_6726A15 = new byte[]{98, 32, 55, 33, 41, 39, 54, 17, 46, 45, 54, 127};  // XOR0x42 " bucketSlot="
    private static final byte[] S_3C925259 = new byte[]{98, 48, 39, 35, 49, 45, 44, 127};  // XOR0x42 " reason="
    private static final byte[] S_695F4276 = new byte[]{14, 35, 52, 35, 98, 48, 39, 54, 48, 43, 39, 52, 39, 98, 35, 32, 45, 48, 54, 120, 98, 47, 35, 58, 98, 35, 54, 54, 39, 47, 50, 54, 49, 98, 49, 45, 55, 48, 33, 39, 127};  // XOR0x42 "Lava retrieve abort: max attempts source="
    private static final byte[] S_58AF268E = new byte[]{14, 35, 52, 35, 98, 48, 39, 54, 48, 43, 39, 52, 39, 98, 38, 45, 44, 39, 120, 98, 49, 45, 55, 48, 33, 39, 98, 38, 43, 49, 35, 50, 50, 39, 35, 48, 39, 38, 98, 39, 58, 50, 39, 33, 54, 39, 38, 127};  // XOR0x42 "Lava retrieve done: source disappeared expected="
    private static final byte[] S_3B112FA7 = new byte[]{14, 35, 52, 35, 98, 48, 39, 54, 48, 43, 39, 52, 39, 98, 35, 32, 45, 48, 54, 120, 98, 49, 45, 55, 48, 33, 39, 98, 44, 45, 54, 98, 36, 45, 55, 44, 38, 98, 39, 58, 50, 39, 33, 54, 39, 38, 127};  // XOR0x42 "Lava retrieve abort: source not found expected="
    private static final byte[] S_1FCB190E = new byte[]{14, 35, 52, 35, 98, 48, 39, 54, 48, 43, 39, 52, 39, 98, 53, 35, 43, 54, 98, 49, 45, 55, 48, 33, 39, 98, 35, 54, 54, 39, 47, 50, 54, 127};  // XOR0x42 "Lava retrieve wait source attempt="
    private static final byte[] S_1077B465 = new byte[]{98, 39, 58, 50, 39, 33, 54, 39, 38, 127};  // XOR0x42 " expected="
    private static final byte[] S_4A283570 = new byte[]{98, 32, 46, 45, 33, 41, 127};  // XOR0x42 " block="
    private static final byte[] S_4040ADCF = new byte[]{14, 35, 52, 35, 98, 48, 39, 54, 48, 59, 98, 50, 46, 35, 33, 39, 98, 35, 36, 54, 39, 48, 98, 47, 43, 49, 49, 43, 44, 37, 98, 49, 45, 55, 48, 33, 39, 98, 39, 58, 50, 39, 33, 54, 39, 38, 127};  // XOR0x42 "Lava retry place after missing source expected="
    private static final byte[] S_1B66E3E3 = new byte[]{14, 35, 52, 35, 98, 48, 39, 35, 50, 50, 46, 59, 98, 49, 41, 43, 50, 50, 39, 38, 120, 98, 44, 45, 98, 50, 46, 35, 33, 39, 47, 39, 44, 54, 98, 54, 35, 48, 37, 39, 54, 127};  // XOR0x42 "Lava reapply skipped: no placement target="
    private static final byte[] S_1EA435E4 = new byte[]{14, 35, 52, 35, 98, 48, 39, 35, 50, 50, 46, 59, 98, 51, 55, 39, 55, 39, 38, 98, 54, 35, 48, 37, 39, 54, 127};  // XOR0x42 "Lava reapply queued target="
    private static final byte[] S_28E76730 = new byte[]{98, 35, 54, 54, 39, 47, 50, 54, 127};  // XOR0x42 " attempt="
    private static final byte[] S_525ADFF4 = new byte[]{36, 46, 45, 53, 43, 44, 37};  // XOR0x42 "flowing"
    private static final byte[] S_6892B600 = new byte[]{54, 35, 48, 37, 39, 54, 29, 53, 35, 54, 39, 48, 29, 32, 55, 33, 41, 39, 54};  // XOR0x42 "target_water_bucket"
    private static final byte[] S_3EB751EE = new byte[]{32, 39, 36, 45, 48, 39, 29, 36, 46, 45, 53};  // XOR0x42 "before_flow"
    private static final byte[] S_4206BCE7 = new byte[]{44, 45, 48, 47, 35, 46};  // XOR0x42 "normal"
    private static final byte[] S_2C = new byte[]{110};
    private static final byte[] S_6EE7D23D = new byte[]{103, 108, 112, 36, 110, 103, 108, 112, 36, 110, 103, 108, 112, 36};  // XOR0x42 "%.2f,%.2f,%.2f"
    private static final byte[] S_22AFBD34 = new byte[]{47, 29, 115, 114, 119, 113, 116, 112, 29};  // XOR0x42 "m_105362_"
    private static final byte[] S_62675549 = new byte[]{39, 44, 49, 55, 48, 39, 10, 35, 49, 17, 39, 44, 54, 1, 35, 48, 48, 43, 39, 38, 11, 54, 39, 47};  // XOR0x42 "ensureHasSentCarriedItem"
    public static AutowebModule INSTANCE;
    public static float targetYaw;
    public static float targetPitch;
    public static boolean hasTargetRotation;
    private final NumberSetting rangeSetting = new NumberSetting("Range", 3.0f, 3.0f, 5.0f);
    private final ListSetting delaySetting = new ListSetting("Delay", 4, 1, 20);
    private final ListSetting fovSetting = new ListSetting("FOV", 70, 1, 180);
    private final BooleanSetting renderSetting = new BooleanSetting("Render", true);
    private final BooleanSetting groundWebSetting = new BooleanSetting("Ground Web", false);
    private final BooleanSetting lavaSetting = new BooleanSetting("Lava", true);
    private final BooleanSetting lavaWaitWaterBucketSetting = new BooleanSetting("Lava Wait Water Bucket", true);
    private int placementTimerTicks = 0;
    private Phase placementPhase = Phase.IDLE;
    private PlacementInfo currentPlacement;
    private PlacementType activePlacementType;
    private Entity target;
    private BlockPos targetBlockPos;
    private int waitTicks;
    private int retrieveTicks;
    private int retrieveAttempts;
    private int lavaReapplyCount;
    private int sourceWaitCount;
    private boolean lavaPlaced;
    private boolean retriedLavaPlace;
    private GroundWebData currentGroundWebData;
    private GroundWebPhase currentGroundWebPhase = GroundWebPhase.IDLE;
    private BlockPos groundWebFootPos;
    private int groundWebBreakTicks;
    private boolean groundWebBreakStarted;
    private BlockPos lastDebugSupportPos;
    private Direction lastDebugFace;
    private Vec3 lastDebugAimPoint;
    private boolean placementRotationReady;
    private int keepRotationTicks;
    private int savedHotbarSlot = -1;
    private boolean pendingSlotRestore;
    private Minecraft mc = Minecraft.getInstance();

    public AutowebModule() {
        super("Autoweb", Category.PLAYER);
        INSTANCE = this;
        this.values.add(this.rangeSetting);
        this.values.add(this.delaySetting);
        this.values.add(this.fovSetting);
        this.values.add(this.renderSetting);
        this.values.add(this.groundWebSetting);
        this.values.add(this.lavaSetting);
        this.values.add(this.lavaWaitWaterBucketSetting);
    }

    private void reset(boolean forceRestoreSlot) {
        boolean keepAim = this.keepRotationTicks > 0 && hasTargetRotation;
        float keepYaw = targetYaw;
        float keepPitch = targetPitch;
        int keepTicks = this.keepRotationTicks;
        if (forceRestoreSlot) {
            this.restoreSavedHotbarIfNeeded();
        } else if (this.savedHotbarSlot != -1) {
            this.pendingSlotRestore = true;
        }
        this.placementPhase = Phase.IDLE;
        this.currentPlacement = null;
        this.activePlacementType = null;
        this.target = null;
        this.targetBlockPos = null;
        this.waitTicks = 0;
        this.retrieveTicks = 0;
        this.retrieveAttempts = 0;
        this.lavaReapplyCount = 0;
        this.sourceWaitCount = 0;
        this.lavaPlaced = false;
        this.retriedLavaPlace = false;
        this.currentGroundWebData = null;
        this.currentGroundWebPhase = GroundWebPhase.IDLE;
        this.groundWebFootPos = null;
        this.groundWebBreakTicks = 0;
        this.groundWebBreakStarted = false;
        this.lastDebugSupportPos = null;
        this.lastDebugFace = null;
        this.lastDebugAimPoint = null;
        this.placementRotationReady = false;
        if (keepAim) {
            targetYaw = keepYaw;
            targetPitch = keepPitch;
            hasTargetRotation = true;
            this.keepRotationTicks = keepTicks;
        } else {
            this.clearTargetRotation();
        }
    }

    private void reset() {
        this.reset(false);
    }

    private void doLavaRetrieve() {
        if (this.mc.player == null || this.mc.level == null || this.mc.gameMode == null || this.currentPlacement == null) {
            this.reset();
            return;
        }
        ++this.retrieveTicks;
        BlockPos expectedSource = this.currentPlacement.place;
        BlockPos actualSource = this.findNearbyLavaSource(expectedSource);
        BlockPos probe = actualSource != null ? actualSource : expectedSource;
        boolean flowingLava = this.hasNearbyFlowingLava(probe);
        boolean targetHasWaterBucket = this.targetHasWaterBucket(this.target);
        boolean timedOut = this.retrieveTicks >= this.getLavaTickDelay();
        boolean readyToRetrieve = targetHasWaterBucket || flowingLava || timedOut;
        boolean shouldAct = (Boolean)this.lavaWaitWaterBucketSetting.get() == false || readyToRetrieve;
        boolean bl = shouldAct;
        if (this.waitTicks > 0 && !readyToRetrieve) {
            --this.waitTicks;
            return;
        }
        if (actualSource != null) {
            this.lavaPlaced = true;
            this.sourceWaitCount = 0;
            this.targetBlockPos = actualSource;
            this.setTargetRotation(AutowebModule.rotationToBlock(actualSource, 0.0f));
            if (!shouldAct) {
                this.debugLog("Lava retrieve wait target water bucket ticks=" + this.retrieveTicks + "/" + this.getLavaTickDelay() + " source=" + this.formatBlockPos(actualSource));
                this.waitTicks = 1;
                return;
            }
            int emptyBucketSlot = this.findEmptyBucketSlot();
            if (emptyBucketSlot == -1) {
                if (++this.retrieveAttempts >= 8) {
                    this.debugLog("Lava retrieve abort: no empty bucket source=" + this.formatBlockPos(actualSource));
                    this.reset();
                    return;
                }
                this.debugLog("Lava retrieve wait bucket attempt=" + this.retrieveAttempts + " source=" + this.formatBlockPos(actualSource) + " lavaBucketSlot=" + this.findLavaBucketSlot());
                this.waitTicks = 1;
                return;
            }
            int attempt = this.retrieveAttempts + 1;
            this.debugLog("Lava retrieve use bucket attempt=" + attempt + " bucketSlot=" + emptyBucketSlot + " source=" + this.formatBlockPos(actualSource) + " reason=" + this.getRetrieveReason(targetHasWaterBucket, flowingLava, timedOut));
            int previousSlot = this.mc.player.getInventory().selected;
            this.mc.player.getInventory().selected = emptyBucketSlot;
            this.sendCarriedItem();
            this.useBucket(this.currentTargetRotation());
            this.mc.player.getInventory().selected = previousSlot;
            this.sendCarriedItem();
            if (++this.retrieveAttempts >= 8) {
                this.debugLog("Lava retrieve abort: max attempts source=" + this.formatBlockPos(actualSource));
                this.reset();
            } else {
                this.waitTicks = 2;
            }
            return;
        }
        if (this.lavaPlaced) {
            this.debugLog("Lava retrieve done: source disappeared expected=" + this.formatBlockPos(expectedSource));
            if (!this.tryReapplyLava()) {
                this.reset();
            }
            return;
        }
        if (++this.sourceWaitCount >= 8) {
            this.debugLog("Lava retrieve abort: source not found expected=" + this.formatBlockPos(expectedSource) + " support=" + this.formatBlockPos(this.currentPlacement.support));
            this.reset();
            return;
        }
        this.debugLog("Lava retrieve wait source attempt=" + this.sourceWaitCount + " expected=" + this.formatBlockPos(expectedSource) + " block=" + this.mc.level.getBlockState(expectedSource).getBlock());
        if (!this.retriedLavaPlace && this.sourceWaitCount >= 2 && this.findLavaBucketSlot() != -1 && this.isPlacementValid()) {
            this.debugLog("Lava retry place after missing source expected=" + this.formatBlockPos(expectedSource));
            this.retriedLavaPlace = true;
            this.executePlacement();
        }
        this.waitTicks = 1;
    }

    private void doGroundWebBreak() {
        if (this.mc.player == null || this.mc.level == null || this.mc.gameMode == null || this.groundWebFootPos == null) {
            this.reset();
            return;
        }
        if (!this.mc.level.getBlockState(this.groundWebFootPos).is(Blocks.COBWEB)) {
            this.debugLog("Ground web break done foot=" + this.formatBlockPos(this.groundWebFootPos));
            if (!this.tryLavaAfterBreak()) {
                this.reset();
            }
            return;
        }
        int swordSlot = this.findSwordSlot();
        if (swordSlot == -1) {
            this.debugLog("Ground web break abort: no sword foot=" + this.formatBlockPos(this.groundWebFootPos));
            this.reset();
            return;
        }
        this.mc.player.getInventory().selected = swordSlot;
        this.sendCarriedItem();
        AngleVector breakRot = AutowebModule.exactRotation(this.mc.player.getEyePosition(), Vec3.atCenterOf((Vec3i)this.groundWebFootPos));
        this.setTargetRotation(breakRot);
        if (!this.groundWebBreakStarted) {
            this.mc.gameMode.startDestroyBlock(this.groundWebFootPos, Direction.UP);
            this.groundWebBreakStarted = true;
        } else {
            this.mc.gameMode.continueDestroyBlock(this.groundWebFootPos, Direction.UP);
        }
        this.mc.player.swing(InteractionHand.MAIN_HAND);
        if (++this.groundWebBreakTicks >= 24) {
            this.debugLog("Ground web break abort: timeout foot=" + this.formatBlockPos(this.groundWebFootPos));
            this.reset();
        }
    }

    private int findCobwebSlot() {
        for (int slot = 0; slot < 9; ++slot) {
            ItemStack stack = this.mc.player.getInventory().getItem(slot);
            if (stack.isEmpty() || stack.getItem() != Items.COBWEB) continue;
            return slot;
        }
        return -1;
    }

    private void tickPlace() {
        if (!this.isPlacementValid()) {
            this.reset();
            return;
        }
        if (!hasTargetRotation) {
            return;
        }
        if (this.currentPlacement != null && this.mc.player != null) {
            this.setTargetRotation(AutowebModule.exactRotation(this.mc.player.getEyePosition(), this.currentPlacement.aim));
        }
        if (!this.placementRotationReady) {
            this.placementRotationReady = true;
            this.preselectPlacementSlot();
            return;
        }
        if (!RotationManager.hasServerLook()) {
            this.preselectPlacementSlot();
            return;
        }
        if (!this.executePlacement()) {
            this.reset();
            return;
        }
        if (this.isGroundWebMode()) {
            if (!this.advanceGroundWebBreak()) {
                this.placementTimerReset();
                this.pendingSlotRestore = true;
            }
            return;
        }
        if (this.activePlacementType == PlacementType.WEB_EXIT_PREDICT) {
            this.placementTimerReset();
            this.keepRotationTicks = 0;
            this.pendingSlotRestore = true;
            this.reset();
            return;
        }
        if (this.activePlacementType == PlacementType.LAVA) {
            this.placementTimerReset();
            this.setTargetRotation(AutowebModule.rotationToBlock(this.currentPlacement.support, 0.0f));
            this.waitTicks = 1;
            this.retrieveTicks = 0;
            this.retrieveAttempts = 0;
            this.sourceWaitCount = 0;
            this.lavaPlaced = false;
            this.retriedLavaPlace = false;
            this.placementPhase = Phase.LAVA_RETRIEVE;
            return;
        }
        if (!this.tryChainLava()) {
            this.placementTimerReset();
            this.keepRotationTicks = 0;
            this.pendingSlotRestore = true;
            this.reset();
        } else {
            this.placementRotationReady = false;
            this.placementPhase = Phase.PLACE;
            this.preselectPlacementSlot();
        }
    }

    private boolean isPhaseActive() {
        return this.placementPhase != Phase.IDLE;
    }

    private boolean isRotationBlocked() {
        MidpearlModule midPearl = MidpearlModule.INSTANCE;
        return midPearl != null && midPearl.enabled && MidpearlModule.hasTargetRotation;
    }

    private int findLavaBucketSlot() {
        for (int slot = 0; slot < 9; ++slot) {
            ItemStack stack = this.mc.player.getInventory().getItem(slot);
            if (stack.isEmpty() || stack.getItem() != Items.LAVA_BUCKET) continue;
            return slot;
        }
        return -1;
    }

    private static AngleVector exactRotation(Vec3 from, Vec3 to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = Mth.wrapDegrees((float)((float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0)));
        float pitch = Mth.clamp((float)((float)(-Math.toDegrees(Math.atan2(dy, dist)))), (float)-90.0f, (float)90.0f);
        return new AngleVector(yaw, pitch);
    }

    private void applyGroundWebData(GroundWebData data) {
        this.currentGroundWebData = data;
        this.currentGroundWebPhase = GroundWebPhase.FOOT_PLACED;
        this.groundWebFootPos = data.footPos;
        this.groundWebBreakTicks = 0;
        this.groundWebBreakStarted = false;
    }

    private void tickSearch() {
        if (!(this.findCobwebSlot() != -1 || ((Boolean)this.lavaSetting.get()).booleanValue() && this.findLavaBucketSlot() != -1)) {
            return;
        }
        if (!this.placementTimerHasPassed((Integer)this.delaySetting.get())) {
            return;
        }
        Optional<PlacementTarget> targetOpt = this.findPlacementTarget();
        if (targetOpt.isEmpty()) {
            return;
        }
        PlacementTarget target = targetOpt.get();
        this.target = target.target;
        this.currentPlacement = target.placement;
        this.activePlacementType = target.type;
        this.targetBlockPos = target.placement.place;
        this.lavaReapplyCount = 0;
        if (target.groundWebData != null) {
            this.applyGroundWebData(target.groundWebData);
        }
        this.setTargetRotation(AutowebModule.exactRotation(this.mc.player.getEyePosition(), this.currentPlacement.aim));
        this.placementRotationReady = false;
        this.placementPhase = Phase.PLACE;
        this.preselectPlacementSlot();
    }

    private void setTargetRotation(AngleVector rotation) {
        if (rotation == null) {
            this.clearTargetRotation();
            return;
        }
        Rotation lastRot = RotationManager.getLastRotation();
        float prevYaw = lastRot.getYaw();
        float unwrappedYaw = prevYaw + Mth.wrapDegrees((float)(rotation.yaw - prevYaw));
        Rotation utilRot = new Rotation(unwrappedYaw, rotation.pitch);
        Rotation fixedRotation = RotationUtil.applySensitivityPatch(utilRot);
        targetYaw = fixedRotation.getYaw();
        targetPitch = Mth.clamp((float)fixedRotation.getPitch(), (float)-90.0f, (float)90.0f);
        hasTargetRotation = true;
        this.keepRotationTicks = 0;
    }

    private Optional<GroundWebData> findGroundWebAt(Entity entity, BlockPos footPos, BlockPos headPos, String label) {
        if (!this.isBlockReplaceable(footPos) || !this.isBlockReplaceable(headPos)) {
            return Optional.empty();
        }
        Optional<PlacementInfo> footPlacement = this.raycastPlacement(footPos.below(), footPos, entity.getX(), entity.getZ(), label + " foot");
        if (footPlacement.isEmpty()) {
            return Optional.empty();
        }
        PlacementInfo headPlacement = this.createPlacementInfo(footPos, headPos);
        return Optional.of(new GroundWebData(footPos, headPos, footPlacement.get(), headPlacement));
    }

    private String formatVec3(Vec3 vec) {
        return String.format("%.2f,%.2f,%.2f", vec.x, vec.y, vec.z);
    }

    private boolean executePlacement() {
        int slot = this.activePlacementType == PlacementType.LAVA ? this.findLavaBucketSlot() : this.findCobwebSlot();
        int n = slot;
        if (slot == -1) {
            this.debugLog("Execute fail: item slot missing type=" + (Object)((Object)this.activePlacementType));
            return false;
        }
        this.preselectPlacementSlot();
        if (this.mc.player.getInventory().selected != slot) {
            if (this.savedHotbarSlot == -1) {
                this.savedHotbarSlot = this.mc.player.getInventory().selected;
            }
            this.mc.player.getInventory().selected = slot;
            this.sendCarriedItem();
        }
        if (this.activePlacementType == PlacementType.LAVA) {
            AngleVector rotation = AutowebModule.exactRotation(this.mc.player.getEyePosition(), this.currentPlacement.aim);
            this.debugLog("Lava place use bucket slot=" + slot + " place=" + this.formatBlockPos(this.currentPlacement.place) + " support=" + this.formatBlockPos(this.currentPlacement.support));
            this.useBucket(rotation);
        } else {
            BlockHitResult hit = new BlockHitResult(this.currentPlacement.aim, this.currentPlacement.face, this.currentPlacement.support, false);
            this.mc.gameMode.useItemOn(this.mc.player, InteractionHand.MAIN_HAND, hit);
            this.mc.player.swing(InteractionHand.MAIN_HAND);
        }
        return true;
    }

    private boolean isEntityInCobweb(Entity entity) {
        return entity != null && this.mc.level != null && this.hasAnyCobweb(entity.getBoundingBox());
    }

    private boolean isGroundWebMode() {
        return this.activePlacementType == PlacementType.GROUND_WEB;
    }

    private String formatBlockPos(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    private static AngleVector rotationToBlock(BlockPos pos, float pitchOffset) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 eye = mc.player.getEyePosition();
        Vec3 target = Vec3.atCenterOf((Vec3i)pos);
        AngleVector rot = AutowebModule.exactRotation(eye, target);
        return new AngleVector(rot.yaw, rot.pitch + pitchOffset);
    }

    private boolean tryChainLava() {
        if (this.target == null || !((Boolean)this.lavaSetting.get()).booleanValue() || this.findLavaBucketSlot() == -1) {
            return false;
        }
        if (!this.shouldPlaceLava(this.target)) {
            return false;
        }
        Optional<PlacementInfo> placement = this.findLavaSourcePlacement(this.target);
        if (placement.isEmpty()) {
            this.debugLog("Chain lava skipped: no placement target=" + this.target.getName().getString());
            return false;
        }
        this.lavaReapplyCount = 0;
        this.applyLavaPlacement(placement.get());
        this.debugLog("Chain lava after cobweb target=" + this.target.getName().getString() + " place=" + this.formatBlockPos(this.currentPlacement.place) + " support=" + this.formatBlockPos(this.currentPlacement.support) + " face=" + this.currentPlacement.face + " aim=" + this.formatVec3(this.currentPlacement.aim));
        return true;
    }

    private boolean shouldPlaceLavaEx(Entity entity, boolean targetInCobweb) {
        if (!((Boolean)this.lavaSetting.get()).booleanValue()) {
            return false;
        }
        if (entity.isOnFire()) {
            this.debugLog("Skip lava because target is on fire: " + entity.getName().getString());
            return false;
        }
        boolean inWeb = targetInCobweb && this.isEntityInCobweb(entity);
        boolean bl = inWeb;
        if (!inWeb && !this.isEntityGrounded(entity)) {
            this.debugLog("Skip lava because target is not grounded: " + entity.getName().getString() + " y=" + String.format("%.2f", entity.getY()) + " motion=" + this.formatVec3(entity.getDeltaMovement()));
            return false;
        }
        if (!inWeb && this.isEntityMoving(entity)) {
            this.debugLog("Skip lava because target is moving: " + entity.getName().getString() + " move=" + this.formatVec3(this.getEntityMovement(entity)));
            return false;
        }
        return true;
    }

    private boolean isPlacementValid() {
        if (this.mc.player == null || this.mc.level == null || this.mc.gameMode == null) {
            return false;
        }
        if (this.target == null || !this.target.isAlive() || this.target.isRemoved()) {
            return false;
        }
        if (this.currentPlacement == null) {
            return false;
        }
        if (this.activePlacementType == PlacementType.LAVA) {
            if (!this.shouldPlaceLavaEx(this.target, this.isEntityInCobweb(this.target))) {
                return false;
            }
            if (this.intersectsPlayer(this.currentPlacement.place)) {
                return false;
            }
            if (this.isWater(this.currentPlacement.place)) {
                return false;
            }
            if (!this.mc.level.getBlockState(this.currentPlacement.place).canBeReplaced()) {
                return false;
            }
            if (!this.mc.level.getFluidState(this.currentPlacement.place).isEmpty()) {
                return false;
            }
            return this.isValidSupport(this.currentPlacement.support);
        }
        if (this.activePlacementType != PlacementType.GROUND_WEB && this.activePlacementType != PlacementType.WEB_EXIT_PREDICT && this.isEntityInCobweb(this.target)) {
            return false;
        }
        if (this.intersectsPlayer(this.currentPlacement.place)) {
            return false;
        }
        if (!this.mc.level.getBlockState(this.currentPlacement.place).canBeReplaced()) {
            return false;
        }
        if (!this.mc.level.getFluidState(this.currentPlacement.place).isEmpty()) {
            return false;
        }
        return this.isValidSupport(this.currentPlacement.support);
    }

    private Vec3 getEntityMovement(Entity entity) {
        return new Vec3(entity.getX() - entity.xo, 0.0, entity.getZ() - entity.zo);
    }

    private boolean isAllCobweb(AABB aabb) {
        if (this.mc.level == null) {
            return false;
        }
        boolean any = false;
        int minX = (int)Math.floor(aabb.minX);
        int maxX = (int)Math.floor(aabb.maxX);
        int minY = (int)Math.floor(aabb.minY);
        int maxY = (int)Math.floor(aabb.maxY);
        int minZ = (int)Math.floor(aabb.minZ);
        int maxZ = (int)Math.floor(aabb.maxZ);
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!aabb.intersects(new AABB(pos))) continue;
                    if (!this.mc.level.getBlockState(pos).is(Blocks.COBWEB)) {
                        return false;
                    }
                    any = true;
                }
            }
        }
        return any;
    }

    private void debugLog(String message) {
    }

    private Optional<PlacementTarget> tryPlacements(Entity entity, List<PredictionTick> ticks, Vec3 movement) {
        for (PredictionTick tick : ticks) {
            Optional<PlacementInfo> place = this.findPlacementForTick(entity, tick);
            if (place.isPresent()) {
                PlacementInfo info = place.get();
                this.debugLog("Predicted web exit target=" + entity.getName().getString() + " ticks=" + tick.ticks + " place=" + this.formatBlockPos(info.place) + " support=" + this.formatBlockPos(info.support) + " face=" + info.face + " move=" + this.formatVec3(movement));
                return Optional.of(new PlacementTarget(entity, info, PlacementType.WEB_EXIT_PREDICT, null));
            }
            Optional<GroundWebData> groundWeb = this.findGroundWebAt(entity, tick.footPos, tick.headPos, "predicted web exit");
            if (!groundWeb.isPresent()) continue;
            GroundWebData data = groundWeb.get();
            this.debugLog("Predicted web exit ground web target=" + entity.getName().getString() + " ticks=" + tick.ticks + " foot=" + this.formatBlockPos(data.footPos) + " head=" + this.formatBlockPos(data.headPos) + " move=" + this.formatVec3(movement));
            return Optional.of(new PlacementTarget(entity, data.footPlacement, PlacementType.GROUND_WEB, data));
        }
        return Optional.empty();
    }

    private Optional<PlacementInfo> findLavaPlacement(Entity entity) {
        for (BlockPos pos : this.getLavaPositions(entity)) {
            Optional<PlacementInfo> placement = this.findLavaPlacementAt(entity, pos);
            if (!placement.isPresent()) continue;
            return placement;
        }
        return Optional.empty();
    }

    private void addBlockPosToList(List<BlockPos> list, BlockPos pos) {
        if (!list.contains(pos)) {
            list.add(pos);
        }
    }

    private void applyPlacement(PlacementInfo placement, PlacementType type) {
        this.currentPlacement = placement;
        this.activePlacementType = type;
        this.targetBlockPos = placement.place;
        this.setTargetRotation(AutowebModule.exactRotation(this.mc.player.getEyePosition(), placement.aim));
        this.placementRotationReady = false;
    }

    private boolean shouldPlaceLava(Entity entity) {
        return this.shouldPlaceLavaEx(entity, false);
    }

    private void applyLavaPlacement(PlacementInfo placement) {
        this.currentPlacement = placement;
        this.activePlacementType = PlacementType.LAVA;
        this.targetBlockPos = placement.place;
        this.setTargetRotation(AutowebModule.exactRotation(this.mc.player.getEyePosition(), placement.aim));
        this.placementRotationReady = false;
    }

    private boolean isEntityGrounded(Entity entity) {
        double yFrac = entity.getY() - Math.floor(entity.getY());
        double yDistance = Math.min(yFrac, 1.0 - yFrac);
        return entity.onGround() || yDistance < 0.08 && Math.abs(entity.getDeltaMovement().y) < 0.12;
    }

    private boolean isAabbIntersecting(AABB aabb, BlockPos pos) {
        BlockPos above = pos.above();
        return this.isCobwebIntersecting(aabb, pos) || this.isCobwebIntersecting(aabb, above);
    }

    private boolean isEntityMoving(Entity entity) {
        Vec3 movement = this.getEntityMovement(entity);
        return movement.x * movement.x + movement.z * movement.z >= 0.01;
    }

    private int findSwordSlot() {
        for (int slot = 0; slot < 9; ++slot) {
            ItemStack stack = this.mc.player.getInventory().getItem(slot);
            if (stack.isEmpty() || !(stack.getItem() instanceof SwordItem)) continue;
            return slot;
        }
        return -1;
    }

    private boolean intersectsPlayer(BlockPos pos) {
        return this.mc.player != null && this.mc.player.getBoundingBox().intersects(new AABB(pos));
    }

    private void addPositionToList(List<PredictionTick> list, int tickAhead, BlockPos foot) {
        BlockPos head = foot.above();
        if (this.mc.level.getBlockState(foot).is(Blocks.COBWEB) && this.mc.level.getBlockState(head).is(Blocks.COBWEB)) {
            return;
        }
        PredictionTick tick = new PredictionTick(tickAhead, foot, head);
        if (!list.contains(tick)) {
            list.add(tick);
        }
    }

    private List<BlockPos> getLavaPositions(Entity entity) {
        ArrayList<BlockPos> list = new ArrayList<BlockPos>();
        this.addBlockPosToList(list, BlockPos.containing((double)entity.getX(), (double)(entity.getY() + 1.0), (double)entity.getZ()));
        AABB box = entity.getBoundingBox();
        int minX = (int)Math.floor(box.minX - 0.45);
        int maxX = (int)Math.floor(box.maxX + 0.45);
        int minZ = (int)Math.floor(box.minZ - 0.45);
        int maxZ = (int)Math.floor(box.maxZ + 0.45);
        int headY = (int)Math.floor(entity.getY() + 1.0);
        int topY = (int)Math.floor(box.maxY - 0.05);
        for (int y = Math.min(headY, topY); y <= Math.max(headY, topY); ++y) {
            for (int x = minX; x <= maxX; ++x) {
                for (int z = minZ; z <= maxZ; ++z) {
                    this.addBlockPosToList(list, new BlockPos(x, y, z));
                }
            }
        }
        return list;
    }

    private Optional<Vec3> findFaceHitPoint(BlockPos pos, Direction face) {
        for (Vec3 point : this.getFacePoints(pos, face)) {
            Optional<Vec3> hit;
            if (this.mc.player.getEyePosition().distanceToSqr(point) > 25.0 || !(hit = this.raycastFace(pos, face, point)).isPresent()) continue;
            return hit;
        }
        return Optional.empty();
    }

    private Vec3 offsetTowardsFace(Vec3 point, Direction face) {
        double step = 0.01;
        return point.add((double)(-face.getStepX()) * step, (double)(-face.getStepY()) * step, (double)(-face.getStepZ()) * step);
    }

    private Vec3 clampAimPoint(BlockPos pos, double x, double z) {
        return new Vec3(this.clampValue(x, (double)pos.getX() + 0.1, (double)pos.getX() + 0.9), (double)pos.getY() + 0.99, this.clampValue(z, (double)pos.getZ() + 0.1, (double)pos.getZ() + 0.9));
    }

    private boolean isValidSupport(BlockPos pos) {
        BlockState state = this.mc.level.getBlockState(pos);
        return !state.canBeReplaced() && !this.getBlockShape(pos).isEmpty();
    }

    private Optional<Vec3> raycastFace(BlockPos pos, Direction face, Vec3 aim) {
        if (this.mc.player == null || this.mc.level == null) {
            return Optional.empty();
        }
        Vec3 offset = this.offsetTowardsFace(aim, face);
        BlockHitResult hit = this.mc.level.clip(new ClipContext(this.mc.player.getEyePosition(), offset, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, (Entity)this.mc.player));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return Optional.empty();
        }
        return hit.getBlockPos().equals((Object)pos) && hit.getDirection() == face ? Optional.of(hit.getLocation()) : Optional.empty();
    }

    private boolean isBlockReplaceable(BlockPos pos) {
        return this.mc.level.getBlockState(pos).canBeReplaced() && this.mc.level.getFluidState(pos).isEmpty() && !this.intersectsPlayer(pos);
    }

    private Optional<PlacementInfo> raycastPlacement(BlockPos supportPos, BlockPos placePos, double targetX, double targetZ, String label) {
        if (!this.isValidSupport(supportPos)) {
            return Optional.empty();
        }
        BlockHitResult lastHit = null;
        Vec3 lastAim = null;
        for (Vec3 candidate : this.getAimCandidates(supportPos, targetX, targetZ)) {
            if (this.mc.player.getEyePosition().distanceToSqr(candidate) > 25.0) continue;
            Vec3 offset = this.offsetTowardsFace(candidate, Direction.UP);
            BlockHitResult hit = this.mc.level.clip(new ClipContext(this.mc.player.getEyePosition(), offset, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, (Entity)this.mc.player));
            if (hit.getType() != HitResult.Type.BLOCK) {
                lastAim = offset;
                continue;
            }
            lastHit = hit;
            lastAim = offset;
            if (!hit.getBlockPos().equals((Object)supportPos) || hit.getDirection() != Direction.UP) continue;
            this.lastDebugSupportPos = supportPos;
            this.lastDebugFace = Direction.UP;
            this.lastDebugAimPoint = hit.getLocation();
            return Optional.of(new PlacementInfo(supportPos, Direction.UP, hit.getLocation(), placePos));
        }
        if (lastHit != null) {
            this.lastDebugSupportPos = lastHit.getBlockPos();
            this.lastDebugFace = lastHit.getDirection();
            this.lastDebugAimPoint = lastAim;
            this.debugLog("Placement fail: " + label + " support mismatch expected=" + this.formatBlockPos(supportPos) + " hit=" + this.formatBlockPos(lastHit.getBlockPos()) + " face=" + lastHit.getDirection() + " aim=" + this.formatVec3(lastAim));
        } else if (lastAim != null) {
            this.lastDebugSupportPos = supportPos;
            this.lastDebugFace = Direction.UP;
            this.lastDebugAimPoint = lastAim;
            this.debugLog("Placement fail: " + label + " ray miss support=" + this.formatBlockPos(supportPos) + " aim=" + this.formatVec3(lastAim));
        }
        return Optional.empty();
    }

    private boolean canPlaceGroundWeb(Entity entity) {
        if (!this.isEntityGrounded(entity)) {
            this.debugLog("Skip ground web because target is not grounded: " + entity.getName().getString() + " y=" + String.format("%.2f", entity.getY()) + " motion=" + this.formatVec3(entity.getDeltaMovement()));
            return false;
        }
        if (this.isEntityMoving(entity)) {
            this.debugLog("Skip ground web because target is moving: " + entity.getName().getString() + " move=" + this.formatVec3(this.getEntityMovement(entity)));
            return false;
        }
        return true;
    }

    private Optional<PlacementInfo> findAimAtSupport(BlockPos supportPos, double x, double z, String label) {
        Vec3 aim = this.clampAimPoint(supportPos, x, z);
        return this.aimAndCheckPlacement(aim, supportPos, label);
    }

    private List<Vec3> getAimCandidates(BlockPos pos, double targetX, double targetZ) {
        double y = (double)pos.getY() + 1.0;
        ArrayList<Vec3> list = new ArrayList<Vec3>();
        list.add(new Vec3((double)pos.getX() + 0.5, y, (double)pos.getZ() + 0.5));
        list.add(new Vec3(this.clampValue(targetX, (double)pos.getX() + 0.25, (double)pos.getX() + 0.75), y, this.clampValue(targetZ, (double)pos.getZ() + 0.25, (double)pos.getZ() + 0.75)));
        list.add(new Vec3((double)pos.getX() + 0.35, y, (double)pos.getZ() + 0.35));
        list.add(new Vec3((double)pos.getX() + 0.65, y, (double)pos.getZ() + 0.35));
        list.add(new Vec3((double)pos.getX() + 0.35, y, (double)pos.getZ() + 0.65));
        list.add(new Vec3((double)pos.getX() + 0.65, y, (double)pos.getZ() + 0.65));
        return list;
    }

    private double clampValue(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private boolean isWater(BlockPos pos) {
        Fluid fluid = this.mc.level.getFluidState(pos).getType();
        return this.mc.level.getBlockState(pos).is(Blocks.WATER) || fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER;
    }

    private List<Vec3> getFacePoints(BlockPos pos, Direction face) {
        AABB faceBox;
        ArrayList<Vec3> list = new ArrayList<Vec3>();
        Optional<Vec3> center = this.findFaceCenter(pos, face);
        if (center.isPresent()) {
            list.add(center.get());
        }
        if ((faceBox = this.getFaceBoundingBox(pos, face)) == null) {
            return list;
        }
        double[] fractions = new double[]{0.5, 0.25, 0.75};
        for (double u : fractions) {
            for (double v : fractions) {
                Vec3 point = null;
                switch (face) {
                    case UP: {
                        point = new Vec3((double)pos.getX() + AutowebModule.lerp(faceBox.minX, faceBox.maxX, u), (double)pos.getY() + faceBox.maxY, (double)pos.getZ() + AutowebModule.lerp(faceBox.minZ, faceBox.maxZ, v));
                        break;
                    }
                    case DOWN: {
                        point = new Vec3((double)pos.getX() + AutowebModule.lerp(faceBox.minX, faceBox.maxX, u), (double)pos.getY() + faceBox.minY, (double)pos.getZ() + AutowebModule.lerp(faceBox.minZ, faceBox.maxZ, v));
                        break;
                    }
                    case EAST: {
                        point = new Vec3((double)pos.getX() + faceBox.maxX, (double)pos.getY() + AutowebModule.lerp(faceBox.minY, faceBox.maxY, u), (double)pos.getZ() + AutowebModule.lerp(faceBox.minZ, faceBox.maxZ, v));
                        break;
                    }
                    case WEST: {
                        point = new Vec3((double)pos.getX() + faceBox.minX, (double)pos.getY() + AutowebModule.lerp(faceBox.minY, faceBox.maxY, u), (double)pos.getZ() + AutowebModule.lerp(faceBox.minZ, faceBox.maxZ, v));
                        break;
                    }
                    case SOUTH: {
                        point = new Vec3((double)pos.getX() + AutowebModule.lerp(faceBox.minX, faceBox.maxX, u), (double)pos.getY() + AutowebModule.lerp(faceBox.minY, faceBox.maxY, v), (double)pos.getZ() + faceBox.maxZ);
                        break;
                    }
                    case NORTH: {
                        point = new Vec3((double)pos.getX() + AutowebModule.lerp(faceBox.minX, faceBox.maxX, u), (double)pos.getY() + AutowebModule.lerp(faceBox.minY, faceBox.maxY, v), (double)pos.getZ() + faceBox.minZ);
                    }
                }
                this.addUniquePoint(list, point);
            }
        }
        return list;
    }

    private boolean useBucket(AngleVector rotation) {
        if (rotation == null || this.mc.player == null || this.mc.gameMode == null) {
            return false;
        }
        InteractionResult result = this.mc.gameMode.useItem((Player)this.mc.player, InteractionHand.MAIN_HAND);
        this.debugLog("Bucket use result=" + result + " yaw=" + rotation.yaw + " pitch=" + rotation.pitch);
        if (result.consumesAction()) {
            this.mc.player.swing(InteractionHand.MAIN_HAND);
        }
        return result.consumesAction();
    }

    private int getLavaTickDelay() {
        return this.mc.level == null ? 9 : Math.max(1, Fluids.LAVA.getTickDelay((LevelReader)this.mc.level) - 1);
    }

    private AABB getFaceBoundingBox(BlockPos pos, Direction face) {
        VoxelShape shape = this.getBlockShape(pos);
        if (shape.isEmpty()) {
            return null;
        }
        AABB result = null;
        double bestValue = AutowebModule.isPositiveFace(face) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        for (AABB box : shape.toAabbs()) {
            boolean bl;
            double faceCoord = 0.0;
            switch (face) {
                case UP: {
                    faceCoord = box.maxY;
                    break;
                }
                case DOWN: {
                    faceCoord = box.minY;
                    break;
                }
                case EAST: {
                    faceCoord = box.maxX;
                    break;
                }
                case WEST: {
                    faceCoord = box.minX;
                    break;
                }
                case SOUTH: {
                    faceCoord = box.maxZ;
                    break;
                }
                case NORTH: {
                    faceCoord = box.minZ;
                }
            }
            boolean better = bl = AutowebModule.isPositiveFace(face) ? faceCoord > bestValue : faceCoord < bestValue;
            if (!better) continue;
            bestValue = faceCoord;
            result = box;
        }
        return result;
    }

    private boolean tryReapplyLava() {
        if (this.target == null || this.target.isRemoved() || !this.target.isAlive()) {
            return false;
        }
        if (this.target.isOnFire()) {
            return false;
        }
        if (this.lavaReapplyCount >= 1) {
            return false;
        }
        if (this.findLavaBucketSlot() == -1) {
            return false;
        }
        if (!this.shouldPlaceLavaEx(this.target, true)) {
            return false;
        }
        Optional<PlacementInfo> placement = this.findLavaSourcePlacement(this.target);
        if (placement.isEmpty()) {
            this.debugLog("Lava reapply skipped: no placement target=" + this.target.getName().getString());
            return false;
        }
        ++this.lavaReapplyCount;
        this.applyLavaPlacement(placement.get());
        this.placementRotationReady = false;
        this.placementPhase = Phase.PLACE;
        this.debugLog("Lava reapply queued target=" + this.target.getName().getString() + " attempt=" + this.lavaReapplyCount + " place=" + this.formatBlockPos(this.currentPlacement.place) + " support=" + this.formatBlockPos(this.currentPlacement.support) + " face=" + this.currentPlacement.face + " aim=" + this.formatVec3(this.currentPlacement.aim));
        return true;
    }

    private boolean isLavaSource(BlockPos pos) {
        return this.mc.level.getBlockState(pos).is(Blocks.LAVA) && this.mc.level.getFluidState(pos).isSource();
    }

    private boolean isFlowingLava(BlockPos pos) {
        FluidState fluidState = this.mc.level.getFluidState(pos);
        Fluid fluid = fluidState.getType();
        return (fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA) && !fluidState.isSource();
    }

    private Optional<Vec3> findFaceCenter(BlockPos pos, Direction face) {
        VoxelShape shape = this.getBlockShape(pos);
        if (shape.isEmpty()) {
            return Optional.empty();
        }
        List aabbs = shape.toAabbs();
        if (aabbs.isEmpty()) {
            return Optional.empty();
        }
        AABB chosen = null;
        double bestValue = AutowebModule.isPositiveFace(face) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        for (AABB box : (java.util.List<AABB>)(Object)aabbs) {
            boolean bl;
            double faceCoord = 0.0;
            switch (face) {
                case UP: {
                    faceCoord = box.maxY;
                    break;
                }
                case DOWN: {
                    faceCoord = box.minY;
                    break;
                }
                case EAST: {
                    faceCoord = box.maxX;
                    break;
                }
                case WEST: {
                    faceCoord = box.minX;
                    break;
                }
                case SOUTH: {
                    faceCoord = box.maxZ;
                    break;
                }
                case NORTH: {
                    faceCoord = box.minZ;
                }
            }
            boolean better = bl = AutowebModule.isPositiveFace(face) ? faceCoord > bestValue : faceCoord < bestValue;
            if (!better) continue;
            bestValue = faceCoord;
            chosen = box;
        }
        if (chosen == null) {
            return Optional.empty();
        }
        double cx = (double)pos.getX() + (chosen.minX + chosen.maxX) * 0.5;
        double cy = (double)pos.getY() + (chosen.minY + chosen.maxY) * 0.5;
        double cz = (double)pos.getZ() + (chosen.minZ + chosen.maxZ) * 0.5;
        switch (face) {
            case UP: {
                cy = (double)pos.getY() + chosen.maxY;
                break;
            }
            case DOWN: {
                cy = (double)pos.getY() + chosen.minY;
                break;
            }
            case EAST: {
                cx = (double)pos.getX() + chosen.maxX;
                break;
            }
            case WEST: {
                cx = (double)pos.getX() + chosen.minX;
                break;
            }
            case SOUTH: {
                cz = (double)pos.getZ() + chosen.maxZ;
                break;
            }
            case NORTH: {
                cz = (double)pos.getZ() + chosen.minZ;
            }
        }
        return Optional.of(new Vec3(cx, cy, cz));
    }

    private VoxelShape getBlockShape(BlockPos pos) {
        BlockState state = this.mc.level.getBlockState(pos);
        VoxelShape shape = state.getShape((BlockGetter)this.mc.level, pos);
        return !shape.isEmpty() ? shape : state.getCollisionShape((BlockGetter)this.mc.level, pos);
    }

    private void addUniquePoint(List<Vec3> list, Vec3 point) {
        for (Vec3 existing : list) {
            if (!(existing.distanceToSqr(point) < 1.0E-6)) continue;
            return;
        }
        list.add(point);
    }

    private String getRetrieveReason(boolean targetHasWaterBucket, boolean flowing, boolean timedOut) {
        if (flowing) {
            return "flowing";
        }
        if (targetHasWaterBucket) {
            return "target_water_bucket";
        }
        return timedOut ? "before_flow" : "normal";
    }

    private boolean tryLavaAfterBreak() {
        if (this.target == null || !((Boolean)this.lavaSetting.get()).booleanValue() || this.findLavaBucketSlot() == -1) {
            return false;
        }
        if (!this.shouldPlaceLavaEx(this.target, true)) {
            return false;
        }
        Optional<PlacementInfo> placement = this.findLavaSourcePlacement(this.target);
        if (placement.isEmpty()) {
            this.debugLog("Ground web lava skipped: no lava placement target=" + this.target.getName().getString());
            return false;
        }
        this.lavaReapplyCount = 0;
        this.applyLavaPlacement(placement.get());
        this.currentGroundWebData = null;
        this.currentGroundWebPhase = GroundWebPhase.IDLE;
        this.groundWebFootPos = null;
        this.groundWebBreakTicks = 0;
        this.groundWebBreakStarted = false;
        this.placementRotationReady = false;
        this.placementPhase = Phase.PLACE;
        this.debugLog("Ground web lava queued target=" + this.target.getName().getString() + " place=" + this.formatBlockPos(this.currentPlacement.place) + " support=" + this.formatBlockPos(this.currentPlacement.support) + " face=" + this.currentPlacement.face + " aim=" + this.formatVec3(this.currentPlacement.aim));
        return true;
    }

    private static boolean isPositiveFace(Direction face) {
        return face == Direction.UP || face == Direction.EAST || face == Direction.SOUTH;
    }

    private boolean isInFov(Entity entity) {
        if (this.mc.player == null) {
            return false;
        }
        double dx = entity.getX() - this.mc.player.getX();
        double dz = entity.getZ() - this.mc.player.getZ();
        float targetYaw = Mth.wrapDegrees((float)((float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0)));
        float yawDiff = Math.abs(Mth.wrapDegrees((float)(targetYaw - this.mc.player.getYRot())));
        return yawDiff <= (float)((Integer)this.fovSetting.get()).intValue();
    }

    private boolean hasAnyCobweb(AABB aabb) {
        if (this.mc.level == null) {
            return false;
        }
        int minX = (int)Math.floor(aabb.minX);
        int maxX = (int)Math.floor(aabb.maxX);
        int minY = (int)Math.floor(aabb.minY);
        int maxY = (int)Math.floor(aabb.maxY);
        int minZ = (int)Math.floor(aabb.minZ);
        int maxZ = (int)Math.floor(aabb.maxZ);
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!this.mc.level.getBlockState(pos).is(Blocks.COBWEB) || !aabb.intersects(new AABB(pos))) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private void sendCarriedItem() {
        if (this.mc.gameMode == null) {
            return;
        }
        try {
            Method method = null;
            try {
                method = this.mc.gameMode.getClass().getDeclaredMethod("m_105362_", new Class[0]);
            }
            catch (NoSuchMethodException ignored) {
                try {
                    method = this.mc.gameMode.getClass().getDeclaredMethod("ensureHasSentCarriedItem", new Class[0]);
                }
                catch (NoSuchMethodException ignored2) {
                    method = null;
                }
            }
            if (method != null) {
                method.setAccessible(true);
                method.invoke((Object)this.mc.gameMode, new Object[0]);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    @Override
    public void onDisable() {
        this.keepRotationTicks = 0;
        hasTargetRotation = false;
        this.reset(true);
    }

    @Override
    public void onEnable() {
        this.keepRotationTicks = 0;
        this.savedHotbarSlot = -1;
        this.pendingSlotRestore = false;
        hasTargetRotation = false;
        this.reset(true);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private void clearTargetRotation() {
        hasTargetRotation = false;
        this.keepRotationTicks = 0;
        RotationManager.clear();
    }

    private boolean isCobwebIntersecting(AABB aabb, BlockPos pos) {
        return aabb.intersects(new AABB(pos)) && !this.mc.level.getBlockState(pos).is(Blocks.COBWEB);
    }

    private boolean hasNearbyFlowingLava(BlockPos center) {
        for (int x = -1; x <= 1; ++x) {
            for (int y = -1; y <= 1; ++y) {
                for (int z = -1; z <= 1; ++z) {
                    if (!this.isFlowingLava(center.offset(x, y, z))) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean placementTimerHasPassed(int required) {
        return this.placementTimerTicks >= required;
    }

    private Optional<GroundWebData> findGroundWebPlacement(Entity entity) {
        if (!this.canPlaceGroundWeb(entity)) {
            return Optional.empty();
        }
        if (this.findSwordSlot() == -1) {
            this.debugLog("Ground web skipped: no sword");
            return Optional.empty();
        }
        BlockPos foot = BlockPos.containing((double)entity.getX(), (double)entity.getY(), (double)entity.getZ());
        return this.findGroundWebAt(entity, foot, foot.above(), "ground web");
    }

    private Optional<PlacementInfo> aimAndCheckPlacement(Vec3 aim, BlockPos expectedSupport, String label) {
        BlockHitResult hit = this.mc.level.clip(new ClipContext(this.mc.player.getEyePosition(), aim, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, (Entity)this.mc.player));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return Optional.empty();
        }
        if (hit.getDirection() != Direction.UP) {
            this.debugLog("Lava fail: " + label + " face not up face=" + hit.getDirection() + " support=" + this.formatBlockPos(hit.getBlockPos()) + " aim=" + this.formatVec3(aim));
            return Optional.empty();
        }
        if (expectedSupport != null && !hit.getBlockPos().equals((Object)expectedSupport)) {
            this.debugLog("Lava fail: support ray mismatch expected=" + this.formatBlockPos(expectedSupport) + " hit=" + this.formatBlockPos(hit.getBlockPos()) + " aim=" + this.formatVec3(aim));
            return Optional.empty();
        }
        BlockPos supportPos = hit.getBlockPos();
        BlockPos placePos = supportPos.relative(hit.getDirection());
        if (this.isWater(placePos)) {
            this.debugLog("Lava fail: " + label + " place is water place=" + this.formatBlockPos(placePos));
            return Optional.empty();
        }
        if (!this.mc.level.getBlockState(placePos).canBeReplaced()) {
            this.debugLog("Lava fail: " + label + " place not replaceable place=" + this.formatBlockPos(placePos));
            return Optional.empty();
        }
        if (this.intersectsPlayer(placePos)) {
            this.debugLog("Lava fail: " + label + " place intersects self place=" + this.formatBlockPos(placePos));
            return Optional.empty();
        }
        if (!this.mc.level.getFluidState(placePos).isEmpty()) {
            this.debugLog("Lava fail: " + label + " place has fluid place=" + this.formatBlockPos(placePos));
            return Optional.empty();
        }
        if (!this.isValidSupport(supportPos)) {
            this.debugLog("Lava fail: " + label + " support invalid support=" + this.formatBlockPos(supportPos));
            return Optional.empty();
        }
        if (this.mc.player.getEyePosition().distanceToSqr(aim) > 25.0) {
            this.debugLog("Lava fail: " + label + " aim too far support=" + this.formatBlockPos(supportPos) + " aim=" + this.formatVec3(aim));
            return Optional.empty();
        }
        return Optional.of(new PlacementInfo(supportPos, hit.getDirection(), aim, placePos));
    }

    private boolean targetHasWaterBucket(Entity entity) {
        if (!(entity instanceof Player)) {
            return false;
        }
        Player player = (Player)entity;
        return player.getMainHandItem().getItem() == Items.WATER_BUCKET || player.getOffhandItem().getItem() == Items.WATER_BUCKET;
    }

    private boolean isValidPlayerTarget(Entity entity) {
        if (!(entity instanceof Player)) {
            return false;
        }
        if (entity == this.mc.player) {
            return false;
        }
        if (!entity.isAlive() || entity.isRemoved()) {
            return false;
        }
        if (AntibotModule.isBot(entity)) {
            return false;
        }
        if (TeamsModule.isSameTeam(entity)) {
            return false;
        }
        return this.isInFov(entity);
    }

    private List<PredictionTick> getPositionsForAabb(AABB aabb, int tickAhead) {
        ArrayList<PredictionTick> list = new ArrayList<PredictionTick>();
        int minX = (int)Math.floor(aabb.minX);
        int maxX = (int)Math.floor(aabb.maxX);
        int minY = (int)Math.floor(aabb.minY);
        int maxY = (int)Math.floor(aabb.maxY);
        int minZ = (int)Math.floor(aabb.minZ);
        int maxZ = (int)Math.floor(aabb.maxZ);
        int footY = (int)Math.floor(aabb.minY + 0.001);
        double centerX = (aabb.minX + aabb.maxX) * 0.5;
        double centerZ = (aabb.minZ + aabb.maxZ) * 0.5;
        BlockPos center = BlockPos.containing((double)centerX, (double)aabb.minY, (double)centerZ);
        if (this.isAabbIntersecting(aabb, center)) {
            this.addPositionToList(list, tickAhead, center);
        }
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!aabb.intersects(new AABB(pos)) || this.mc.level.getBlockState(pos).is(Blocks.COBWEB)) continue;
                    int chosenY = y <= footY ? footY : y - 1;
                    this.addPositionToList(list, tickAhead, new BlockPos(x, chosenY, z));
                }
            }
        }
        final double sortX = centerX;
        final double sortY = (double)footY + 0.5;
        final double sortZ = centerZ;
        list.sort(new Comparator<PredictionTick>(){

            @Override
            public int compare(PredictionTick a, PredictionTick b) {
                double da = Vec3.atCenterOf((Vec3i)a.footPos).distanceToSqr(sortX, sortY, sortZ);
                double db = Vec3.atCenterOf((Vec3i)b.footPos).distanceToSqr(sortX, sortY, sortZ);
                return Double.compare(da, db);
            }
        });
        return list;
    }

    private void restoreSavedHotbarIfNeeded() {
        if (this.mc == null || this.mc.player == null) {
            this.savedHotbarSlot = -1;
            this.pendingSlotRestore = false;
            return;
        }
        if (this.savedHotbarSlot != -1 && this.mc.player.getInventory().selected != this.savedHotbarSlot) {
            this.mc.player.getInventory().selected = this.savedHotbarSlot;
            this.sendCarriedItem();
        }
        this.savedHotbarSlot = -1;
        this.pendingSlotRestore = false;
    }

    private void preselectPlacementSlot() {
        if (this.mc.player == null) {
            return;
        }
        int slot = this.activePlacementType == PlacementType.LAVA ? this.findLavaBucketSlot() : this.findCobwebSlot();
        int n = slot;
        if (slot == -1 || slot > 8) {
            return;
        }
        int current = this.mc.player.getInventory().selected;
        if (current == slot) {
            return;
        }
        if (this.savedHotbarSlot == -1) {
            this.savedHotbarSlot = current;
        }
        this.mc.player.getInventory().selected = slot;
        this.sendCarriedItem();
        this.pendingSlotRestore = false;
    }

    private Optional<PlacementTarget> findWebExitPlacement(Entity entity, int cobwebSlot) {
        if (cobwebSlot == -1) {
            return Optional.empty();
        }
        Vec3 movement = this.getEntityMovement(entity);
        if (movement.x * movement.x + movement.z * movement.z < 4.0E-4) {
            return Optional.empty();
        }
        for (int tickAhead = 2; tickAhead <= 3; ++tickAhead) {
            Optional<PlacementTarget> placement;
            AABB predicted = entity.getBoundingBox().move(movement.x * (double)tickAhead, 0.0, movement.z * (double)tickAhead);
            if (this.isAllCobweb(predicted) || !(placement = this.tryPlacements(entity, this.getPositionsForAabb(predicted, tickAhead), movement)).isPresent()) continue;
            return placement;
        }
        return Optional.empty();
    }

    private boolean advanceGroundWebBreak() {
        if (this.currentGroundWebData == null) {
            this.reset();
            return false;
        }
        switch (this.currentGroundWebPhase) {
            case FOOT_PLACED: {
                this.currentGroundWebPhase = GroundWebPhase.HEAD_PLACED;
                this.applyPlacement(this.currentGroundWebData.headPlacement, PlacementType.GROUND_WEB);
                return true;
            }
            case HEAD_PLACED: {
                this.startGroundWebBreak();
                return true;
            }
        }
        this.reset();
        return false;
    }

    private PlacementInfo createPlacementInfo(BlockPos supportPos, BlockPos placePos) {
        Vec3 aim = new Vec3((double)supportPos.getX() + 0.5, (double)supportPos.getY() + 1.0, (double)supportPos.getZ() + 0.5);
        return new PlacementInfo(supportPos, Direction.UP, aim, placePos);
    }

    private Optional<PlacementInfo> findLavaGroundPlacement(double x, double y, double z, String label) {
        Vec3 start = new Vec3(x, y + 0.2, z);
        Vec3 end = new Vec3(x, y - 2.2, z);
        BlockHitResult hit = this.mc.level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, (Entity)this.mc.player));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return Optional.empty();
        }
        if (hit.getDirection() != Direction.UP) {
            this.debugLog("Lava fail: " + label + " ground probe face not up face=" + hit.getDirection() + " support=" + this.formatBlockPos(hit.getBlockPos()) + " foot=" + this.formatVec3(new Vec3(x, y, z)));
            return Optional.empty();
        }
        return this.findAimAtSupport(hit.getBlockPos(), x, z, label);
    }

    private AngleVector currentTargetRotation() {
        return new AngleVector(targetYaw, targetPitch);
    }

    private Optional<PlacementInfo> findLavaPlacementAt(Entity entity, BlockPos place) {
        if (!this.mc.level.getBlockState(place).canBeReplaced()) {
            return Optional.empty();
        }
        if (this.intersectsPlayer(place)) {
            return Optional.empty();
        }
        Vec3 eye = this.mc.player.getEyePosition();
        double dx = entity.getX() - eye.x;
        double dz = entity.getZ() - eye.z;
        Direction nearest = Direction.getNearest((float)((float)dx), (float)0.0f, (float)((float)dz));
        ArrayList<Direction> directions = new ArrayList<Direction>();
        directions.add(nearest);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (direction == nearest) continue;
            directions.add(direction);
        }
        directions.add(Direction.DOWN);
        directions.add(Direction.UP);
        for (Direction direction : directions) {
            Direction face;
            BlockPos supportPos = place.relative(direction);
            Optional<Vec3> hit = this.findFaceHitPoint(supportPos, face = direction.getOpposite());
            if (hit.isEmpty()) continue;
            return Optional.of(new PlacementInfo(supportPos, face, hit.get(), place));
        }
        return Optional.empty();
    }

    private void placementTimerReset() {
        this.placementTimerTicks = 0;
    }

    private Optional<PlacementTarget> findPlacementTarget() {
        if (this.mc.level == null || this.mc.player == null) {
            return Optional.empty();
        }
        float range = ((Float)this.rangeSetting.get()).floatValue();
        ArrayList<Entity> candidates = new ArrayList<Entity>();
        for (Entity e : this.mc.level.entitiesForRendering()) {
            if (!(e instanceof Player) || e == this.mc.player || e.distanceTo((Entity)this.mc.player) > range || !this.isValidPlayerTarget(e)) continue;
            candidates.add(e);
        }
        candidates.sort(new Comparator<Entity>(){

            @Override
            public int compare(Entity a, Entity b) {
                return Double.compare(a.distanceTo((Entity)AutowebModule.this.mc.player), b.distanceTo((Entity)AutowebModule.this.mc.player));
            }
        });
        int cobwebSlot = this.findCobwebSlot();
        int lavaSlot = (Boolean)this.lavaSetting.get() != false ? this.findLavaBucketSlot() : -1;
        for (Entity entity : candidates) {
            Optional<PlacementInfo> lavaPlace;
            Optional<GroundWebData> groundWeb;
            boolean inCobweb = this.isEntityInCobweb(entity);
            if (inCobweb) {
                Optional<PlacementTarget> webExit = this.findWebExitPlacement(entity, cobwebSlot);
                if (webExit.isPresent()) {
                    return webExit;
                }
                this.debugLog("Skip cobweb because target stays in cobweb: " + entity.getName().getString());
            }
            if (!inCobweb && ((Boolean)this.groundWebSetting.get()).booleanValue() && cobwebSlot != -1 && (groundWeb = this.findGroundWebPlacement(entity)).isPresent()) {
                GroundWebData data = groundWeb.get();
                this.debugLog("Ground web target=" + entity.getName().getString() + " foot=" + this.formatBlockPos(data.footPos) + " head=" + this.formatBlockPos(data.headPos));
                return Optional.of(new PlacementTarget(entity, data.footPlacement, PlacementType.GROUND_WEB, data));
            }
            Optional cobwebPlace = !inCobweb && cobwebSlot != -1 ? this.findLavaPlacement(entity) : Optional.empty();
            Optional optional = cobwebPlace;
            if (cobwebPlace.isPresent()) {
                PlacementInfo info = (PlacementInfo)cobwebPlace.get();
                this.debugLog("Cobweb target=" + entity.getName().getString() + " place=" + this.formatBlockPos(info.place) + " support=" + this.formatBlockPos(info.support) + " face=" + info.face + " aim=" + this.formatVec3(info.aim));
                return Optional.of(new PlacementTarget(entity, info, PlacementType.COBWEB, null));
            }
            if (lavaSlot == -1 || !this.shouldPlaceLavaEx(entity, inCobweb) || !(lavaPlace = this.findLavaSourcePlacement(entity)).isPresent()) continue;
            PlacementInfo info = lavaPlace.get();
            this.debugLog("Lava target=" + entity.getName().getString() + " place=" + this.formatBlockPos(info.place) + " support=" + this.formatBlockPos(info.support) + " face=" + info.face + " aim=" + this.formatVec3(info.aim));
            return Optional.of(new PlacementTarget(entity, info, PlacementType.LAVA, null));
        }
        return Optional.empty();
    }

    private void startGroundWebBreak() {
        if (this.findSwordSlot() == -1) {
            this.debugLog("Ground web break skipped: no sword");
            this.reset();
            return;
        }
        this.currentPlacement = null;
        this.activePlacementType = null;
        this.targetBlockPos = this.groundWebFootPos;
        AngleVector breakRot = AutowebModule.exactRotation(this.mc.player.getEyePosition(), Vec3.atCenterOf((Vec3i)this.groundWebFootPos));
        this.setTargetRotation(breakRot);
        this.groundWebBreakTicks = 0;
        this.groundWebBreakStarted = false;
        this.placementPhase = Phase.GROUND_WEB_BREAK;
    }

    private Optional<PlacementInfo> findLavaSourcePlacement(Entity entity) {
        Vec3 foot = new Vec3(entity.getX(), entity.getY(), entity.getZ());
        Optional<PlacementInfo> placement = this.findLavaGroundPlacement(entity.getX(), entity.getY(), entity.getZ(), "current");
        if (placement.isPresent()) {
            return placement;
        }
        this.debugLog("Lava fail: no lava placement foot=" + this.formatVec3(foot) + " motion=" + this.formatVec3(entity.getDeltaMovement()));
        return Optional.empty();
    }

    private int findEmptyBucketSlot() {
        for (int slot = 0; slot < 9; ++slot) {
            ItemStack stack = this.mc.player.getInventory().getItem(slot);
            if (stack.isEmpty() || stack.getItem() != Items.BUCKET) continue;
            return slot;
        }
        return -1;
    }

    private BlockPos findNearbyLavaSource(BlockPos center) {
        if (this.isLavaSource(center)) {
            return center;
        }
        BlockPos best = null;
        double bestDistanceSq = Double.POSITIVE_INFINITY;
        for (int x = -1; x <= 1; ++x) {
            for (int y = -1; y <= 1; ++y) {
                for (int z = -1; z <= 1; ++z) {
                    double distanceSq;
                    BlockPos pos = center.offset(x, y, z);
                    if (!this.isLavaSource(pos) || !((distanceSq = Vec3.atCenterOf((Vec3i)center).distanceToSqr(Vec3.atCenterOf((Vec3i)pos))) < bestDistanceSq)) continue;
                    best = pos;
                    bestDistanceSq = distanceSq;
                }
            }
        }
        return best;
    }

    private Optional<PlacementInfo> findPlacementForTick(Entity entity, PredictionTick tick) {
        if (this.mc.level.getBlockState(tick.headPos).is(Blocks.COBWEB)) {
            return Optional.empty();
        }
        return this.findLavaPlacementAt(entity, tick.headPos);
    }

    @Override
    public void onTick() {
    }

    @Override
    public void onPreTick() {
        Minecraft mc;
        this.mc = mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.gameMode == null) {
            return;
        }
        ScaffoldModule scaffold = ModuleManager.getModule(ScaffoldModule.class);
        if (scaffold != null && scaffold.enabled) {
            this.reset(true);
            return;
        }
        if (this.placementTimerTicks < 1000000) {
            ++this.placementTimerTicks;
        }
        if (this.pendingSlotRestore) {
            this.restoreSavedHotbarIfNeeded();
        }
        if (this.keepRotationTicks > 0) {
            --this.keepRotationTicks;
            if (this.keepRotationTicks == 0 && this.placementPhase == Phase.IDLE) {
                this.clearTargetRotation();
            }
        }
        if (!this.isPhaseActive() && this.isRotationBlocked()) {
            this.reset();
            return;
        }
        switch (this.placementPhase) {
            case LAVA_RETRIEVE: {
                this.doLavaRetrieve();
                break;
            }
            case GROUND_WEB_BREAK: {
                this.doGroundWebBreak();
                break;
            }
            case IDLE: {
                this.tickSearch();
                break;
            }
            case PLACE: {
                this.tickPlace();
            }
        }
    }

    @Override
    public void onRender3D() {
    }

    public enum Phase {
        IDLE, PLACE, LAVA_RETRIEVE, GROUND_WEB_BREAK;
    }

    public enum GroundWebPhase {
        IDLE, FOOT_PLACED, HEAD_PLACED;
    }

    private static final class PlacementInfo {
        final BlockPos support;
        final Direction face;
        final Vec3 aim;
        final BlockPos place;

        PlacementInfo(BlockPos support, Direction face, Vec3 aim, BlockPos place) {
            this.support = support;
            this.face = face;
            this.aim = aim;
            this.place = place;
        }
    }

    public enum PlacementType {
        COBWEB, WEB_EXIT_PREDICT, GROUND_WEB, LAVA;
    }

    private static final class GroundWebData {
        final BlockPos footPos;
        final BlockPos headPos;
        final PlacementInfo footPlacement;
        final PlacementInfo headPlacement;

        GroundWebData(BlockPos footPos, BlockPos headPos, PlacementInfo footPlacement, PlacementInfo headPlacement) {
            this.footPos = footPos;
            this.headPos = headPos;
            this.footPlacement = footPlacement;
            this.headPlacement = headPlacement;
        }
    }

    private static final class PlacementTarget {
        final Entity target;
        final PlacementInfo placement;
        final PlacementType type;
        final GroundWebData groundWebData;

        PlacementTarget(Entity target, PlacementInfo placement, PlacementType type, GroundWebData groundWebData) {
            this.target = target;
            this.placement = placement;
            this.type = type;
            this.groundWebData = groundWebData;
        }
    }

    private static final class AngleVector {
        final float yaw;
        final float pitch;

        AngleVector(float yaw, float pitch) {
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }

    private static final class PredictionTick {
        final int ticks;
        final BlockPos footPos;
        final BlockPos headPos;

        PredictionTick(int ticks, BlockPos footPos, BlockPos headPos) {
            this.ticks = ticks;
            this.footPos = footPos;
            this.headPos = headPos;
        }

        public boolean equals(Object o) {
            if (!(o instanceof PredictionTick)) {
                return false;
            }
            PredictionTick other = (PredictionTick)o;
            return this.ticks == other.ticks && this.footPos.equals((Object)other.footPos) && this.headPos.equals((Object)other.headPos);
        }

        public int hashCode() {
            return this.footPos.hashCode() * 31 + this.headPos.hashCode() + this.ticks;
        }
    }
}

