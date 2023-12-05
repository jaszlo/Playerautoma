# Playerautoma
Minecraft Fabric mod that allows to record player movement and inputs to replay those.
Also, re-stacks blocks when used up like Inventory tweaks. Everything also works on servers **BUT**
will be **regarded as cheating** by most servers.
Does not record chat input or looting of chests. The latter will be added in feature updates!

## Disclaimer
This mod is still early in development.
If you encounter any bugs please let me know [here](https://github.com/jaszlo/Playerautoma/issues).

## Installation
This mod obviously requires Minecraft [Fabric](https://fabricmc.net/). Simply put the playerautoma-vx.x.jar and the [FabricAPI](https://modrinth.com/mod/fabric-api) JAR in the `.minecraft/mods` folder.

## Usage
The mod has a menu which should be self-explanatory and can be opened using the 'O' Key.

Other than that the default keybindings are as follows.
- **G** &emsp;  Start Recording
- **H** &emsp; Stop Recording
- **J**  &emsp; Start Replay
- **K** &emsp; Cancel Replay
- **L** &emsp; Start looped Replay
- **U** &emsp; Store Recording
-  &nbsp;**I**  &emsp; Load Recording

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
