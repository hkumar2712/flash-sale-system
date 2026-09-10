# FlashBid — Build Tracker

> Distributed Flash-Sale & Real-Time Auction Platform
> Stack: Java, Spring Boot, Kafka, Redis, Postgres, AWS (Free Tier/LocalStack), Docker, Kubernetes, MCP/AI

**How to use this file:** update the `Status` column as we go. Statuses: `TODO`, `IN PROGRESS`, `DONE`, `SKIPPED`.
Commit this file to the repo root as `PROGRESS.md` and update it every session — it doubles as your project log for interviews.

---

## Phase 0 — Foundations (Setup)
| # | Item | Status | Notes |
|---|---|---|---|
| 0.1 | Git repo initialized, `.gitignore`, README skeleton | DONE | Pushed to GitHub via SSH auth |
| 0.2 | Docker + Docker Compose installed & verified | TODO | |
| 0.3 | `user-service`: bare Spring Boot app, `/hello` endpoint | DONE | Verified in browser, Spring Boot 4.1.1, Java 25 |
| 0.4 | Dockerfile for `user-service`, runs in container | TODO | |
| 0.5 | `docker-compose.yml` skeleton (just user-service for now) | TODO | |
| 0.6 | Postgres container added to compose, service connects | TODO | |

## Phase 1 — AuthN / AuthZ
| # | Item | Status | Notes |
|---|---|---|---|
| 1.1 | `User` entity + repository (Spring Data JPA) | TODO | |
| 1.2 | Password hashing (BCrypt), register endpoint | TODO | |
| 1.3 | Login endpoint issuing JWT (access + refresh token) | TODO | |
| 1.4 | Spring Security filter chain for JWT validation | TODO | |
| 1.5 | Role-based access control (BUYER, SELLER, ADMIN) | TODO | `@PreAuthorize` |
| 1.6 | Refresh token rotation + revocation (stored in Redis/DB) | TODO | |
| 1.7 | (Stretch) Swap custom auth for Keycloak (OIDC) | TODO | Optional, do custom first for learning |

## Phase 2 — Catalog Service + DB Locking
| # | Item | Status | Notes |
|---|---|---|---|
| 2.1 | `catalog-service`: bare Spring Boot app | TODO | |
| 2.2 | `Product` / `Inventory` entity with `@Version` column | TODO | Optimistic locking |
| 2.3 | CRUD endpoints for products (admin/seller only) | TODO | |
| 2.4 | "Reserve stock" endpoint using optimistic locking, handle `OptimisticLockException` + retry | TODO | Core concurrency lesson #1 |
| 2.5 | Pessimistic locking variant (`SELECT ... FOR UPDATE`) for comparison | TODO | Benchmark later |
| 2.6 | Pagination + read replicas concept (simulate with 2nd Postgres read-only user) | TODO | |

## Phase 3 — Redis
| # | Item | Status | Notes |
|---|---|---|---|
| 3.1 | Redis container in compose, Spring Data Redis connected | TODO | |
| 3.2 | Cache-aside pattern for product reads | TODO | |
| 3.3 | Distributed lock via Redisson (`RLock`) for stock reservation | TODO | Compare vs DB optimistic lock |
| 3.4 | Lock fencing tokens (prevent stale writes) | TODO | |
| 3.5 | Sliding-window rate limiter (per user/IP) | TODO | |
| 3.6 | Sorted Set leaderboard for live bid ranking | TODO | |
| 3.7 | Idempotency key store (Redis) for order creation | TODO | |

## Phase 4 — Kafka
| # | Item | Status | Notes |
|---|---|---|---|
| 4.1 | Kafka + Zookeeper/KRaft container in compose | TODO | |
| 4.2 | Topics designed: `bid.placed`, `inventory.reserved`, `order.created`, `payment.completed`, `user.activity` | TODO | |
| 4.3 | Producer in `bidding-service` publishing `bid.placed` | TODO | |
| 4.4 | Consumer group in `order-service` | TODO | |
| 4.5 | Partitioning strategy keyed by `itemId` | TODO | Preserve per-item order |
| 4.6 | Dead-letter topic + retry handling | TODO | |
| 4.7 | Saga pattern: order creation orchestration across services | TODO | |

## Phase 5 — Bidding & Order Services
| # | Item | Status | Notes |
|---|---|---|---|
| 5.1 | `bidding-service`: place-bid endpoint | TODO | |
| 5.2 | Concurrency-safe "highest bid" logic (Redis + lock) | TODO | |
| 5.3 | `order-service`: create order from won bid | TODO | |
| 5.4 | Payment stub (fake gateway) + ledger table (ACID) | TODO | |
| 5.5 | Circuit breaker / bulkhead (Resilience4j) between services | TODO | |
| 5.6 | WebFlux non-blocking variant for read-heavy endpoints | TODO | Optional but recommended |
| 5.7 | Locking benchmark: optimistic vs pessimistic vs Redis lock under load | TODO | Document in `docs/locking-benchmark.md` |

## Phase 6 — Containerization
| # | Item | Status | Notes |
|---|---|---|---|
| 6.1 | Dockerfile for every service | TODO | |
| 6.2 | Full `docker-compose.yml` (all services + infra) | TODO | |
| 6.3 | Health checks + startup ordering (`depends_on` + healthcheck) | TODO | |

## Phase 7 — Kubernetes (local: kind/minikube)
| # | Item | Status | Notes |
|---|---|---|---|
| 7.1 | kind/minikube cluster running locally | TODO | |
| 7.2 | K8s manifests: Deployment + Service per microservice | TODO | |
| 7.3 | ConfigMaps + Secrets for config | TODO | |
| 7.4 | Horizontal Pod Autoscaler demo (simulate load) | TODO | |
| 7.5 | Ingress controller for routing | TODO | |

## Phase 8 — Cloud (Free Tier / LocalStack)
| # | Item | Status | Notes |
|---|---|---|---|
| 8.1 | LocalStack running S3, SQS/SNS locally | TODO | |
| 8.2 | S3 for product images | TODO | |
| 8.3 | (Optional, real AWS Free Tier) One-time deploy to ECS Fargate or EKS for demo | TODO | Watch billing alerts! |
| 8.4 | CloudWatch-equivalent: centralized logging (ELK or Loki locally) | TODO | |

## Phase 9 — Recommendation / Self-Learning Service
| # | Item | Status | Notes |
|---|---|---|---|
| 9.1 | `recommendation-service`: consumes `user.activity` from Kafka | TODO | |
| 9.2 | Feature store (simple table/Redis) of user-item interactions | TODO | |
| 9.3 | Baseline collaborative filtering model (batch, offline) | TODO | |
| 9.4 | Incremental/online update loop (retrain every N minutes) | TODO | This is the "self-learning" piece |
| 9.5 | Serve recommendations via cached vectors in Redis | TODO | |

## Phase 10 — AI / MCP Integration
| # | Item | Status | Notes |
|---|---|---|---|
| 10.1 | `mcp-gateway-service`: MCP server scaffold | TODO | |
| 10.2 | Tool: `get_order_status` | TODO | |
| 10.3 | Tool: `check_inventory` | TODO | |
| 10.4 | Tool: `flag_suspicious_bid` (fraud heuristic) | TODO | |
| 10.5 | Wire up an LLM client (Claude) to call these tools | TODO | |

## Phase 11 — Load Testing & Polish
| # | Item | Status | Notes |
|---|---|---|---|
| 11.1 | k6/Gatling script simulating flash-sale spike | TODO | |
| 11.2 | p50/p95/p99 latency report | TODO | |
| 11.3 | Architecture diagram (draw.io / Mermaid) | TODO | |
| 11.4 | Final README with setup instructions | TODO | |

---

## Progress Summary
- **Total items:** 62
- **Done:** 2
- **In Progress:** 0
- **Remaining:** 60

## Session Log
| Date | What we did | Time spent |
|---|---|---|
| | | |
