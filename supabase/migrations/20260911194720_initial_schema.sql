-- Create locations table
CREATE TABLE IF NOT EXISTS public.locations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Set up Row Level Security (RLS)
ALTER TABLE public.locations ENABLE ROW LEVEL SECURITY;

-- Allow anonymous inserts (Since we don't have auth setup yet, anyone with the anon key can insert)
CREATE POLICY "Allow anonymous inserts"
ON public.locations
FOR INSERT
TO anon
WITH CHECK (true);

-- Allow anonymous selects (Optional, for you to read the data)
CREATE POLICY "Allow anonymous selects"
ON public.locations
FOR SELECT
TO anon
USING (true);
