# Architecture

## Entry

`dev.openhachimi.HachimiClient` - `@Mod("hachimi")`. On `FMLClientSetupEvent` it calls
`FontManager.init()`, `PayloadHelper.init()`, `TickManager.init()` and `EventRegistrar.register()`.

> The dumped body has no `@Mod` class: the real client is handed to the game by the native loader.
> This entry only exists so the tree can be launched as a normal Forge mod.

## Core

| Class | Package | Confidence | Evidence |
|---|---|---|---|
| `Category` | modules | high | enum COMBAT/PLAYER/RENDER/MOVEMENT/MISC |
| `Module` | modules | high | base module: name/enabled/category/key/values + event hooks |

- `Module` (was KinematicHelper) is the base of every cheat module: `name`, `enabled`, `expanded`,
  `category`, `key`, `List<Setting<?>> values`, plus the hook surface `onTick`, `onPlayerTick`,
  `onIncomingPacket`, `onOutgoingPacket`, `onRender2D`, `onRender3D`, `onMovementInput`,
  `onAttackEntity`, `onSlowdown`, `onUseItem`, `onKeyPressed`, `prepareSettings`.
- `Category` is the ClickGui column enum.
- `EventRegistrar` (was CadenceHelper) is the only class registering Forge listeners; it owns one
  handler instance and forwards every event to the module list.
- Settings: `Setting<T>` -> `BooleanSetting` / `ModeSetting` (modes[]) / `ColorSetting` /
  `NumberSetting` / `EnumSetting` / `IntSetting` / `DoubleSetting` / `ListSetting`.

## Modules (49)

| Class | Module | Original class |
|---|---|---|
| `AimassistModule` | 'AimAssist' (ContainerHelper.COMBAT) | ScanHelper |
| `AntibotModule` | 'AntiBot' (ContainerHelper.MISC) | BatchHelper |
| `AntinickModule` | 'AntiNick' (ContainerHelper.MISC) | FieldHelper |
| `AntistaffModule` | 'AntiStaff' (ContainerHelper.MISC) | ValueHelper |
| `ArraylistModule` | 'ArrayList' (ContainerHelper.RENDER) | ValidatorHelper |
| `AutoclickerModule` | 'AutoClicker' (ContainerHelper.COMBAT) | TokenHelper |
| `AutopearlModule` | 'AutoPearl' (ContainerHelper.MISC) | LayoutHelper |
| `AutorodModule` | 'AutoRod' (ContainerHelper.MISC) | ChordHelper |
| `AutosoupModule` | 'AutoSoup' (ContainerHelper.MISC) | MaskHelper |
| `AutosprintModule` | 'AutoSprint' (ContainerHelper.MOVEMENT) | FragmentHelper |
| `AutotoolsModule` | 'AutoTools' (ContainerHelper.PLAYER) | VertexHelper |
| `AutowebModule` | 'Autoweb' (ContainerHelper.PLAYER) | PolygonHelper |
| `ChatoverlayModule` | 'ChatOverlay' (ContainerHelper.RENDER) | ReaderHelper |
| `ChestespModule` | 'ChestESP' (ContainerHelper.RENDER) | WriterHelper |
| `CheststealerModule` | 'ChestStealer' (ContainerHelper.PLAYER) | TextureHelper |
| `ClickguiModule` | 'ClickGui' (ContainerHelper.RENDER) | BuilderHelper |
| `CriticalsModule` | 'Criticals' (ContainerHelper.COMBAT) | WeightHelper |
| `CrystalauraModule` | 'CrystalAura' (ContainerHelper.COMBAT) | DepthHelper |
| `DisablebossbarModule` | 'DisableBossBar' (ContainerHelper.RENDER) | ParserHelper |
| `EagleModule` | 'Eagle' (ContainerHelper.MOVEMENT) | SpriteHelper |
| `EspModule` | 'ESP' (ContainerHelper.RENDER) | EncoderHelper |
| `FakelagModule` | 'FakeLag' (ContainerHelper.MISC) | PivotHelper |
| `FastplaceModule` | 'FastPlace' (ContainerHelper.PLAYER) | CanvasHelper |
| `FastwebModule` | 'FastWeb' (ContainerHelper.MOVEMENT) | VectorHelper |
| `FpsboostModule` | 'FpsBoost' (ContainerHelper.RENDER) | DecoderHelper |
| `FriendModule` | 'Friend' (ContainerHelper.MISC) | SpanHelper |
| `HealthbypassModule` | 'HealthBypass' (ContainerHelper.MISC) | TraceHelper |
| `InventoryrenderModule` | 'InventoryRender' (ContainerHelper.RENDER) | SerializerHelper |
| `InvmanagerModule` | 'InvManager' (ContainerHelper.PLAYER) | WidgetHelper |
| `JumpresetModule` | 'JumpReset' (ContainerHelper.COMBAT) | GridHelper |
| `KeepsprintModule` | 'KeepSprint' (ContainerHelper.MOVEMENT) | MatrixHelper |
| `KillauraModule` | 'KillAura' (ContainerHelper.COMBAT) | RangeHelper |
| `MidpearlModule` | 'MidPearl' (ContainerHelper.MISC) | VaultHelper |
| `NametagsModule` | 'NameTags' (ContainerHelper.RENDER) | ResolverHelper |
| `NicknameModule` | 'Nickname' (ContainerHelper.RENDER) | HandlerHelper |
| `NofovModule` | 'NoFov' (ContainerHelper.RENDER) | ListenerHelper |
| `NojumpdelayModule` | 'NoJumpDelay' (ContainerHelper.MOVEMENT) | BufferHelper |
| `NoslowModule` | 'NoSlow' (ContainerHelper.MOVEMENT) | ChannelHelper |
| `NotificationModule` | 'Notification' (ContainerHelper.RENDER) | ObserverHelper |
| `PotionHudModule` | 'Potion HUD' (ContainerHelper.RENDER) | PublisherHelper |
| `ProjaimModule` | 'ProjAim' (ContainerHelper.COMBAT) | IndexHelper |
| `ProjectileModule` | 'Projectile' (ContainerHelper.RENDER) | DispatcherHelper |
| `S08detectorModule` | 'S08Detector' (ContainerHelper.MISC) | AnchorHelper |
| `ScaffoldModule` | 'Scaffold' (ContainerHelper.PLAYER) | StreamHelper |
| `ScoreboardhudModule` | 'ScoreboardHud' (ContainerHelper.RENDER) | ControllerHelper |
| `TargethudModule` | 'TargetHUD' (ContainerHelper.RENDER) | CoordinatorHelper |
| `TeamsModule` | 'Teams' (ContainerHelper.MISC) | ElementHelper |
| `VelocityModule` | 'Velocity' (ContainerHelper.COMBAT) | RateHelper |
| `WatermarkModule` | 'Watermark' (ContainerHelper.RENDER) | SchedulerHelper |

## Classes per package

| Package | Classes |
|---|---|
| `dev.openhachimi.command` | 1 |
| `dev.openhachimi.config` | 2 |
| `dev.openhachimi.event` | 1 |
| `dev.openhachimi.hud` | 2 |
| `dev.openhachimi.modules` | 51 |
| `dev.openhachimi.render` | 6 |
| `dev.openhachimi.security` | 3 |
| `dev.openhachimi.settings` | 9 |
| `dev.openhachimi.utils` | 43 |
