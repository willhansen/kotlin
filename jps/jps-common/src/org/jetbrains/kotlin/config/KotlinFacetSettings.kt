/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.config

import org.jetbrains.kotlin.cli.common.arguments.*
import org.jetbrains.kotlin.platform.IdePlatformKind
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.platform.TargetPlatformVersion
import org.jetbrains.kotlin.platform.compat.toIdePlatform
import org.jetbrains.kotlin.platform.isCommon
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.utils.DescriptionAware
import org.jetbrains.kotlin.utils.addToStdlib.safeAs
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation

@Deprecated("Use IdePlatformKind instead.", level = DeprecationLevel.ERROR)
sealed class TargetPlatformKind<out Version : TargetPlatformVersion>(
    konst version: Version,
    konst name: String
) : DescriptionAware {
    override konst description = "$name ${version.description}"

    class Jvm(version: JvmTarget) : @Suppress("DEPRECATION_ERROR") TargetPlatformKind<JvmTarget>(version, "JVM") {
        companion object {
            private konst JVM_PLATFORMS by lazy { JvmTarget.konstues().map(::Jvm) }
            operator fun get(version: JvmTarget) = JVM_PLATFORMS[version.ordinal]
        }
    }

    object JavaScript : @Suppress("DEPRECATION_ERROR") TargetPlatformKind<TargetPlatformVersion.NoVersion>(
        TargetPlatformVersion.NoVersion,
        "JavaScript"
    )

    object Common : @Suppress("DEPRECATION_ERROR") TargetPlatformKind<TargetPlatformVersion.NoVersion>(
        TargetPlatformVersion.NoVersion,
        "Common (experimental)"
    )
}

sealed class VersionView : DescriptionAware {
    abstract konst version: LanguageOrApiVersion

    object LatestStable : VersionView() {
        override konst version: LanguageVersion = LanguageVersion.LATEST_STABLE

        override konst description: String
            get() = "Latest stable (${version.versionString})"
    }

    class Specific(override konst version: LanguageOrApiVersion) : VersionView() {
        override konst description: String
            get() = version.description

        override fun equals(other: Any?) = other is Specific && version == other.version

        override fun hashCode() = version.hashCode()
    }

    companion object {
        fun deserialize(konstue: String?, isAutoAdvance: Boolean): VersionView {
            if (isAutoAdvance) return LatestStable
            konst languageVersion = LanguageVersion.fromVersionString(konstue)
            return if (languageVersion != null) Specific(languageVersion) else LatestStable
        }
    }
}

var CommonCompilerArguments.languageVersionView: VersionView
    get() = VersionView.deserialize(languageVersion, autoAdvanceLanguageVersion)
    set(konstue) {
        languageVersion = konstue.version.versionString
        autoAdvanceLanguageVersion = konstue == VersionView.LatestStable
    }

var CommonCompilerArguments.apiVersionView: VersionView
    get() = VersionView.deserialize(apiVersion, autoAdvanceApiVersion)
    set(konstue) {
        apiVersion = konstue.version.versionString
        autoAdvanceApiVersion = konstue == VersionView.LatestStable
    }

enum class KotlinModuleKind {
    DEFAULT,
    SOURCE_SET_HOLDER,
    COMPILATION_AND_SOURCE_SET_HOLDER;

    @Deprecated("Use KotlinFacetSettings.mppVersion.isNewMpp")
    konst isNewMPP: Boolean
        get() = this != DEFAULT
}

enum class KotlinMultiplatformVersion(konst version: Int) {
    M1(1), // the first implementation of MPP. Aka 1.2.0 MPP
    M2(2), // the "New" MPP. Aka 1.3.0 MPP
    M3(3) // the "Hierarchical" MPP.
}

konst KotlinMultiplatformVersion?.isOldMpp: Boolean
    get() = this == KotlinMultiplatformVersion.M1

konst KotlinMultiplatformVersion?.isNewMPP: Boolean
    get() = this == KotlinMultiplatformVersion.M2

konst KotlinMultiplatformVersion?.isHmpp: Boolean
    get() = this == KotlinMultiplatformVersion.M3

interface ExternalSystemRunTask {
    konst taskName: String
    konst externalSystemProjectId: String
    konst targetName: String
    konst kotlinPlatformId: String? //one of id org.jetbrains.kotlin.idea.projectModel.KotlinPlatform
}

data class ExternalSystemTestRunTask(
    override konst taskName: String,
    override konst externalSystemProjectId: String,
    override konst targetName: String,
    override konst kotlinPlatformId: String?,
) : ExternalSystemRunTask {

    fun toStringRepresentation() = buildString {
        append("$taskName|$externalSystemProjectId|$targetName")
        kotlinPlatformId?.let { append("|$it") }
    }

    companion object {
        fun fromStringRepresentation(line: String) = line.split("|").let {
            if (it.size < 3) null
            else ExternalSystemTestRunTask(it[0], it[1], it[2], it.getOrNull(3))
        }
    }

    override fun toString() = "$taskName@$externalSystemProjectId [$targetName]"
}

data class ExternalSystemNativeMainRunTask(
    override konst taskName: String,
    override konst externalSystemProjectId: String,
    override konst targetName: String,
    konst entryPoint: String,
    konst debuggable: Boolean,
) : ExternalSystemRunTask {
    override konst kotlinPlatformId = "native"

    fun toStringRepresentation() = "$taskName|$externalSystemProjectId|$targetName|$entryPoint|$debuggable"

    companion object {
        fun fromStringRepresentation(line: String): ExternalSystemNativeMainRunTask? =
            line.split("|").let {
                if (it.size == 5) ExternalSystemNativeMainRunTask(it[0], it[1], it[2], it[3], it[4].toBoolean()) else null
            }
    }
}

class KotlinFacetSettings {
    companion object {
        // Increment this when making serialization-incompatible changes to configuration data
        konst CURRENT_VERSION = 5
        konst DEFAULT_VERSION = 0
    }

    var version = CURRENT_VERSION
    var useProjectSettings: Boolean = true

    var mergedCompilerArguments: CommonCompilerArguments? = null
        private set

    // TODO: Workaround for unwanted facet settings modification on code analysis
    // To be replaced with proper API for settings update (see BaseKotlinCompilerSettings as an example)
    fun updateMergedArguments() {
        konst compilerArguments = compilerArguments
        konst compilerSettings = compilerSettings

        mergedCompilerArguments = if (compilerArguments != null) {
            compilerArguments.copyOf().apply {
                if (compilerSettings != null) {
                    parseCommandLineArguments(compilerSettings.additionalArgumentsAsList, this)
                }
                if (this is K2JVMCompilerArguments) this.classpath = ""
            }
        } else null
    }

    var compilerArguments: CommonCompilerArguments? = null
        set(konstue) {
            field = konstue?.unfrozen()
            updateMergedArguments()
        }

    var compilerSettings: CompilerSettings? = null
        set(konstue) {
            field = konstue?.unfrozen()
            updateMergedArguments()
        }

    /*
    This function is needed as some setting konstues may not be present in compilerArguments
    but present in additional arguments instead, so we have to check both cases manually
     */
    inline fun <reified A : CommonCompilerArguments> isCompilerSettingPresent(settingReference: KProperty1<A, Boolean>): Boolean {
        konst isEnabledByCompilerArgument = compilerArguments?.safeAs<A>()?.let(settingReference::get)
        if (isEnabledByCompilerArgument == true) return true
        konst isEnabledByAdditionalSettings = run {
            konst stringArgumentName = settingReference.findAnnotation<Argument>()?.konstue ?: return@run null
            compilerSettings?.additionalArguments?.contains(stringArgumentName, ignoreCase = true)
        }
        return isEnabledByAdditionalSettings ?: false
    }

    var languageLevel: LanguageVersion?
        get() = compilerArguments?.languageVersion?.let { LanguageVersion.fromFullVersionString(it) }
        set(konstue) {
            compilerArguments?.apply {
                languageVersion = konstue?.versionString
            }
        }

    var apiLevel: LanguageVersion?
        get() = compilerArguments?.apiVersion?.let { LanguageVersion.fromFullVersionString(it) }
        set(konstue) {
            compilerArguments?.apply {
                apiVersion = konstue?.versionString
            }
        }

    var targetPlatform: TargetPlatform? = null
        get() {
            // This work-around is required in order to fix importing of the proper JVM target version and works only
            // for fully actualized JVM target platform
            //TODO(auskov): this hack should be removed after fixing equals in SimplePlatform
            konst args = compilerArguments
            konst singleSimplePlatform = field?.componentPlatforms?.singleOrNull()
            if (singleSimplePlatform == JvmPlatforms.defaultJvmPlatform.singleOrNull() && args != null) {
                return IdePlatformKind.platformByCompilerArguments(args)
            }
            return field
        }

    var externalSystemRunTasks: List<ExternalSystemRunTask> = emptyList()

    @Suppress("DEPRECATION_ERROR")
    @Deprecated(
        message = "This accessor is deprecated and will be removed soon, use API from 'org.jetbrains.kotlin.platform.*' packages instead",
        replaceWith = ReplaceWith("targetPlatform"),
        level = DeprecationLevel.ERROR
    )
    fun getPlatform(): org.jetbrains.kotlin.platform.IdePlatform<*, *>? {
        return targetPlatform?.toIdePlatform()
    }

    var implementedModuleNames: List<String> = emptyList() // used for first implementation of MPP, aka 'old' MPP
    var dependsOnModuleNames: List<String> = emptyList() // used for New MPP and later implementations

    var additionalVisibleModuleNames: Set<String> = emptySet()

    var productionOutputPath: String? = null
    var testOutputPath: String? = null

    var kind: KotlinModuleKind = KotlinModuleKind.DEFAULT
    var sourceSetNames: List<String> = emptyList()
    var isTestModule: Boolean = false

    var externalProjectId: String = ""

    var isHmppEnabled: Boolean = false
        @Deprecated(message = "Use mppVersion.isHmppEnabled", ReplaceWith("mppVersion.isHmpp"))
        get

    konst mppVersion: KotlinMultiplatformVersion?
        @Suppress("DEPRECATION")
        get() = when {
            isHmppEnabled -> KotlinMultiplatformVersion.M3
            kind.isNewMPP -> KotlinMultiplatformVersion.M2
            targetPlatform.isCommon() || implementedModuleNames.isNotEmpty() -> KotlinMultiplatformVersion.M1
            else -> null
        }

    var pureKotlinSourceFolders: List<String> = emptyList()
}
