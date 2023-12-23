# Playerautoma
Record and replay player actions. Also, re-stack blocks when used up like Inventory tweaks. The mod works on servers but can be regarded as cheating by some.

The following actions are recorded:
 * movement
 * item interactions
 * moving items around in the inventory, chests and crafting tables as well as crafting itself

The mod will not add support to record chat actions like commands or writing messages as it would allow for the creation of spam bots.

## Any trouble?
This mod is still early in development.
If you encounter any bugs or problems please let me know [here](https://github.com/jaszlo/Playerautoma/issues) and if you want to contribute feel free to create a [pull request](https://github.com/jaszlo/Playerautoma/pulls).

## Installation
This mod requires Minecraft [Fabric](https://fabricmc.net/). Simply put the playerautoma-vx.x.jar and  [Fabric API](https://modrinth.com/mod/fabric-api) JAR in `.minecraft/mods`.


## Usage
The mod has a menu which should be self-explanatory and can be opened using the `O` Key.

Other than that the default keybindings are as follows.
- `G`  Start Recording
- `H`  Stop Recording
- `J`   Start Replay
- `K`  Cancel Replay
- `L`  Start looped Replay
- `U`  Store Recording
-  `I`   Load Recording

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
