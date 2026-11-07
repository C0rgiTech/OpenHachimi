/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.hud.Notifications;
import dev.openhachimi.settings.BooleanSetting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;

public class AntistaffModule
extends Module {
    private static final byte[] S_EDBAF86 = new byte[]{42, 39, 59, 50, 43, 58, 39, 46, 120, 49, 112, 33, 39, 52, 39, 44, 54};  // XOR0x42 "heypixel:s2cevent"
    private static final byte[] S_7868FC7E = new byte[]{3, 44, 54, 43, 17, 54, 35, 36, 36};  // XOR0x42 "AntiStaff"
    private static final byte[] S_515F423A = new byte[]{3, 55, 54, 45, 14, 39, 35, 52, 39, 5, 35, 47, 39};  // XOR0x42 "AutoLeaveGame"
    private static final byte[] S_6294C1B2 = new byte[]{50, 46, 35, 59, 39, 48, 49};  // XOR0x42 "players"
    private static final byte[] S_2EEFAA = new byte[]{38, 35, 54, 35};  // XOR0x42 "data"
    private static final byte[] S_337A8B = new byte[]{44, 35, 47, 39};  // XOR0x42 "name"
    private static final byte[] S_2734AEA8 = new byte[]{17, 54, 35, 36, 36, 98, 6, 39, 54, 39, 33, 54, 39, 38, 120, 98};  // XOR0x42 "Staff Detected: "
    private static final byte[] S_194F5 = new byte[]{42, 55, 32};  // XOR0x42 "hub"
    private static final byte[] S_7B = new byte[]{57};
    private static final byte[] S_5B = new byte[]{25};
    private static final byte[] S_B4C55 = new byte[]{(byte)167, (byte)228, (byte)212, (byte)165, (byte)206, (byte)233};
    private static final byte[] S_22A0A656 = new byte[]{3, 44, 38, 48, 39, 53, 41, 48, 43, 49, 54};  // XOR0x42 "Andrewkrist"
    private static final byte[] S_1EFD0F = new byte[]{0, 3, 1, 49};  // XOR0x42 "BACs"
    private static final byte[] S_1DC439B5 = new byte[]{1, 35, 44, 38, 59, 3, 50, 45, 49, 54, 46, 39};  // XOR0x42 "CandyApostle"
    private static final byte[] S_2D257CA3 = new byte[]{33, 42, 55, 44, 59, 43, 115};  // XOR0x42 "chunyi1"
    private static final byte[] S_3F7CEAA2 = new byte[]{1, 55, 54, 39, 26, 43, 35, 45, 27, 27};  // XOR0x42 "CuteXiaoYY"
    private static final byte[] S_725F0F78 = new byte[]{6, 35, 52, 43, 38, 49, 55, 44};  // XOR0x42 "Davidsun"
    private static final byte[] S_580696C8 = new byte[]{(byte)167, (byte)205, (byte)215, (byte)167, (byte)205, (byte)225, (byte)166, (byte)249, (byte)219, (byte)164, (byte)242, (byte)214, (byte)167, (byte)242, (byte)243, (byte)171, (byte)225, (byte)218, (byte)171, (byte)225, (byte)218};
    private static final byte[] S_252999EB = new byte[]{(byte)166, (byte)250, (byte)222, (byte)164, (byte)247, (byte)245, (byte)164, (byte)220, (byte)213, (byte)165, (byte)216, (byte)197};
    private static final byte[] S_3C59143C = new byte[]{(byte)164, (byte)200, (byte)212, (byte)171, (byte)221, (byte)241, 29, 35, 53, 35, (byte)171, (byte)235, (byte)238, (byte)167, (byte)204, (byte)221};
    private static final byte[] S_2FAE4520 = new byte[]{(byte)164, (byte)212, (byte)213, (byte)164, (byte)202, (byte)218, (byte)170, (byte)193, (byte)222, (byte)166, (byte)255, (byte)217};
    private static final byte[] S_311D075B = new byte[]{(byte)164, (byte)220, (byte)233, (byte)170, (byte)210, (byte)229, (byte)164, (byte)220, (byte)213, (byte)165, (byte)198, (byte)244};
    private static final byte[] S_21681B = new byte[]{4, 43, 35, 123};  // XOR0x42 "Fia9"
    private static final byte[] S_7C2C883A = new byte[]{4, 48, 55, 43, 54, 29, 1, 35, 44, 38, 59};  // XOR0x42 "Fruit_Candy"
    private static final byte[] S_2C3B3671 = new byte[]{(byte)167, (byte)251, (byte)253, (byte)167, (byte)211, (byte)200, (byte)167, (byte)210, (byte)217, 3};
    private static final byte[] S_A99A1 = new byte[]{(byte)167, (byte)208, (byte)235, (byte)167, (byte)230, (byte)232};
    private static final byte[] S_1358AF32 = new byte[]{10, 39, 44, 37, 46, 43, 39};  // XOR0x42 "Henglie"
    private static final byte[] S_646C512A = new byte[]{14, 35, 47, 15, 45, 46, 43, 44, 33, 39, 44, 29};  // XOR0x42 "LamMolincen_"
    private static final byte[] S_60633D8D = new byte[]{8, 35, 59, 29, 29, 1, 42, 45, 55};  // XOR0x42 "Jay__Chou"
    private static final byte[] S_57DCF6B8 = new byte[]{9, 43, 35, 44, 35, 29, 29, 112, 118};  // XOR0x42 "Kiana__24"
    private static final byte[] S_C9520D9 = new byte[]{(byte)164, (byte)220, (byte)215, (byte)166, (byte)250, (byte)200, (byte)166, (byte)251, (byte)228, (byte)166, (byte)250, (byte)244, (byte)167, (byte)227, (byte)211, (byte)164, (byte)222, (byte)217, (byte)164, (byte)222, (byte)202};
    private static final byte[] S_16A9EAE4 = new byte[]{(byte)164, (byte)247, (byte)195, (byte)167, (byte)255, (byte)243, (byte)167, (byte)205, (byte)232, (byte)166, (byte)254, (byte)216, (byte)167, (byte)218, (byte)230, (byte)167, (byte)218, (byte)230, (byte)167, (byte)218, (byte)230};
    private static final byte[] S_1EE112F = new byte[]{(byte)165, (byte)249, (byte)253, (byte)170, (byte)243, (byte)196, (byte)167, (byte)231, (byte)244};
    private static final byte[] S_7FB055A9 = new byte[]{15, 35, 37, 43, 33, 43, 35, 44};  // XOR0x42 "Magician"
    private static final byte[] S_3D90F3 = new byte[]{47, 33, (byte)167, (byte)200, (byte)226, (byte)170, (byte)255, (byte)255};
    private static final byte[] S_25522056 = new byte[]{15, 44, 35, 47, 14, 39, 45, 29};  // XOR0x42 "MnamLeo_"
    private static final byte[] S_1DD31A73 = new byte[]{9, 55, 48, 43, 29, 22, 55, 48, 37, 55, 54};  // XOR0x42 "Kuri_Turgut"
    private static final byte[] S_25CEEF5F = new byte[]{(byte)166, (byte)250, (byte)193, (byte)171, (byte)251, (byte)230, (byte)161, (byte)195, (byte)215, (byte)161, (byte)192, (byte)209};
    private static final byte[] S_49D34F98 = new byte[]{19, 43, 43, 43, 43, 43, 43, 43};  // XOR0x42 "Qiiiiiii"
    private static final byte[] S_727551B7 = new byte[]{51, 58, 54, 47, 46, 33, 123, 123};  // XOR0x42 "qxtmlc99"
    private static final byte[] S_51C3325 = new byte[]{(byte)167, (byte)222, (byte)225, (byte)166, (byte)250, (byte)200, (byte)170, (byte)207, (byte)225, (byte)167, (byte)199, (byte)203, 112, 113, 113};
    private static final byte[] S_387D741F = new byte[]{(byte)165, (byte)231, (byte)220, (byte)167, (byte)223, (byte)211, (byte)166, (byte)251, (byte)201, (byte)171, (byte)194, (byte)213};
    private static final byte[] S_1C053FC = new byte[]{(byte)171, (byte)222, (byte)222, (byte)164, (byte)222, (byte)202, (byte)164, (byte)222, (byte)202, 13, 13};
    private static final byte[] S_13BE9C2F = new byte[]{17, 41, 59, 36, 45, 59};  // XOR0x42 "Skyfoy"
    private static final byte[] S_6DA14A01 = new byte[]{17, 54, 39, 46, 46, 35, 48, 33, 35, 54};  // XOR0x42 "Stellarcat"
    private static final byte[] S_6828B53B = new byte[]{(byte)167, (byte)194, (byte)218, (byte)170, (byte)201, (byte)231, (byte)166, (byte)250, (byte)230, (byte)164, (byte)215, (byte)225, (byte)166, (byte)250, (byte)207, (byte)167, (byte)228, (byte)192, (byte)166, (byte)250, (byte)230, (byte)171, (byte)216, (byte)252};
    private static final byte[] S_35074CF7 = new byte[]{22, 39, 47, 50, 15, 45, 44, 45};  // XOR0x42 "TempMono"
    private static final byte[] S_307DE31A = new byte[]{(byte)164, (byte)213, (byte)226, (byte)171, (byte)197, (byte)205, (byte)167, (byte)221, (byte)221, (byte)171, (byte)217, (byte)232};
    private static final byte[] S_B78FB = new byte[]{(byte)167, (byte)242, (byte)205, (byte)167, (byte)206, (byte)232};
    private static final byte[] S_B2619 = new byte[]{(byte)167, (byte)242, (byte)205, 10};
    private static final byte[] S_5F7C8F6E = new byte[]{(byte)167, (byte)242, (byte)205, (byte)167, (byte)196, (byte)217, (byte)167, (byte)210, (byte)217, (byte)166, (byte)250, (byte)244, (byte)167, (byte)230, (byte)235, (byte)166, (byte)255, (byte)253, (byte)166, (byte)251, (byte)201, (byte)165, (byte)253, (byte)254};
    private static final byte[] S_730C40DB = new byte[]{58, 18, 43, 48, 118, 54, 39, 29};  // XOR0x42 "xPir4te_"
    private static final byte[] S_10BF60 = new byte[]{(byte)170, (byte)219, (byte)216, (byte)165, (byte)235, (byte)248};
    private static final byte[] S_31F35085 = new byte[]{(byte)164, (byte)220, (byte)215, (byte)166, (byte)250, (byte)200, (byte)166, (byte)251, (byte)228, (byte)166, (byte)250, (byte)244, (byte)167, (byte)192, (byte)240, (byte)167, (byte)237, (byte)208};
    private static final byte[] S_31FBCE97 = new byte[]{(byte)164, (byte)220, (byte)215, (byte)166, (byte)250, (byte)200, (byte)166, (byte)251, (byte)228, (byte)166, (byte)250, (byte)244, (byte)171, (byte)217, (byte)232, (byte)167, (byte)230, (byte)222};
    private static final byte[] S_12A83B6F = new byte[]{(byte)164, (byte)220, (byte)215, (byte)166, (byte)250, (byte)200, (byte)166, (byte)251, (byte)228, (byte)166, (byte)250, (byte)244, (byte)166, (byte)250, (byte)239, (byte)166, (byte)248, (byte)206, (byte)167, (byte)242, (byte)211, (byte)167, (byte)251, (byte)246, 6, 14};
    private static final byte[] S_2F69605E = new byte[]{(byte)165, (byte)249, (byte)253, (byte)170, (byte)243, (byte)196, (byte)166, (byte)251, (byte)193, 17, 3, 15, 3};
    private static final byte[] S_516D9EDE = new byte[]{44, 43, 37, 42, 54, 32, 35, 48, 59};  // XOR0x42 "nightbary"
    private static final byte[] S_6282E9A9 = new byte[]{(byte)166, (byte)255, (byte)209, (byte)170, (byte)246, (byte)246, (byte)165, (byte)216, (byte)198, (byte)165, (byte)192, (byte)254, (byte)171, (byte)197, (byte)211, (byte)164, (byte)222, (byte)237, (byte)171, (byte)217, (byte)194};
    private static final byte[] S_722346DE = new byte[]{17, 54, 35, 48, 12, 13, 115};  // XOR0x42 "StarNO1"
    private static final byte[] S_164E0A4 = new byte[]{(byte)167, (byte)242, (byte)205, (byte)167, (byte)228, (byte)212, (byte)165, (byte)206, (byte)233};
    private static final byte[] S_6C146D35 = new byte[]{(byte)167, (byte)228, (byte)212, (byte)165, (byte)206, (byte)233, (byte)165, (byte)216, (byte)198, 18, 1, (byte)167, (byte)205, (byte)245};
    private static final byte[] S_3FBF715F = new byte[]{(byte)167, (byte)242, (byte)205, 10, (byte)166, (byte)253, (byte)236, 32, 55, 37};
    private static final byte[] S_6AAF31EA = new byte[]{58, 43, 35, 45, 54, 55, 36, 39, 43};  // XOR0x42 "xiaotufei"
    private static final byte[] S_A32D2 = new byte[]{(byte)167, (byte)199, (byte)193, (byte)167, (byte)236, (byte)247};
    private static final byte[] S_ACD99F5 = new byte[]{1, 55, 54, 39, 5, 43, 48, 46, 19, 46, 19, 46};  // XOR0x42 "CuteGirlQlQl"
    private static final byte[] S_C08CB = new byte[]{(byte)167, (byte)255, (byte)235, (byte)165, (byte)238, (byte)214};
    private static final byte[] S_3873740D = new byte[]{(byte)167, (byte)250, (byte)193, (byte)167, (byte)210, (byte)203, (byte)167, (byte)240, (byte)217, (byte)164, (byte)203, (byte)209, (byte)167, (byte)245, (byte)231, (byte)166, (byte)249, (byte)214};
    private static final byte[] S_6EBBCE1B = new byte[]{(byte)167, (byte)199, (byte)193, (byte)167, (byte)236, (byte)247, (byte)165, (byte)216, (byte)198, (byte)164, (byte)247, (byte)201, (byte)170, (byte)237, (byte)215, (byte)167, (byte)205, (byte)245};
    private static final byte[] S_308EF606 = new byte[]{(byte)164, (byte)200, (byte)211, (byte)171, (byte)193, (byte)195, (byte)165, (byte)216, (byte)198, (byte)167, (byte)199, (byte)193, (byte)167, (byte)236, (byte)247};
    private static final byte[] S_249A2C34 = new byte[]{(byte)167, (byte)199, (byte)193, (byte)167, (byte)236, (byte)247, (byte)165, (byte)223, (byte)227, (byte)166, (byte)250, (byte)207, (byte)171, (byte)196, (byte)208};
    private static final byte[] S_3384B874 = new byte[]{(byte)164, (byte)200, (byte)212, (byte)171, (byte)221, (byte)241, (byte)166, (byte)250, (byte)244, (byte)167, (byte)242, (byte)205, (byte)167, (byte)206, (byte)232};
    private static final byte[] S_1A11696E = new byte[]{(byte)165, (byte)249, (byte)193, (byte)166, (byte)251, (byte)228, (byte)164, (byte)241, (byte)215, (byte)165, (byte)216, (byte)198, (byte)170, (byte)201, (byte)228, (byte)167, (byte)200, (byte)217, (byte)164, (byte)194, (byte)215};
    private static final byte[] S_2748BEF5 = new byte[]{9, 43, 9, 43, 3, 47, 35, 44};  // XOR0x42 "KiKiAman"
    private static final byte[] S_1B5E9 = new byte[]{21, 17, (byte)164, (byte)215, (byte)199};
    private static final byte[] S_7874F660 = new byte[]{(byte)167, (byte)255, (byte)235, (byte)165, (byte)238, (byte)214, 51, 53, 51};
    private static final byte[] S_4222D57 = new byte[]{(byte)165, (byte)236, (byte)227, (byte)165, (byte)210, (byte)196, (byte)167, (byte)211, (byte)218, 111, 115};
    private static final byte[] S_4222D58 = new byte[]{(byte)165, (byte)236, (byte)227, (byte)165, (byte)210, (byte)196, (byte)167, (byte)211, (byte)218, 111, 112};
    private static final byte[] S_4222D59 = new byte[]{(byte)165, (byte)236, (byte)227, (byte)165, (byte)210, (byte)196, (byte)167, (byte)211, (byte)218, 111, 113};
    private static final byte[] S_4222D5A = new byte[]{(byte)165, (byte)236, (byte)227, (byte)165, (byte)210, (byte)196, (byte)167, (byte)211, (byte)218, 111, 118};
    private static final byte[] S_4222D5B = new byte[]{(byte)165, (byte)236, (byte)227, (byte)165, (byte)210, (byte)196, (byte)167, (byte)211, (byte)218, 111, 119};
    private static final byte[] S_4222D5C = new byte[]{(byte)165, (byte)236, (byte)227, (byte)165, (byte)210, (byte)196, (byte)167, (byte)211, (byte)218, 111, 116};
    private static final byte[] S_4222D5D = new byte[]{(byte)165, (byte)236, (byte)227, (byte)165, (byte)210, (byte)196, (byte)167, (byte)211, (byte)218, 111, 117};
    private static final byte[] S_4222D5E = new byte[]{(byte)165, (byte)236, (byte)227, (byte)165, (byte)210, (byte)196, (byte)167, (byte)211, (byte)218, 111, 122};
    private static final byte[] S_4222D5F = new byte[]{(byte)165, (byte)236, (byte)227, (byte)165, (byte)210, (byte)196, (byte)167, (byte)211, (byte)218, 111, 123};
    private static final byte[] S_237DB9 = new byte[]{(byte)165, (byte)236, (byte)227, (byte)165, (byte)210, (byte)196, (byte)167, (byte)211, (byte)218, 111, 115, 114};
    private static final byte[] S_B1B76 = new byte[]{(byte)167, (byte)230, (byte)235, (byte)166, (byte)255, (byte)253};
    private static final byte[] S_1F93B28 = new byte[]{(byte)170, (byte)203, (byte)252, (byte)165, (byte)243, (byte)241, (byte)166, (byte)250, (byte)255};
    private static final byte[] S_4E99EFFF = new byte[]{(byte)167, (byte)205, (byte)237, (byte)164, (byte)237, (byte)214, (byte)166, (byte)250, (byte)207, (byte)164, (byte)223, (byte)231, (byte)167, (byte)213, (byte)237, (byte)167, (byte)253, (byte)211};
    private static final byte[] S_4916E543 = new byte[]{(byte)171, (byte)250, (byte)227, (byte)166, (byte)255, (byte)226, (byte)167, (byte)230, (byte)232, (byte)165, (byte)252, (byte)204};
    private static final byte[] S_1D18AC8 = new byte[]{(byte)165, (byte)231, (byte)220, (byte)166, (byte)254, (byte)228, (byte)167, (byte)239, (byte)210};
    private static final byte[] S_24E4577 = new byte[]{(byte)171, (byte)235, (byte)238, (byte)167, (byte)209, (byte)231, (byte)166, (byte)251, (byte)210};
    private static final byte[] S_1A58750E = new byte[]{(byte)167, (byte)222, (byte)225, (byte)166, (byte)250, (byte)200, (byte)170, (byte)207, (byte)225, (byte)170, (byte)194, (byte)194, 112, 113, 113};
    private static final byte[] S_A92D9 = new byte[]{(byte)167, (byte)211, (byte)243, (byte)167, (byte)230, (byte)232};
    private static final byte[] S_2BEC5FA2 = new byte[]{(byte)167, (byte)245, (byte)229, (byte)167, (byte)199, (byte)201, (byte)167, (byte)250, (byte)193, (byte)166, (byte)250, (byte)195};
    private static final byte[] S_67199DAF = new byte[]{(byte)167, (byte)205, (byte)254, (byte)167, (byte)205, (byte)225, (byte)166, (byte)249, (byte)219, (byte)164, (byte)242, (byte)214, (byte)167, (byte)242, (byte)243, (byte)171, (byte)225, (byte)218, (byte)171, (byte)225, (byte)218};
    private static final byte[] S_12FDFAC = new byte[]{(byte)166, (byte)250, (byte)203, (byte)167, (byte)217, (byte)255, (byte)164, (byte)223, (byte)194};
    private static final byte[] S_C935663 = new byte[]{(byte)164, (byte)220, (byte)215, (byte)166, (byte)250, (byte)200, (byte)166, (byte)251, (byte)228, (byte)166, (byte)250, (byte)244, (byte)167, (byte)227, (byte)211, (byte)167, (byte)227, (byte)211, (byte)164, (byte)222, (byte)202};
    private static final byte[] S_64E526CA = new byte[]{(byte)166, (byte)250, (byte)239, (byte)166, (byte)248, (byte)206, (byte)167, (byte)242, (byte)211, (byte)167, (byte)251, (byte)246, 6, 14};
    private static final byte[] S_113940 = new byte[]{47, 39, 44, 37, 33, 42, 39, 44, 113, 122, 122, 118};  // XOR0x42 "mengchen3884"
    private final BooleanSetting autoLeaveGame = new BooleanSetting("AutoLeaveGame", true);
    private long lastFlagTime = 0L;
    private static final List<String> heypixelStaff = Arrays.asList("妖猫", "Andrewkrist", "BACs", "CandyApostle", "chunyi1", "CuteXiaoYY", "Davidsun", "叕口仙气就飘飘", "东海林皇", "抖音_awa马原", "斗战胜佛", "枫萧林然", "Fia9", "Fruit_Candy", "广告君A", "咩太", "Henglie", "LamMolincen_", "Jay__Chou", "Kiana__24", "枕上书丶塑望月", "流影只会嘤嘤嘤", "绿豆奶", "Magician", "mc加载", "MnamLeo_", "Kuri_Turgut", "七鹤さん", "Qiiiiiii", "qxtmlc99", "圣上荣光233", "神坑之逗", "霜月月OO", "Skyfoy", "Stellarcat", "倘若两散不如两难", "TempMono", "无量域雪", "小匪", "小H", "小军君丶天使之翼", "xPir4te_", "虚空", "枕上书丶傲寒", "枕上书丶雪夜", "枕上书丶中二少年DL", "绿豆乃SAMA", "nightbary", "体贴的炼金术雀", "StarNO1", "小妖猫", "妖猫的PC号", "小H修bug", "xiaotufei", "元宵", "CuteGirlQlQl", "彩笔", "布吉岛打工仔", "元宵的测试号", "抑郁的元宵", "元宵睡不醒", "抖音丶小匪", "练书法的苦力怕", "KiKiAman", "WS故", "彩笔qwq", "管理员-1", "管理员-2", "管理员-3", "管理员-4", "管理员-5", "管理员-6", "管理员-7", "管理员-8", "管理员-9", "管理员-10", "天使", "艾米丽", "可比不来嗯忑", "鸡你太美", "神伦子", "马哥乐", "圣上荣耀233", "呱太", "巧克布丁", "叼口仙气就飘飘", "三国杀", "枕上书丶塑塑月", "中二少年DL", "mengchen3884");
    private final List<PayloadDecoder> decoders = Arrays.asList(new PayloadDecoder(){

        @Override
        public String decode(byte[] data) {
            return AntistaffModule.this.decodeLobby(data);
        }
    }, new PayloadDecoder(){

        @Override
        public String decode(byte[] data) {
            return AntistaffModule.this.decodeGame(data);
        }
    }, new PayloadDecoder(){

        @Override
        public String decode(byte[] data) {
            return AntistaffModule.this.decodeZlib(data);
        }
    }, new PayloadDecoder(){

        @Override
        public String decode(byte[] data) {
            return AntistaffModule.this.decodeJsonStart(data);
        }
    }, new PayloadDecoder(){

        @Override
        public String decode(byte[] data) {
            return AntistaffModule.this.decodeRaw(data);
        }
    });

    public AntistaffModule() {
        super("AntiStaff", Category.MISC);
        this.values.add(this.autoLeaveGame);
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

    public static boolean isStaff(String name) {
        if (name == null) {
            return false;
        }
        AntistaffModule module = ModuleManager.getModule(AntistaffModule.class);
        return module != null && module.enabled && heypixelStaff.contains(name);
    }

    private void onStaffDetected(String staffName) {
        long current = System.currentTimeMillis();
        if (current - this.lastFlagTime > 5000L) {
            Notifications.show("Staff Detected: " + staffName, Notifications.NotificationType.WARNING, 15000);
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.gameMode != null && ((Boolean)this.autoLeaveGame.get()).booleanValue()) {
                mc.player.connection.sendCommand("hub");
            }
            this.lastFlagTime = current;
        }
    }

    private void checkJsonForStaff(JsonObject root, String key) {
        if (!root.has(key) || !root.get(key).isJsonObject()) {
            return;
        }
        JsonObject container = root.getAsJsonObject(key);
        for (Map.Entry entry : container.entrySet()) {
            JsonObject playerObj = ((JsonElement)entry.getValue()).getAsJsonObject();
            String name = playerObj.has("name") ? playerObj.get("name").getAsString() : null;
            if (name == null || !heypixelStaff.contains(name)) continue;
            this.onStaffDetected(name);
        }
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
            JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();
            this.checkJsonForStaff(json, "players");
            this.checkJsonForStaff(json, "data");
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
            String name;
            if (info.getProfile() == null || (name = info.getProfile().getName()) == null || !heypixelStaff.contains(name)) continue;
            this.onStaffDetected(name);
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

    private static interface PayloadDecoder {
        public String decode(byte[] var1);
    }
}

