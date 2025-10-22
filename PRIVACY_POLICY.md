# Privacy Policy for AI-Enhanced Screen Time Manager

_Last updated: 2024-02-22_

AI-Enhanced Screen Time Manager ("the App") helps you build healthier digital habits. We respect your privacy and collect only the minimum data needed to provide the service.

## Data we collect

* **App usage metadata:** package identifiers, foreground duration, and timestamps. No message content, screenshots, or keystrokes are ever captured.
* **Limit configurations:** categories, schedules, and grace periods you configure.
* **Override requests:** your written reason, requested minutes, AI summaries, and decision outcomes.
* **Trust telemetry:** aggregate counters (override streaks, honored deals, mismatch totals).
* **Future Me notes:** affirmations you write to yourself.

All data is stored locally on your device and optionally in your Supabase project if you authenticate. You control the Supabase backend and keys.

## Permissions

* **Usage Access** to read app usage totals.
* **Accessibility Service** to display a guard screen over restricted apps (no keylogging).
* **POST_NOTIFICATIONS** to show reminders and deal follow-ups.
* **Optional**: Calendar, coarse location, and Do Not Disturb access to improve context-aware coaching. These are disabled by default.

## How we use data

* Enforce app/category limits, quiet hours, and grace timers.
* Evaluate override requests with the help of Gemini via a Supabase Edge Function. Only your reason and context snapshot JSON are transmitted.
* Track trust score trends and adapt negotiation difficulty.
* Generate weekly habit summaries and Future Me reminders.

## Data retention & deletion

Data stays on-device unless you export or sync with Supabase. You can delete all data at any time via **Settings → Delete my data**, which wipes local Room/Datastore storage and sends a delete call to your Supabase tables.

## Security

* All outbound requests use HTTPS.
* Gemini API keys are stored server-side in Supabase Edge Functions, never in the client app.
* Row Level Security policies ensure each Supabase user only accesses their records.

## Children’s privacy

The App is designed for general audiences and should not be used by children under 13 without parental guidance.

## Changes

We may update this policy as features evolve. Updates will be posted in-app and on the project README.

## Contact

For questions or feedback, contact the maintainers of your Supabase deployment or open an issue in the project repository.
