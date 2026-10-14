# PrintXpress

Native Android customer app for **PrintXpress**, a digital printing service for Sri Lanka (CSE5011 Mobile Application Development — WRIT1).

Built with **Java**, **XML Views / Fragments**, **Navigation Component**, and **Room (SQLite)**. Cross-platform frameworks (Flutter / React Native / Ionic) are not used, per the brief.

## Requirements

- Android Studio Ladybug (2024.2) or newer recommended
- JDK 17
- Android SDK 35
- Min SDK **26** / Target SDK **35**

## Open in Android Studio

1. Clone this repository.
2. Open Android Studio → **File → Open** → select this project root (the folder containing `settings.gradle`).
3. Let Gradle sync finish (Android Studio will create `local.properties` with your SDK path if needed).
4. Run on an emulator or device with the green **Run** button (`app` configuration).

## Build from the command line

```bash
# Optional: set SDK location if local.properties is missing
echo "sdk.dir=$HOME/Android/Sdk" > local.properties

./gradlew assembleDebug
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`

## What you can do in the app

- Register / sign in with **email or Sri Lankan mobile number** (+ validation)
- Browse print categories (business cards, flyers, posters, banners, stickers, mugs, t-shirts)
- View pricing, materials, sizes, and sample previews
- Place orders with quantity, paper/material, size, custom text and/or artwork upload, pickup or delivery, and scheduled date
- Track order status; cancel or reschedule before printing begins
- Manage profile, delivery addresses, saved designs, and order history
- View notifications / seasonal offers, print guidelines, and FAQ

Demo control on order detail: **Demo: advance status** simulates shop workflow for testing.

## Project layout

```
app/src/main/java/com/printxpress/app/
  data/          Room entities, DAOs, repository, product seed data
  ui/            Fragments, adapters, shared UI helpers
  util/          Input validation and background executors
app/src/main/res/
  layout/        XML screens and list item layouts
  navigation/    Navigation graph
```
