# Roadmap

For BBS mod, there would be three stages: BBS Studio Port, Old Core Adaptation, 
and Missing Features. Here is the breakdown of these stages:

## BBS Studio Port

This is the stage where I port majority of useful features from BBS Studio into a 
Minecraft Fabric mod. Besides that, I'll also add any features that are essential to 
the port (like model blocks, although that's also part of BBS Studio).

Beside that, here is a list to finish:

- [x] Optimize keyframe editor rendering
- [x] Clean up the UI
- [x] Finish remaining features
  - [x] Shaders for subtitle renderer
  - [x] Shadow for film controller
  - [x] Fix bundled models work incorrectly
- [x] Implement proper data managers
- [x] Bring back language editor stuff
- [x] Implement language picker thing
- [x] ffmpeg warning
- [x] Fix can't exit the keyframe graph editor
- [x] Check out player models
- [x] Make last undo unmergeable if 5 seconds passed or smth
- [x] Fix Iris bugs

## Old Core Adaptation

In this stage, I'll implement any missing features from my mods that are core 
experience. Like following:

- [ ] Vanilla animation system for models
  - [ ] Basic properties like looking, swinging (+inverted), swiping, leaning pitch
  - [ ] Item anchors
  - [ ] Editor
- [ ] Drag and drop system
  - [ ] Images (.jpg, .gif, .webp) -> png
  - [ ] PNG skins 64x32 to 64x64
  - [ ] Audio (.mp3, .aiff) -> .wav mono 16bit
  - [ ] .geo.json + .animation.json -> .bbs.json
  - [ ] .mp4 -> .wav
- [ ] Mob form
- [ ] Emoticons and S&B
- [ ] Action system
  - [ ] In-game recording
- [ ] Damage control
- [ ] Audio editor
- [ ] Structure form
- [ ] BB gun?
- [ ] 白某人是个萌新's suggestion about mouse travel
- [ ] Video recording
  - [ ] Export audio
  - [x] Movies folder location
  - [x] Presets
  - [x] ffmpeg motion blur

## Missing features

And finally, once the BBS Studio Port and Old Core Features are done, it's time to 
implement any features that are missing for the user base. Anything that is going to 
be used by a lot of people. No niche features.

The list is TBD.

# Release

Once all of these stages are passed, and people are happy with the tools for the most 
part. It's time preparing for the release. For the release, I need to record a tutorial 
series that teaches how to practically use BBS mod to record machinimas by examples.

### Miscellaneous

Epic features

- [ ] Framebuffer form
- [ ] Clip based actors