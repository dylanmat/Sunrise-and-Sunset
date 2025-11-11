# List of issues to fix


* "Hub variable for final level" pulldown is not populated with anything. It should contain a list of hub variables that are numeric. As a result this warning occurs: [SunriseSunset] Sunrise skipped (missing target level)
* Scheduled a sunrise for 10:05 AM and at that time, the app just reported: [SunriseSunset] Sunrise scheduled for Nov 12 10:05 AM and did nothing. Probably because it didn't have a target level.
* set the app to sunrise at 10:15 AM and refreshed the app and get this error: groovy.lang.MissingMethodException: No signature of method: user_app_sunriseSunset_Sunrise___Sunset_Light_Experience_1003.runOnce() is applicable for argument types: (java.util.Date, org.codehaus.groovy.runtime.MethodClosure) values: [Tue Nov 11 10:15:00 PST 2025, org.codehaus.groovy.runtime.MethodClosure@10b607e] Possible solutions: run(), run() on line 101 (method updated)
