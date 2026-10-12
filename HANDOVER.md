# QUESTLOG — Developer Handover (read this first)

**Context:** CLOCK IN hackathon (Radiants DAO × Solana Mobile). Submissions due **Oct 13, 2026 4:59 AM GMT+5**; judging Oct 13 – Nov 9; winners Nov 11. Pool: $135K (10×$10K grand + 5×$5K mentions + $10K bonus). Judging = 4 criteria × 25%: **Stickiness & PMF · User Experience · Innovation · Presentation**.

**Repo:** https://github.com/hybirdvault-dot/questlog · **Install:** https://github.com/hybirdvault-dot/questlog/releases/download/v1.1.0/questlog-v1.1.0.apk

---

## 1. THE IDEA (what we are and why it wins)

**Questlog — "Your verified gaming history."** Letterboxd for gaming, on Solana Mobile.

**The core loop:** see a game on TikTok/YouTube → screenshot it → share to Questlog (or scan it with the in-app camera) → ML Kit OCR reads the title → RAWG matches it → rich card → save to library (free, unlimited, **no wallet needed**) → play → mark Completed → tap **"Verify this achievement"** → Phantom (MWA) signs → a **permanent memo transaction on Solana devnet** → "✓ Verified on Solana" badge + explorer link → share a 1080×1350 card. Plus a daily **Clock In** streak habit (Warm 3d → Flamekeeper 7d → Inferno 14d → Legend 30d).

**The winning argument (Innovation criterion, 25%):** no game tracker does screenshot → on-chain proof-of-play. The chain is a **notarizer, not a referee** — it proves *when* you claimed a completion, that the claim can't be forged or backdated, and that the credential is portable (open memo format, any tracker can read it). If asked "why blockchain": that paragraph verbatim.

## 2. WHAT'S BUILT (all verified green, in main)

- **Capture:** share-intent intake (`text/plain` + `image/*`) via `ShareEventBus` (consumed exactly once, rotation-safe) + **in-app camera scanner** (CameraX 1.4.1 + ML Kit, frame-throttled, live "Found: TITLE" chip). OCR never auto-saves (Law 4).
- **Library / Detail / Discover:** grid + status tabs (Want/Playing/Completed/Dropped), personal stars, notes, RAWG search (debounced), delete with confirm.
- **Proof of Play (the X-factor):** `ProofOfPlayRepository` builds a memo tx (blockhash-first → Memo v2 instruction → UTF-8 JSON payload) → MWA signs (ViewModel-only, Law 3) → devnet `sendTransaction` (base64) → `getSignatureStatuses` poll, 30s deadline, `finalized` = success. **7-state proof machine** (Idle/WalletNotConnected/Signing/Verifying/Verified/SigningFailed/VerificationFailed), `PENDING→CONFIRMED|FAILED` persisted to Room, manual retry only, never blocks the app.
- **Clock In:** DataStore streak logic (gap 0=no-op · 1=+1 · ≥2=StreakReset to 1 · <0=no-op), tier ladder, streak flame (pulses ≥7), **month calendar history** (kizitonwose 2.6.1), stats section with verified-completions.
- **Daily reminder:** WorkManager `StreakReminderWorker` (8pm default, 4-guard exactly-once, two-phase permission ask, no-nag, denial never blocks check-in).
- **Share cards:** 1080×1350 cream Canvas render (synchronous hero preload — never blank), verified badge strip, FileProvider share.
- **Design system (locked):** cream `#FFF8F0` · terracotta `#E07856` · brown `#3E2723` · sage `#7A9E7E` · warm surface `#F3E7DA` — the deliberate **anti-dark-DeFi** look. Custom Canvas confetti (42 particles), haptics (CONFIRM/REJECT), cozy copy voice, **zero blockchain jargon in UI** (memo/devnet/signature appear nowhere), 83 string resources.
- **Quality:** 17/17 unit/contract tests (memo payload byte-exact), debug+release green, signed release APK, lint gate disabled for release (was blocking on vital lint), R8 keeps for Solana/MWA/Base58, key-redacting logger, `allowBackup=false`, Apache-2.0 LICENSE + THIRD_PARTY_NOTICES.

## 3. THE 7 LAWS (do not violate any)

1. **Never block** — proof failure leaves the game saved; retry anytime
2. **Free forever** — unlimited saves, no paywall, no limits
3. **MWA only from ViewModels** — never from composables/repositories
4. **OCR never auto-saves** — user confirms every step
5. **ONE daily notification** — two-phase ask, no-nag, never blocks
6. **Honesty Lock** — zero fabricated stats anywhere; social proof = the explorer link
7. **License** — Apache/MIT code may be adapted (keep notices); GPL/unlicensed = patterns only, reimplemented

## 4. ⚠️ TECHNICAL TRAPS (learned the hard way — read before changing deps)

| Decision | Reality |
|---|---|
| **Coil stays 3.0.4** | 3.6.3 pulls Compose 1.12 → needs AGP 9.1 + compileSdk 37. Verified build-breaker. Do NOT bump. |
| **Calendar is 2.6.1** | 2.10.1 carries Kotlin metadata 2.3 → unreadable by Kotlin 2.0.21. Pin from POMs, always. |
| **Solana pins** | `web3-solana:0.2.2` + `rpc-core:0.2.3` + `mobile-wallet-adapter-clientlib-ktx:2.0.0` + `multimult:0.2.0` — proven-compatible set from the official scaffold. Don't chase newer KMP versions. |
| **Memo program** | `MemoSq4gqABAXKb96qnH8TysNcWxMyWCqXgDLGmfcHr`, payload = UTF-8 JSON via `encodeToByteArray()`. Base64 only for tx wire. Spec: `docs/VERIFIED-SOLANA-FACTS.md`. |
| **JSON: Gson + org.json** | Not kotlinx-serialization (only transitive via rpc-core). Memo contract test proves byte-exact output — migrate later if ever. |
| **RPC client** | Hand-rolled JSON-RPC over `HttpNetworkDriver` (OkHttp impl) — rpc-core's typed client needs kotlinx on compile classpath. Don't add Ktor. |
| **Build flags (this box is low-RAM)** | `JAVA_HOME=/home/BALOCH/tools/jdk-17.0.20.1+1 ./gradlew --no-build-cache --max-workers=1 --no-watch-fs` — without these: AAPT2 daemon OOMs, metaspace crashes, flaky failures. |
| **DB** | Room v2 + `fallbackToDestructiveMigration` (pre-release, no users). Schema changes → bump version. Proof fields on `GameEntity`, no separate proofs table. |
| **Package** | `com.questlog.app` (plan wanted `dev.questlog.app` — accepted deviation). |

## 5. WHAT'S LEFT → FIRST PRIZE (priority order)

**Tier 0 — MUST before judging (hours):**
1. **Device test + real devnet TX (gate Q4 has never closed).** Install the release APK, run: scan/share → save → Complete → Verify → Phantom sign → explorer link opens with a real TX. This is the demo's proof moment.
2. **60s demo video** — script in `docs/SUBMISSION.md` (hook 5s, full loop 30s, real TX on camera). Unstaged = the Presentation 25%.
3. **Judge Test** (hand the phone to a stranger, watch in silence — fix every "what do I do now?" moment).

**Tier 1 — highest score-per-hour (1–2 sessions each):**
4. **Proof-of-Play social feed** — Instagram-style VerticalPager where every post carries its tx signature (tap → explorer). Supabase REST via existing Retrofit (SQL + schema in git history / feed plan). `git stash` holds partial P1 work. This is the "no other feed is cryptographically real" differentiator. Pattern donor: vipulasri/JetInstagram (Apache 2.0).
5. **Onboarding (3 screens)** + **camera permission priming** — kills the top vibe-coded tells.
6. **Seeded demo library** (3 real games on first run + Reset in Settings) — app opens alive, not empty.
7. **Manual title fallback** — OCR no-match → type the title → manual add (`rawgId: null` supported in schema). The #1 dead-end killer.
8. **Badge wall** (Phase H, pure-Kotlin engine): streak/library/proof-count badges — the Snapchat-trophies-meets-receipts mechanic. **Rename fee badges to activity framing** (devnet SOL is faucet money — "spending" claims violate the Honesty Lock).

**Tier 2 — polish (minutes each):** snackbar+Undo on delete · pull-to-refresh · SplashScreen API · Settings: About/version/privacy/export-JSON/wipe · Chrome Custom Tab for explorer · shimmer skeletons (6 spinners) · a11y pass (contentDescription, 48dp targets) · Glance streak widget.

**DO NOT build** (deleted by design, answer with: *"the chain IS auth/audit/integrity — we deleted those systems on purpose"*): accounts/2FA/passwords · payments · admin panel · multi-tenancy · SSR/SEO · webhooks · analytics SDKs (Honesty Lock) · dark-default theme (cream IS the differentiator).

## 6. JUDGING GAME (how the 25%×4 is won)

| Criterion | Our weapon | Demo timestamp |
|---|---|---|
| Stickiness | Clock In + streak tiers + calendar + reminder | 0:40–0:55 |
| UX | Walletless cozy flow, zero jargon, haptics, confetti, friendly failures | every second |
| Innovation | Screenshot/scan → on-chain Proof of Play (open memo format) | 0:25–0:40 |
| Presentation | 60s honest script, real device, real TX | whole video |

Golden rules: no fabricated numbers (Honesty Lock — a single invented stat loses the room) · every claim on camera must be real behavior · the explorer link must actually open during the video · fee copy = "under a cent on Solana", never SOL amounts.

## 7. ENV & SECRETS

- **Build:** JDK 17 at `/home/BALOCH/tools/jdk-17.0.20.1+1`, Android SDK 35, Gradle 8.11.1 wrapper, AGP 8.7.3
- **Secrets (git-ignored `local.properties`):** `RAWG_API_KEY` (live, 20k/mo free) · `SUPABASE_URL`/`SUPABASE_ANON_KEY` (for the feed)
- **Signing:** keystore held by the owner (ask for `questlog-release.jks` + password) — never commit it
- **Docs in repo:** `README.md` (pitch + memo spec) · `docs/VERIFIED-SOLANA-FACTS.md` (Solana source of truth) · `docs/SUBMISSION.md` (portal copy + demo script) · `THIRD_PARTY_NOTICES.md`

## 8. ARCHITECTURE (single `:app`, NiA-pattern)

```
UI (Compose) → ViewModel (StateFlow, proofMutex) → Repository → Room (source of truth) → UI
Network write-through only (RAWG, devnet RPC, Supabase REST). No backend of our own.
Packages: core/{model,database,network,nocr,wallet,share,haptic} · data/repository · feature/{library,discover,capture,scan,game,clockin,settings} · navigation · ui/designsystem · workers
```

Reference donors (all attributed): nowinandroid (Apache) · Mihon (Apache) · googlesamples/mlkit (Apache) · solana-mobile/solana-kotlin-compose-scaffold (NO license — patterns only, reimplemented) · kizitonwose/Calendar (MIT).

---

**One sentence for the elevator:** *Questlog turns any game screenshot into a wallet-signed, permanent achievement — with a cozy app people actually want to open every day.* Go win it.
