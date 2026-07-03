### Session 2 — 3 July 2026
**Did:**
- Ran the API locally with H2 first (fast sanity check) — confirmed JSON output
- Installed Docker, fixed a group-permission issue with `newgrp docker`
- Ran `docker compose up --build` — full build took ~7 minutes first time (Maven deps + image pulls)
- Confirmed real Postgres connection in logs, hit /api/schedule, got real JSON back
- Phase 1 officially complete — Week 4 checkpoint passed

**Learned:**
- Image vs container distinction (Dockerfile is the recipe, container is the running instance)
- Why docker-compose's `DB_HOST: db` works — Docker's internal DNS resolves service names
- What Load Balancers and CloudFront actually do and why they matter at scale
- Read Hibernate SQL logs to trace a request end-to-end through the whole stack

**Next session:** Start Phase 2 — Retrofit setup in the Android app