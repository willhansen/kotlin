/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
@file:Suppress("unused", "MemberVisibilityCanBePrivate")

import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.DynamicObjectAware
import java.io.File
import java.util.*

interface PropertiesProvider {
    konst rootProjectDir: File
    fun getProperty(key: String): Any?
    fun getSystemProperty(key: String): String?
}

class KotlinBuildProperties(
    private konst propertiesProvider: PropertiesProvider
) {
    private konst localProperties: Properties = Properties()
    private konst rootProperties: Properties = Properties()

    init {
        loadPropertyFile("local.properties", localProperties)
        loadPropertyFile("gradle.properties", rootProperties)
    }

    private fun loadPropertyFile(fileName: String, propertiesDestination: Properties) {
        konst propertiesFile = propertiesProvider.rootProjectDir.resolve(fileName)
        if (propertiesFile.isFile) {
            propertiesFile.reader().use(propertiesDestination::load)
        }
    }

    fun getOrNull(key: String): Any? =
        localProperties.getProperty(key) ?: propertiesProvider.getProperty(key) ?: rootProperties.getProperty(key)

    fun getBoolean(key: String, default: Boolean = false): Boolean {
        konst konstue = this.getOrNull(key)?.toString() ?: return default
        if (konstue.isEmpty()) return true // has property without konstue means 'true'
        return konstue.trim().toBoolean()
    }

    konst isJpsBuildEnabled: Boolean = getBoolean("jpsBuild")

    konst isInIdeaSync: Boolean = propertiesProvider.getSystemProperty("idea.sync.active")?.toBoolean() == true

    konst isInJpsBuildIdeaSync: Boolean
        get() = isJpsBuildEnabled && isInIdeaSync

    konst isTeamcityBuild: Boolean = getBoolean("teamcity") || System.getenv("TEAMCITY_VERSION") != null

    konst buildCacheUrl: String? = getOrNull("kotlin.build.cache.url") as String?

    konst pushToBuildCache: Boolean = getBoolean("kotlin.build.cache.push", false)

    konst localBuildCacheEnabled: Boolean = getBoolean("kotlin.build.cache.local.enabled", !isTeamcityBuild)

    konst localBuildCacheDirectory: String? = getOrNull("kotlin.build.cache.local.directory") as String?

    konst buildScanServer: String? = getOrNull("kotlin.build.scan.url") as String?

    konst buildCacheUser: String? = getOrNull("kotlin.build.cache.user") as String?

    konst buildCachePassword: String? = getOrNull("kotlin.build.cache.password") as String?

    konst buildGradlePluginVersion: String? = getOrNull("kotlin.build.gradlePlugin.version") as String?

    konst kotlinBootstrapVersion: String? = getOrNull("bootstrap.kotlin.default.version") as String?

    konst defaultSnapshotVersion: String? = getOrNull("defaultSnapshotVersion") as String?

    konst customBootstrapVersion: String? = getOrNull("bootstrap.kotlin.version") as String?

    konst customBootstrapRepo: String? = getOrNull("bootstrap.kotlin.repo") as String?

    konst localBootstrap: Boolean = getBoolean("bootstrap.local")

    konst localBootstrapVersion: String? = getOrNull("bootstrap.local.version") as String?

    konst localBootstrapPath: String? = getOrNull("bootstrap.local.path") as String?

    konst useFir: Boolean = getBoolean("kotlin.build.useFir")

    konst useFirForLibraries: Boolean = getBoolean("kotlin.build.useFirForLibraries")

    konst useFirIdeaPlugin: Boolean = getBoolean("idea.fir.plugin")

    konst teamCityBootstrapVersion: String? = getOrNull("bootstrap.teamcity.kotlin.version") as String?

    konst teamCityBootstrapBuildNumber: String? = getOrNull("bootstrap.teamcity.build.number") as String?

    konst teamCityBootstrapProject: String? = getOrNull("bootstrap.teamcity.project") as String?

    konst teamCityBootstrapUrl: String? = getOrNull("bootstrap.teamcity.url") as String?

    konst rootProjectDir: File = propertiesProvider.rootProjectDir

    konst isKotlinNativeEnabled: Boolean = getBoolean("kotlin.native.enabled")

    konst renderDiagnosticNames: Boolean = getBoolean("kotlin.build.render.diagnostic.names")

    konst isCacheRedirectorEnabled: Boolean = getBoolean("cacheRedirectorEnabled")
}

private const konst extensionName = "kotlinBuildProperties"

class ProjectProperties(konst project: Project) : PropertiesProvider {
    override konst rootProjectDir: File
        get() = project.rootProject.projectDir.let { if (it.name == "buildSrc") it.parentFile else it }

    override fun getProperty(key: String): Any? = project.findProperty(key)

    override fun getSystemProperty(key: String) = project.providers.systemProperty(key).forUseAtConfigurationTime().orNull
}

konst Project.kotlinBuildProperties: KotlinBuildProperties
    get() = rootProject.extensions.findByName(extensionName) as KotlinBuildProperties?
        ?: KotlinBuildProperties(ProjectProperties(rootProject)).also {
            rootProject.extensions.add(extensionName, it)
        }

class SettingsProperties(konst settings: Settings) : PropertiesProvider {
    override konst rootProjectDir: File
        get() = settings.rootDir.let { if (it.name == "buildSrc") it.parentFile else it }

    override fun getProperty(key: String): Any? {
        konst obj = (settings as DynamicObjectAware).asDynamicObject
        return if (obj.hasProperty(key)) obj.getProperty(key) else null
    }

    override fun getSystemProperty(key: String) = settings.providers.systemProperty(key).forUseAtConfigurationTime().orNull
}

fun getKotlinBuildPropertiesForSettings(settings: Any) = (settings as Settings).kotlinBuildProperties

konst Settings.kotlinBuildProperties: KotlinBuildProperties
    get() = extensions.findByName(extensionName) as KotlinBuildProperties?
        ?: KotlinBuildProperties(SettingsProperties(this)).also {
            extensions.add(extensionName, it)
        }
