-- MANUAL NEON / POSTGRESQL ONLY. Never executed by Spring Boot.
-- Source: project-owner verification of https://kasiefm971.co.za/shows.html
-- Monday through Sunday, 2026-09-13. Presenters/descriptions are unverified.
-- Run the WHOLE file, with psql ON_ERROR_STOP or in a transactional SQL editor.
-- Replacement changes show IDs. Review consumers of existing IDs beforehand.
-- Intentionally refuses reruns if the named backup table already exists.
BEGIN;

-- Stop concurrent schedule edits while snapshotting/replacing this small dataset.
LOCK TABLE public.shows IN ACCESS EXCLUSIVE MODE;

-- Refuse replacement when other tables reference shows, including CASCADE FKs.
-- This avoids silently deleting related records or leaving reassigned references.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint
               WHERE contype = 'f' AND confrelid = 'public.shows'::regclass) THEN
        RAISE EXCEPTION 'Shows has referencing foreign keys; review dependencies before replacement';
    END IF;
END $$;

-- Full data snapshot, including original IDs and any legacy NULL days.
-- Do not use IF NOT EXISTS: a previous backup must never be reused/overwritten.
CREATE TABLE public.shows_backup_before_verified_20260913 AS
    TABLE public.shows;

ALTER TABLE public.shows ADD COLUMN IF NOT EXISTS day_of_week varchar(9);

-- No DROP, TRUNCATE, identity reset, or sequence reset.
DELETE FROM public.shows;

INSERT INTO public.shows (day_of_week, name, start_time, end_time, presenter, description)
VALUES
    ('MONDAY', 'Whisper in the Dark', '00:00', '03:00', NULL, NULL),
    ('MONDAY', 'Vuka Kasie', '03:00', '06:00', NULL, NULL),
    ('MONDAY', 'Asiye 6-9 Breakfast Show', '06:00', '09:00', NULL, NULL),
    ('MONDAY', 'Morning Essentials', '09:00', '12:00', NULL, NULL),
    ('MONDAY', 'Semphete', '12:00', '15:00', NULL, NULL),
    ('MONDAY', 'Home Drive with Napo', '15:00', '18:00', NULL, NULL),
    ('MONDAY', 'Kasie Talk', '18:00', '21:00', NULL, NULL),
    ('MONDAY', 'Late Night Affair', '21:00', '00:00', NULL, NULL),
    ('TUESDAY', 'Whisper in the Dark', '00:00', '03:00', NULL, NULL),
    ('TUESDAY', 'Vuka Kasie', '03:00', '06:00', NULL, NULL),
    ('TUESDAY', 'Asiye 6-9 Breakfast Show', '06:00', '09:00', NULL, NULL),
    ('TUESDAY', 'Morning Essentials', '09:00', '12:00', NULL, NULL),
    ('TUESDAY', 'Semphete', '12:00', '15:00', NULL, NULL),
    ('TUESDAY', 'Home Drive with Napo', '15:00', '18:00', NULL, NULL),
    ('TUESDAY', 'Kasie Talk', '18:00', '21:00', NULL, NULL),
    ('TUESDAY', 'Late Night Affair', '21:00', '00:00', NULL, NULL),
    ('WEDNESDAY', 'Whisper in the Dark', '00:00', '03:00', NULL, NULL),
    ('WEDNESDAY', 'Vuka Kasie', '03:00', '06:00', NULL, NULL),
    ('WEDNESDAY', 'Asiye 6-9 Breakfast Show', '06:00', '09:00', NULL, NULL),
    ('WEDNESDAY', 'Morning Essentials', '09:00', '12:00', NULL, NULL),
    ('WEDNESDAY', 'Semphete', '12:00', '15:00', NULL, NULL),
    ('WEDNESDAY', 'Home Drive with Napo', '15:00', '18:00', NULL, NULL),
    ('WEDNESDAY', 'Kasie Talk', '18:00', '21:00', NULL, NULL),
    ('WEDNESDAY', 'Late Night Affair', '21:00', '00:00', NULL, NULL),
    ('THURSDAY', 'Whisper in the Dark', '00:00', '03:00', NULL, NULL),
    ('THURSDAY', 'Vuka Kasie', '03:00', '06:00', NULL, NULL),
    ('THURSDAY', 'Asiye 6-9 Breakfast Show', '06:00', '09:00', NULL, NULL),
    ('THURSDAY', 'Morning Essentials', '09:00', '12:00', NULL, NULL),
    ('THURSDAY', 'Semphete', '12:00', '15:00', NULL, NULL),
    ('THURSDAY', 'Home Drive with Napo', '15:00', '18:00', NULL, NULL),
    ('THURSDAY', 'Kasie Talk', '18:00', '21:00', NULL, NULL),
    ('THURSDAY', 'Late Night Affair', '21:00', '00:00', NULL, NULL),
    ('FRIDAY', 'Whisper in the Dark', '00:00', '03:00', NULL, NULL),
    ('FRIDAY', 'Vuka Kasie', '03:00', '06:00', NULL, NULL),
    ('FRIDAY', 'Asiye 6-9 Breakfast Show', '06:00', '09:00', NULL, NULL),
    ('FRIDAY', 'Morning Essentials', '09:00', '12:00', NULL, NULL),
    ('FRIDAY', 'Semphete', '12:00', '15:00', NULL, NULL),
    ('FRIDAY', 'Home Drive with Napo', '15:00', '18:00', NULL, NULL),
    ('FRIDAY', 'The Weekend Takeover', '18:00', '21:00', NULL, NULL),
    ('FRIDAY', 'Club 971', '21:00', '00:00', NULL, NULL),
    ('SATURDAY', 'Midnight Express', '00:00', '03:00', NULL, NULL),
    ('SATURDAY', 'Kusempondo Zankomo', '03:00', '06:00', NULL, NULL),
    ('SATURDAY', 'Scoreline Show', '06:00', '09:00', NULL, NULL),
    ('SATURDAY', 'Ezakwantu', '09:00', '12:00', NULL, NULL),
    ('SATURDAY', 'Urban Chart Show', '12:00', '15:00', NULL, NULL),
    ('SATURDAY', 'The Lifestyle Corner', '15:00', '18:00', NULL, NULL),
    ('SATURDAY', 'The Weekend Takeover', '18:00', '21:00', NULL, NULL),
    ('SATURDAY', 'Club 971', '21:00', '00:00', NULL, NULL),
    ('SUNDAY', 'Midnight Express', '00:00', '03:00', NULL, NULL),
    ('SUNDAY', 'Kusempondo Zankomo', '03:00', '06:00', NULL, NULL),
    ('SUNDAY', 'Asimdumise', '06:00', '09:00', NULL, NULL),
    ('SUNDAY', 'Centre Stage', '09:00', '12:00', NULL, NULL),
    ('SUNDAY', 'Seven Colours', '12:00', '15:00', NULL, NULL),
    ('SUNDAY', 'Soul Food', '15:00', '18:00', NULL, NULL),
    ('SUNDAY', 'The Revival', '18:00', '21:00', NULL, NULL),
    ('SUNDAY', 'Late Night Affair', '21:00', '00:00', NULL, NULL);

-- Abort and roll back the entire replacement if the dataset shape is wrong.
DO $$
BEGIN
    IF (SELECT count(*) FROM public.shows) <> 56 THEN
        RAISE EXCEPTION 'Expected exactly 56 shows';
    END IF;
    IF (SELECT count(DISTINCT day_of_week) FROM public.shows) <> 7
       OR EXISTS (SELECT 1 FROM public.shows GROUP BY day_of_week HAVING count(*) <> 8)
       OR EXISTS (SELECT 1 FROM public.shows
                  WHERE day_of_week IS NULL OR day_of_week NOT IN
                    ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY')) THEN
        RAISE EXCEPTION 'Expected eight shows on each of seven valid days';
    END IF;
    IF EXISTS (SELECT 1 FROM public.shows
               GROUP BY day_of_week, start_time HAVING count(*) <> 1)
       OR EXISTS (SELECT 1 FROM public.shows
                  WHERE start_time NOT IN ('00:00','03:00','06:00','09:00','12:00','15:00','18:00','21:00')
                     OR end_time <> (start_time + interval '3 hours')::time
                     OR presenter IS NOT NULL OR description IS NOT NULL) THEN
        RAISE EXCEPTION 'Unexpected slot times, duplicates, or unverified metadata';
    END IF;
END $$;

SELECT day_of_week, count(*) AS shows_per_day
FROM public.shows GROUP BY day_of_week ORDER BY day_of_week;
COMMIT;

-- Backup is retained after commit. If any statement fails, ROLLBACK the session
-- before continuing. Sequence values may advance even on rollback; gaps are safe.
