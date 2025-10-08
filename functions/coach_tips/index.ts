/// <reference lib="deno.unstable" />
import { serve } from "https://deno.land/std@0.208.0/http/server.ts";
import { Status } from "https://deno.land/std@0.208.0/http/http_status.ts";

const GEMINI_API_KEY = Deno.env.get("GEMINI_API_KEY");
if (!GEMINI_API_KEY) {
  console.error("Missing GEMINI_API_KEY environment variable");
}

interface CoachPayload {
  weeklyUsage?: string;
}

interface CoachContract {
  tips: string[];
}

async function callGemini(prompt: string): Promise<string> {
  const response = await fetch(`https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=${GEMINI_API_KEY}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      contents: [{ role: "user", parts: [{ text: prompt }] }],
    }),
  });
  if (!response.ok) {
    throw new Error(`Gemini error ${response.status}`);
  }
  const data = await response.json();
  const text = data.candidates?.[0]?.content?.parts?.[0]?.text;
  if (!text) {
    throw new Error("Empty Gemini response");
  }
  return text;
}

function sanitizeJson(text: string): CoachContract {
  const trimmed = text.trim();
  const start = trimmed.indexOf("{");
  const end = trimmed.lastIndexOf("}");
  if (start === -1 || end === -1) {
    throw new Error("Gemini response missing JSON");
  }
  return JSON.parse(trimmed.slice(start, end + 1)) as CoachContract;
}

serve(async (req) => {
  if (req.method !== "POST") {
    return new Response("Method not allowed", { status: Status.MethodNotAllowed });
  }
  try {
    const body = (await req.json()) as CoachPayload;
    const prompt = `Weekly usage: ${body.weeklyUsage ?? "{}"}. Respond with JSON {"tips":["12-20 word actionable tip", "..."]}.`;
    const result = sanitizeJson(await callGemini(prompt));
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
