-- MANUAL ONLY. Review and run against the intended database before rollout.
-- Adds a nullable column without assigning days or changing existing rows.
BEGIN;
ALTER TABLE public.shows
    ADD COLUMN IF NOT EXISTS day_of_week varchar(9);
ALTER TABLE public.shows
    ADD CONSTRAINT shows_weekly_day_valid CHECK (
        day_of_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY',
                        'FRIDAY', 'SATURDAY', 'SUNDAY')
    ) NOT VALID;
ALTER TABLE public.shows VALIDATE CONSTRAINT shows_weekly_day_valid;
COMMIT;

-- Run the constraint addition once (skip it if shows_weekly_day_valid exists).
-- Inspect rows needing an explicit, reviewed day assignment:
SELECT id, name, start_time, end_time, day_of_week
FROM public.shows WHERE day_of_week IS NULL ORDER BY id;

-- Assign days using PUT /api/schedule/{id} or separately reviewed per-ID SQL.
-- Do not assign all existing records an arbitrary day or duplicate them blindly.

-- OPTIONAL SECOND STAGE: ONLY after all rows have verified days.
-- This fails safely while any NULL days remain; it does not backfill/delete data.
-- ALTER TABLE public.shows ALTER COLUMN day_of_week SET NOT NULL;
