
# AGENTS.md

## Prompt A — Full build

Create a Hubitat app in Groovy that simulates the gentle, natural progression of a **sunrise** and **sunset** on selected lights. Keep the code straightforward and readable.

- Natural look: subtle brightness shifts, warm–cool color journey, light touches of color.
- No harsh jumps. Slow, barely noticeable changes with occasional, soft color surprises.
- Use Hue/Saturation to weave color naturally.

**Dimmer rules:**
- Sunrise starts at 1%.
- Sunset starts at current levels.
- End level comes from a selectable Hub Variable.

**Inputs (Sunrise and Sunset each):**
- Days of week to run.
- Start and end time (support real sunrise/sunset or fixed times).
- Select color-capable lights.
- Mode to change to after finished.

**Lockout switch:**
- Turn on at sequence start.
- Turn off at sequence end.

**Sequence:**
- Gradual adjustments across total time.
- Gentle randomness within a natural palette.
- No neon, no dramatic saturation, no patterns that feel fake.

**After completion:**
- Turn off lockout switch.
- Change to selected mode.

**Metadata to include:**
- iconUrl: ""
- iconX2Url: ""
- iconX3Url: ""
- singleInstance: true

---

## Prompt B — Minimal/MVP

Build a simple Hubitat Groovy app for a sunrise and sunset sequence.

- Sunrise starts at 1%.
- Sunset starts at current level.
- Selectable Hub Variable for end level.
- Inputs per section: days, start/end time (real sunrise/sunset allowed), target lights, mode after finish.
- Lockout switch on at start, off at end.
- Natural palette, slow changes, gentle surprises.

Metadata required:
- iconUrl: ""
- iconX2Url: ""
- iconX3Url: ""
- singleInstance: true

---

## Prompt C — Ultra-short Reset

Create a Hubitat Groovy app with sunrise and sunset sequences:
- Sunrise 1%, sunset current level.
- End level from Hub Variable.
- Inputs: days, start/end time (real sunrise/sunset), lights, mode after finish.
- Lockout switch on at start, off at end.
- Natural hue/saturation, slow changes, no dramatic shifts.
- Metadata: iconUrl: "", iconX2Url: "", iconX3Url: "", singleInstance: true.
