# Academic Solutions

A Java 17+ Spring Boot dashboard for tracking academic progress, peer learning, and career readiness. It includes all ten modules from the project brief and stores records in an embedded H2 database.

This starter has no sign-in or role-based access control. Do not enter real student data or make the dashboard publicly available with real records until authentication and student-data privacy requirements are added for your institution.

## Run

Install Java 17+ and Maven, then run:

```sh
mvn spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080). The H2 console is at [http://localhost:8080/h2-console](http://localhost:8080/h2-console) with JDBC URL `jdbc:h2:file:./academic-solutions-db`, username `sa`, and a blank password.

Locally, the database is file-backed H2 and remains in the project directory between restarts. The dashboard provides record creation and deletion for all modules; CGPA is calculated as a credit-weighted average of academic grade points, and semester average is calculated from entered semester assessment scores.

## Free hosting: Render + Namecheap

The project is prepared for a free Render web service using the built-in H2 database. The `render` Spring profile uses a local H2 file, but Render Free has an ephemeral filesystem: records can be lost whenever the service restarts, redeploys, or spins down after inactivity. This setup is for demos only, not for retaining student records.

1. Push this project to a GitHub repository you control.
2. In [Render](https://dashboard.render.com/), create a **Blueprint** from that GitHub repository and approve the free service in `render.yaml`. No database credentials are required.
3. After the Render service is live, open its **Settings → Custom Domains** and add `learnzy.org`. Render will also add `www.learnzy.org` and redirect it to the root domain.
4. In Namecheap **Domain List → Manage → Advanced DNS**, remove existing `AAAA` records and conflicting root `A`/`www` records. Add an `A Record` with host `@` and value `216.24.57.1`, and a `CNAME Record` with host `www` and value `<your-render-service>.onrender.com`. Replace the CNAME value with the exact Render service hostname shown in its dashboard.
5. Return to Render and verify the custom domain. Render provisions HTTPS after DNS propagates.

The H2 console is disabled in the hosted `render` profile. The dashboard has no sign-in or role-based access control; do not enter real student data or leave real records publicly accessible.

## Modules

1. LeetCode Tracker
2. GitHub Repo Evaluator
3. Academic Score Tracker
4. Semester-wise Progress
5. Area-of-Interest Finder
6. Personalised Mock Tests
7. Peer Space
8. Career Advancement Platform
9. Company-Specific Preparation
10. Campus Hiring Mock Readiness

## API

- `GET /api/modules` — module definitions
- `GET /api/records` — all activity records
- `GET /api/records?module=academic-score` — records for one module
- `POST /api/records` — create a record
- `DELETE /api/records/{id}` — delete a record
- `GET /api/stats` — dashboard totals, weighted CGPA, and semester average
