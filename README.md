# Questlog

Questlog — Your verified gaming history. Save any game the moment you discover it, prove your completions on-chain. Letterboxd for gaming, built for Solana Mobile.

## The core loop

1. See a game you like — screenshot it (or copy its link) in any app.
2. Share it straight into Questlog from the system share sheet.
3. Questlog reads the screenshot on-device (OCR, no upload) and matches it against RAWG.
4. Add it to your library — free, no wallet, no account.
5. Mark it completed and rate it when you're done.
6. Verify the completion on-chain via Phantom — a permanent, verifiable Proof of Play.

Every day, tap **Clock In** to keep your streak alive (Warm → Flamekeeper → Inferno → Legend).

## Proof-of-Play v1 — open spec

A verified completion is a Solana transaction carrying a single Memo v2 instruction. The memo is a UTF-8 JSON string (not base58/base64 — those are used only for the transaction wire format and signature display):

```json
{ "v": 1, "app": "questlog", "title": "Palworld", "rawgId": 718135, "completedAt": 1760000000, "rating": 4.5 }
```

- Memo program: `MemoSq4gqABAXKb96qnH8TysNcWxMyWCqXgDLGmfcHr` (Memo v2)
- `completedAt` is Unix epoch seconds; `rating` is the player's 1–5 rating; `rawgId` is the RAWG game id.
- RPC: the transaction is built against the latest finalized blockhash, signed by the wallet (Mobile Wallet Adapter), sent, and polled until `finalized`.

## Why it wins

| Criteria | Feature |
|---|---|
| Stickiness | Clock In + streak tiers (Warm, Flamekeeper, Inferno, Legend) give a daily reason to return. |
| UX | A walletless, cozy capture → library flow with zero blockchain jargon. |
| Innovation | The screenshot → OCR → on-chain Proof-of-Play pipeline turns an everyday screenshot into a verifiable achievement. |
| Presentation | A tight, honest 60-second demo — capture a game, complete it, verify it. |

## How to run

1. Put a RAWG API key in `local.properties` at the repo root:
   ```properties
   RAWG_API_KEY=your_rawg_api_key_here
   ```
2. Use JDK 17 (`export JAVA_HOME=/path/to/jdk-17`).
3. Build:
   ```
   ./gradlew assembleDebug
   ```

## Screenshots

> _Placeholder — add capture/verify/Clock In screenshots here._

## Roadmap

Pro tier is a v2 roadmap item.
