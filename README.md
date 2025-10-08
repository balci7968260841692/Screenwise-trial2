# AI-Enhanced Screen Time Manager

This repository contains everything needed to run the AI-Enhanced Screen Time Manager Android client plus the Supabase backend artifacts.

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
3. Open the project in Android Studio and sync Gradle. If the Gradle wrapper jar is missing, run `gradle wrapper` once on a machine with Gradle installed.

### (e) Run the app
1. Build the `app` module and install on a device running Android 8.0 or newer.
2. Grant Usage Access, Accessibility, Notification, and optional Calendar/Location permissions when requested.
3. The app starts a foreground service with a persistent notification to protect limits. Use the Home screen to view remaining time, submit override requests, and launch the Weekly Coach.

## Project structure

```
app/                         Android app module (Jetpack Compose + Hilt + Room)
functions/ai_negotiation/    Supabase Edge Function (Gemini proxy + README)
functions/coach_tips/        Minimal Gemini helper for weekly coaching
supabase_schema.sql          Database schema and RLS policies
README_ANDROID.md            Android-specific build instructions
PRIVACY_POLICY.md            Store-ready privacy policy
PLAY_LISTING_DRAFT.md        Draft Google Play listing copy
```

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
