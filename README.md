## Autoupdate lib
Allows mindustry mods to easily check for updates and notify the user about them.

## Usage
The jar artifact of the library must be added to the mod as an implementation dependency in order for it to work.
Theres a simple way to add it:
1. Download a jar file from the "releases" tab
2. Create a subfolder `lib/` in your mod project
3. Copy the jar file to the `lib/` folder
4. Add the following lines to your `build.gradle` file:
```
dependencies {
	implementation files("lib/Autoupdate-lib.jar")
}
```
If the `dependencies` block already exists in your gradle file (and it probably does),
you should only add the `implementation...` line to the said block.

You must also specify the following lines in your `mod.(h)json` file. The file must be placed either in the root of your mod or in assets folder.
```
#!VERSION version
#!REPO "author/repo"
```
The first line specifies the version which will be used for comparisons (must be a number), the second specifies the mod repo. Note that if the local and remote metadata files have differet repo values,
the library will download a release from the repository specified ***in the remote metadata file***

Optionally you can also specify the branch and a "no update" token:

The first specifies the branch which will be used to check updates.

If the latter is present in the __REMOTE__ (on the github repo) metadata file, the mod will NOT be updated even if the repo has a newer version.
You can use this to prevent the library from showing phantom update notifications during some maintenances.
```
#!BRANCH "target_branch"
#!NO_UPDATE
```
example of a mod.hjson file:
```
#!VERSION 2.4                       (can be an integer or a float, should be increased upon every release)
#!REPO "MHeMoTexHuK/New-controls"   (checking https://github.com/MHeMoTexHuK/new-control)
#!BRANCH "master"                   (checking the master branch)
#!NO_UPDATE                         (updates will be performed)
...normal mod info...
```
Anything after the value of the token is ignored, i.e. you can type "4.2beta" and it'll be interpreted as "4.2" (float)

### Checking for updates
Call Updater.checkUpdates(currentMod) after the client load event and it'll check whether the repo contains a newer version.
If it does, the library will prompt the user to automatically download & install the updste.

Note that the library checks the latest commit, ***but downloads the latest release***. 
If there's a mismatch between these two, the user may receive phantom update notifications!
TODO: upload the artifact to maven or smth?

# About the control tokens
The control tokens can be placed in any order in any place of the metadata file.
