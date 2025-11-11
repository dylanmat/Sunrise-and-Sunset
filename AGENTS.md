# Prompt A — Full build

**Task:**
Create a Hubitat app in Groovy that simulates the gentle, natural progression of a **sunrise** and **sunset** on selected lights. Keep the code straightforward and readable.

**Behavior & philosophy:**

* The look and feel must be **natural**: subtle brightness shifts, a warm–cool color journey, and light touches of color.
* **No harsh jumps.** Changes should be slow, barely noticeable, with occasional soft “surprise” color accents that still feel realistic.
* Prefer a smooth Hue/Saturation approach for color weaving; don’t rely only on color temperature.
* Color range should stay within what you’d actually see outdoors (natural palette only).

**Dimmer rules:**

* **Sunrise:** all selected lights begin at **1%**.
* **Sunset:** use each light’s **current** dimmer level as the starting point.
* **Ending level:** read from a **Hub Variable** that the user selects in the app.

**Inputs (two sections: one for Sunrise, one for Sunset):**
For each section, include inputs for:

1. Days of week to run.
2. Start and end **time for each day**. Support picking the real **sunrise or sunset** times as options in addition to fixed times.
3. Which **color-capable** lights to control (treat them as color-temperature bulbs but we’ll use Hue/Saturation to weave color).
4. Which **Hub Mode** to switch to **after** the sequence completes.

**Lockout switch:**

* Add an input for a **switch** that turns **on at the sequence start** and **off at the sequence end**.
* This switch is used by other automations to avoid conflicts during the sequence.

**Sequence design:**

* Progress gradually from start to end time, adjusting brightness and color in small steps.
* Make timing and step sizes simple and predictable in code, but the **color path** should include gentle randomness within a natural palette (no neon, no extreme saturation).
* Ensure occasional, soft color “surprises” appear and fade in/out very slowly.
* Avoid deterministic, looped patterns that look fake.

**Reliability & safety:**

* Respect the user’s choices at all times.
* If a light is turned off by the user during a sequence, don’t turn it back on.
* If devices or inputs are missing, fail safely with clear, friendly logs.

**After completion:**

* Turn **off** the lockout switch.
* Change **Hub Mode** to the user-selected mode for that section.

**App metadata (don’t forget):**

* `iconUrl: ""`
* `iconX2Url: ""`
* `iconX3Url: ""`
* `singleInstance: true`

**What to avoid:**

* Don’t include technical explanations or platform internals in comments.
* Don’t hardcode location-specific values.
* Don’t use dramatic color jumps or fast fades.
* Don’t assume device capabilities beyond basic color and level control.

**Deliverable:**

* A single, tidy Groovy app file with clear, minimal comments, simple preferences pages for Sunrise and Sunset, and readable logic.

---

# Prompt B — Minimal/MVP build

Build a simple Hubitat Groovy app that runs **two gentle lighting sequences**: one for **sunrise**, one for **sunset**.

**Rules:**

* **Sunrise** starts at **1%** dimmer; **sunset** starts at each bulb’s **current level**.
* The final dimmer level comes from a **selectable Hub Variable**.
* Use a **natural color palette** with subtle hue/saturation changes; keep shifts slow and barely noticeable with occasional soft surprises.
* Provide inputs per section (sunrise/sunset): days of week; start and end time (support real sunrise/sunset as options); target lights; Hub Mode to set after completion.
* Provide a **lockout switch** input; turn it **on at start**, **off at end**.

**Must include in metadata:**

* `iconUrl: ""`, `iconX2Url: ""`, `iconX3Url: ""`, `singleInstance: true`.

**Constraints:**

* No dramatic color jumps.
* No platform-specific technical commentary.
* Simple, readable code; gentle step timing from start to end; safe behavior if a user manually changes a device.

**Output:**

* One Groovy app file, concise and clean.

---

# Prompt C — Ultra-short (paste when Codex drifts)

Create a Hubitat Groovy app with two sequences: **sunrise** and **sunset**.

* Sunrise starts at **1%**; sunset starts at **current levels**.
* End level comes from a **selectable Hub Variable**.
* Inputs for each section: **days**, **start/end time** (allow real sunrise/sunset), **lights**, and **Hub Mode after finish**.
* Add a **lockout switch**: turn **on at start**, **off at end**.
* Use a **natural** hue/saturation journey: **slow**, subtle changes with occasional gentle surprises; **no dramatic shifts**.
* Include metadata: `iconUrl: ""`, `iconX2Url: ""`, `iconX3Url: ""`, `singleInstance: true`.
* Keep code simple and readable.

---

If you want, I can also produce a tiny acceptance-checks list for pasting under the prompt to keep Codex focused.
