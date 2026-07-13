# connect-sdk-demo

Private demo of the **TSANet Connect SDK**: a branded web application that
exercises the full SDK surface — partner discovery, dynamic process forms, the
complete collaboration-case lifecycle, attachments, and webhooks — against the
TSANet Connect **BETA** and **DEV** environments, switchable at runtime from
the Settings tab.

Seeded from `tsanetgit/Connect_SDK` branch `raw-impl`; the upstream modules are
kept as-is and the demo is layered on top.

## Modules

| Module | Origin | What it is |
|---|---|---|
| `connect-library` | upstream | The SDK: facades over the generated OpenAPI client, SQLite offline cache |
| `TSANet-integration-app` | upstream | Interactive CLI console (`tsa>`) over the SDK |
| `TSANet-integration-demo` | upstream | Scenario runner (Acme/Beta scripted flows) |
| `demo-ui` | **this repo** | Branded web demo: Spring Boot REST layer + tabbed SPA |

## Quick links

- **[User Guide](docs/USER_GUIDE.md)** — what the demo does and how to drive it
- **[Runbook](docs/RUNBOOK.md)** — build, start, credentials, tunnel, shutdown
- **[AWS hosting options](docs/aws-hosting-options.md)** — on-demand hosting decision
- **[Porting scope](docs/porting-scope.md)** — Python/JS/TS follow-on scoping

## Build prerequisite (unusual)

`connect-library` generates its API client from the OpenAPI spec in a **sibling
checkout**: `../Connect-API-Code` must exist with the **`beta` branch** checked
out. See the [Runbook](docs/RUNBOOK.md) for the exact commands.

## Environments

Selectable in-app (Settings tab); credentials and the SQLite cache are kept
separately per environment.

| Environment | Base URL | Notes |
|---|---|---|
| **BETA** (default) | `https://connect2.tsanet.net` | Java Connect API, beta contract |
| **DEV** | `https://connect2.tahoelab.us` | Java Connect API DEV deployment. NOT `api.tahoelab.us` — that's the separate Laravel app and 404s `/v1` |
| Production | `https://connect2.tsanet.org` | Deliberately not selectable — never point the demo here |
