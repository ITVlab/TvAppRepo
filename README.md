# TvAppRepo
An app repository of Android TV apps

In Android TV's stock launcher, all apps that appear must have the `LEANBACK_LAUNCHER` attribute and have a TV banner. While this helps
guarantee the app works on TVs, it can make it difficult for other apps to be launched. While it's not impossible, it's not as convenient as
having an icon on the homescreen.

Using a python script, tiny shortcut apps are generated and can be installed. They simply contain the face of a Leanback app. However, once
they're opened they simply redirect users to the intended app. It's a simple workaround.

This workaround has a few advantages. By not modifying the APK directly, it allows updates to continue through the Google Play Store.
In fact, the actual app isn't touched at all. These shortcuts could point to anything: a website, a specific shortcut in an app,
or basically any Intent.

The python script cannot be run in the app. The app contains several sideloading tools, including supporting #SIDELOADTAG, showing all your
downloaded apps, and browsing apps that have generated shortcuts. The app should also be able to notify users when it needs to be updated.

## Python Script
The script takes the `/python/Shortcut` sample project and clones it to `/python/temp_apk`. Then it replaces `strings.xml` with new info,
along with modifying `build.gradle` and `AndroidManifest.xml` to use a new package name. The banner is swapped out. Finally, the shortcut
APK is generated and ready to be downloaded and installed.

It can be called in several ways.

`python leanbackshortcut.py --firebase --windows`

* `--firebase` indicates that the Firebase database will be queried for results
* `--windows` indicates that you're using a Windows command prompt. This changes some of the commands.

Before you get started, you'll need to add some additional files.

### config.txt
This config file uses a KEY=VALUE pair for particular settings related to your personal configuration. Currently there are two properties
needed: `SHORTCUT_APP_DIRECTORY` and `ANDROID_SDK_LOCATION`.

    SHORTCUT_APK_DIRECTORY=c:/Users/me/TvAppRepo/python/Shortcut
    ANDROID_SDK_LOCATION=c:/Users/me/AppData/Local/Android/sdk

### keys.txt
The shortcuts are still apps that need to be signed. This can be done by putting the values into a file called `keys.txt`.

    storeFile file("C:\\Users\\me\\keys\\thiskey.jks")
    storePassword "P@55word"
    keyAlias "@ndroid"
    keyPassword "Purple"
    
### Additional Notes
* On Windows, the task Java Binary needs to be killed at the end of every run in order for the next to start.
* Bulk APK generations from Firebase do not work
* Banners must be in the PNG file format. While not exactly enforced in the script, it causes compiler problems.

### Download
You can check out the **Releases** tab to download the latest version. Alternatively one can use the #SIDELOADTAG **`TVAPPREPO102`**
to get version 1.0.2 (which should then be able to update to the newest version).
