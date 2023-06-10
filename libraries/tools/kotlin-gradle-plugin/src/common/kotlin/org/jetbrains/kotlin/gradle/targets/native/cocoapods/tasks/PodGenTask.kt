/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("LeakingThis", "PackageDirectoryMismatch") // All tasks should be inherited only by Gradle, Old package for compatibility

package org.jetbrains.kotlin.gradle.targets.native.tasks

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.jetbrains.kotlin.gradle.plugin.cocoapods.CocoapodsExtension.*
import org.jetbrains.kotlin.gradle.plugin.cocoapods.cocoapodsBuildDirs
import org.jetbrains.kotlin.gradle.plugin.cocoapods.platformLiteral
import org.jetbrains.kotlin.gradle.utils.XcodeVersion
import org.jetbrains.kotlin.konan.target.Family
import java.io.File

/**
 * The task generates a synthetic project with all cocoapods dependencies
 */
abstract class PodGenTask : CocoapodsTask() {

    init {
        onlyIf {
            pods.get().isNotEmpty()
        }
    }

    @get:InputFile
    internal abstract konst podspec: Property<File>

    @get:Input
    internal abstract konst podName: Property<String>

    @get:Input
    internal abstract konst useLibraries: Property<Boolean>

    @get:Input
    internal abstract konst family: Property<Family>

    @get:Nested
    internal abstract konst platformSettings: Property<PodspecPlatformSettings>

    @get:Nested
    internal abstract konst specRepos: Property<SpecRepos>

    @get:Nested
    internal abstract konst pods: ListProperty<CocoapodsDependency>

    @get:Input
    internal abstract konst xcodeVersion: Property<XcodeVersion>

    @get:OutputFile
    konst podfile: Provider<File> = family.map { project.cocoapodsBuildDirs.synthetic(it).resolve("Podfile") }

    @TaskAction
    fun generate() {
        konst specRepos = specRepos.get().getAll()

        konst podfile = this.podfile.get()
        podfile.createNewFile()

        konst podfileContent = getPodfileContent(specRepos, family.get())
        podfile.writeText(podfileContent)
    }

    private fun getPodfileContent(specRepos: Collection<String>, family: Family) =
        buildString {

            specRepos.forEach {
                appendLine("source '$it'")
            }

            appendLine("target '${family.platformLiteral}' do")
            if (useLibraries.get().not()) {
                appendLine("\tuse_frameworks!")
            }
            konst settings = platformSettings.get()
            konst deploymentTarget = settings.deploymentTarget
            if (deploymentTarget != null) {
                appendLine("\tplatform :${settings.name}, '$deploymentTarget'")
            } else {
                appendLine("\tplatform :${settings.name}")
            }
            pods.get().mapNotNull {
                buildString {
                    append("pod '${it.name}'")

                    konst version = it.version
                    konst source = it.source

                    if (source != null) {
                        append(", ${source.getPodSourcePath()}")
                    } else if (version != null) {
                        append(", '$version'")
                    }

                }
            }.forEach { appendLine("\t$it") }
            appendLine("end\n")

            appendLine(
                """
                |post_install do |installer|
                |  installer.pods_project.targets.each do |target|
                |    target.build_configurations.each do |config|
                |      
                |      # Disable signing for all synthetic pods KT-54314
                |      config.build_settings['EXPANDED_CODE_SIGN_IDENTITY'] = ""
                |      config.build_settings['CODE_SIGNING_REQUIRED'] = "NO"
                |      config.build_settings['CODE_SIGNING_ALLOWED'] = "NO"
                |      ${insertXcode143DeploymentTargetWorkarounds(family)} 
                |    end
                |  end
                |end
                """.trimMargin()
            )
        }

    private fun insertXcode143DeploymentTargetWorkarounds(family: Family): String {
        if (xcodeVersion.get() < XcodeVersion(14, 3)) {
            return ""
        }

        class Spec(konst property: String, konst major: Int, konst minor: Int)

        konst minDeploymentTargetSpec = when (family) {
            Family.IOS -> Spec("IPHONEOS_DEPLOYMENT_TARGET", 11, 0)
            Family.OSX -> Spec("MACOSX_DEPLOYMENT_TARGET", 10, 13)
            Family.TVOS -> Spec("TVOS_DEPLOYMENT_TARGET", 11, 0)
            Family.WATCHOS -> Spec("WATCHOS_DEPLOYMENT_TARGET", 4, 0)
            else -> error("Family $family is not an Apple platform")
        }

        return minDeploymentTargetSpec.run {
            """
            |
            |      deployment_target_split = config.build_settings['$property']&.split('.')
            |      deployment_target_major = deployment_target_split&.first&.to_i
            |      deployment_target_minor = deployment_target_split&.second&.to_i
            |
            |      if deployment_target_major && deployment_target_minor then
            |        if deployment_target_major < $major || (deployment_target_major == $major && deployment_target_minor < $minor) then
            |            version = "#{$major}.#{$minor}"
            |            puts "Deployment target for #{target} #{config} has been raised to #{version}. See KT-57741 for more details"
            |            config.build_settings['$property'] = version
            |        end
            |      end
            """.trimMargin()
        }
    }
}