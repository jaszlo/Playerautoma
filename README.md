# FabricModTemplate
A Template to start developing Minecraft Fabric mods
To Rename the mod from template do the following steps.

1. Refactor the package `net.jasper.template` package to `net.jasper.<your_mod_name>`
2. Refactor the classes `TemplateMod.java`, `TemplateModClient.java` and `TemplateModDataGenerator.java` according to your Mod's name also the MOD_ID variable in those Classes.
3. Refactor the `template.mixins.json`according to your mods name and change the field `package` inside the file.
4. Inside the `fabric.mod.json` apply the following changes:
     - Change *id* from `template` to `<your_mod_name>`
     - Change *name* from `template` to `<your_mod_name>`
     - Change the entrypoints to fit the new package path and file names

## Further required steps to Steup intelliJ

1. Project Structure > Select JDK to `JDK17` and Language Level to `SKD default`
2. Settings > Build, Execution, Deployment > Build Tools > Gradle > Select Gradle JVM to `Project SDK`
3. run `.\gradlew genSources`
4. Gradle (Sidebar) > Tasks > fabric > runClient
