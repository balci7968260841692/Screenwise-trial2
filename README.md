# AI-Enhanced Screen Time Manager

This repository contains everything needed to run the AI-Enhanced Screen Time Manager Android client plus the Supabase backend artifacts.

## Quick fix if tests fail locally

If `./gradlew` or Android Studio sync fails with `gradle-wrapper.jar missing`, do the following on a machine that can access
`services.gradle.org`:

1. Run `gradle wrapper --gradle-version 8.14.3 --distribution-type=bin` from the repository root (install Gradle first if
   needed).
2. Keep the generated `gradle/wrapper/gradle-wrapper.jar` locally. The repository ignores this binary so pull requests do not
   trip the "binary files are not supported" warning.
3. Re-run `./gradlew test`.

## Give the assistant access to the Android SDK

The execution environment available to the assistant is completely offline—it cannot download the Android SDK or any Gradle
artifacts from Google. If you want the assistant to execute `./gradlew` commands (including tests) on your behalf, bundle the
tooling it needs directly inside this repository:

1. **Provide the Gradle wrapper JAR out-of-band.** Generate it locally with `gradle wrapper --gradle-version 8.14.3` and share
   it (for example via a ZIP or Base64 text file) before asking the assistant to build. Because the binary is ignored, drop it
   into `gradle/wrapper/` right before running commands.
2. **Vendor a minimal Android SDK.** Copy the required directories (for example `platforms/android-36/`, `build-tools/36.0.0/`,
   and `platform-tools/`) into a folder such as `tools/android-sdk/` at the repo root. Using the SDK Platform and Build Tools
   that match the app's `compileSdk`/`targetSdk` (36) prevents version mismatch errors when assembling `debug` or `release`
   builds.
3. **Point `local.properties` at the vendored SDK.** Create or edit `local.properties` so `sdk.dir=tools/android-sdk` (use a
   relative path so it works in the sandbox).
4. **Optional: vendor extra dependencies.** If your build depends on Maven artifacts that are not already committed, create a
   local Maven repository (e.g. `third_party/m2repository/`) and add it to `settings.gradle.kts`.

Once those assets exist in the repo, the assistant can run Gradle tasks without network access.

> **Can I just commit a ZIP plus a script that unpacks the SDK?**
>
> Yes, that works as long as the archive contains the Android 36 Platform and matching Build Tools noted above and the script
> extracts them into the location referenced by `sdk.dir` before you ask the assistant to build (for example by running
> `./scripts/unpack-sdk.sh` so that `tools/android-sdk/platforms/android-36/` and `tools/android-sdk/build-tools/36.0.0/`
> exist). Keep in mind that the build still consumes the unpacked directories, so remember to run the script—or expand the
> ZIP manually—any time you refresh the repository.

## Step-by-step setup

### (a) Create a Supabase project
1. Sign in at [supabase.com](https://supabase.com) and create a new project.
2. Note the generated `SUPABASE_URL` and anon `SUPABASE_KEY`.

### (b) Apply the database schema
1. Open the Supabase SQL editor.
2. Paste the contents of [`supabase_schema.sql`](./supabase_schema.sql) and run it once. This creates tables for limits, overrides, trust, and weekly insights with Row Level Security policies.

### (c) Deploy the Edge Functions and set `GEMINI_API_KEY`
1. Install the Supabase CLI if you have not already (`npm install -g supabase`).
2. From the repository root run:
   ```bash
   supabase functions deploy ai_negotiation --project-ref YOUR_PROJECT_REF
   supabase functions deploy coach_tips --project-ref YOUR_PROJECT_REF
   supabase functions secrets set GEMINI_API_KEY=YOUR_GEMINI_KEY --project-ref YOUR_PROJECT_REF
   ```
3. `ai_negotiation` expects override payloads shaped like `{ "reason": "...", "context": "{}" }`. `coach_tips` expects `{ "weeklyUsage": "{}" }` and returns actionable suggestions.

### (d) Configure the Android app
1. Copy [`local.properties.example`](./local.properties.example) to `local.properties` in the project root.
2. Replace the placeholders with your Supabase project URL and anon key.
   * If you're using the Supabase project provided for this repo, you can paste the following values directly:

     ```properties
     SUPABASE_URL=https://hthlqwwiwkpiugdeyatz.supabase.co
     SUPABASE_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imh0aGxxd3dpd2twaXVnZGV5YXR6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjAzNzAyNTIsImV4cCI6MjA3NTk0NjI1Mn0.7MSb_QSkBKHgMF7QWVGsZNcw5T930uzvhD838u2M8XE
     ```

   > **Why the assistant can't commit `local.properties` for you**
   >
   > The file contains environment-specific paths (like your Android SDK location) and secrets (Supabase keys). Keeping it out of
   > version control prevents accidental leaks and avoids breaking other developers' setups. That's why the repo only ships
   > `local.properties.example`—you create your own copy locally with the correct values.
3. Open the project in Android Studio and sync Gradle. If `./gradlew` fails because `gradle/wrapper/gradle-wrapper.jar` is missing, generate it once on a machine with Gradle installed by running `gradle wrapper --gradle-version 8.14.3` (or the version declared in `gradle/wrapper/gradle-wrapper.properties`) from the repo root, then copy the resulting JAR into `gradle/wrapper/` locally before launching Gradle again.

### (e) Run the app
1. Build the `app` module and install on a device running Android 8.0 or newer.
2. Grant Usage Access, Accessibility, Notification, and optional Calendar/Location permissions when requested.
3. The app starts a foreground service with a persistent notification to protect limits. Use the Home screen to view remaining time, submit override requests, and launch the Weekly Coach.

### How screen time limits and overrides behave in practice

* **You set the limits.** During onboarding and in Settings you pick which apps/categories to manage and assign the daily minutes and quiet hours you want enforced.
* **Actual usage is tracked on-device.** Once Usage Access permission is granted, the app reads per-package usage durations from `UsageStatsManager`, persists them to Room, and updates the “time left” widgets/services in real time.
* **Guard rails kick in automatically.** When you hit a limit the `ShieldActivity` takes over the offending app via the accessibility guard, shows the AI-assisted reflection/negotiation flow, and applies the trust-scaled cooldowns/grace periods defined in `UsageScheduleEngine`.
* **Overrides require justification.** To reclaim time you explain the reason and minutes requested. The request is sent to the Supabase Edge Function where Gemini evaluates alignment, proposes counter-offers when the ask is large, and returns a decision logged in the `override_requests` table.

This mirrors the “assign your own screen time, justify overrides” experience described in the product brief.

### Identity model: device-first by default

The reference implementation is intentionally single-device. All Room tables, trust math, and WorkManager jobs operate against a local profile whose `userId` is set to `"local"`, so you do **not** need to ship a login screen to enforce limits, generate APK/AAB artifacts, or test the AI override loop. Each installation keeps its own usage history, and data export/delete actions wipe only the current device unless you layer on remote sync.

Supabase Edge Functions are invoked with the anon key purely for AI negotiation and coaching—they do not assume an authenticated session. If you want shared profiles or cross-device continuity, add Supabase Auth (or your provider of choice), persist the returned `user_id`, and include it in any Postgrest writes you implement. Until then, the app “recognizes” the user by the device it is installed on.

## Project structure

```
app/                         Android app module (Jetpack Compose + Hilt + Room)
supabase/functions/ai_negotiation/    Supabase Edge Function (Gemini proxy + README)
supabase/functions/coach_tips/        Minimal Gemini helper for weekly coaching
supabase_schema.sql          Database schema and RLS policies
README_ANDROID.md            Android-specific build instructions
PRIVACY_POLICY.md            Store-ready privacy policy
PLAY_LISTING_DRAFT.md        Draft Google Play listing copy
```

> **Why there is no `android/` folder**
>
> This repository is already an Android Studio project at the root. The `app/` module contains all Android sources, and the top-level Gradle files (`build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`) sit beside it. When you open the repo in Android Studio or run Gradle commands, use the repository root—there is no additional wrapper directory.

## Building release artifacts

Use standard Gradle tasks from the project root to create APKs or Android App Bundles:

```bash
./gradlew :app:assembleRelease      # Builds a signed/unsigned release APK (configure signingConfig as needed)
./gradlew :app:bundleRelease        # Builds an AAB for Play Store upload
```

Both tasks operate on the `app` module directly; you do not need a separate `android/` folder. Configure signing keys in `app/build.gradle.kts` or via `gradle.properties`/`local.properties` before running these commands.

## Testing

Run unit and UI tests from the repo root:

```
./gradlew test
./gradlew connectedAndroidTest
```

## Compliance & safety features

* All AI calls proxy through the Supabase Edge Functions where `GEMINI_API_KEY` lives—no keys are bundled with the client.
* Trust score and negotiation logic adapt difficulty, enforce cooldowns via the repositories, and support deals that affect future trust.
* Users can export or delete data (Room + Supabase) from the Settings screen stubs.
* Accessibility labels, high-contrast palette, and dynamic text support are built into the Compose UI.
* Foreground service + WorkManager ensure protection persists across device restarts.

## Manual OEM checklist

Test the persistent service and Shield Activity on Samsung (One UI), Google Pixel (AOSP), and Xiaomi (MIUI) devices to verify:
* Foreground notification survives background restrictions.
* Accessibility service remains enabled after reboot.
* Permission revocation surfaces warnings in Settings/Home.
