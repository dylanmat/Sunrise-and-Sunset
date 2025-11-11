# Sunrise & Sunset Light Experience for Hubitat

This app creates a natural, gentle lighting experience that mimics sunrise and sunset using selected color-capable bulbs. The focus is subtle changes in brightness and color — slow, smooth, and barely noticeable, with occasional soft color surprises. Nothing harsh, nothing dramatic.

## What It Does

- Two independent sequences: **Sunrise** and **Sunset**
- Lights gently shift brightness and color over time
- Uses natural color palettes — warm tones, soft blends, gentle randomness
- Avoids sharp transitions or fake-looking animations

## Key Rules

- **Sunrise starts at 1% brightness**
- **Sunset starts at each bulb’s current brightness**
- Final brightness comes from a **selected Hub Variable**
- Color shifts use hue/saturation blending instead of just color temperature

## Automation Lockout

You can select a switch that turns ON at the beginning of a sequence and OFF at the end.  
Other automations can use this switch to avoid interfering during the experience.

## Inputs

Sunrise and Sunset each include:
- Days of week
- Start and end time (supports real sunrise/sunset or a fixed time)
- Lights to control
- Mode to change to after the sequence completes

## Why AGENTS.md Exists

Codex often drifts or over-complicates the automation.  
`AGENTS.md` contains focused prompts designed to keep it on track so generation stays clean and consistent.

## Where To Start

1. Open `AGENTS.md`
2. Copy the prompt you want — Full, Minimal, or Ultra-short
3. Paste into Codex
4. Review output, lightly test with a couple bulbs first

---

This is a first-draft README — we can expand installation steps, screenshots, or examples later once your app is generated.

---

## ✅ **Download AGENTS.md**

Your prompts are stored here:

📄 **[Download AGENTS.md](sandbox:/mnt/data/AGENTS.md)**

Let me know if you'd like:
- a LICENSE file,
- a structure for `/apps` and `/drivers`,
- or a more detailed README with examples, troubleshooting, and feature expansion. ​:contentReference[oaicite:0]{index=0}​
