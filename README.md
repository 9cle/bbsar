# BBSAR

BBSAR is a BBS fork for Minecraft 1.21.1 that captures Windows playback audio
alongside BBS video recordings and muxes the result into the exported MP4.

## Requirements

- Minecraft 1.21.1, Fabric Loader, Fabric API, and Java 21
- FFmpeg available as `ffmpeg`, or configured in BBS's video settings
- Windows x64 and Visual Studio C++ Build Tools to build the WASAPI capture DLL

The capture records the default Windows playback mix. It can include audio from
other applications and Windows notifications, not only Minecraft. Disable
**Capture system audio** in the video export settings if you want a silent export.

To build on Windows, install the Visual Studio x64/x86 C++ build tools workload
and run `gradlew.bat build`. The Gradle build compiles and packages the small
WASAPI bridge automatically. Other operating systems can build BBSAR, but
system-audio capture is currently Windows x64 only.

This checkout follows upstream's `1.21.1` branch; it does not target Minecraft
1.20.4.
