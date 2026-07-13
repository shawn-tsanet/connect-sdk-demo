# Connect SDK Demo — User Guide

How to drive the demo app itself. For build/start/stop mechanics see the
[Runbook](RUNBOOK.md); this guide assumes the server is running at
http://localhost:8090 (or a tunneled/hosted URL).

> **Status note.** Everything below reflects the app as built. Flows marked
> *unverified* have not yet run against live BETA data because no member
> credentials were available when this guide was written — behavior is
> implemented per the OpenAPI contract but not runtime-proven.

## 1. What this demonstrates

The demo tells the member integration story end-to-end on top of
`connect-library` (the Connect SDK): a member's engineer authenticates,
finds a partner company, fills the partner's own process form, submits a
collaboration request, and works the case through its lifecycle — without
ever leaving their (simulated) tooling. It exists to show prospective
members what the SDK gives them out of the box.

## 2. The header badge

The badge in the top-right corner is the connection truth:

| Badge | Meaning |
|---|---|
| Green — "Company — email" | Authenticated; shows who BETA thinks you are |
| Amber — "Not configured" | No credentials saved yet (Settings tab) |
| Amber — "Auth failed: Connect API returned 500 — Error processing request" | BETA rejected the credentials. The API's legacy error mode returns 500 (not 401) for bad logins, so this is almost always a wrong username/password |

## 3. Settings — credentials

1. Open the **Settings** tab
2. Enter the BETA member username and password → **Save Credentials**
3. The badge goes green and every other tab comes alive

Credentials persist to a mode-600 properties file on the server host
(`~/.tsanet-demo-ui/credentials.properties` locally; ephemeral `/tmp` in the
container). They are never committed, logged, or returned by the API —
`GET /api/settings` reports only the username. **Clear** wipes the file.

When the app is deployed with the Basic-auth gate enabled
(`TSANET_DEMO_AUTH_PASSWORD` set), the browser prompts for the gate
credentials before the page loads — that gate protects the demo itself and is
unrelated to the BETA member credentials entered inside it.

## 4. Dashboard — collaboration cases

Lists every case visible to your member account, newest activity first.

- **Direction chips**: *outbound* = submitted by your company; *inbound* =
  received from a partner. Computed by comparing each case's companies with
  the identity in the badge. The dropdown filters by direction.
- Click any row to open the case detail view.

## 5. New Collaboration — partner search and the dynamic form

The heart of the demo:

1. **Find a Partner** — search the member directory by name. Results show
   company, department, and (where routing dictates) a specific process form.
2. **Select** — the app fetches that partner's *configured process form* live
   from BETA and renders it: sections, required markers, and field types
   (text, dropdown, date, textarea) exactly as the partner defined them.
3. Fill the standard fields (internal case number, summary, description) plus
   the partner's custom fields, then **Submit Collaboration Request**.
4. On success you get the new case id + a link straight into the case detail.

*Unverified:* custom-field type strings and dropdown-option formatting are
implemented defensively (newline-delimited options first, comma fallback) but
need one live form to confirm rendering. If a field renders as the wrong
input type, that's a one-line mapping fix — report it.

## 6. Case detail — lifecycle, notes, attachments

Opens from the Dashboard or after creating a case.

- **Summary grid** — status, direction, both companies, timestamps, case token.
- **Actions** — contextual by direction:
  - *Inbound* (you are the receiver): **Approve** (with engineer contact +
    next steps), **Reject** (with reason), **Request Info**
  - *Outbound* (you submitted): **Respond to Info Request**, **Close Case**
  - Always: **Add Note** (summary, description, priority)
  
  The demo deliberately does not second-guess which actions the case's current
  state allows — the API is the authority, and its validation errors surface
  in the status line. *(Unverified: exact state-transition rules against live
  cases.)*
- **Notes Timeline** and **Response History** — the full conversation and
  every engineer response on the case.
- **Attachments** — *Load Config* shows the case's attachment configuration;
  when the partner supports it, the upload form forwards files to the case.
  *(Unverified against live BETA.)*

Engineer emails in action forms must be on your member company's registered
domain — the API rejects others (business rule, not a demo bug).

## 7. Webhooks

Manage the member account's webhook subscriptions:

- List subscriptions with active state; **Deliveries** shows the recent
  delivery log per subscription (event, HTTP status, attempt, success).
- **New Subscription** — callback URL + comma-separated event types.
- **Delete** removes a subscription (confirmation prompt).

Note: inbound webhook *receiving* is not part of this demo — it lists and
manages subscriptions and shows TSANet's delivery attempts outward.

## 8. Troubleshooting

| Symptom | Cause / action |
|---|---|
| Everything says "BETA credentials not configured" | No credentials saved — Settings tab |
| Badge: "Auth failed … 500 Error processing request" | Wrong BETA credentials (legacy error mode — 500 means login rejected) |
| Actions fail with "Connect API returned 4xx/5xx — …" | Upstream validation: wrong case state, off-domain engineer email, or the legacy-500 quirk. The response body is shown verbatim |
| Case list loads but partner search errors | Partner search requires a valid session — re-check the badge first |
| Build fails: `cannot find symbol … WebhooksApi` | Sibling `Connect-API-Code` checkout is on the wrong branch — needs `beta` |

## 9. What the demo intentionally does not do

- No inbound webhook receiver (subscriptions management only)
- No multi-user/session handling — one shared SDK session per server instance
- No case assignment, SLA, or reporting views — outside the SDK's surface
- OAuth client-credentials auth — planned upstream (`tsanetgit/Connect_SDK#27`,
  currently on hold); the demo will gain an OAuth mode in Settings when the
  SDK does

## 10. References

- Upstream SDK: `tsanetgit/Connect_SDK` (branch `raw-impl`)
- Transport contract: `tsanetgit/Connect-API-Code`
  `connect-core-api/src/main/resources/openapi.yaml` (branch `beta`)
- Business rules (case lifecycle validity): TSANet Connect GitBook /
  Integration Guide — never inferred from the schema
