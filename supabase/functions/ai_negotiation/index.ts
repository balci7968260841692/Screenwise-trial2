/// <reference lib="deno.unstable" />
import { serve } from "https://deno.land/std@0.208.0/http/server.ts";
import { Status } from "https://deno.land/std@0.208.0/http/http_status.ts";

const GEMINI_API_KEY = Deno.env.get("GEMINI_API_KEY");
if (!GEMINI_API_KEY) {
  console.error("Missing GEMINI_API_KEY environment variable");
}

const kv = await Deno.openKv();
const RATE_LIMIT_WINDOW_MS = 60_000;
const RATE_LIMIT_MAX = 10;

interface NegotiationPayload {
  reason?: string;
  context?: string;
}

interface CoachPayload {
  weeklyUsage?: string;
}

type EdgeRequest = NegotiationPayload & CoachPayload & { mode?: "coach" };

interface GeminiResponse {
  candidates?: Array<{
    content?: { parts?: Array<{ text?: string }>; };
  }>;
}

interface NegotiationContract {
  summary: string;
  intent: "work" | "leisure" | "utility";
  urgency: "low" | "med" | "high";
  sincerity: number;
  mismatches: string[];
  alignment: number;
  counterMinutes: number;
  deal?: {
    type: "breathing" | "walk" | "defer" | "curfew";
    description: string;
    verify: "timer" | "manual" | "steps";
  };
  tone: "supportive" | "neutral" | "firmer";
}

interface CoachContract {
  tips: string[];
}

async function enforceRateLimit(identifier: string): Promise<boolean> {
  const now = Date.now();
  const key = ["ratelimit", identifier, Math.floor(now / RATE_LIMIT_WINDOW_MS)];
  const res = await kv.get<number>(key);
  const count = res.value ?? 0;
  if (count >= RATE_LIMIT_MAX) {
    return false;
  }
  await kv.atomic().check(res).set(key, count + 1, { expireIn: RATE_LIMIT_WINDOW_MS }).commit();
  return true;
}

function buildNegotiationPrompt(body: NegotiationPayload): string {
  return `You are a mindful digital wellbeing coach. Analyze the user reason: ${body.reason ?? ""}. Context JSON: ${body.context ?? "{}"}. Respond with JSON in the schema {"summary":"...","intent":"work|leisure|utility","urgency":"low|med|high","sincerity":0..1,"mismatches":["..."],"alignment":0..1,"counterMinutes":number,"deal":{"type":"breathing|walk|defer|curfew","description":"...","verify":"timer|manual|steps"},"tone":"supportive|neutral|firmer"}. Keep response strictly JSON.`;
}

function buildCoachPrompt(body: CoachPayload): string {
  return `You are an AI habit coach. Weekly usage: ${body.weeklyUsage ?? "{}"}. Return JSON {"tips":["12-20 word actionable tip", "..."]}.`;
}

async function callGemini(prompt: string): Promise<string> {
  const response = await fetch(`https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=${GEMINI_API_KEY}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      contents: [
        {
          role: "user",
          parts: [{ text: prompt }],
        },
      ],
    }),
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(`Gemini error ${response.status}: ${text}`);
  }

  const data = (await response.json()) as GeminiResponse;
  const text = data.candidates?.[0]?.content?.parts?.[0]?.text;
  if (!text) {
    throw new Error("Empty response from Gemini");
  }
  return text;
}

function sanitizeJson<T>(text: string): T {
  const trimmed = text.trim();
  const start = trimmed.indexOf("{");
  const end = trimmed.lastIndexOf("}");
  if (start === -1 || end === -1) {
    throw new Error("Response missing JSON object");
  }
  const jsonText = trimmed.slice(start, end + 1);
  return JSON.parse(jsonText) as T;
}

serve(async (req) => {
  try {
    if (req.method !== "POST") {
      return new Response("Method not allowed", { status: Status.MethodNotAllowed });
    }

    const identifier = req.headers.get("x-client-id") ?? req.headers.get("x-forwarded-for") ?? "anonymous";
    const allowed = await enforceRateLimit(identifier);
    if (!allowed) {
      return new Response(JSON.stringify({ error: "rate_limit" }), {
        status: Status.TooManyRequests,
        headers: { "Content-Type": "application/json" },
      });
    }

    const body = (await req.json()) as EdgeRequest;
    if (body.mode === "coach") {
      const prompt = buildCoachPrompt(body);
      const result = sanitizeJson<CoachContract>(await callGemini(prompt));
      return new Response(JSON.stringify(result), {
        status: Status.OK,
        headers: { "Content-Type": "application/json" },
      });
    }

    if (!body.reason) {
      return new Response(JSON.stringify({ error: "reason_required" }), {
        status: Status.BadRequest,
        headers: { "Content-Type": "application/json" },
      });
    }

    const prompt = buildNegotiationPrompt(body);
    const result = sanitizeJson<NegotiationContract>(await callGemini(prompt));
    return new Response(JSON.stringify(result), {
      status: Status.OK,
      headers: { "Content-Type": "application/json" },
    });
  } catch (error) {
    console.error(error);
    return new Response(JSON.stringify({ error: "internal_error" }), {
      status: Status.InternalServerError,
      headers: { "Content-Type": "application/json" },
    });
  }
});
