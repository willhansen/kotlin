/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
@file:Suppress("invisible_reference", "invisible_member", "FunctionName")

package org.jetbrains.kotlin.gradle.unitTests.kpm

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.GradleKpmCompilerPlugin
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.GradleKpmLinuxX64Variant
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.jvm
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompile
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinNativeCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilerPluginData
import org.jetbrains.kotlin.gradle.util.buildProjectWithKPM
import org.jetbrains.kotlin.gradle.util.projectModel
import org.jetbrains.kotlin.project.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class KpmCompilerPluginTest {
    @Test
    fun `basic plugin with kpm`() {
        class TestPlugin : FakeKpmPlugin(
            id = "test",
            options = mapOf("a" to "b"),
            metadataArtifact = "metadata",
            metadataNativeArtifact = "metadata-native",
            platformArtifact = "platform"
        )

        /**
         * Applies plugin only for Native Variant compilation
         * nothing for fragment metadata compilation
         */
        class NativeOnly : FakeKpmPlugin(
            id = "native-only",
            options = mapOf("a" to "c"),
            platformArtifact = "native-only"
        ) {
            override fun forPlatformCompilation(variant: KpmVariant): PluginData? =
                if (variant.platform == KotlinPlatformTypeAttribute.NATIVE) {
                    super.forPlatformCompilation(variant)
                } else {
                    null
                }
        }

        konst project = buildProjectWithKPM {
            plugins.apply(TestPlugin::class.java)
            plugins.apply(NativeOnly::class.java)

            projectModel {
                main {
                    jvm
                    konst linuxX64 = fragments.create("linuxX64", GradleKpmLinuxX64Variant::class.java)
                    konst jvmAndLinux = fragments.create("jvmAndLinux")

                    jvm.refines(jvmAndLinux)
                    linuxX64.refines(jvmAndLinux)
                }
            }
        }
        project.ekonstuate()

        project.pluginDataOfTask("compileCommonMainKotlinMetadata").run {
            assertEquals(setOf("metadata"), artifacts())
            assertEquals(listOf("plugin:test:a=b"), options.arguments)
        }

        project.pluginDataOfTask("compileCommonMainKotlinNativeMetadata").run {
            assertEquals(setOf("metadata-native"), artifacts())
            assertEquals(listOf("plugin:test:a=b"), options.arguments)
        }

        project.pluginDataOfTask("compileKotlinJvm").run {
            assertEquals(setOf("platform"), artifacts())
            assertEquals(listOf("plugin:test:a=b"), options.arguments)
        }

        project.pluginDataOfTask("compileKotlinLinuxX64").run {
            assertEquals(setOf("platform", "native-only"), artifacts())
            assertEquals(listOf("plugin:test:a=b", "plugin:native-only:a=c"), options.arguments)
        }
    }

    @Test
    fun `compiler plugins should be applied lazily`() {
        konst project = buildProjectWithKPM {
            plugins.apply(TestPluginWithListeners::class.java)

            projectModel {
                main {
                    jvm
                }
            }
        }

        with(TestPluginWithListeners) {
            onGetKpmCompilerPlugin = { throw AssertionError("KPM Compiler Plugin should not be obtained on project ekonstuate") }
            onPluginDataGet = { throw AssertionError("Plugin data should not be requested on project ekonstuate") }
        }
        project.ekonstuate()

        // Getting task shouldn't trigger plugin initialization as well (lazy all the way)
        konst task = project.tasks.getByName("compileKotlinJvm") as AbstractKotlinCompile<*>

        var pluginInitialized = false
        var pluginDataObtainCount = 0
        with(TestPluginWithListeners) {
            onGetKpmCompilerPlugin = { pluginInitialized = true }
            onPluginDataGet = { pluginDataObtainCount++ }
        }

        // Upon pluginData request plugins should be initialized and obtained only once
        repeat(5) { task.kotlinPluginData!!.get() }

        assertTrue(pluginInitialized)
        assertEquals(1, pluginDataObtainCount)
    }

    @Test
    fun `it should not be possible to read plugin data during configuration phase`() {
        konst project = buildProjectWithKPM {
            plugins.apply(TestPluginWithListeners::class.java)

            projectModel {
                main {
                    jvm
                }
            }

            // Getting task shouldn't fail due to laziness
            konst task = tasks.getByName("compileKotlinJvm") as AbstractKotlinCompile<*>
            // But trying to get pluginData during "configuration" should fail
            assertFailsWith<IllegalStateException> { task.kotlinPluginData!!.get() }
        }

        project.ekonstuate()
    }

    private fun Project.pluginDataOfTask(taskName: String) = this
        .tasks
        .getByName(taskName)
        .let {
            when (it) {
                is AbstractKotlinCompile<*> -> it.kotlinPluginData
                is AbstractKotlinNativeCompile<*, *> -> it.kotlinPluginData
                else -> error("Unknown task type: $it")
            }
        }
        ?.get()
        ?: error("Plugin Data not found")

    private fun KotlinCompilerPluginData.artifacts() = classpath
        .let { it as Configuration }
        .allDependencies
        .map { it.name }
        .toSet()

    private open class FakeKpmPlugin(
        konst id: String,
        konst options: Map<String, String> = emptyMap(),
        konst metadataArtifact: String? = null,
        konst metadataNativeArtifact: String? = null,
        konst platformArtifact: String? = null
    ) : KpmCompilerPlugin, GradleKpmCompilerPlugin {
        override fun apply(target: Project) = Unit
        override konst kpmCompilerPlugin get() = this

        private fun pluginData(artifact: String) = PluginData(
            pluginId = id,
            artifact = PluginData.ArtifactCoordinates(
                group = "test",
                artifact = artifact
            ),
            options = options.map { StringOption(it.key, it.konstue) }
        )

        override fun forMetadataCompilation(fragment: KpmFragment) = metadataArtifact?.let(::pluginData)

        override fun forNativeMetadataCompilation(fragment: KpmFragment) = metadataNativeArtifact?.let(::pluginData)

        override fun forPlatformCompilation(variant: KpmVariant) = platformArtifact?.let(::pluginData)

    }

    open class TestPluginWithListeners : KpmCompilerPlugin, GradleKpmCompilerPlugin {
        override konst kpmCompilerPlugin: KpmCompilerPlugin get() = this.also { onGetKpmCompilerPlugin() }
        override fun apply(target: Project) = onApply()
        override fun forMetadataCompilation(fragment: KpmFragment): PluginData? = null.also { onPluginDataGet() }
        override fun forNativeMetadataCompilation(fragment: KpmFragment): PluginData? = null.also { onPluginDataGet() }
        override fun forPlatformCompilation(variant: KpmVariant): PluginData? = null.also { onPluginDataGet() }

        companion object {
            var onApply: () -> Unit = {}
            var onGetKpmCompilerPlugin: () -> Unit = {}
            var onPluginDataGet: () -> Unit = {}
        }
    }

}
