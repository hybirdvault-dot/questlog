# Questlog — Submission Package

CLOCK IN hackathon · Radiants DAO × Solana Mobile · contestant: hybirdvault-dot
Repo: https://github.com/hybirdvault-dot/questlog

## One-line pitch

Questlog — your verified gaming history. Save any game the moment you discover it, prove your completions on-chain. Letterboxd for gaming, built for Solana Mobile.

## Criteria answers (portal, one paragraph each)

### Stickiness & PMF

The core loop is built to become a daily habit in two layers. First, capture: gamers constantly discover games on TikTok, YouTube, and Reddit and forget them within minutes — Questlog turns "share screenshot to app" into a two-second save with confetti and haptics, so the library grows without effort. Second, retention: a daily Clock In with a streak system (Warm at 3 days, Flamekeeper at 7, Inferno at 14, Legend at 30), a tier progress bar, and one gentle 8pm reminder with a two-phase permission ask that never nags and never blocks. Streaks are computed locally from real check-in data — no fabricated numbers anywhere in the product. The collection itself compounds: every saved game with its status, personal rating, and on-chain proofs is a portfolio the user built, and the free-forever law (unlimited games, no paywall, ever) means there is no ceiling where engagement has to stop.

### User Experience

Questlog is designed against the genre it joins: warm cream and terracotta instead of a dark DeFi dashboard, rounded card design, cover art as hero, and copy that sounds human ("Verified on-chain. That's permanent."). The crypto layer is invisible until asked for — the entire library, capture, and stats flow works with no wallet, no account, and no internet-required-for-storage (Room is the source of truth). When you do want proof, it's one button: "Verify this achievement", which opens Phantom, signs, and returns a badge and explorer link. Haptics fire on every real action (save, check-in, completion, verification, and warning states), confetti celebrates the moments worth celebrating, loading states are warm skeletons rather than spinners, and every failure path — OCR miss, network down, wallet rejected, devnet timeout — is friendly copy with a retry, never a dead end. Zero blockchain jargon in the UI: the words memo, devnet, and signature appear nowhere in the product.

### Innovation / X-factor

No game tracker on earth does this: screenshot → OCR reads the title → RAWG matches it → rich card → save → mark completed → tap "Verify this achievement" → a permanent, wallet-signed record on Solana. Proof of Play v1 is an open, documented memo format — `{"v":1,"app":"questlog","title":...,"rawgId":...,"completedAt":...,"rating":...}` — so any future tracker can read and verify the same credentials. The answer to "why does this need blockchain" is that the chain is a notarizer, not a referee: it proves when you claimed the completion, that the claim cannot be forged or backdated, and that the credential is portable — it outlives our app. Social proof is the explorer link itself, which is stronger than any number we could print. The mobile-wallet-adapter integration is the real thing: Phantom signs, the memo transacts on devnet, and the app polls for finalization with a 30-second ceiling before offering a clean retry.

### Presentation & Demo

The 60-second demo is screenshot-first and honest: hook in the first five seconds, the full core loop inside thirty, real device, real timings, real devnet transaction. [0-5s] "Gamers discover games on TikTok — and forget them. Questlog saves them. And proves them." [5-15s] A Palworld screenshot shared from TikTok into Questlog, OCR finding the game, the card appearing, "Add to library" with confetti. [15-25s] Library → detail → status animating Playing → Completed. [25-40s] "Verify this achievement" → Phantom signs → the explorer badge slides in with a real, checkable transaction. [40-55s] Clock In → flame → streak tier → share card. [55-60s] "Questlog. Your verified gaming history." Every claim in the video is real on-screen behavior — nothing staged, no fabricated stats, no mock data.

## Builder Profile draft

Builder: hybirdvault-dot (pseudonymous OK per program rules). Solo builder for CLOCK IN. Questlog is my answer to a problem I actually have: I discover games on my phone every week and lose them the same day. Built in the open across 16 reviewed build sessions — architecture follows nowinandroid (Apache 2.0), library UX after Mihon, OCR after Google's ML Kit samples, wallet integration after Solana Mobile's Kotlin Compose scaffold (patterns only, reimplemented), all attributed in THIRD_PARTY_NOTICES.md with the Apache 2.0 LICENSE in the repo. Currently: app complete and tested with a 17-test contract suite locking the on-chain memo format; next: a Proof-of-Play social feed where every post is a verified completion with its transaction attached.

## Demo script (recording checklist)

- Real Android phone, 60fps, Do Not Disturb on
- Phantom on devnet with faucet SOL; one fresh confirmed TX in the can + 2 backup recordings
- Shoot the TikTok-screenshot share moment first (it's the hook)
- Explorer link must actually open during the video (proves the TX is real)
- Keep total runtime ≤60s; portal cap unverified so stay under
- Screenshots for the store listing: Library grid · game detail with verified badge · Clock In streak · share card

## Video length note

Portal cap unconfirmed; 60s chosen to be safely under any stated limit.
