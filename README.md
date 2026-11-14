# OpenHachimi


## 笑点解析

此“300[中国元](https://zh.wikipedia.org/zh-hans/%E4%BA%BA%E6%B0%91%E5%B8%81)可绕Native”大端并没有像我初步猜测的使用了 [ZelixKlassMaster™](https://zelix.com) 模糊器，而是靠一个狗屎级的障眼法躲过了人工dump：
把所有[类](https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html) rename后打包进 `net.minecraftforge.common.internal.util` 来躲过 dump，经过[Claude Opus5.5](https://claude.ai)长达30[秒]("https://zh.wikipedia.org/zh-hans/%E7%A7%92")的分析后找到了所被狗屎障眼法隐藏的[类](https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html)， 
不知道此客户端作者是否[脑溢血](https://zh.wikipedia.org/wiki/%E8%85%A6%E5%87%BA%E8%A1%80)又或者是客户端的编码者本身的[脑](https://zh.wikipedia.org/wiki/%E8%84%91)部结构遭到损坏导致[智力](https://zh.wikipedia.org/wiki/%E6%99%BA%E5%8A%9B)低下以至于使用一个会回退到 [Java](https://zh.wikipedia.org/wiki/Java) 层 XOR 加密的[Native](https://en.wikipedia.org/wiki/Java_Native_Interface)壳，使他的[Native](https://en.wikipedia.org/wiki/Java_Native_Interface)字符串解密成了[棍母](https://zh.wikipedia.org/wiki/%E5%90%89%E5%90%89%E5%9B%BD%E6%96%87%E5%8C%96)，令人[忍俊不禁](https://baike.baidu.com/item/%E5%BF%8D%E9%9A%BD%E4%B8%8D%E7%A6%81/22455727)。在经过[溜溜梅](https://en.wikipedia.org/wiki/Large_language_model)以及
人工rename和修复编译错误后得到了这坨狗屎的原始代码。

**因为我认为在此客户端消耗太多时间不值得，所以部分原始码Rename的并不完整，部分类可能Rename是错误的，您可自行恢复或在本项目提交[PR](https://github.com/C0rgiTech/OpenHachimi/pulls)。**

此外，笔者发现这个狗屎客户端的脚手架模块计数器竟然他妈的有一千行！。笔者怀疑这个[类](https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html)里塞入了完整的[当个创世神渲染管线](https://deepwiki.com/dhui626/Minecraft-Renderer/2-rendering-pipeline)，也有可能是他的[溜溜梅](https://en.wikipedia.org/wiki/Large_language_model)在生成的时候陷入了某种自我重复的死循环导致[类](https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html)的长度和我的[阴茎](https://zh.wikipedia.org/wiki/%E9%98%B4%E8%8C%8E)一样长。这个客户端的[EventBus](https://docs.minecraftforge.net/en/latest/concepts/events/)也一样狗屎，令人忍俊不禁的是他居然使用了「module 父类 call 事件，然后子类 [@OverRide](https://docs.oracle.com/javase/tutorial/java/IandI/override.html)」这种大鸡巴方案。整个事件分发链路变成了一坨难以追踪的继承地狱。任何[智力](https://zh.wikipedia.org/wiki/%E6%99%BA%E5%8A%9B)正常的[锻造](https://github.com/MinecraftForge/MinecraftForge)模组开发者都会用正确的事件订阅方式，而不是搞这种父[类](https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html) call、子[类](https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html) [@OverRide](https://docs.oracle.com/javase/tutorial/java/IandI/override.html) 的脑溢血智障设计。这种方案的唯一优点是可以让后续维护者的两颗[睾丸](http://zh.wikipedia.org/wiki/%E7%9D%BE%E4%B8%B8)被吓掉。为了防止自己的客户端被开源或者[裂缝](https://zh.wikipedia.org/wiki/%E5%AF%86%E7%A0%81%E5%88%86%E6%9E%90)，我们的客户端作者加入了一个非常[婴幼儿](https://zh.wikipedia.org/wiki/%E5%AC%B0%E5%85%92)的反调试，它竟然会检测你的启动命令行里面是否有-javaagent，我操！。这个反调试简直太[鸡巴](https://zh.wikipedia.org/wiki/%E9%98%B4%E8%8C%8E)[高级](https://zh.wikipedia.org/wiki/%E5%90%8C%E6%80%A7%E6%81%8B)了，分析者肯定会被这样[高级](https://zh.wikipedia.org/wiki/%E5%90%8C%E6%80%A7%E6%81%8B)的设计吓掉大牙。

 **Hachimi** Minecraft 1.20.1 客户端本体（`hachimi_body.deobf.jar`，246 个类，从热注入 loader 中 dump 得到）的
反编译、去混淆并重新组织后的源码树。

> ⚠️ 仅用于安全研究 / 互操作分析 / 技术教育。本仓库含反编译的闭源代码及抄自 OpenZen 的第三方代码，
> **不授予任何开源许可**（`All Rights Reserved`）。详见 [DISCLAIMER.md](DISCLAIMER.md)。

- Java 17 / ForgeGradle 6 / Forge 1.20.1（`47.4.20`）
- 原始包名是伪装的 `net.minecraftforge.common.internal.util`（全部 246 个类）；
  现已全部迁移到 `dev.openhachimi.*`，按职责拆分。
- 每一处类改名都记录在 RENAME_MAP.csv 里，并附判定依据
  （`high` = 由 module 字符串/类职责证实，`mid` = 强信号，
  `low` / `unverified` = 保持原名）。

## 目录结构

```
src/main/java/dev/openhachimi/
 |-- HachimiClient.java    @Mod 入口（新增的胶水代码，见 ARCHITECTURE.md）
 |-- modules/  (49)   Module.java、Category.java，每个作弊模块一个文件
 |-- settings/ (9)  Setting<T> + Boolean/Mode/Color/Number/Enum/List 各类设置
 |-- event/    (1)   EventRegistrar —— 所有 MinecraftForge.EVENT_BUS 监听器
 |-- render/   (6)  ClickGui 界面、着色器管线、贴图、模板(stencil)、2D 绘制助手
 |-- hud/      (2)   RenderableHud 接口 + Notifications
 |-- utils/    (43)  其余 helper/manager（名字保留 —— 见 RENAME_MAP.csv）
 |-- security/ (3)  AntiDebug、IntegrityCheck、NativeLoader（hachimi.dll）
 |-- config/   (2)   ConfigManager（配置文件）、FontManager（ttf）
 `-- command/  (1)   CommandManager（.friend/.set/.theme/.save/...）
```

## 构建

离线可直接编译（用本机 Gradle 缓存里已解析好的 official 映射 Forge jar，不走网络）：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tools/build.ps1
# -> build/libs/openhachimi-1.0.0.jar   119 源文件 -> 247 个 class，0 错误
```

脚本流程：把依赖 jar 汇到 `build/cp/`（通配符 classpath，避开命令行长度限制）→ `javac` 编译
`src/main/java` → 拷贝 `src/main/resources` → 打包 `build/libs/openhachimi-1.0.0.jar`。

Gradle（首次需要联网让 ForgeGradle 解析依赖）：

```bash
./gradlew build        # ForgeGradle + Forge userdev 需要联网
./gradlew runClient    # 加载该 mod 的开发版客户端
```

`settings.gradle` 已不再拉取 Foojay toolchain 插件（离线不友好）；改用本地的 JDK 17 toolchain。

## 字符串

客户端里的每个字面量都是 `byte[]` 逐字节 XOR `0x42`。数组保留，每条声明后面加了解码后的明文注释：

```java
private static final byte[] S_4538EE81 = new byte[]{{17, 114, 122, 6, ...}};  // XOR0x42 "S08Detector"
```

## dump 下来的本体里不包含的东西

| 资源 | 说明 |
|---|---|
| `assets/hachimi/font/pingfang.ttf`、`bold.ttf` | 由 FontManager 读取；随 loader 发，不在 body jar 里 |
| `assets/hachimi/native/hachimi.dll` | 由 NativeLoader 释放到 java.io.tmpdir；提供 nDecode / guardCheck / hash64 |
| `@Mod` 引导 | 本体没有（由 loader 自行 patch 游戏）—— HachimiClient 是后加的 |

NativeLoader 已被改写为指向 `dev.openhachimi.render.Render2D` 和
`dev.openhachimi.security.IntegrityCheck`（连这两个名字的 XOR 编码副本也改了），所以原始 DLL 不会再认得
改名后的类。若需要走 native 路径，请重新 patch DLL 或还原这两个旧名字。

## 编译状态

**已编译通过**：119 个源文件 → 247 个 class，`tools/build.ps1` 退出码 0。

修掉的都是 CFR 反编译产物，分五类：丢局部变量声明（~30 处：`strW`/`chipW`/`chipH`/`valueY`/
`offHand`/`targetSlot`/`cap`/`i`）、伪枚举 `extends Enum<X>`（9 个，含带 GL 常量的 Format/Filter）、
声明未初始化（12 个，字符串常量取自 XOR 解码值）、丢泛型（6 处）、过时 SRG 方法名
（`f_133143_` → `id()`、`f_268504_` → `entityId()`）与 `Packet<?>#handle` 捕获错、`AbstractTexture#load`
未实现、文件名与 public 类名不一致（3 处）、CFR 丢掉的未使用 final 字段（4 个）。

没有任何一处是靠“注释掉/塞 return”糊过去的。逐条清单见 COMPILE_STATUS.md。

## 抄袭自 OpenZen（`shit.zen.utils`）

拿去混淆后的本体与 **OpenZen**（包根 `shit.zen`）做了对比。本体里的方法名/字段名
基本保留，因此比对方式是：方法名集合匹配（去掉一份 Minecraft/JDK 通用名黑名单）+ 方法体逐字抽查。
只能对比 **dump 时已加载的那 246 个类**。

**结论：Java util 层整体照搬自 OpenZen 的 `shit.zen.utils`**

### 被照搬的 util 类（本体中命中的"特有、非 MC"方法数）

| OpenZen `utils` 类 | 命中 | 照搬程度 |
|---|--:|---|
| game/ItemUtil | 41 | 近乎逐字 —— `getSwordDamage`/`getAxeDamage`/`getBestSword`/`getBestCrossbowScore`/`getCrossbowScore`/`getDigSpeed`/`isEndCrystal`/`isKBStick`（只改了局部变量名，如 `damage`→`valence`）|
| render/RenderUtil | 36 | 立即模式绘制助手 `drawRoundedRect`/`drawQuad`/`drawShadow`/`drawWithShader`/`enableScissor`/`setShaderTexture` |
| rotation/RotationUtil | 31 | `angleDiff`/`exactRotation`/`getBallisticAngles`/`rotationToBlock`/`directionFromRotation`/`getEntityHitResult` |
| game/PlayerUtil | 18 | 含 `sendCarriedItem`（同样用反射调用 `ensureHasSentCarriedItem`）|
| render/ProjectionUtil | 15 | `project`/`updateMatrices`/矩阵缓冲助手 |
| rotation/Rotation、render/RenderHelper | 各 14 | 旋转值对象；`mulPose`/`rotationDegrees` GL 助手 |
| game/MovementUtil | 14 | **部分** —— 移动效果/速度助手抄了，但 `getDirectionYaw`/`handleStrafe` **没有** |
| game/EntityUtil | 14 | `getScreenBounds`/`getInterpolatedAABB`/`getCameraRelativePos` |
| game/RayTraceUtil、game/MotionSimulator | 12 / 11 | 预测射线；跳跃/摩擦力运动模拟 |
| game/ItemAlertTracker、misc/IOUtil | 11 / 10 | 物品追踪；gson/文件 IO |
| animation/*（AnimationTimer、SmoothAnimationTimer、NamedAnimationTimer、AnimationBuilder、SpringAnimation）、math/CubicBezier、math/LerpUtil、math/MathUtil | 各 3–7 | 缓动/动画栈 |
| game/BlockUtil、rotation/RotationHandler、misc/ComponentUtil/ColorUtil/PacketUtil/Assets | 各 3–8 | 部分 |

本体中未发现：`CpsUtil.toDelayMsWithJitter`、`MovementUtil.getDirectionYaw`，以及 OpenZen 的大部分
GUI/HUD 层（本次只比对了 util）。

### 同样被镜像：事件总线
本体的 `api.event` / `api.ieventbus` / `api.eventpriority` / `api.cancelable` 镜像了 OpenZen 的
`shit.zen.event`（`EventBus` / `EventPriority` / `Cancellable` / `@EventTarget`）。

### 着色器 / 模糊 —— 没有抄
OpenZen 所有特征性着色器 token —— `twoSigmaSq`、`BlurDir`、`TexelSize`、`halfK`、`rdist`、
`ralpha`、`roundSDF`、`RECT_VERTICES_COORDS`、`rvertexcoord`、`StencilTexture`、`BlurRadius`、
`6.28318530718`、`average /= 80.0` —— 在本体中 **0 命中**（只有 MC 原版的 `ModelViewMat` uniform 相同）。
两套是不同的设计：

| | OpenZen（`render/BlurShader`、`shader/ShaderSource`） | Hachimi 本体 |
|---|---|---|
| 高斯模糊 | 动态核 `halfK = ceil(sigma*2)`（上限 24），`exp(-i*i/twoSigmaSq)`，uniform `BlurDir`/`TexelSize`/`Radius` | 固定 **Rastergrid 5-tap** 线性采样（权重 `0.2270270270`/`0.3162162162`/`0.0702702703`，偏移 `1.3846`/`3.2308`），uniform `uDir` |
| 圆角面板 | `common.glsl` 的 `rdist`/`ralpha`、`rounded_rect.fsh` 的 `roundSDF`、`bloom.fsh` 16×5 环形模糊、`stencil.fsh` | 多模式 SDF `sdBox`/`sdBoxQuad`/`sdRing`/`sdArc`/`sdCapsule`；另有独立的 erf/cdf/probit SDF 文字面板着色器（`uRows[96]`/`uSigma`）|

因此着色器/模糊代码是独立编写的；抄袭仅限于 `shit.zen.utils` 的 Java 助手（以及事件总线的结构）。
