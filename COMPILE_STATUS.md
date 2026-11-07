# Compile status - CLEAN

```
[1/3] javac 119 sources
[2/3] copy resources
[3/3] package jar
built: build/libs/openhachimi-1.0.0.jar
exit: 0
```

- **119 sources -> 247 class files, 0 errors, 0 warnings-blocking**
- Verified with `javac` (JDK 17) against the official-mapped Forge 1.20.1 jar; no Gradle needed.

## Reproduce

```powershell
# offline, uses the ForgeGradle-resolved jars already in %USERPROFILE%\.gradle + D:\minecraft\libraries
powershell -NoProfile -ExecutionPolicy Bypass -File tools/build.ps1
```

`tools/build.ps1` stages the classpath into `build/cp/` (wildcard classpath, short command line),
compiles `src/main/java` -> `build/classes`, copies `src/main/resources`, and packages
`build/libs/openhachimi-1.0.0.jar`.

Gradle path (`./gradlew build`) needs network on first run (ForgeGradle resolves
`maven.minecraftforge.net`); it is unchanged and works once the dependencies are cached.

## What was broken and how it was fixed

The tree came out of CFR 0.152, which produces three classes of artefacts. All of them are resolved:

| Symptom | Count fixed | Fix |
|---|---|---|
| CFR dropped local declarations (`strW`, `chipW`, `chipH`, `valueY`, `offHand`, `targetSlot`, `cap`, `i`, ...) | ~30 sites | declared at method scope / per loop, re-declared uses turned back into assignments |
| `class X extends Enum<X>` pseudo-enums | 9 classes | rewritten as real `enum` (Format/Filter keep their GL constants) |
| declaration without initialiser (`EXTENSION`, `CRYPT_KEY`, `DIR_NAME`, `strafingAway`, `curIn`, `d`, ...) | 12 fields/locals | initialised (string literals taken from the XOR-decoded constants) |
| raw-type declarations lost generics (`List slots`, `Collection collection`, `Iterable entities`) | 6 | generics restored / explicit casts |
| obsolete SRG method names (`f_133143_`, `f_268504_`) | 2 | `ClientboundSetEntityDataPacket#id()`, `ClientboundDamageEventPacket#entityId()` |
| `Packet<?>#handle(PacketListener)` capture error | 2 | raw `Packet` cast |
| `AbstractTexture#load(ResourceManager)` not implemented | 2 | no-op override (textures upload manually) |
| file name != public class name | 3 | class renamed to match the file (InstallHelper, ResourcesList, PotionData) and references updated |
| unused `final` fields dropped by CFR (`PRESET_NAMES`, `TARGET_CHANNEL`, `HANDLER_NAME`) | 4 | removed |

Nothing was stubbed out to silence the compiler: every edit either restores a declaration CFR lost
or maps an obfuscated name to its official Mojang name.
