# Playerautoma
Minecraft Fabric mod that allows to record player movement and inputs to replay those. 
Also, re-stacks blocks when used up like Inventory tweaks. Everything also works on servers **BUT**
will be **regarded as cheating** by most servers.

Use at your own risk!

## Installation
This mod required Minecraft Fabric. When that is set up simply put the playerautoma-vx.x.jar 
in the `.minecraft/mods` Folder. You will also require the [FabricAPI](https://www.curseforge.com/minecraft/mc-mods/fabric-api) 
(Alternative [Modrinth](https://modrinth.com/mod/fabric-api) link).


## Usage
The mod Has a Menu which should be self-explanatory and can be opened using the 'O' Key.
Other than that the Keybindings are as follows:
#### Recordings
- G: Start Recording
- H: Stop Recording
- U: Store Recording to file
- I: Load Recording from file
#### Replays
- J: Start Replay
- K: Cancel Replay
- L: Start looped Replay

## Examples
In this example you can see PlayerAutoma being used in order to traverse a Jump'n Run once and then use the recording
to do it automatically.

![Gif of the Situation explained above](documentation/resources/jump-n-run-demo.gif)

In this example you can see PlayerAutoma being used in order to build a farm-module once and then use the recording
to do it automatically.

![Gif of the Situation explained above](documentation/resources/farm-demo.gif)


## Development Setup

1. Project Structure > Select JDK to `JDK17` and Language Level to `SKD default`
2. Settings > Build, Execution, Deployment > Build Tools > Gradle > Select Gradle JVM to `Project SDK`
3. run `.\gradlew genSources`
4. Gradle (Sidebar) > Tasks > fabric > runClient

This should start the minecraft client
