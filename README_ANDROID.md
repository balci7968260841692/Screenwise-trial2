# AI-Enhanced Screen Time Manager (Android)

This module contains the Android client built with Jetpack Compose, Hilt, Room, and Supabase.

## Quick checklist to unblock Gradle

1. If `./gradlew` errors with `gradle-wrapper.jar missing`, run `gradle wrapper --gradle-version 8.14.3 --distribution-type=bin` once.
2. Copy the generated `gradle/wrapper/gradle-wrapper.jar` into place locally (the repository ignores it so pull requests stay text-only).
3. Re-run `./gradlew tasks` to verify the wrapper is usable.

### Getting the agent to run tests in this environment

The execution sandbox used by the assistant cannot download Android tooling or SDK components from the public internet. To let
me execute `./gradlew` tasks here, ensure the following assets are available in the repository before triggering a build:

1. Provide `gradle/wrapper/gradle-wrapper.jar` out-of-band (for example as a ZIP or Base64 text file) and place it in `gradle/wrapper/` immediately before running commands. The binary itself is ignored by Git to avoid PR upload errors.
2. Vendor an Android SDK (matching the app's `compileSdk` 36) somewhere inside the repo (for example `tools/android-sdk/`) and
   add a checked-in `local.properties` that points `sdk.dir` to that relative path. Only the required pieces—
   `platforms/android-36/android.jar`, `build-tools/36.0.0`, and `platform-tools/`—are needed for unit tests.
3. If you rely on libraries that are not already part of the repo, create a local Maven repository directory (e.g.
   `third_party/m2repository`) with the AAR/JAR artifacts you use and add it to `repositories { maven { url = uri("../third_party/m2repository") } }` in `settings.gradle.kts`.

Once those artifacts exist in the repository, the sandbox can resolve dependencies without reaching external hosts and Gradle
tasks (including tests) will succeed.

> **Prefer an unpacked SDK, but ZIP + script is acceptable.**
>
> If you commit a compressed SDK archive alongside a script (for example `scripts/unpack-sdk.sh`), the assistant can run the
> script to expand it before invoking Gradle. Just make sure the script restores the expected directory layout (e.g.,
> `tools/android-sdk/platforms/...`) and that `local.properties` points `sdk.dir` at the post-extraction location.

## Exact steps to recreate the passing `assembleDebug`

Use the checklist below whenever you want to reproduce the same successful build the assistant achieved earlier:

1. **Sync to the updated branch** that already includes the dependency upgrades and resource fixes (e.g., `please_work`).
2. **Generate `gradle/wrapper/gradle-wrapper.jar`** by running
   `gradle wrapper --gradle-version 8.14.3 --distribution-type=bin` on a networked machine, then copy the resulting JAR into the
   repo locally (the `.gitignore` keeps it out of commits).
3. **Decide how Gradle runs offline:**
   * Preferred: rely on the regenerated wrapper and simply execute `./gradlew`.
   * Alternative: unpack a Gradle 8.14.3 distribution into `tools/gradle-8.14.3/` and call
     `tools/gradle-8.14.3/bin/gradle`.
4. **Provide the Android SDK Platform 36 and Build Tools 36.0.0.** Vendor the unpacked directories under `tools/android-sdk/`
   so `platforms/android-36/`, `build-tools/36.0.0/`, and `platform-tools/` all exist. If you store the SDK as a ZIP, include a
   script that extracts to those paths before the build.
5. **Create `local.properties`** (copied from `local.properties.example`) containing at least:
   ```properties
   sdk.dir=tools/android-sdk
   SUPABASE_URL=https://hthlqwwiwkpiugdeyatz.supabase.co
   SUPABASE_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imh0aGxxd3dpd2twaXVnZGV5YXR6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjAzNzAyNTIsImV4cCI6MjA3NTk0NjI1Mn0.7MSb_QSkBKHgMF7QWVGsZNcw5T930uzvhD838u2M8XE
   ```
   Update `sdk.dir` if you used a different location and keep this file untracked.
6. **(Optional)** Deploy the Supabase SQL + Edge Functions so AI overrides respond in real time; this is not required for
   compilation but provides end-to-end parity.
7. **Run the build command:**
   ```bash
   ./gradlew clean assembleDebug
   ```
   or, if you are using the vendored distribution, run
   `tools/gradle-8.14.3/bin/gradle --no-daemon --console=plain -Dorg.gradle.jvmargs="-Xmx4g" clean assembleDebug`.

Completing these steps ensures the sandbox (and any other offline environment) reproduces the previous successful
`assembleDebug` result.

### What the existing JVM tests exercise

Although instrumented UI tests require a device, the project includes JVM-based unit tests that validate the key behaviour users
care about before you run the app on hardware:

| Test file | Focus |
| --- | --- |
| `TrustCalculatorTest` | Confirms trust scores respond to override streaks, honoured deals, and natural decay. |
| `OverrideNegotiatorTest` | Ensures large extension requests trigger AI counter-offers and deals when trust is moderate or low. |
| `UsageScheduleEngineTest` | Checks that quiet hours and per-app schedules block usage with configurable grace windows. |
| `OfflineFallbackTest` | Verifies override decisions fall back to cached policies if Supabase/Gemini are temporarily unavailable. |

Running these tests (`./gradlew test`) gives confidence that when someone exceeds a self-defined limit, the AI review flow asks
for justification and applies the expected negotiation rules.

## Prerequisites

* Android Studio Giraffe or newer
* JDK 17
* Android SDK Platform 36 + Build Tools 36.0.0
* Supabase project with the SQL + Edge Functions from this repo

## Local configuration

1. Copy `local.properties.example` to `local.properties` (or edit the existing file) and add:

```
SUPABASE_URL=https://YOURPROJECT.supabase.co
SUPABASE_KEY=YOUR_ANON_KEY
```

> **Why `local.properties` stays local**
>
> The file often contains machine-specific paths (e.g., `sdk.dir`) and Supabase credentials. Committing it would leak secrets
> and break other developers' setups. The repo therefore provides only `local.properties.example`; create your own copy locally
> with the actual values when you set up the project.

2. (Optional) Adjust the default override cap by adding `DEFAULT_OVERRIDE_CAP_MINUTES` in `local.properties` if desired.
3. If `./gradlew` fails because `gradle/wrapper/gradle-wrapper.jar` is absent, generate it once with a local Gradle install:

```
gradle wrapper --gradle-version 8.14.3
```

After the command succeeds, copy the newly created `gradle-wrapper.jar` into `gradle/wrapper/` locally before running Gradle commands.

## Running the app

1. Sync Gradle in Android Studio.
2. Build & run the `app` module on a device running Android 8.0+.
3. Grant Usage Access, Accessibility, and Notification permissions when prompted.

## End-to-end device testing checklist

Follow this sequence to exercise the full override flow on a physical phone:

1. **Provision Supabase backend**
   1. Create a Supabase project (free tier works) and note the project URL.
   2. In the Supabase SQL editor, paste and run the contents of `supabase_schema.sql` from this repo.
   3. In the `supabase/functions/ai_negotiation` folder, deploy the Edge Function with `supabase functions deploy ai_negotiation` and set
      the `GEMINI_API_KEY` secret (`supabase secrets set GEMINI_API_KEY=...`).
   4. Deploy the coaching helper from `supabase/functions/coach_tips` with `supabase functions deploy coach_tips` (it uses the same secret if present).

2. **Configure the Android project**
   1. Copy `local.properties.example` to `local.properties`.
   2. Fill in `SUPABASE_URL` and `SUPABASE_KEY` with the values from your Supabase project (anon key).
      * If you're using the Supabase project shared for this build, copy the following into
        `local.properties`:

        ```properties
        SUPABASE_URL=https://hthlqwwiwkpiugdeyatz.supabase.co
        SUPABASE_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imh0aGxxd3dpd2twaXVnZGV5YXR6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjAzNzAyNTIsImV4cCI6MjA3NTk0NjI1Mn0.7MSb_QSkBKHgMF7QWVGsZNcw5T930uzvhD838u2M8XE
        ```
   3. Optional: add `DEFAULT_OVERRIDE_CAP_MINUTES=<number>` if you want a custom daily override cap while testing.
   4. If you generated a new `gradle-wrapper.jar`, keep it alongside the project locally (no commit is needed because the file is ignored).

3. **Build & install on the device**
   1. Enable Developer Options and USB/Wi-Fi debugging on the phone.
   2. Connect the device to Android Studio (or run `adb devices` to confirm it is detected).
   3. Select the device target and click **Run** in Android Studio, or execute `./gradlew installDebug` from the terminal.
   4. Wait for the `AI-Enhanced Screen Time Manager` app to appear on the device.

4. **Complete onboarding & limits**
   1. Launch the app and step through onboarding: choose focus goals, pick apps/categories, and assign daily limits or quiet
      hours. All subsequent enforcement uses these values.
   2. Add at least one “Future Me” note so you can see adaptive reminders later.

5. **Grant required system permissions**
   1. When the app prompts for Usage Access, tap **Open Settings** → enable access for the app.
   2. Enable the accessibility service so the Shield screen can appear over blocked apps.
   3. Approve POST_NOTIFICATIONS (Android 13+) so you receive foreground/service reminders.
   4. Allow the app to ignore battery optimisations when prompted so the monitoring service survives in the background.

6. **Trigger an override scenario**
   1. Use a tracked app until you exceed the configured daily limit or quiet hour.
   2. The Shield activity will appear; tap **Request more time** and supply a reason and number of minutes.
   3. The app calls the Supabase `ai_negotiation` function, displays the Gemini-reviewed summary, and surfaces the grant/deny or
      counter-offer decision.
   4. Accept or decline the negotiated deal and confirm completion when applicable (e.g., breathing exercise).

7. **Verify data & trust adjustments**
   1. Return to the Home screen to view remaining time, trust score changes, and recent overrides.
   2. In the Supabase dashboard, inspect the `override_requests` and `trust_state` tables to confirm the device upload.
   3. Use Settings → **Export data** to download a JSON snapshot or **Delete my data** to wipe local/remote stores when finished.

8. **Optional weekly coaching test**
   * Run `adb shell cmd jobscheduler run -f <package> 1001` (replace `<package>` with the app ID) to force the WorkManager job
     that invokes the `coach_tips` function. Check the returned suggestions in the Weekly Coach screen.

Following these steps on real hardware demonstrates limit enforcement, AI-mediated overrides, data synchronisation, and cleanup.

### Building APKs/AABs

Because the repository root is already the Android project root, you can build release artifacts directly from here:

```bash
./gradlew :app:assembleRelease   # Release APK (configure signing before uploading)
./gradlew :app:bundleRelease     # Android App Bundle for Play Store
```

No separate `android/` folder is required—the `app/` module encapsulates the Android client.

### Identity & accounts

Out of the box, the Android client is device-scoped: Room, DataStore, and the trust/difficulty engines operate on a single local profile whose `userId` defaults to `"local"`. You can ship the guardian experience, build APK/AAB artifacts, and exercise the AI override flow without implementing Supabase Auth or any login UI. Each install maintains its own usage/override history, and the Settings “delete/export” actions touch only on-device data unless you add remote sync.

If you later introduce multi-device sync, hook up Supabase Auth (email, magic link, OAuth, etc.), cache the signed-in `user_id`, and include it in your Postgrest mutations. The bundled Edge Function calls already work with the anon key and do not require an authenticated session.

## Weekly coach worker

The `WeeklyCoachWorker` is scheduled via WorkManager (configure in your Application class or a startup routine) to call the Supabase `coach_tips` edge function. The worker uses the Supabase anon key from `local.properties` and respects the trust/difficulty logic baked into the repositories.

## Data deletion/export

The repositories expose methods (`CoachRepository.addNote`, `OverridesRepository.observe`, etc.) that can be used in Settings flows to export JSON or wipe both local and remote data. Stub UI actions are included for wiring.

## Testing

```
./gradlew test
./gradlew connectedAndroidTest
```

## Architecture overview

* **Presentation:** Compose navigation screens (Onboarding, Home, Shield, Weekly Coach, Settings) managed by `HomeViewModel` with flows.
* **Domain:** Trust & difficulty engines (`TrustCalculator`, `OverrideNegotiator`, `UsageScheduleEngine`).
* **Data:** Room entities/DAOs, DataStore preferences, Supabase Function client for Gemini interactions.
* **Background:** Foreground accessibility service + WorkManager weekly job.

## Permissions & hardening

The app surfaces status cards (see Home/Settings stubs) when permissions are revoked, runs a sticky foreground service, and enforces a configurable daily override cap (`BuildConfig.DEFAULT_DAILY_OVERRIDE_CAP`). Client-side rate limiting and cooldown policies can be layered on `OverridesRepository.submitOverride` for production deployments.
