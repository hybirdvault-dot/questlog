#!/usr/bin/env python3
"""Questlog hackathon pitch deck — 8 slides, 16:9, ReportLab."""
import os
from reportlab.lib.pagesizes import landscape
from reportlab.lib.units import mm
from reportlab.lib.colors import HexColor
from reportlab.pdfgen import canvas
from reportlab.lib.utils import ImageReader

W, H = landscape((1280 * mm / 96, 720 * mm / 96))  # 16:9
CREAM = HexColor("#FFF8F0"); INK = HexColor("#3E2723"); ACC = HexColor("#E07856")
SAGE = HexColor("#7A9E7E"); SOFT = HexColor("#6B544A"); WARM = HexColor("#F3E7DA")

OUT = "/home/BALOCH/Desktop/the app/questlog-deck.pdf"
SC = "/home/BALOCH/Desktop/the app/screenshots"

def page(c, num):
    c.setFillColor(CREAM); c.rect(0, 0, W, H, fill=1, stroke=0)
    c.setFillColor(ACC); c.rect(0, H - 6*mm, W, 6*mm, fill=1, stroke=0)
    c.setFillColor(SOFT); c.setFont("Helvetica", 8)
    c.drawRightString(W - 14*mm, 8*mm, f"Questlog · CLOCK IN · {num}/8")

def title(c, t, sub=None):
    c.setFillColor(INK); c.setFont("Helvetica-Bold", 30)
    c.drawString(14*mm, H - 26*mm, t)
    c.setFillColor(ACC); c.rect(14*mm, H - 31*mm, 34*mm, 2.2*mm, fill=1, stroke=0)
    if sub:
        c.setFillColor(SOFT); c.setFont("Helvetica", 13)
        c.drawString(14*mm, H - 42*mm, sub)

def bullet(c, x, y, head, body, w=200):
    c.setFillColor(ACC); c.circle(x + 1.6*mm, y - 1*mm, 1.6*mm, fill=1, stroke=0)
    c.setFillColor(INK); c.setFont("Helvetica-Bold", 12.5)
    c.drawString(x + 6*mm, y - 2*mm, head)
    c.setFillColor(SOFT); c.setFont("Helvetica", 10.5)
    for i, line in enumerate(body.split("\n")):
        c.drawString(x + 6*mm, y - 8*mm - i * 5*mm, line)

def shot(c, path, x, y, w):
    if os.path.exists(path):
        img = ImageReader(path); iw, ih = img.getSize()
        h = w * ih / iw
        c.drawImage(img, x, y, w, h, preserveAspectRatio=True, mask="auto")

c = canvas.Canvas(OUT, pagesize=(W, H))
c.setTitle("Questlog — Pitch Deck (CLOCK IN)")
c.setAuthor("Questlog")

# 1 COVER
page(c, 1)
c.setFillColor(INK); c.setFont("Helvetica-Bold", 58)
c.drawString(14*mm, H*0.52, "Questlog.")
c.setFillColor(ACC); c.setFont("Helvetica-Bold", 26)
c.drawString(14*mm, H*0.42, "Your verified gaming history.")
c.setFillColor(SOFT); c.setFont("Helvetica", 14)
c.drawString(14*mm, H*0.32, "Save any game the moment you discover it. Prove your completions on-chain.")
c.setFont("Helvetica", 11)
c.drawString(14*mm, H*0.22, "Built for Solana Mobile · CLOCK IN hackathon · github.com/hybirdvault-dot/questlog")
c.showPage()

# 2 PROBLEM
page(c, 2)
title(c, "The problem", "Gamers discover dozens of games a week. They remember almost none.")
y = H - 55*mm
bullet(c, 16*mm, y, "Discovery is everywhere, memory is nowhere", "TikTok, YouTube, Reddit — seen once, gone forever.")
bullet(c, 16*mm, y - 18*mm, "Trackers demand work before they give value", "Accounts, manual entry, paywalls, no reason to return.")
bullet(c, 16*mm, y - 36*mm, "Achievements are screenshots on a phone", "Nothing is verifiable, portable, or permanent.")
c.showPage()

# 3 SOLUTION
page(c, 3)
title(c, "The core loop", "Two seconds from screenshot to saved game. No wallet needed.")
steps = ["1. See a game anywhere", "2. Screenshot or scan it", "3. OCR + RAWG match (~2s)", "4. Save to library — free forever", "5. Complete it, rate it", "6. Prove it on-chain (optional)"]
x0 = 16*mm
for i, s in enumerate(steps):
    row, col = divmod(i, 3)
    c.setFillColor(WARM); c.roundRect(x0 + col * 88*mm, H - 62*mm - row*22*mm, 82*mm, 17*mm, 3*mm, fill=1, stroke=0)
    c.setFillColor(INK); c.setFont("Helvetica-Bold", 11)
    c.drawString(x0 + col * 88*mm + 5*mm, H - 57*mm - row*22*mm, s)
shot(c, f"{SC}/app-screenshot-1.jpg", W - 62*mm, 16*mm, 46*mm)
c.showPage()

# 4 X-FACTOR
page(c, 4)
title(c, "Proof of Play — the X-factor", "No game tracker on earth does this.")
y = H - 55*mm
bullet(c, 16*mm, y, "One tap: \"Verify this achievement\"", "Phantom signs a memo transaction on Solana.")
bullet(c, 16*mm, y - 16*mm, "Permanent, unforgeable, timestamped", "Not a referee — a notarized credential that outlives the app.")
bullet(c, 16*mm, y - 32*mm, "Open memo format (v1)", '{"v":1,"app":"questlog","title":...,"rawgId":...,"completedAt":...}')
bullet(c, 16*mm, y - 48*mm, "Social proof = the explorer link itself", "Every verified card carries its real transaction.")
shot(c, f"{SC}/app-screenshot-4.jpg", W - 62*mm, 16*mm, 46*mm)
c.showPage()

# 5 DEMO WALL
page(c, 5)
title(c, "The app — real screens", "Warm, cozy, zero blockchain jargon in the UI.")
xs = [16*mm, 74*mm, 132*mm, 190*mm, 248*mm]
for i, x in enumerate(xs[:5]):
    shot(c, f"{SC}/app-screenshot-{i+1}.jpg", x, H - 132*mm, 52*mm)
c.setFillColor(SOFT); c.setFont("Helvetica", 10.5)
c.drawString(16*mm, 18*mm, "Capture · Library · Detail · Proof of Play · Clock In streak — free forever, wallet optional.")
c.showPage()

# 6 TECH
page(c, 6)
title(c, "How it is built", "Native Android · offline-first · zero backend cost")
y = H - 52*mm
bullet(c, 16*mm, y, "Kotlin + Jetpack Compose + Hilt + Room", "NiA-pattern offline-first. Room is the source of truth.")
bullet(c, 16*mm, y - 16*mm, "ML Kit on-device OCR + RAWG API", "Screenshots never leave the phone. 20k free lookups/month.")
bullet(c, 16*mm, y - 32*mm, "Solana Mobile Wallet Adapter + Memo v2", "Devnet proven: blockhash → sign → send → 30s finalized poll.")
bullet(c, 16*mm, y - 48*mm, "17/17 contract tests · signed release · R8-minified", "Memo payload byte-exact. Apache-2.0, full notices shipped.")
c.showPage()

# 7 STATUS
page(c, 7)
title(c, "Status — shipping, not slides", "16 reviewed build sessions. Every feature tested green.")
y = H - 52*mm
for item in ["Camera scanner + share-to-app capture (OCR)", "Library, detail, ratings, notes, search",
             "Clock In streaks, tiers, calendar history, daily reminder", "Proof of Play: full 7-state flow, devnet TXs",
             "1080x1350 share cards with verified badge", "Signed release APK v1.1.0 live on GitHub",
             "Production repo: README, spec, LICENSE, notices"]:
    c.setFillColor(SAGE); c.circle(18*mm, y - 1*mm, 1.8*mm, fill=1, stroke=0)
    c.setFillColor(INK); c.setFont("Helvetica", 12); c.drawString(24*mm, y - 2*mm, item)
    y -= 8.5*mm
shot(c, f"{SC}/app-screenshot-6.jpg", W - 62*mm, 16*mm, 46*mm)
c.showPage()

# 8 CLOSE
page(c, 8)
c.setFillColor(INK); c.setFont("Helvetica-Bold", 44)
c.drawCentredString(W/2, H*0.55, "Questlog.")
c.setFillColor(ACC); c.setFont("Helvetica-Bold", 20)
c.drawCentredString(W/2, H*0.44, "Your verified gaming history.")
c.setFillColor(SOFT); c.setFont("Helvetica", 12)
c.drawCentredString(W/2, H*0.32, "Next: Proof-of-Play social feed · badge wall · home-screen widget")
c.drawCentredString(W/2, H*0.25, "github.com/hybirdvault-dot/questlog")
c.showPage()

c.save()
print("DECK DONE:", OUT)
