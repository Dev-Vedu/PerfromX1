# Project & Employee Performance Management System

Spring Boot + Thymeleaf + MySQL. Covers the full workflow from project creation
through task assignment, progress tracking, deadline extension requests, review,
performance rating, productivity scoring and reward suggestions — plus the AI code
optimization chatbot.

## Requirements

- JDK 17 or newer
- MySQL 8 running on `localhost:3306`
- IntelliJ IDEA (Community is fine)
- Internet access on first run so Maven can download dependencies

## Getting it running

1. Unzip, then in IntelliJ: **File → Open** and select the `pms` folder (the one
   containing `pom.xml`). Choose *Open as Project* / *Trust Project*. Wait for the
   Maven import to finish.
2. Enable annotation processing for Lombok:
   **Settings → Build, Execution, Deployment → Compiler → Annotation Processors →
   Enable annotation processing.** Install the Lombok plugin if IntelliJ prompts you.
3. Create the database (or let the connection string create it):
   ```sql
   CREATE DATABASE pms_db;
   ```
4. Set your MySQL username and password in `src/main/resources/application.properties`
   (defaults are `root` / `root`).
5. Run `PmsApplication`, then open <http://localhost:8080>.

Tables are created automatically by Hibernate (`ddl-auto=update`), and demo data is
seeded on the first empty start.

## Demo accounts

| Username | Password   | Role         |
|----------|------------|--------------|
| `lead`   | `lead123`  | Project lead |
| `hr`     | `hr123`    | HR / Admin   |
| `priya`  | `priya123` | Employee     |
| `arjun`  | `arjun123` | Employee     |
| `sneha`  | `sneha123` | Employee     |

Sign in as `lead` first — that account owns the seeded projects and tasks.

## Turning on the Claude features

Everything works without an API key: the insight generator, risk notes and the code
chatbot all fall back to local heuristics so the app never breaks. To get real model
output, set an environment variable before running:

```
CLAUDE_API_KEY=sk-ant-...
```

In IntelliJ: **Run → Edit Configurations → Environment variables**.

The model name lives in `application.properties` under `claude.model`. If the API
rejects it, check the current model IDs in Anthropic's docs and update that one line.

`pom.xml` also carries a commented-out Spring AI block. The project talks to the
Claude API directly through `ClaudeClient` (a plain `RestClient`), so Spring AI is
optional — uncomment it only if you want the `ChatClient` abstractions.

## Where things are

```
com.pms.entity        User, Project, Task, ExtensionRequest, PerformanceReview,
                      WorkLog, Attendance, Notification, CodeReview
com.pms.repository    Spring Data JPA repositories
com.pms.service       Business logic: tasks, extensions, performance scoring,
                      reports, notifications, insights, code review, scheduler
com.pms.controller    Thymeleaf-backed MVC controllers
com.pms.dto           Forms and read models (ProductivityScore, ProjectHealth, …)
com.pms.config        Security, Claude properties, web config
resources/templates   Thymeleaf views, layout fragment in fragments/layout.html
resources/static      app.css and app.js
```

## Workflow map

| Stage | Where it lives |
|-------|----------------|
| Project creation | `/projects/new`, lead only |
| Team management | `/projects/{id}` → add or remove members |
| Task assignment | `/tasks/new` with dates, priority, description |
| Task execution | `/tasks` for employees |
| Progress tracking | Sign-out prompt (`fragments/layout.html`) → `/tasks/bulk-progress` |
| Extension request | `/tasks/{id}` → request extension with justification |
| Request review | `/extensions` → approve or reject with a comment |
| Task completion | Submit for review → lead completes or requests revision |
| Performance rating | `/tasks/{id}` → quality, timeliness, punctuality, professionalism |
| Analytics | `/performance`, `/performance/{id}` |
| Rewards | `/rewards`, suggestions only |
| Automation | `AutomationScheduler` — reminders, risk detection, daily digest |
| Code chatbot | `/chatbot` |

## Notes

- Passwords are BCrypt-hashed. The seeded ones above are plain text only in the seeder.
- `spring.thymeleaf.cache=false` is on for development; turn it on for production.
- Reward suggestions are advisory. Nothing changes a salary automatically.
