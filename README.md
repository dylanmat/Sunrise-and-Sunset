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

## Setup

1. Install the app code in **Apps Code** on your Hubitat hub.
2. Create or pick a **Hub Variable** that holds the final dimmer level (1–100).
3. Add the app from the **Apps** list and configure Sunrise and Sunset individually:
   - Choose the days to run and the start/end option (fixed time or real sunrise/sunset).
   - Select the color-capable lights to guide through the sequence.
   - Pick the Hub Variable for the finishing dimmer level and the mode to set when done.
4. (Optional) Assign a lockout switch so other automations know to wait while the sequence runs.

The app turns the lockout switch on at the beginning of any sequence, gradually walks through gentle hue and level steps, then turns the switch off and changes to the mode you chose.

## Change Log

- **0.1.4** – Populate the hub-variable selector with numeric entries and guard runs when a target level is missing.
- **0.1.3** – Restore scheduled starts by using Hubitat-supported runOnce handlers while keeping the existing fade behavior intact.
- **0.1.2** – Stabilize scheduling and playback so sequences start reliably and keep their active fades in sync even if both run together.
- **0.1.1** – Restore the full configuration UI, honor hub-variable end levels including zero, and smooth the start/finish handling.
- **0.1.0** – Initial release with natural sunrise and sunset journeys, hub-variable end levels, scheduling by day, and lockout switch control.
