# VERIFIED SOLANA FACTS — B-session source of truth

Written 2026-10-03. Every fact below was extracted from real, working source code —
not from memory. Supersedes any "pin from sample-app-kotlin" instruction in the
Build Bible (that repo is dead; org renamed `solanamobile` → `solana-mobile`).

## Reference implementation

- Repo: `solana-mobile/solana-kotlin-compose-scaffold` (successor of `sample-app-kotlin`)
- Status: studied end-to-end (D4 gate Q3 closed)
- **LICENSE: NONE in repo → PATTERNS ONLY, never verbatim code.** Adapt to our
  architecture; the Maven libraries themselves are Apache 2.0 (verified in POM).
- Local mirror: `Desktop/questlog-references/scaffold/` (12 files, for Qoder reference attachments)
- Scaffold stack is OLDER than ours (Kotlin 1.9 / AGP 8.1 / kapt) — transplanting
  would downgrade the toolchain. Patterns only.

## LOCKED dependency trio (proven compatible together — D4 gate Q2 closed)

```kotlin
implementation("com.solanamobile:web3-solana:0.2.2")                    // tx building
implementation("com.solanamobile:rpc-core:0.2.3")                       // RPC client
implementation("com.solanamobile:mobile-wallet-adapter-clientlib-ktx:2.0.0") // Phantom signing
implementation("io.github.funkatronics:multimult:0.2.0")                // base58/encoding
// NO Ktor — we implement HttpNetworkDriver over our existing OkHttp (zero new HTTP deps)
```

Fallback ladder (only if a pin fails on Kotlin 2.0.21): clientlib-ktx `2.0.0` → `2.0.8`
(last 2.0.x patch). Never jump minors mid-hackathon.

## RPC driver (ruling)

rpc-core expects `com.solana.networking.HttpNetworkDriver`:
`override suspend fun makeHttpRequest(request: HttpRequest): String`.
`HttpRequest` carries `url`, `method`, `properties` (headers), `body`.
We write `OkHttpHttpDriver` (~20 lines) in `core/wallet/` — no Ktor.

## Memo instruction (D4 gate Q5 closed)

- Program ID: `MemoSq4gqABAXKb96qnH8TysNcWxMyWCqXgDLGmfcHr` (Memo v2)
- Encoding: **UTF-8 JSON string** via `message.encodeToByteArray()` — not base58/base64.
  Base58 is used only for tx wire format + signature display.
- Canonical payload (Build Bible ruling #3):

```json
{ "v": 1, "app": "questlog", "title": "Palworld", "rawgId": 58175, "completedAt": 1760000000, "rating": 4.5 }
```

- Imports from web3-solana: `com.solana.publickey.SolanaPublicKey`,
  `com.solana.transaction.{AccountMeta, Message, Transaction, TransactionInstruction}`

## Transaction ordering (mandatory)

1. Fetch latest blockhash (`LatestBlockhashUseCase` pattern) — FIRST
2. Build `Message` with memo instruction
3. MWA sign (Activity-scoped, ViewModel-owned)
4. Send (`SendTransactionsUseCase` pattern)
5. Poll `getSignatureStatuses` — 30,000 ms deadline; success = status
   `finalized`; failure = `err != null` or timeout → FAILED state + retry button

## D4 gate scoreboard

| Q | Item | Status |
|---|---|---|
| Q1 | RAWG curl proof (search no description / detail has `description_raw`) | **BLOCKED — key in local.properties is INVALID (401). Captain: new free key from rawg.io/apidocs** |
| Q2 | Exact artifacts + versions | ✅ LOCKED (above) |
| Q3 | Study reference end-to-end | ✅ Done (scaffold) |
| Q4 | Devnet sanity tx (faucet SOL, latency) | Pending — Oct 4 on device |
| Q5 | Memo encoding = UTF-8 JSON | ✅ Confirmed |
| Q6 | Failure UX: FAILED → retry, no auto-retry; wallet-reject friendly; 30s timeout | Decided (Build Bible) |
| Q7 | Local fallback law: proof fields null, game saved, never blocks | Decided (Build Bible) |
| Q8 | FileProvider + share-card render | Re-verify at A5 |
| Q9 | `identityUri` = URL we control | GitHub repo URL (after push) |
