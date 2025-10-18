# ai_negotiation Edge Function

This Deno function routes Gemini reasoning and negotiation responses to the Android app via Supabase Functions.

## Deployment

1. Ensure `GEMINI_API_KEY` is set in your Supabase project's Edge Function environment variables:

```
supabase functions secrets set GEMINI_API_KEY=your-key-here
```

2. Deploy the function:

```
supabase functions deploy ai_negotiation --project-ref your-project-ref
```

3. (Optional) Deploy the weekly coach variant by reusing the same handler with `{"mode":"coach"}` payloads.

## Request format

```
POST /functions/v1/ai_negotiation
{
  "reason": "Need to finish a work task",
  "context": "{\"app\":\"com.example\"}"
}
```

To request weekly tips:

```
{
  "mode": "coach",
  "weeklyUsage": "{\"totalMinutes\":210}"
}
```

## Response schema

* Negotiation response matches the contract described in the Android repository (`NegotiationContract`).
* Coach response returns `{ "tips": ["..."] }`.

The function enforces a lightweight rate limit using Deno KV per client identifier.
