## Autoupdate lib
Allows any mindustry mods to easily check for updates.

## Usage
The jar must be added to the mod as an implementation dependency in order for it to work.

Specify the following lines in your mod.(h)json file:
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

##