## Autoupdate lib
Allows any mindustry mods to easily check for updates.

## Usage
The jar must be added to the mod as an implementation dependency in order for it to work.
Theres a simple way to add it:
1. Create a subfolder `lib/` in your mod project
2. Copy the jar file to the `lib/` folder
3. Add the following lines to your `build.gradle` file:
```
dependencies {
	implementation fileTree(include: ['*.jar'], dir: 'lib')
}
```
If `dependencies` block already exists in your gradle file (and it probably does),
you should only add the `implementation...` line to the existing block.

You must also specify the following lines in your `mod.(h)json` file:
```
#!VERSION integer_version;
#!REPO author/repo;
```
example:
```
#!VERSION 24;
#!REPO MHeMoTexHuK/New-controls;
...normal mod info...
```

Call Updater.checkUpdates(currentMod) after client load and it'll check whether the repo has a newer version.
If it has, it'll prompt the user to automatically update it and will download & install it.

TODO: upload the artifact to maven or smth?