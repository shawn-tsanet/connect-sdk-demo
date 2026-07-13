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

1. Open http://localhost:8090 → **Settings** tab — one card per environment (BETA / DEV)
2. Enter the member username and password on the right card → **Save Credentials**; **Make Active** to switch environments
3. The header badge should flip to green with your company name (that's `/api/me` succeeding against the active environment)

DEV (`connect2.tahoelab.us`) already has working credentials in the ops `.env` (`TSANET_DEV_API_*`, company 1110 "Test Shawn") plus the loadtest UAT accounts. BETA awaits real member credentials.

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

## 6. Optional: hosted on AWS (container ready, service not provisioned)

Decision is on-demand App Runner in `us-west-2` (see [aws-hosting-options.md](aws-hosting-options.md)). The Dockerfile and Basic-auth gate are built and verified; the ECR repo + App Runner service get created on first deploy day.

**Auth gate** — the app is open when `TSANET_DEMO_AUTH_PASSWORD` is unset (local use). Any hosted deploy MUST set it:

```bash
TSANET_DEMO_AUTH_USER=<pick>  TSANET_DEMO_AUTH_PASSWORD=<pick>
```

Visitors then get a browser password prompt before anything loads. `/healthz` stays open for App Runner health checks.

**Build + test the image locally** (jar must be built first, step 1 — the Docker build only packages it):

```bash
cd /Users/shawnray/projects/shawn-tsanet-connect-sdk-demo
docker build --platform linux/amd64 -t connect-sdk-demo .
docker run --rm -p 8090:8090 -e TSANET_DEMO_AUTH_USER=shawn -e TSANET_DEMO_AUTH_PASSWORD=<pick> connect-sdk-demo
```

(`--platform linux/amd64` is required for App Runner — it does not run arm64 images.)

**Provisioned 2026-07-13** (verified end-to-end, normally kept PAUSED):

- Service URL: `https://zzp9cuim4t.us-west-2.awsapprunner.com` (gate user `shawn`; password in `~/.tsanet-demo-ui/apprunner-gate-password.txt`)
- Resume before a demo (takes ~1 min), pause after — paused = no compute billing:
  ```bash
  aws apprunner resume-service --service-arn arn:aws:apprunner:us-west-2:806878963871:service/connect-sdk-demo/babbd4a41c474a5190dee635a9332193
  aws apprunner pause-service  --service-arn arn:aws:apprunner:us-west-2:806878963871:service/connect-sdk-demo/babbd4a41c474a5190dee635a9332193
  ```
- Ship a new build: rebuild the jar (step 1), then
  ```bash
  docker build --platform linux/amd64 -t connect-sdk-demo .
  aws ecr get-login-password | docker login --username AWS --password-stdin 806878963871.dkr.ecr.us-west-2.amazonaws.com
  docker tag connect-sdk-demo:latest 806878963871.dkr.ecr.us-west-2.amazonaws.com/connect-sdk-demo:latest
  docker push 806878963871.dkr.ecr.us-west-2.amazonaws.com/connect-sdk-demo:latest
  aws apprunner start-deployment --service-arn arn:aws:apprunner:us-west-2:806878963871:service/connect-sdk-demo/babbd4a41c474a5190dee635a9332193
  ```
- The CLI session expires roughly daily — `aws login` (as `tsanet-demo-cli`, incognito if the browser holds a root session) before any of the above.
- Container filesystem is ephemeral: re-enter member credentials in Settings after every deploy/resume.

### Managing the service in the AWS Console

Everything lives in the App Runner console in **us-west-2 (Oregon)** — check the
region picker; other regions will show no services. Direct link:
`https://us-west-2.console.aws.amazon.com/apprunner/home?region=us-west-2#/services`
→ click `connect-sdk-demo`.

Sign in as **`tsanet-demo-cli`** (its policies cover App Runner, ECR, and logs;
access-denied banners on unrelated pages are expected). Root is only needed for
IAM changes.

| Action | Where |
|---|---|
| Pause / Resume | **Actions** menu (same effect as the CLI commands above) |
| Deploy latest image from ECR | **Deploy** button (= `start-deployment`) |
| Startup/lifecycle events, Spring Boot output | **Logs** tab (Event logs / Application logs via CloudWatch) |
| Traffic, latency, instance count | **Metrics** tab |
| Gate env vars, CPU/memory, health check | **Configuration** tab → Edit (any change triggers a redeploy) |
| Public URL | "Default domain" on the service overview |

Container images are managed separately: Console → **ECR** → repositories →
`connect-sdk-demo` (same region). App Runner pulls `:latest` on each deploy.

> **Do not delete the service** to turn it off — the public URL is minted per
> service, so delete + recreate changes the URL everywhere it's been shared.
> Pause is the off switch. This matters doubly now: App Runner stopped
> accepting new customers 2026-04-30 (existing services stay operational), so
> a deleted service may not be recreatable — see the deprecation note in
> [aws-hosting-options.md](aws-hosting-options.md). Successor when needed:
> ECS Express Mode, fed by the same ECR image.

## 7. Shutdown

```bash
# Ctrl+C in the server/ngrok terminals, or:
pkill -f demo-ui-0.0.1-SNAPSHOT.jar
pkill -f ngrok
```

Credentials stay on disk for next time unless you clear them in Settings.
