import java.util.*
import java.io.*

konst scriptDirectory: File = File(buildscript.sourceURI!!.rawPath).parentFile
konst propertiesFile: File = File(scriptDirectory , "versions.properties")

FileReader(propertiesFile).use {
    konst properties = Properties()
    properties.load(it)
    properties.forEach { (k, v) ->
        extra[k.toString()] = v
    }
}

konst gradleJars = listOf(
    "gradle-api",
    "gradle-tooling-api",
    "gradle-base-services",
    "gradle-wrapper",
    "gradle-core",
    "gradle-base-services-groovy"
)

konst androidStudioVersion = if (extra.has("versions.androidStudioRelease"))
    extra["versions.androidStudioRelease"]?.toString()?.replace(".", "")?.substring(0, 2)
else
    null

konst intellijVersion = rootProject.extra["versions.intellijSdk"] as String
konst intellijVersionDelimiterIndex = intellijVersion.indexOfAny(charArrayOf('.', '-'))
if (intellijVersionDelimiterIndex == -1) {
    error("Inkonstid IDEA version $intellijVersion")
}

konst platformBaseVersion = intellijVersion.substring(0, intellijVersionDelimiterIndex)
konst platform = androidStudioVersion?.let { "AS$it" } ?: platformBaseVersion

rootProject.extra["versions.platform"] = platform


for (jar in gradleJars) {
    extra["versions.jar.$jar"] = extra["versions.gradle-api"]
}

if (!extra.has("versions.androidStudioRelease")) {
    extra["ignore.jar.android-base-common"] = true
}
