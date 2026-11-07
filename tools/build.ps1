# build.ps1 - offline build for OpenHachimi (javac + jar, no Gradle network access)
#
# Gradle (ForgeGradle 6) needs maven.minecraftforge.net on first run; this script builds the same
# sources straight against the ForgeGradle-resolved official-mapped Forge jar that already lives in
# the local Gradle cache, so it works fully offline.
#
# usage:  powershell -NoProfile -ExecutionPolicy Bypass -File tools/build.ps1 [-Run]

param(
    [string]$ForgeVersion = "1.20.1-47.4.20",
    [string]$GradleHome   = "$env:USERPROFILE\.gradle",
    [string]$LibRoot      = "D:\minecraft\libraries",
    [switch]$Run
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

$jdkHome = $env:JAVA_HOME
if (-not $jdkHome) {
    $candidates = Get-ChildItem "C:\Program Files\Eclipse Adoptium","C:\Program Files\Java" -Directory -ErrorAction SilentlyContinue |
                  Where-Object { $_.Name -match 'jdk-?17' } | Select-Object -First 1
    if ($candidates) { $jdkHome = $candidates.FullName }
}
if (-not $jdkHome) { throw "JDK 17 not found; set JAVA_HOME" }
$javac = Join-Path $jdkHome "bin\javac.exe"
$jar   = Join-Path $jdkHome "bin\jar.exe"

$forgeJar = Join-Path $GradleHome "caches\forge_gradle\minecraft_user_repo\net\minecraftforge\forge\$($ForgeVersion)_mapped_official_1.20.1\forge-$($ForgeVersion)_mapped_official_1.20.1.jar"
if (-not (Test-Path $forgeJar)) { throw "mapped Forge jar not found: $forgeJar (run './gradlew build' once with network)" }

$extra = @(
    "net\minecraftforge\fmlcore\1.20.1-47.4.26\fmlcore-1.20.1-47.4.26.jar",
    "net\minecraftforge\fmlloader\1.20.1-47.4.26\fmlloader-1.20.1-47.4.26.jar",
    "net\minecraftforge\javafmllanguage\1.20.1-47.4.26\javafmllanguage-1.20.1-47.4.26.jar",
    "net\minecraftforge\mclanguage\1.20.1-47.4.26\mclanguage-1.20.1-47.4.26.jar",
    "net\minecraftforge\lowcodelanguage\1.20.1-47.4.26\lowcodelanguage-1.20.1-47.4.26.jar",
    "net\minecraftforge\coremods\5.2.4\coremods-5.2.4.jar",
    "net\minecraftforge\accesstransformers\8.0.4\accesstransformers-8.0.4.jar",
    "net\minecraftforge\JarJarFileSystems\0.3.19\JarJarFileSystems-0.3.19.jar",
    "net\minecraftforge\JarJarMetadata\0.3.19\JarJarMetadata-0.3.19.jar",
    "net\minecraftforge\JarJarSelector\0.3.19\JarJarSelector-0.3.19.jar",
    "net\minecraftforge\mergetool\1.1.5\mergetool-1.1.5-api.jar",
    "net\minecraftforge\unsafe\0.2.0\unsafe-0.2.0.jar",
    "net\minecraftforge\fmlearlydisplay\1.20.1-47.4.26\fmlearlydisplay-1.20.1-47.4.26.jar",
    "cpw\mods\bootstraplauncher\1.1.2\bootstraplauncher-1.1.2.jar",
    "cpw\mods\securejarhandler\2.1.10\securejarhandler-2.1.10.jar",
    "cpw\mods\modlauncher\10.0.9\modlauncher-10.0.9.jar",
    "net\minecraftforge\forgespi\7.0.1\forgespi-7.0.1.jar",
    "net\minecraftforge\eventbus\6.2.33\eventbus-6.2.33.jar",
    "org\spongepowered\mixin\0.8.5\mixin-0.8.5.jar",
    "org\joml\joml\1.10.5\joml-1.10.5.jar",
    "org\lwjgl\lwjgl\3.3.1\lwjgl-3.3.1.jar",
    "org\lwjgl\lwjgl-glfw\3.3.1\lwjgl-glfw-3.3.1.jar",
    "org\lwjgl\lwjgl-opengl\3.3.1\lwjgl-opengl-3.3.1.jar",
    "org\lwjgl\lwjgl-stb\3.3.1\lwjgl-stb-3.3.1.jar",
    "com\google\code\gson\gson\2.10.1\gson-2.10.1.jar",
    "com\google\guava\guava\31.1-jre\guava-31.1-jre.jar",
    "com\google\guava\failureaccess\1.0.1\failureaccess-1.0.1.jar",
    "io\netty\netty-buffer\4.1.82.Final\netty-buffer-4.1.82.Final.jar",
    "io\netty\netty-common\4.1.82.Final\netty-common-4.1.82.Final.jar",
    "io\netty\netty-transport\4.1.82.Final\netty-transport-4.1.82.Final.jar",
    "io\netty\netty-codec\4.1.82.Final\netty-codec-4.1.82.Final.jar",
    "com\mojang\authlib\4.0.43\authlib-4.0.43.jar",
    "com\mojang\logging\1.1.1\logging-1.1.1.jar",
    "com\mojang\brigadier\1.1.8\brigadier-1.1.8.jar",
    "it\unimi\dsi\fastutil\8.5.9\fastutil-8.5.9.jar",
    "org\apache\commons\commons-lang3\3.12.0\commons-lang3-3.12.0.jar",
    "org\slf4j\slf4j-api\2.0.1\slf4j-api-2.0.1.jar"
)

$cp = @($forgeJar)
foreach ($rel in $extra) {
    $p = Join-Path $LibRoot $rel
    if (Test-Path $p) { $cp += $p } else { Write-Host "[warn] missing library: $rel" }
}
$cpString = $cp -join ';'

$classes = Join-Path $root "build\classes"
$libsDir = Join-Path $root "build\libs"
Remove-Item $classes -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force $classes, $libsDir | Out-Null

$srcs = Get-ChildItem (Join-Path $root "src\main\java") -Recurse -Filter *.java | ForEach-Object { $_.FullName }
Write-Host ("[1/3] javac {0} sources" -f $srcs.Count)
# stage the jars into one folder, use the "dir/*" wildcard classpath,
# and keep the 119 source paths in an @argfile (cmd line limit is 8191 chars)
$cpDir = Join-Path $root "build\cp"
New-Item -ItemType Directory -Force $cpDir | Out-Null
foreach ($j in $cp) {
    $dest = Join-Path $cpDir (Split-Path $j -Leaf)
    if (-not (Test-Path $dest)) { Copy-Item $j $dest -Force }
}
$srcList = Join-Path $root "build\sources.txt"
[IO.File]::WriteAllLines($srcList, ($srcs | ForEach-Object { $_ }), (New-Object Text.UTF8Encoding($false)))
$logFile = Join-Path $root "build\javac.log"
Push-Location $root
try {
    cmd /c "`"$javac`" -encoding UTF-8 -proc:none -nowarn -d `"build\classes`" -cp `"build\cp\*`" @`"build\sources.txt`" > `"build\javac.log`" 2>&1"
    $javacCode = $LASTEXITCODE
} finally { Pop-Location }
Get-Content $logFile -ErrorAction SilentlyContinue | Where-Object { $_ -match 'error:' } | ForEach-Object { Write-Host $_ }
if ($javacCode -ne 0) { throw "javac failed (exit $javacCode), see build\javac.log" }

Write-Host "[2/3] copy resources"
Copy-Item (Join-Path $root "src\main\resources\*") $classes -Recurse -Force

Write-Host "[3/3] package jar"
$outJar = Join-Path $libsDir "openhachimi-1.0.0.jar"
Remove-Item $outJar -Force -ErrorAction SilentlyContinue
& $jar cf $outJar -C $classes .
if ($LASTEXITCODE -ne 0) { throw "jar failed" }

Write-Host ("built: {0} ({1} bytes)" -f $outJar, (Get-Item $outJar).Length)
if ($Run) { Write-Host "start a client with: ./gradlew runClient (needs network once)" }
