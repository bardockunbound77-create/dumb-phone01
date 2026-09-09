# Dumb Phone

A minimal Android home-screen replacement. When set as your default launcher,
it replaces your entire home screen and app drawer with just 8 tiles:

**Torch · Phone · Messages · Email · Gallery · Camera · Clock · Recorder**

There is no app drawer, no search bar, and no way to open anything else
(including social apps) from this screen — they simply aren't reachable from
here. A long press on empty space opens system Settings, so you can still
change wifi, volume, or switch your launcher back at any time.

This folder is a complete Android Studio project. It is **source code**, not
a finished .apk — it needs to be compiled once. Below are two ways to do that,
no coding required.

---

## Option A — Get a built APK with no software install (recommended)

1. Create a free GitHub account if you don't have one: https://github.com/signup
2. Create a new repository (e.g. "dumb-phone"), and upload every file in this
   folder to it (drag-and-drop works on github.com, or use "Add file → Upload files").
   Make sure the `.github` folder (with the `workflows` folder inside it) gets uploaded too —
   some browsers hide folders starting with a dot, so if drag-and-drop skips it,
   upload that folder separately.
3. Go to the **Actions** tab of your new repository. A workflow called
   "Build APK" will run automatically (or click "Run workflow" if it doesn't).
4. Wait for the green checkmark (a couple of minutes), then open that run and
   download the **DumbPhone-debug-apk** artifact — it's a zip containing
   `app-debug.apk`.
5. Transfer `app-debug.apk` to your phone (email it to yourself, or use a
   cable/cloud drive) and tap it to install. You'll need to allow
   "install unknown apps" for whatever app you used to open it — Android will
   prompt you for this automatically.

## Option B — Build it yourself in Android Studio

1. Install Android Studio (free): https://developer.android.com/studio
2. Open this folder as a project ("Open" → select the `DumbPhone` folder).
3. Let it sync (first time takes a few minutes, it downloads the Android SDK).
4. Click **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.
5. Click "locate" in the popup to find `app-debug.apk`, then copy it to your phone and install it.

---

## After installing, to actually use it as your home screen

1. Install the APK.
2. Press your phone's Home button.
3. Android will ask "Use Dumb Phone as your Home app?" — choose **Always** (or
   go to **Settings → Apps → Default apps → Home app** and pick "Dumb Phone").
4. To go back to your normal launcher later, open that same Settings screen
   and pick your old launcher again — or long-press anywhere blank on the
   Dumb Phone screen, which opens Settings directly.

## Notes & things worth knowing

- **No social media, by design**: because this app *is* your home screen and
  has no app drawer, apps like Instagram/TikTok/etc. stay installed on your
  phone but simply have no icon or entry point to launch them from here. If
  you want to be extra strict, uninstall those apps entirely rather than
  relying on the launcher alone — a determined user could still find another
  way to reach them (e.g. via a notification, or by reinstalling a launcher).
- **Messages/Email/Gallery/Clock/Recorder** open whatever your phone already
  has set as default for that task (or the best match it can find) — this app
  doesn't include its own messaging or email client, it just launches your
  phone's existing ones.
- **Torch** uses your phone's own camera flash hardware directly — no camera
  app is opened, it just switches the flash on/off.
- This is a debug build, fine for personal use. If you ever want to publish
  it to the Play Store, that requires a signed "release" build, which is a
  separate, more involved step.
