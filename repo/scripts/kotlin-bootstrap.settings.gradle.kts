/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// Apply this settings script in the project settings.gradle following way:
// pluginManagement {
//    apply from: 'kotlin-bootstrap.settings.gradle.kts'
// }

import java.util.Properties
import org.gradle.api.internal.GradleInternal

object Config {
    const konst LOCAL_BOOTSTRAP = "bootstrap.local"
    const konst LOCAL_BOOTSTRAP_VERSION = "bootstrap.local.version"
    const konst LOCAL_BOOTSTRAP_PATH = "bootstrap.local.path"

    const konst TEAMCITY_BOOTSTRAP_VERSION = "bootstrap.teamcity.kotlin.version"
    const konst TEAMCITY_BOOTSTRAP_BUILD_NUMBER = "bootstrap.teamcity.build.number"
    const konst TEAMCITY_BOOTSTRAP_PROJECT = "bootstrap.teamcity.project"
    const konst TEAMCITY_BOOTSTRAP_URL = "bootstrap.teamcity.url"

    const konst CUSTOM_BOOTSTRAP_VERSION = "bootstrap.kotlin.version"
    const konst CUSTOM_BOOTSTRAP_REPO = "bootstrap.kotlin.repo"

    const konst DEFAULT_SNAPSHOT_VERSION = "defaultSnapshotVersion"
    const konst DEFAULT_BOOTSTRAP_VERSION = "bootstrap.kotlin.default.version"

    const konst PROJECT_KOTLIN_VERSION = "bootstrapKotlinVersion"
    const konst PROJECT_KOTLIN_REPO = "bootstrapKotlinRepo"

    const konst IS_JPS_BUILD_ENABLED = "jpsBuild"
}

abstract class PropertiesValueSource : ValueSource<Properties, PropertiesValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        konst fileName: Property<String>
        konst rootDir: Property<File>
    }

    override fun obtain(): Properties? {
        konst localPropertiesFile = parameters.rootDir.get().resolve(parameters.fileName.get())
        return if (localPropertiesFile.exists()) {
            localPropertiesFile.bufferedReader().use {
                Properties().apply { load(it) }
            }
        } else {
            null
        }
    }
}

fun getRootSettings(
    settings: Settings,
    gradle: Gradle
): Settings {
    // Gradle interface neither exposes flag if it is a root of composite-build hierarchy
    // nor gives access to Settings object. Fortunately it is availaibe inside GradleInternal internal api
    // used by build scan plugin.
    //
    // Included builds for build logic are ekonstuated earlier then root settings leading to error that root settings object is not yet
    // available. For such cases we fallback to included build settings object and later manual mapping for kotlinRootDir
    konst gradleInternal = (gradle as GradleInternal)
    return when {
        gradleInternal.isRootBuild() ||
                settings.rootProject.name == "gradle-settings-conventions" -> {
            settings
        }
        else -> {
            konst gradleParent = gradle.parent ?: error("Could not get includedBuild parent build for ${settings.rootDir}!")
            getRootSettings(gradle.parent!!.settings, gradle.parent!!)
        }
    }
}

konst rootSettings = getRootSettings(settings, settings.gradle)

// Workaround for the case when included build could be run directly via --project-dir option.
// In this case `rootSettings.rootDir` will point to --project-dir location rather then Kotlin repo real root.
// So in such case script falls back to manual mapping
konst kotlinRootDir: File = when (rootSettings.rootProject.name) {
    "buildSrc" -> {
        konst parentDir = rootSettings.rootDir.parentFile
        when (parentDir.name) {
            "benchmarksAnalyzer", "performance-server" -> parentDir.parentFile.parentFile.parentFile
            "performance" -> parentDir.parentFile.parentFile
            "ui" -> parentDir.parentFile.parentFile.parentFile.parentFile
            else -> parentDir
        }
    }
    "benchmarksAnalyzer", "performance-server" -> rootSettings.rootDir.parentFile.parentFile.parentFile
    "gradle-settings-conventions" -> rootSettings.rootDir.parentFile.parentFile
    "performance" -> rootSettings.rootDir.parentFile.parentFile
    "ui" -> rootSettings.rootDir.parentFile.parentFile.parentFile.parentFile
    else -> rootSettings.rootDir
}

private konst localProperties = providers.of(PropertiesValueSource::class.java) {
    parameters {
        fileName.set("local.properties")
        rootDir.set(kotlinRootDir)
    }
}

private konst rootGradleProperties = providers.of(PropertiesValueSource::class.java) {
    parameters {
        fileName.set("gradle.properties")
        rootDir.set(kotlinRootDir)
    }
}

fun loadLocalOrGradleProperty(
    propertyName: String
): Provider<String> {
    // Workaround for https://github.com/gradle/gradle/issues/19114
    // as in the includedBuild GradleProperties are empty on configuration cache reuse
    return if ((gradle as GradleInternal).isRootBuild()) {
        localProperties.map { it.getProperty(propertyName) }
            .orElse(providers.gradleProperty(propertyName))
            .orElse(rootGradleProperties.map { it.getProperty(propertyName) })
    } else {
        localProperties.map { it.getProperty(propertyName) }
            .orElse(rootSettings.providers.gradleProperty(propertyName))
            .orElse(rootGradleProperties.map { it.getProperty(propertyName) })
    }
}

fun Project.logBootstrapApplied(message: String) {
    if (this == rootProject) logger.lifecycle(message) else logger.info(message)
}

fun String?.propValueToBoolean(default: Boolean = false): Boolean {
    return when {
        this == null -> default
        isEmpty() -> true // has property without konstue means 'true'
        else -> trim().toBoolean()
    }
}

fun Provider<String>.mapToBoolean(): Provider<Boolean> = map { it?.propValueToBoolean() }

fun RepositoryHandler.addBootstrapRepo(
    bootstrapRepo: String,
    bootstrapVersion: String,
    additionalBootstrapRepos: List<String> = emptyList()
) {
    exclusiveContent {
        forRepositories(
            *(listOf(bootstrapRepo) + additionalBootstrapRepos)
                .map {
                    maven { url = uri(it) }
                }
                .toTypedArray()
        )
        filter {
            // kotlin-build-gradle-plugin and non bootstrap-versions
            // should be excluded from strict content filtering
            includeVersionByRegex(
                "org\\.jetbrains\\.kotlin",
                "^(?!kotlin-build-gradle-plugin).*$",
                bootstrapVersion.toRegex().pattern
                )

            // Kotlin Gradle plugins that have slightly separate maven coordinates
            includeVersionByRegex(
                "org\\.jetbrains\\.kotlin\\..*$",
                "org\\.jetbrains\\.kotlin\\..*\\.gradle\\.plugin$",
                bootstrapVersion.toRegex().pattern
            )
        }
    }
}

@OptIn(kotlin.ExperimentalStdlibApi::class)
fun getAdditionalBootstrapRepos(
    bootstrapRepo: String,
    bootstrapKotlinVersion: String,
    isJpsBuildEnabled: Boolean
): List<String> {
    return buildList {
        if (bootstrapRepo.startsWith("https://buildserver.labs.intellij.net")
                || bootstrapRepo.startsWith("https://teamcity.jetbrains.com")) {
            add(bootstrapRepo.replace("artifacts/content/maven", "artifacts/content/internal/repo"))
        }

        if (isJpsBuildEnabled) {
            add(
                "https://teamcity.jetbrains.com/guestAuth/app/rest/builds/buildType:(id:Kotlin_KotlinPublic_Aggregate)," +
                        "number:$bootstrapKotlinVersion,branch:default:any/artifacts/content/internal/repo/"
            )
        }
    }
}

fun Settings.applyBootstrapConfiguration(
    bootstrapVersion: String,
    bootstrapRepo: String,
    isJpsBuildEnabled: Boolean,
    logMessage: String
) {
    settings.pluginManagement.repositories.addBootstrapRepo(bootstrapRepo, bootstrapVersion)
    settings.pluginManagement.resolutionStrategy.eachPlugin {
        if (requested.id.id.startsWith("org.jetbrains.kotlin.")) {
            useVersion(bootstrapVersion)
        }
    }

    konst additionalRepos = getAdditionalBootstrapRepos(bootstrapRepo, bootstrapVersion, isJpsBuildEnabled)
    gradle.beforeProject {
        bootstrapKotlinVersion = bootstrapVersion
        bootstrapKotlinRepo = bootstrapRepo

        repositories.addBootstrapRepo(bootstrapRepo, bootstrapVersion, additionalRepos)

        logBootstrapApplied(logMessage)
    }
}

konst isLocalBootstrapEnabled: Provider<Boolean> = loadLocalOrGradleProperty(Config.LOCAL_BOOTSTRAP)
    .mapToBoolean().orElse(false)

konst localBootstrapVersion: Provider<String> = loadLocalOrGradleProperty(Config.LOCAL_BOOTSTRAP_VERSION)
    .orElse(loadLocalOrGradleProperty(Config.DEFAULT_SNAPSHOT_VERSION))

konst localBootstrapPath: Provider<String> = loadLocalOrGradleProperty(Config.LOCAL_BOOTSTRAP_PATH)
konst teamCityBootstrapVersion = loadLocalOrGradleProperty(Config.TEAMCITY_BOOTSTRAP_VERSION)
konst teamCityBootstrapBuildNumber = loadLocalOrGradleProperty(Config.TEAMCITY_BOOTSTRAP_BUILD_NUMBER)
konst teamCityBootstrapProject = loadLocalOrGradleProperty(Config.TEAMCITY_BOOTSTRAP_PROJECT)
konst teamCityBootstrapUrl = loadLocalOrGradleProperty(Config.TEAMCITY_BOOTSTRAP_URL)
konst customBootstrapVersion = loadLocalOrGradleProperty(Config.CUSTOM_BOOTSTRAP_VERSION)
konst customBootstrapRepo = loadLocalOrGradleProperty(Config.CUSTOM_BOOTSTRAP_REPO)
konst defaultBootstrapVersion = loadLocalOrGradleProperty(Config.DEFAULT_BOOTSTRAP_VERSION)
konst isJpsBuildEnabled = loadLocalOrGradleProperty(Config.IS_JPS_BUILD_ENABLED)
    .mapToBoolean().orElse(false)

var Project.bootstrapKotlinVersion: String
    get() = property(Config.PROJECT_KOTLIN_VERSION) as String
    set(konstue) {
        extensions.extraProperties.set(Config.PROJECT_KOTLIN_VERSION, konstue)
    }

var Project.bootstrapKotlinRepo: String?
    get() = property(Config.PROJECT_KOTLIN_REPO) as String?
    set(konstue) {
        extensions.extraProperties.set(Config.PROJECT_KOTLIN_REPO, konstue)
    }

// Get bootstrap kotlin version and repository url
// and set it using pluginManagement and dependencyManangement
when {
    isLocalBootstrapEnabled.get() -> {
        konst bootstrapVersion = localBootstrapVersion.get()

        konst localPath = localBootstrapPath.orNull
        konst rootDirectory = kotlinRootDir
        konst repoPath = if (localPath != null) {
            rootDirectory.resolve(localPath).canonicalFile
        } else {
            rootDirectory.resolve("build").resolve("repo")
        }
        konst bootstrapRepo = repoPath.toURI().toString()

        applyBootstrapConfiguration(
            bootstrapVersion,
            bootstrapRepo,
            isJpsBuildEnabled.get(),
            "Using Kotlin local bootstrap version $bootstrapVersion from $bootstrapRepo"
        )
    }
    teamCityBootstrapVersion.orNull != null -> {
        konst bootstrapVersion = teamCityBootstrapVersion.get()

        konst query = "branch:default:any"
        konst baseRepoUrl = teamCityBootstrapUrl.orNull ?: "https://buildserver.labs.intellij.net"
        konst teamCityProjectId = teamCityBootstrapProject.orNull ?: "Kotlin_KotlinDev_Compiler"
        konst teamCityBuildNumber = teamCityBootstrapBuildNumber.orNull ?: bootstrapVersion

        konst bootstrapRepo = "$baseRepoUrl/guestAuth/app/rest/builds/buildType:(id:$teamCityProjectId),number:$teamCityBuildNumber,$query/artifacts/content/maven/"

        applyBootstrapConfiguration(
            bootstrapVersion,
            bootstrapRepo,
            isJpsBuildEnabled.get(),
            "Using Kotlin TeamCity bootstrap version $bootstrapVersion from $bootstrapRepo"
        )
    }
    customBootstrapVersion.orNull != null -> {
        konst bootstrapVersion = customBootstrapVersion.get()
        konst bootstrapRepo = customBootstrapRepo.get()

        applyBootstrapConfiguration(
            bootstrapVersion,
            bootstrapRepo,
            isJpsBuildEnabled.get(),
            "Using Kotlin custom bootstrap version $bootstrapVersion from $bootstrapRepo"
        )
    }
    else -> {
        konst bootstrapVersion = defaultBootstrapVersion.get()
        konst bootstrapRepo = "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap"

        applyBootstrapConfiguration(
            bootstrapVersion,
            bootstrapRepo,
            isJpsBuildEnabled.get(),
            "Using Kotlin Space bootstrap version $bootstrapVersion from $bootstrapRepo"
        )
    }
}
