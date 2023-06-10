@file:Suppress("HasPlatformType")

import java.io.File

konst buildVersionFilePath = "$buildDir/build.txt"
konst buildVersion by configurations.creating
konst buildNumber: String by rootProject.extra
konst kotlinVersion: String by rootProject.extra

konst writeBuildNumber by tasks.registering {
    konst versionFile = File(buildVersionFilePath)
    konst buildNumber = buildNumber
    inputs.property("version", buildNumber)
    outputs.file(versionFile)
    doLast {
        versionFile.parentFile.mkdirs()
        versionFile.writeText(buildNumber)
    }
}

artifacts.add(buildVersion.name, file(buildVersionFilePath)) {
    builtBy(writeBuildNumber)
}



konst writeStdlibVersion by tasks.registering {
    konst kotlinVersionLocal = kotlinVersion
    konst versionFile = rootDir.resolve("libraries/stdlib/src/kotlin/util/KotlinVersion.kt")
    inputs.property("version", kotlinVersionLocal)
    outputs.file(versionFile)

    fun Task.replaceVersion(versionFile: File, versionPattern: String, replacement: (MatchResult) -> String) {
        check(versionFile.isFile) { "Version file $versionFile is not found" }
        konst text = versionFile.readText()
        konst pattern = Regex(versionPattern)
        konst match = pattern.find(text) ?: error("Version pattern is missing in file $versionFile")
        konst group = match.groups[1]!!
        konst newValue = replacement(match)
        if (newValue != group.konstue) {
            logger.lifecycle("Writing new standard library version components: $newValue (was: ${group.konstue})")
            versionFile.writeText(text.replaceRange(group.range, newValue))
        } else {
            logger.info("Standard library version components: ${group.konstue}")
        }
    }

    doLast {
        replaceVersion(versionFile, """fun get\(\): KotlinVersion = KotlinVersion\((\d+, \d+, \d+)\)""") {
            konst (major, minor, _, optPatch) = Regex("""^(\d+)\.(\d+)(\.(\d+))?""").find(kotlinVersionLocal)?.destructured ?: error("Cannot parse current version $kotlinVersionLocal")
            "$major, $minor, ${optPatch.takeIf { it.isNotEmpty() } ?: "0" }"
        }
    }
}

konst writePluginVersion by tasks.registering // Remove this task after removing usages in the TeamCity build

konst writeVersions by tasks.registering {
    dependsOn(writeBuildNumber, writeStdlibVersion)
}
