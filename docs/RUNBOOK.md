# Demo startup runbook

How to bring the Connect SDK Demo up from cold, once you have BETA member credentials. All paths are absolute; every command is copy-paste-ready.

## One-time prerequisites (already done on the Mac mini)

- JDK 21 (Homebrew, keg-only — hence the `JAVA_HOME` pin below)
- Sibling spec repo at `~/projects/Connect-API-Code` with the **`beta` branch** checked out (the `develop` branch breaks the build — V2 webhook APIs)
- ngrok installed and authenticated (only needed for sharing a public URL)
- AWS CLI configured as `tsanet-demo-cli` (only needed for hosted deploys — nothing provisioned yet)

## 1. Build (skip if no code changed since last build)

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21
export PATH="$JAVA_HOME/bin:$PATH"
cd /Users/shawnray/projects/shawn-tsanet-connect-sdk-demo
mvn -q -pl connect-library -am install -DskipTests
mvn -q -pl demo-ui -am package -DskipTests
```

If the build fails with `cannot find symbol ... WebhooksApi`, the sibling spec repo is on the wrong branch:

```bash
cd /Users/shawnray/projects/Connect-API-Code && git checkout beta
```

## 2. Start the server

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21
"$JAVA_HOME/bin/java" -jar /Users/shawnray/projects/shawn-tsanet-connect-sdk-demo/demo-ui/target/demo-ui-0.0.1-SNAPSHOT.jar
```

Server listens on **http://localhost:8090**. Leave this terminal running; Ctrl+C stops it.

## 3. Enter credentials (first time, or after clearing)

1. Open http://localhost:8090 → **Settings** tab
2. Enter the BETA member username and password → **Save Credentials**
3. The header badge should flip to green with your company name (that's `/api/me` succeeding against `connect2.tsanet.org`)

Credentials persist to `~/.tsanet-demo-ui/credentials.properties` (mode 600, never in git) — subsequent startups skip this step. **Settings → Clear** wipes them.

Badge decoder:
- **Green "Company — email"** — authenticated, everything live
- **Amber "Not configured"** — no credentials saved yet
- **Amber "Auth failed: Connect API returned 500 — Error processing request"** — BETA rejected the credentials (its legacy error mode returns 500, not 401; wrong password looks like this)

## 4. First-session verification sweep (once, with the first real credentials)

Two things were built against assumptions that need one live payload to confirm — run through these and note anything that renders wrong:

1. **New Collaboration** → search a partner → select → check the process form: do dropdowns show one option per line (options delimiter), and do all field types render as sensible inputs?
2. **Case detail** on any existing case → do the lifecycle action buttons match what the case state actually allows?

Report anomalies to Claude — the fixes are one-liners in `app.js` field-type mapping.

## 5. Optional: public URL for a live demo (ngrok)

```bash
ngrok http 8090
```

Copy the `https://*.ngrok-free.dev` URL it prints. Caveats (free tier): visitors see a one-click interstitial page first, and the URL changes every restart. Kill with Ctrl+C when the call is done — don't leave the tunnel up unattended.

## 6. Optional: hosted on AWS (not yet built)

Decision is on-demand App Runner in `us-west-2` (see [aws-hosting-options.md](aws-hosting-options.md)). The containerization + deploy pipeline hasn't been built yet — when you want it, refresh the CLI session with `aws login` and ask Claude to provision; that session will need a Dockerfile, an ECR repo, and an App Runner service (pause between demos to stop billing).

## 7. Shutdown

```bash
# Ctrl+C in the server/ngrok terminals, or:
pkill -f demo-ui-0.0.1-SNAPSHOT.jar
pkill -f ngrok
```

Credentials stay on disk for next time unless you clear them in Settings.
