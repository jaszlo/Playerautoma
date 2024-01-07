# Playerautoma
Record and replay player actions. Also, re-stack blocks when used up like Inventory tweaks. The mod works on servers but can be regarded as cheating by some.

<div align="center">
  <video src="https://github.com/jaszlo/Playerautoma/assets/55958177/b257fb70-db16-4803-a528-d540a14a3909" width="400" />
</div>

The following actions are recorded:
 * movement
 * item interactions
 * moving items around in the inventory, chests and crafting tables as well as crafting itself

The mod will not add support to record chat actions like commands or writing messages as it would allow for the creation of spam bots.

Checkout the [Changelog](Changelog.md) for more information on the latest changes.
Also, checkout the [Modrinth](https://modrinth.com/mod/playerautoma) page information to support the mod development.
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

## Development Setup

1. Project Structure > Select JDK to `JDK17` and Language Level to `SKD default`
2. Settings > Build, Execution, Deployment > Build Tools > Gradle > Select Gradle JVM to `Project SDK`
3. run `.\gradlew genSources`
4. Gradle (Sidebar) > Tasks > fabric > runClient

This should start the minecraft client
