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

def appVersion() { "0.1.7" }

preferences {
    page(name: "mainPage")
    page(name: "sunrisePage")
    page(name: "sunsetPage")
}

def mainPage() {
    dynamicPage(name: "mainPage", title: "Sunrise & Sunset Light Experience v${appVersion()}", install: true, uninstall: true) {
        section("General") {
            input "lockSwitch", "capability.switch", title: "Lockout switch", required: false
            input "enableDebug", "bool", title: "Enable debug logging", required: false, defaultValue: false
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
        input "${key}StartType", "enum", title: "Start time", required: false, submitOnChange: true,
            options: ["Fixed","Sunrise","Sunset"], defaultValue: "Fixed"
        if ((settings["${key}StartType"] ?: "Fixed") == "Fixed") {
            input "${key}StartTime", "time", title: "Fixed start", required: false
        }
        input "${key}EndType", "enum", title: "End time", required: false, submitOnChange: true,
            options: ["Fixed","Sunrise","Sunset"], defaultValue: "Fixed"
        if ((settings["${key}EndType"] ?: "Fixed") == "Fixed") {
            input "${key}EndTime", "time", title: "Fixed end", required: false
        }
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
    state.activeSequences = [:]
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
        String handler = key == "sunrise" ? "sunriseBegin" : "sunsetBegin"
        runOnce(next, handler)
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
    if (state.activeSequences[key]) {
        logWarn "${key.capitalize()} already running"
        return
    }
    Integer target = targetLevel(key)
    if (target == null) {
        logWarn "${key.capitalize()} skipped (missing target level)"
        scheduleNext(key)
        return
    }
    Map<String, Integer> startingLevels = [:]
    lights.each { device ->
        Integer level = key == "sunrise" ? 1 : ((device.currentLevel ?: 1) as Integer)
        if (key == "sunrise") {
            device.setLevel(1, 10)
        }
        startingLevels[device.id as String] = level
    }
    updateLockSwitch(true)
    Date startTime = new Date()
    Date endTime = resolveTimeForDate(key, settings["${key}EndType"], settings["${key}EndTime"], startTime)
    if (!endTime || !endTime.after(startTime)) {
        endTime = new Date(startTime.time + 45 * 60 * 1000)
    }
    Long duration = Math.max(10_000L, endTime.time - startTime.time)
    Integer steps = Math.max(60, Math.round(duration / 15_000d))
    Long interval = Math.max(2L, Math.round(duration / steps / 1000d))
    state.activeSequences[key] = [
        levels: startingLevels,
        start: startTime.time,
        end: endTime.time,
        steps: steps,
        interval: interval,
        target: target,
        accent: accentPlan(steps, key),
        step: 0
    ]
    logInfo "${key.capitalize()} started (ends ${formatTime(endTime)})"
    logDebug "${key} duration ${duration}ms, ${steps} steps, interval ${interval}s"
    runIn(1, "sequenceTick", [overwrite: false, data: [key: key]])
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
    def devices = settings["${key}Lights"]?.findAll { it }
    devices?.each { device ->
        String deviceId = device.id as String
        Integer startLevel = (seq.levels[deviceId] ?: (key == "sunrise" ? 1 : (device.currentLevel ?: 1))) as Integer
        if (device.currentSwitch != "on") {
            return
        }
        Integer level = Math.max(0, Math.min(100, (startLevel + ((seq.target - startLevel) * progress)).round() as Integer))
        def color = colorFor(progress, seq.accent, key)
        color.level = level
        device.setColor(color)
        Integer hueVal = ((color.hue ?: 0) as Number).intValue()
        Integer satVal = ((color.saturation ?: 0) as Number).intValue()
        logDebug "${key} step ${step}/${seq.steps} -> ${device.displayName} level ${level} hue ${hueVal} sat ${satVal}"
    }
    if (progress >= 1.0d) {
        finishSequence(key)
    } else {
        runIn(seq.interval as Long, "sequenceTick", [overwrite: false, data: [key: key]])
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
    Map base = interpolateColor(colorPath(key), progress ?: 0d)
    if (accent && progress >= accent.start && progress <= accent.end) {
        Double span = Math.max(0.001d, accent.end - accent.start)
        Double local = clampDouble((progress - accent.start) / span, 0d, 1d)
        Double blend = Math.sin(Math.PI * local)
        base.hue += (accent.hue - base.hue) * blend * 0.75d
        base.saturation += (accent.saturation - base.saturation) * blend * 0.6d
    }
    Double hue = base.hue + randomSoft(-1.5d, 1.5d)
    Double sat = base.saturation + randomSoft(-3d, 3d)
    [hue: safeHue(hue), saturation: clamp(sat, 20d, 90d)]
}

def accentPlan(Integer steps, String key) {
    if (steps < 20) return null
    Double start = 0.18d + Math.random() * 0.5d
    Double width = 0.12d + Math.random() * 0.12d
    List palette = key == "sunrise"
        ? [[hue: 72d, saturation: 74d], [hue: 78d, saturation: 62d], [hue: 8d, saturation: 82d]]
        : [[hue: 86d, saturation: 76d], [hue: 74d, saturation: 68d], [hue: 12d, saturation: 84d]]
    Map accentColor = palette[(Math.random() * palette.size()).intValue()]
    [start: start, end: Math.min(0.96d, start + width), hue: accentColor.hue, saturation: accentColor.saturation]
}

def randomSoft(Double min, Double max) {
    (Math.random() * (max - min)) + min
}

def interpolateColor(List path, Double progress) {
    if (!path) return [hue: 20d, saturation: 60d]
    Double pos = clampDouble(progress, 0d, 1d)
    Map lower = path.first()
    Map upper = path.last()
    path.each { stop ->
        if (stop.pos <= pos && stop.pos >= lower.pos) {
            lower = stop
        }
        if (stop.pos >= pos && stop.pos <= upper.pos) {
            upper = stop
        }
    }
    if (lower.pos == upper.pos) {
        return [hue: lower.hue, saturation: lower.saturation]
    }
    Double ratio = (pos - lower.pos) / (upper.pos - lower.pos)
    [
        hue: lower.hue + (upper.hue - lower.hue) * ratio,
        saturation: lower.saturation + (upper.saturation - lower.saturation) * ratio
    ]
}

def colorPath(String key) {
    if (key == "sunrise") {
        return [
            [pos: 0d, hue: 4d, saturation: 82d],
            [pos: 0.35d, hue: 10d, saturation: 76d],
            [pos: 0.7d, hue: 18d, saturation: 58d],
            [pos: 1d, hue: 26d, saturation: 44d]
        ]
    }
    [
        [pos: 0d, hue: 18d, saturation: 64d],
        [pos: 0.4d, hue: 24d, saturation: 74d],
        [pos: 0.68d, hue: 12d, saturation: 68d],
        [pos: 0.85d, hue: 88d, saturation: 70d],
        [pos: 1d, hue: 78d, saturation: 48d]
    ]
}

def clampDouble(Double value, Double min, Double max) {
    Math.max(min, Math.min(max, value))
}

def clamp(Double value, Double min, Double max) {
    Math.max(min, Math.min(max, value)).round() as Integer
}

def safeHue(Double value) {
    Double limited = clampDouble(value, 0d, 100d)
    if (limited >= 38d && limited <= 60d) {
        limited = limited < 49d ? 37d : 61d
    }
    limited.round() as Integer
}

def targetLevel(String key) {
    String varName = settings["${key}TargetVar"]
    if (!varName) return null
    def entry = numericHubVariables()[varName]
    if (!entry) {
        logWarn "Hub variable ${varName} unavailable or not numeric"
        return null
    }
    try {
        def raw = entry.value ?: entry.currentValue ?: location?.hubVariables?.get(varName)?.value ?: location?.hubVariables?.get(varName)?.currentValue ?: "0"
        Integer value = (raw as BigDecimal).intValue()
        return Math.max(0, Math.min(100, value))
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
    numericHubVariables().keySet()?.toList()?.sort() ?: []
}

def numericHubVariables() {
    Map vars = [:]
    try {
        vars = getAllGlobalVars() ?: [:]
    } catch (ignored) {}
    (vars ?: [:]).findAll { it.value?.type in ["integer", "bigdecimal"] } ?: [:]
}

def formatTime(Date date) {
    if (!date) return "unknown"
    date.format("MMM d h:mm a", location?.timeZone ?: TimeZone.getTimeZone("UTC"))
}

def logInfo(msg) { log.info "[SunriseSunset] ${msg}" }

def logWarn(msg) { log.warn "[SunriseSunset] ${msg}" }

def logDebug(msg) {
    if (enableDebug) {
        log.debug "[SunriseSunset] ${msg}"
    }
}

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
