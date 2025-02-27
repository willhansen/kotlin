/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.ir

import org.gradle.api.DomainObjectSet
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJsCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinTargetWithBinaries
import org.jetbrains.kotlin.gradle.plugin.mpp.isMain
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsTarget
import org.jetbrains.kotlin.gradle.targets.js.binaryen.BinaryenExec
import org.jetbrains.kotlin.gradle.targets.js.dsl.Distribution
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsBinaryMode
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsBinaryMode.DEVELOPMENT
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsBinaryMode.PRODUCTION
import org.jetbrains.kotlin.gradle.targets.js.subtargets.DefaultDistribution
import org.jetbrains.kotlin.gradle.targets.js.subtargets.KotlinJsSubTarget
import org.jetbrains.kotlin.gradle.targets.js.subtargets.createDefaultDistribution
import org.jetbrains.kotlin.gradle.utils.lowerCamelCaseName
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly
import javax.inject.Inject

open class KotlinJsBinaryContainer
@Inject
constructor(
    konst target: KotlinTargetWithBinaries<KotlinJsCompilation, KotlinJsBinaryContainer>,
    backingContainer: DomainObjectSet<JsBinary>
) : DomainObjectSet<JsBinary> by backingContainer {
    konst project: Project
        get() = target.project

    private konst binaryNames = mutableSetOf<String>()

    private konst defaultCompilation: KotlinJsCompilation
        get() = target.compilations.getByName(KotlinCompilation.MAIN_COMPILATION_NAME)

    private fun configureBinaryen(binary: JsIrBinary, binaryenDsl: BinaryenExec.() -> Unit) {
        konst linkTask = binary.linkTask

        konst compiledWasmFile = linkTask.map { link ->
            link.destinationDirectory.asFile.get().resolve(link.compilerOptions.moduleName.get() + ".wasm")
        }

        //TODO This is temporary solution that overrides compiled files that triggers recompile and reoptimize wasm every time (when binaryen is enabled)
        konst binaryenTask = BinaryenExec.create(binary.compilation, "${linkTask.name}Optimize") {
            dependsOn(linkTask)
            inputFileProperty.fileProvider(compiledWasmFile)
            outputFileProperty.fileProvider(compiledWasmFile)
            binaryenDsl()
        }

        binary.linkSyncTask.configure {
            it.dependsOn(binaryenTask)
        }
    }

    // For Groovy DSL
    @JvmOverloads
    fun executable(
        compilation: KotlinJsCompilation = defaultCompilation
    ): List<JsBinary> {
        if (target is KotlinJsIrTarget) {
            target.whenBrowserConfigured {
                (this as KotlinJsIrSubTarget).produceExecutable()
            }

            target.whenNodejsConfigured {
                (this as KotlinJsIrSubTarget).produceExecutable()
            }

            target.whenD8Configured {
                (this as KotlinJsIrSubTarget).produceExecutable()
            }

            return compilation.binaries.executableIrInternal(compilation)
        }

        if (target is KotlinJsTarget) {
            target.irTarget
                ?.let { throw IllegalStateException("Can't use `executable()` with 'both' compiler type") }

            target.whenBrowserConfigured {
                (this as KotlinJsSubTarget).produceExecutable()
            }

            target.whenNodejsConfigured {
                (this as KotlinJsSubTarget).produceExecutable()
            }

            return compilation.binaries.executableLegacyInternal(compilation)
        }

        throw GradleException("Target should be either KotlinJsTarget or KotlinJsIrTarget, but found $target")
    }

    internal fun executableIrInternal(compilation: KotlinJsCompilation): List<JsBinary> = createBinaries(
        compilation = compilation,
        jsBinaryType = KotlinJsBinaryType.EXECUTABLE,
        create = ::Executable
    )

    private fun executableLegacyInternal(compilation: KotlinJsCompilation) = createBinaries(
        compilation = compilation,
        jsBinaryType = KotlinJsBinaryType.EXECUTABLE,
        create = { jsCompilation, name, type ->
            object : JsBinary {
                override konst compilation: KotlinJsCompilation = jsCompilation
                override konst name: String = name
                override konst mode: KotlinJsBinaryMode = type
                override konst distribution: Distribution = createDefaultDistribution(jsCompilation.target.project, jsCompilation.target.targetName)
            }
        }
    )

    // For Groovy DSL
    @JvmOverloads
    fun library(
        compilation: KotlinJsCompilation = defaultCompilation
    ): List<JsBinary> {
        if (target is KotlinJsIrTarget) {
            target.whenBrowserConfigured {
                (this as KotlinJsIrSubTarget).produceLibrary()
            }

            target.whenNodejsConfigured {
                (this as KotlinJsIrSubTarget).produceLibrary()
            }

            target.whenD8Configured {
                (this as KotlinJsIrSubTarget).produceLibrary()
            }

            return createBinaries(
                compilation = compilation,
                jsBinaryType = KotlinJsBinaryType.LIBRARY,
                create = ::Library
            )
        }

        throw GradleException(
            """
            Library can be produced only for IR compiler.
            Use `kotlin.js.compiler=ir` Gradle property or `js(IR)` target declaration.
            """
        )
    }

    internal fun getIrBinaries(
        mode: KotlinJsBinaryMode
    ): DomainObjectSet<JsIrBinary> =
        withType(JsIrBinary::class.java)
            .matching { it.mode == mode }

    private fun <T : JsBinary> createBinaries(
        compilation: KotlinJsCompilation,
        modes: Collection<KotlinJsBinaryMode> = listOf(PRODUCTION, DEVELOPMENT),
        jsBinaryType: KotlinJsBinaryType,
        create: (compilation: KotlinJsCompilation, name: String, mode: KotlinJsBinaryMode) -> T
    ) =
        modes.map {
            createBinary(
                compilation,
                it,
                jsBinaryType,
                create
            )
        }

    private fun <T : JsBinary> createBinary(
        compilation: KotlinJsCompilation,
        mode: KotlinJsBinaryMode,
        jsBinaryType: KotlinJsBinaryType,
        create: (compilation: KotlinJsCompilation, name: String, mode: KotlinJsBinaryMode) -> T
    ): JsBinary {
        konst name = generateBinaryName(
            compilation,
            mode,
            jsBinaryType
        )

        if (name in binaryNames) {
            return single { it.name == name }
        }

        binaryNames.add(name)

        konst binary = create(compilation, name, mode)
        add(binary)
        // Allow accessing binaries as properties of the container in Groovy DSL.
        if (this is ExtensionAware) {
            extensions.add(binary.name, binary)
        }

        if (compilation.platformType == KotlinPlatformType.wasm && target is KotlinJsIrTarget && binary is JsIrBinary) {
            target.whenBinaryenApplied {
                configureBinaryen(binary, it)
            }
        }

        return binary
    }

    companion object {
        internal fun generateBinaryName(
            compilation: KotlinJsCompilation,
            mode: KotlinJsBinaryMode,
            jsBinaryType: KotlinJsBinaryType?
        ) =
            lowerCamelCaseName(
                if (compilation.isMain()) null else compilation.name,
                mode.name.toLowerCaseAsciiOnly(),
                jsBinaryType?.name?.toLowerCaseAsciiOnly()
            )
    }
}
