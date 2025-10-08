create table if not exists public.users (
    id uuid primary key default gen_random_uuid(),
    email text unique not null,
    created_at timestamptz default now()
);

create table if not exists public.app_limits (
    id uuid primary key default gen_random_uuid(),
    user_id uuid references public.users(id) on delete cascade,
    type text not null,
    package_or_category text not null,
    daily_minutes integer not null,
    schedules_json jsonb not null default '{}'::jsonb,
    grace_seconds integer not null default 60,
    created_at timestamptz default now()
);

create table if not exists public.override_requests (
    request_id uuid primary key default gen_random_uuid(),
    user_id uuid references public.users(id) on delete cascade,
    pkg text not null,
    requested_mins integer not null,
    user_reason text,
    ai_summary jsonb,
    context_json jsonb,
    decision text,
    granted_mins integer,
    difficulty text,
    deal jsonb,
    deal_due_at timestamptz,
    deal_completed_at timestamptz,
    created_at timestamptz default now()
);

create table if not exists public.trust_state (
    user_id uuid primary key references public.users(id) on delete cascade,
    score integer not null default 60,
    last_update timestamptz default now(),
    recent_overrides integer not null default 0,
    mismatches integer not null default 0,
    honored_deals integer not null default 0,
    on_track_streak integer not null default 0,
    off_track_streak integer not null default 0
);

create table if not exists public.habit_insights (
    id uuid primary key default gen_random_uuid(),
    user_id uuid references public.users(id) on delete cascade,
    tips jsonb not null,
    created_at timestamptz default now()
);

alter table public.users enable row level security;
alter table public.app_limits enable row level security;
alter table public.override_requests enable row level security;
alter table public.trust_state enable row level security;
alter table public.habit_insights enable row level security;

create policy "Users can select own data" on public.users
    for select using (auth.uid() = id);

create policy "Users can manage their limits" on public.app_limits
    for all using (auth.uid() = user_id) with check (auth.uid() = user_id);

create policy "Users can manage override requests" on public.override_requests
    for all using (auth.uid() = user_id) with check (auth.uid() = user_id);

create policy "Users can manage trust" on public.trust_state
    for all using (auth.uid() = user_id) with check (auth.uid() = user_id);

create policy "Users can read insights" on public.habit_insights
    for select using (auth.uid() = user_id);

create policy "Users can insert insights" on public.habit_insights
    for insert with check (auth.uid() = user_id);
