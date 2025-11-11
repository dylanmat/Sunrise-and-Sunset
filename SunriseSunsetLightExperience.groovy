/*
 * Sunrise & Sunset Light Experience
 */

definition(
    name: "Sunrise & Sunset Light Experience",
    namespace: "sunriseSunset",
    author: "Sunrise Team",
    description: "Gentle sunrise and sunset lighting sequences.",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
    singleInstance: true
)

def appVersion() { "0.1.0" }

def preferences() {
    page(name: "mainPage")
    page(name: "sunrisePage")
    page(name: "sunsetPage")
}

def mainPage() {
    dynamicPage(name: "mainPage", title: "Sunrise & Sunset Light Experience v${appVersion()}") {
        section("General") {
            input "lockSwitch", "capability.switch", title: "Lockout switch", required: false
        }
        section("Sunrise") {
            href "sunrisePage", title: "Configure Sunrise"
        }
        section("Sunset") {
            href "sunsetPage", title: "Configure Sunset"
        }
    }
}

def sunrisePage() {
    dynamicPage(name: "sunrisePage", title: "Sunrise Settings") {
        buildSequenceInputs("sunrise", "Sunrise")
    }
}

def sunsetPage() {
    dynamicPage(name: "sunsetPage", title: "Sunset Settings") {
        buildSequenceInputs("sunset", "Sunset")
    }
}

def buildSequenceInputs(String key, String label) {
    section("${label} schedule") {
        input "${key}Days", "enum", title: "Days", multiple: true, required: false,
            options: ["Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"]
        input "${key}StartType", "enum", title: "Start time", required: false,
            options: ["Fixed","Sunrise","Sunset"], defaultValue: "Fixed"
        input "${key}StartTime", "time", title: "Fixed start", required: false
        input "${key}EndType", "enum", title: "End time", required: false,
            options: ["Fixed","Sunrise","Sunset"], defaultValue: "Fixed"
        input "${key}EndTime", "time", title: "Fixed end", required: false
    }
    section("${label} devices") {
        input "${key}Lights", "capability.colorControl", title: "Lights", multiple: true, required: false
        input "${key}TargetVar", "enum", title: "Hub Variable for final level", required: false,
            options: hubVariableOptions()
        input "${key}Mode", "mode", title: "Mode after completion", required: false
    }
}

def installed() {
    initialize()
}

def updated() {
    unschedule()
    unsubscribe()
    initialize()
}

def initialize() {
    state.activeSequences = state.activeSequences ?: [:]
    logInfo "Initializing v${appVersion()}"
    scheduleNext("sunrise")
    scheduleNext("sunset")
}

def scheduleNext(String key) {
    def lights = settings["${key}Lights"]
    def days = normalizeDays(settings["${key}Days"])
    if (!lights || !days) {
        logInfo "${key.capitalize()} inactive (missing devices or days)"
        return
    }
    Date next = nextOccurrence(key, settings["${key}StartType"], settings["${key}StartTime"], days)
    if (next) {
        runOnce(next, "${key}Begin")
        logInfo "${key.capitalize()} scheduled for ${formatTime(next)}"
    } else {
        logWarn "${key.capitalize()} not scheduled (no future time)"
    }
}

def sunriseBegin() { beginSequence("sunrise") }
def sunsetBegin() { beginSequence("sunset") }

def beginSequence(String key) {
    def lights = settings["${key}Lights"]
    if (!lights) {
        logWarn "${key.capitalize()} skipped (no lights)"
        scheduleNext(key)
        return
    }
    state.activeSequences[key] = [step: 0]
    updateLockSwitch(true)
    Map deviceState = [:]
    lights.each { device ->
        Integer level = key == "sunrise" ? 1 : (device.currentLevel ?: 1)
        if (key == "sunrise") {
            device.setLevel(1, 10)
        }
        deviceState[device.id as String] = [device: device, level: level]
    }
    Integer target = targetLevel(key)
    if (!target) {
        logWarn "${key.capitalize()} skipped (missing target level)"
        finishSequence(key)
        return
    }
    Date startTime = new Date()
    Date endTime = resolveTimeForDate(key, settings["${key}EndType"], settings["${key}EndTime"], startTime)
    if (!endTime || !endTime.after(startTime)) {
        endTime = new Date(startTime.time + 45 * 60 * 1000)
    }
    Integer steps = Math.max(20, ((endTime.time - startTime.time) / (90 * 1000)) as Integer)
    Long interval = Math.max(45L, ((endTime.time - startTime.time) / steps / 1000) as Long)
    state.activeSequences[key] = [
        devices: deviceState,
        start: startTime.time,
        end: endTime.time,
        steps: steps,
        interval: interval,
        target: target,
        accent: accentPlan(steps, key)
    ]
    logInfo "${key.capitalize()} started (ends ${formatTime(endTime)})"
    runIn(1, "sequenceTick", [overwrite: true, data: [key: key]])
}

def sequenceTick(data) {
    String key = data?.key
    def seq = state.activeSequences[key]
    if (!seq) {
        return
    }
    Integer step = (seq.step ?: 0) + 1
    seq.step = step
    Double progress = Math.min(1.0d, (step as Double) / (seq.steps as Double))
    seq.devices.values().each { info ->
        def device = info.device
        if (!device || device.currentSwitch != "on") {
            return
        }
        Integer startLevel = info.level ?: 1
        Integer level = (startLevel + ((seq.target - startLevel) * progress)).round()
        def color = colorFor(progress, seq.accent, key)
        color.level = level
        device.setColor(color)
    }
    if (progress >= 1.0d) {
        finishSequence(key)
    } else {
        runIn(seq.interval as Long, "sequenceTick", [overwrite: true, data: [key: key]])
    }
}

def finishSequence(String key) {
    state.activeSequences.remove(key)
    updateLockSwitch(false)
    def mode = settings["${key}Mode"]
    if (mode && location?.modes?.find { it.name == mode }) {
        setLocationMode(mode)
    }
    logInfo "${key.capitalize()} complete"
    scheduleNext(key)
}

def colorFor(Double progress, Map accent, String key) {
    Double startHue = key == "sunrise" ? 8d : 50d
    Double endHue = key == "sunrise" ? 55d : 8d
    Double startSat = key == "sunrise" ? 80d : 35d
    Double endSat = key == "sunrise" ? 30d : 75d
    Double hue = startHue + (endHue - startHue) * progress
    Double sat = startSat + (endSat - startSat) * progress
    if (accent && progress >= accent.start && progress <= accent.end) {
        hue += accent.shift
        sat = Math.max(20d, Math.min(90d, sat + accent.satShift))
    }
    hue += randomSoft( -4d, 4d)
    sat += randomSoft(-6d, 6d)
    [hue: clamp(hue, 0d, 100d), saturation: clamp(sat, 5d, 90d)]
}

def accentPlan(Integer steps, String key) {
    if (steps < 10) return null
    Double start = 0.2d + Math.random() * 0.5d
    Double width = 0.1d + Math.random() * 0.1d
    Double shift = key == "sunrise" ? 8d : -10d
    Double satShift = key == "sunrise" ? 5d : 10d
    [start: start, end: Math.min(0.95d, start + width), shift: shift, satShift: satShift]
}

def randomSoft(Double min, Double max) {
    (Math.random() * (max - min)) + min
}

def clamp(Double value, Double min, Double max) {
    Math.max(min, Math.min(max, value)).round(2)
}

def targetLevel(String key) {
    String varName = settings["${key}TargetVar"]
    if (!varName) return null
    def vars = location?.hubVariables ?: [:]
    def var = vars[varName]
    if (!var) return null
    try {
        BigDecimal raw = (var.value ?: var.currentValue ?: "0") as BigDecimal
        Integer value = raw.toInteger()
        return Math.max(1, Math.min(100, value))
    } catch (Exception e) {
        logWarn "Unable to read hub variable ${varName}: ${e.message}"
        return null
    }
}

def resolveTimeForDate(String key, String type, String fixed, Date anchor) {
    Map sun = getSunriseAndSunset(date: anchor)
    Date result
    if (type == "Sunrise") result = sun.sunrise
    else if (type == "Sunset") result = sun.sunset
    else if (fixed) result = toDate(anchor, fixed, location?.timeZone ?: TimeZone.getTimeZone("UTC"))
    if (!result) return null
    result.after(anchor) ? result : null
}

def nextOccurrence(String key, String type, String fixed, days) {
    if (!days) return null
    TimeZone tz = location?.timeZone ?: TimeZone.getTimeZone("UTC")
    Date now = new Date()
    (0..7).collect { offset ->
        Date candidate = new Date(now.time + offset * 24L * 60L * 60L * 1000L)
        String dayName = candidate.format("EEEE", tz)
        if (!days.contains(dayName)) return null
        Map sun = getSunriseAndSunset(date: candidate)
        Date target
        if (type == "Sunrise") target = sun.sunrise
        else if (type == "Sunset") target = sun.sunset
        else if (fixed) target = toDate(candidate, fixed, tz)
        if (target && target.after(now)) return target
        return null
    }.find { it }
}

def toDate(Date day, String timeText, TimeZone tz) {
    if (!timeText) return null
    Date parsed = timeToday(timeText, tz)
    Date dayStart = new Date(day.time)
    dayStart.clearTime()
    Calendar parsedCal = Calendar.getInstance(tz)
    parsedCal.time = parsed
    Calendar result = Calendar.getInstance(tz)
    result.time = dayStart
    result.set(Calendar.HOUR_OF_DAY, parsedCal.get(Calendar.HOUR_OF_DAY))
    result.set(Calendar.MINUTE, parsedCal.get(Calendar.MINUTE))
    result.set(Calendar.SECOND, parsedCal.get(Calendar.SECOND))
    result.set(Calendar.MILLISECOND, 0)
    result.time
}

def hubVariableOptions() {
    def vars = location?.hubVariables ?: [:]
    if (!vars) return []
    vars.keySet().toList().sort()
}

def formatTime(Date date) {
    if (!date) return "unknown"
    date.format("MMM d h:mm a", location?.timeZone ?: TimeZone.getTimeZone("UTC"))
}

def logInfo(msg) { log.info "[SunriseSunset] ${msg}" }

def logWarn(msg) { log.warn "[SunriseSunset] ${msg}" }

def updateLockSwitch(boolean turningOn) {
    if (!lockSwitch) return
    if (turningOn) {
        lockSwitch.on()
        return
    }
    if (!(state.activeSequences?.values()?.find { it })) {
        lockSwitch.off()
    }
}

def normalizeDays(value) {
    if (!value) return []
    value instanceof Collection ? value.findAll { it } : [value]
}
