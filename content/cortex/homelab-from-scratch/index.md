---
title: Homelab from Scratch
summary: A from-scratch guide to building a four-node Kubernetes homelab — private WireGuard mesh, hardened cloud edge, automatic TLS, GitOps, sealed secrets, a CI/CD pipeline, and a recovery runbook for the day everything goes wrong.
---

## What you're about to build

By the last chapter you will have:

- four Ubuntu 24.04 machines — three at home, one in the cloud — wired into a private WireGuard mesh
- a K3s cluster with Calico networking, where every workload runs only where you tell it to
- exactly one machine on the public internet, fronting everything else through a hardened Traefik edge
- automatic TLS via Let's Encrypt + Cloudflare DNS-01 — no manual cert renewals, ever
- Argo CD watching a Git repo and syncing your apps without you
- Sealed Secrets so you can commit production credentials to that repo and sleep at night
- PostgreSQL pinned to one worker; Keycloak on top of it for identity
- GitHub Actions building images and shipping them straight into the cluster
- a recovery runbook that takes you from cold metal back to a running cluster, layer by layer

The demo workload is **whoami** — a 5 MB Go service that echoes its environment back at you. It's the simplest thing that exercises the whole path: DNS → edge → ingress → service → pod. If whoami works, every app you deploy after will work the same way.

## The shape of it

```d2
direction: down

internet: Internet {
  shape: cloud
}

edge: Cloud edge — vm-1 {
  shape: rectangle
  traefik: Traefik\n:80 :443\nhostNetwork
}

mesh: WireGuard mesh — 172.27.15.0/24 {
  shape: package
}

home: Home LAN — 192.168.15.0/24 {
  shape: rectangle
  ms1: ms-1\nK3s server
  wk1: wk-1\nPostgres
  wk2: wk-2\nArgo CD
}

internet -> edge.traefik: TLS *.homelab.example
edge -> mesh: encrypted tunnel
mesh -> home.ms1
mesh -> home.wk1
mesh -> home.wk2
```

The rule that runs through every chapter: **only the edge is on the public internet.** The home boxes never receive a packet from outside the mesh. That single constraint shapes the firewall rules, the ingress topology, the certificate flow, and how you'd bring this back from the dead at three in the morning.

## How to read this book

The chapters build on each other. If you're rebuilding from cold metal, follow them top-to-bottom — every chapter assumes the previous one is done.

| Section | What you'll do |
|---|---|
| **[1. Foundations](/cortex/homelab-from-scratch/foundations-why-a-homelab)** | Decide why, plan the architecture, gather the hardware. |
| **[2. Domain & DNS](/cortex/homelab-from-scratch/domain-and-dns-buy-a-domain-on-godaddy)** | Buy a domain, move DNS to Cloudflare, mint an API token for cert-manager. |
| **[3. The Nodes](/cortex/homelab-from-scratch/the-nodes-pick-your-hardware)** | Pick boxes, install Ubuntu, harden SSH, prep the kernel. |
| **[4. Private Mesh](/cortex/homelab-from-scratch/private-mesh-why-a-private-mesh)** | Build a four-peer WireGuard mesh that ignores the public internet. |
| **[5. Kubernetes Base](/cortex/homelab-from-scratch/kubernetes-base-why-k3s)** | K3s server, Calico CNI, three workers, node labels and taints. |
| **[6. The Edge](/cortex/homelab-from-scratch/the-edge-pin-traefik-to-the-edge)** | Traefik on hostNetwork, edge firewall, cert-manager, your first whoami URL. |
| **[7. Secrets & GitOps](/cortex/homelab-from-scratch/secrets-and-gitops-sealed-secrets)** | Sealed Secrets, Argo CD, app-of-apps, GitHub Actions image pipeline. |
| **[8. Stateful Services](/cortex/homelab-from-scratch/stateful-services-postgresql-on-a-pinned-node)** | Postgres on a pinned node, network policies, Keycloak identity. |
| **[9. Operate & Recover](/cortex/homelab-from-scratch/operate-and-recover-quick-health-check)** | Daily health check, backups, the cold-metal recovery runbook. |

About **two hours** of reading, plus a weekend or two of doing.

## Conventions

- Every command runs as **root** unless noted; one `sudo -i` at the top of a chapter is the simplest mental model.
- The placeholder domain is **`homelab.example`** (RFC 2606 reserved). Find-and-replace with your own.
- Public-IP placeholders use the documentation ranges: **`203.0.113.10`** for the home WAN, **`198.51.100.25`** for the cloud edge.
- Private addresses match the real cluster behind these docs: home LAN `192.168.15.0/24`, WireGuard mesh `172.27.15.0/24`. They're harmless to publish.
- Node names are real: **`ms-1`** (control-plane, home), **`wk-1`** and **`wk-2`** (workers, home), **`vm-1`** (edge, cloud).
- Anything tagged **footgun** is something I have personally stepped on.

Onward to [Why a homelab?](/cortex/homelab-from-scratch/foundations-why-a-homelab) →
