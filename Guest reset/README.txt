GHALIBXCHEATS GUEST RESET
=========================

Target device:
- Android 14
- Normal Free Fire package: com.dts.freefireth
- Target file: /storage/emulated/0/Android/data/com.dts.freefireth/files/localconfig.json

Included payload:
{"testCodePatch":true,"resetGuest":true}

What the app does:
- Install / Replace: writes the bundled localconfig.json to the target path using Shizuku.
- Before replacing an existing target file, it saves the original as:
  localconfig.json.ghalibxcheats.bak
- Remove / Restore: restores that backup if present; otherwise deletes localconfig.json.
- Check: reports whether the target file exists.

IMPORTANT:
- This is a file installer/remover. It does not guarantee that Free Fire will interpret
  these two JSON keys or perform a guest reset. That behavior is controlled by the game.
- Close Free Fire before installing or removing the file.
- Keep your guest account credentials / recovery details safe.
- Shizuku must be running and this app must be granted Shizuku permission.
- On a non-rooted phone, start Shizuku with Wireless debugging. It must be restarted
  after a device reboot.

Build:
- Open this project in Android Studio (JDK 17, Android SDK 35), then run:
  gradle assembleDebug
- Output: app/build/outputs/apk/debug/app-debug.apk

Shizuku API: 13.1.5. The app uses the legacy newProcess entry point through reflection
because it is private in this API line; re-check against future Shizuku API versions.
