# Hosting options for the Connect demo UI

Research only — nothing provisioned. `demo-ui` is a single Spring Boot jar (Tomcat embedded, serves its own static frontend) that calls out to `connect2.tsanet.org` (BETA) over HTTPS. No inbound webhook receiver, no persistent state beyond a local SQLite cache file.

## AWS options

| Option | Fit | Notes |
|---|---|---|
| **App Runner** | Best fit | Point at the GitHub repo or a container image, it builds/deploys/scales automatically, gives you a public HTTPS URL out of the box. Cheapest to operate for a low-traffic demo (scales to zero-ish, pay per use). No VPC/ALB/cluster to manage. |
| **ECS Fargate** | Overkill for this | Container orchestration for a single stateless jar is more moving parts (task def, service, ALB, security groups) than the app needs. Reach for this only if the demo grows into multiple services. |
| **Elastic Beanstalk** | Viable, dated | Handles the jar deploy + load balancer + scaling for you, but AWS has been steering new workloads toward App Runner/ECS; more legacy console surface to learn for a one-off demo. |
| **Amplify** | Wrong shape | Built for static/SSR frontends with Lambda functions, not a monolithic Spring Boot backend. Would force splitting frontend/backend for no benefit here. |
| **EC2** | Avoid | Full manual ops (patching, TLS termination, process supervision) for something App Runner does managed. Only worth it if you need arbitrary long-running processes or non-HTTP ports. |

**Recommendation: AWS App Runner.** Lowest operational overhead for a single branded demo jar, built-in HTTPS, autoscaling down to near-zero cost between demos, straightforward source (GitHub) or image-based deploy.

## Alternatives outside AWS

- **Fly.io** — one-command deploy of a Docker image, generous free tier, very low friction for a demo. Worth considering if you want to skip AWS account/IAM setup entirely.
- **Railway / Render** — similar "push a repo, get a URL" experience to App Runner, arguably even less config.
- **Local + tunnel (ngrok/Cloudflare Tunnel)** — zero hosting cost, good for a live walkthrough on a call, not suitable for an always-on link you hand out.

## Open questions for Shawn

- Is this demo link something you want live indefinitely (persistent hosting) or only spun up around specific customer/prospect calls (on-demand)?
- Any preference to keep this inside your existing AWS account (Q4 AWS collaboration-model work) vs. a lighter-weight platform, given it's unrelated to that integration effort?
