# AI-Enhanced Screen Time Manager (Android)

This module contains the Android client built with Jetpack Compose, Hilt, Room, and Supabase.

## Prerequisites

* Android Studio Giraffe or newer
* JDK 17
* Android SDK 34
* Supabase project with the SQL + Edge Functions from this repo

## Local configuration

1. Copy `local.properties.example` to `local.properties` (or edit the existing file) and add:

```
SUPABASE_URL=https://YOURPROJECT.supabase.co
SUPABASE_KEY=YOUR_ANON_KEY
```

2. (Optional) Adjust the default override cap by adding `DEFAULT_OVERRIDE_CAP_MINUTES` in `local.properties` if desired.
3. If the Gradle wrapper jar is missing, generate it once with a local Gradle install:

```
gradle wrapper
```

## Running the app

1. Sync Gradle in Android Studio.
2. Build & run the `app` module on a device running Android 8.0+.
3. Grant Usage Access, Accessibility, and Notification permissions when prompted.

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
