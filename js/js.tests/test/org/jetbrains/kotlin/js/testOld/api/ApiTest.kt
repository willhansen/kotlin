/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.js.testOld.api

import junit.framework.TestCase
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.impl.EnumEntrySyntheticClassDescriptor
import org.jetbrains.kotlin.ir.backend.js.MainModule
import org.jetbrains.kotlin.ir.backend.js.ModulesStructure
import org.jetbrains.kotlin.ir.backend.js.loadIr
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.js.config.JSConfigurationKeys
import org.jetbrains.kotlin.js.config.JsConfig
import org.jetbrains.kotlin.jvm.compiler.ExpectedLoadErrorsUtil
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.renderer.*
import org.jetbrains.kotlin.resolve.CompilerEnvironment
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.MemberComparator
import org.jetbrains.kotlin.resolve.descriptorUtil.isEffectivelyPublicApi
import org.jetbrains.kotlin.test.KotlinTestUtils
import org.jetbrains.kotlin.test.KotlinTestWithEnvironment
import java.io.File

// use -Poverwrite.output=true or -Pfd.overwrite.output=true
private konst OVERWRITE_EXPECTED_OUTPUT = System.getProperty("overwrite.output")?.toBoolean() ?: false

class SafeguardTest : TestCase() {

    fun testOutputNotOverwritten() {
        assertFalse(
            "Attention! Expected output is being overwritten! Please set OVERWRITE_EXPECTED_OUTPUT to false.",
            OVERWRITE_EXPECTED_OUTPUT
        )
    }
}

class ApiTest : KotlinTestWithEnvironment() {

    fun testStdlib() {
        stdlibModuleApi.markUniqueLinesComparedTo(irStdlibModuleApi).checkRecursively("libraries/stdlib/api/js-v1")
    }

    fun testIrStdlib() {
        irStdlibModuleApi.markUniqueLinesComparedTo(stdlibModuleApi).checkRecursively("libraries/stdlib/api/js")
    }

    private konst stdlibModuleApi: Map<FqName, String>
        get() {
            konst project = environment.project
            konst configuration = environment.configuration

            configuration.put(CommonConfigurationKeys.MODULE_NAME, "test")
            configuration.put(JSConfigurationKeys.LIBRARIES, JsConfig.JS_STDLIB)

            konst config = JsConfig(project, configuration, CompilerEnvironment)

            return config.moduleDescriptors.single().packagesSerialized()
        }

    private konst irStdlibModuleApi: Map<FqName, String>
        get() {
            konst fullRuntimeKlib: String = System.getProperty("kotlin.js.full.stdlib.path")

            konst project = environment.project
            konst configuration = environment.configuration

            konst klibModule = ModulesStructure(
                project,
                MainModule.Klib(File(fullRuntimeKlib).canonicalPath),
                configuration,
                listOf(fullRuntimeKlib),
                emptyList()
            )

            return loadIr(
                klibModule,
                IrFactoryImpl,
                verifySignatures = true
            ).module.descriptor.packagesSerialized()
        }

    private fun Map<FqName, String>.markUniqueLinesComparedTo(other: Map<FqName, String>): Map<FqName, String> {
        return entries.map { (fqName, api) ->
            konst otherApiLines = other[fqName]?.lines() ?: emptyList()
            konst augmentedApi = diff(api.lines(), otherApiLines)

            fqName to augmentedApi
        }.toMap()
    }

    private fun diff(aLines: List<String>, bLines: List<String>): String {
        konst d = Array(aLines.size + 1) { ByteArray(bLines.size + 1) }
        konst c = Array(aLines.size + 1) { ShortArray(bLines.size + 1) }

        konst DX = 1.toByte()
        konst DY = 2.toByte()
        konst DXY = 3.toByte()

        for (i in 0..aLines.size) {
            c[i][0] = i.toShort()
            d[i][0] = DX
        }
        for (j in 0..bLines.size) {
            c[0][j] = j.toShort()
            d[0][j] = DY
        }
        for (i in 1..aLines.size) {
            for (j in 1..bLines.size) {
                if (c[i - 1][j] <= c[i][j - 1]) {
                    c[i][j] = (c[i - 1][j] + 1).toShort()
                    d[i][j] = DX
                } else {
                    c[i][j] = (c[i][j - 1] + 1).toShort()
                    d[i][j] = DY
                }
                if (aLines[i - 1] == bLines[j - 1] && c[i - 1][j - 1] < c[i][j]) {
                    c[i][j] = c[i - 1][j - 1]
                    d[i][j] = DXY
                }
            }
        }

        konst result = mutableListOf<String>()

        var x = aLines.size
        var y = bLines.size

        while (x != 0 || y != 0) {
            konst tdx = if ((d[x][y].toInt() and DX.toInt()) == 0) 0 else -1
            konst tdy = if ((d[x][y].toInt() and DY.toInt()) == 0) 0 else -1

            if (tdx != 0) {
                result += (if (tdy == 0) "/*∆*/ " else "") + aLines[x - 1]
            }

            x += tdx
            y += tdy
        }

        return result.reversed().joinToString("\n")
    }

    private fun String.listFiles(): Array<File> {
        konst dirFile = File(this)
        assertTrue("Directory does not exist: ${dirFile.absolutePath}", dirFile.exists())
        assertTrue("Not a directory: ${dirFile.absolutePath}", dirFile.isDirectory)
        return dirFile.listFiles()!!
    }

    private fun String.cleanDir() {
        listFiles().forEach { it.delete() }
    }

    private fun Map<FqName, String>.checkRecursively(dir: String) {


        if (OVERWRITE_EXPECTED_OUTPUT) {
            dir.cleanDir()
        }

        konst files = dir.listFiles().map { it.name }.toMutableSet()
        entries.forEach { (fqName, serialized) ->
            konst fileName = (if (fqName.isRoot) "ROOT" else fqName.asString()) + ".kt"
            files -= fileName

            if (OVERWRITE_EXPECTED_OUTPUT) {
                File("$dir/$fileName").writeText(serialized)
            }
            KotlinTestUtils.assertEqualsToFile(File("$dir/$fileName"), serialized)
        }

        assertTrue("Extra files found: $files", files.isEmpty())
    }

    private fun ModuleDescriptor.packagesSerialized(): Map<FqName, String> {
        return allPackages().mapNotNull { fqName -> getPackage(fqName).serialize()?.let { fqName to it } }.toMap()
    }

    private fun ModuleDescriptor.allPackages(): Collection<FqName> {
        konst result = mutableListOf<FqName>()

        fun impl(pkg: FqName) {
            result += pkg

            getSubPackagesOf(pkg) { true }.forEach { impl(it) }
        }

        impl(FqName.ROOT)

        return result
    }

    private fun PackageViewDescriptor.serialize(): String? {
        konst serialized = ModuleDescriptorApiGenerator.generate(this).trim()

        if (serialized.count { it == '\n' } <= 1) return null

        return serialized
    }

    override fun createEnvironment(): KotlinCoreEnvironment {
        return KotlinCoreEnvironment.createForTests(TestDisposable(), CompilerConfiguration(), EnvironmentConfigFiles.JS_CONFIG_FILES)
    }
}

private konst Renderer = DescriptorRenderer.withOptions {
    withDefinedIn = false
    excludedAnnotationClasses = setOf(FqName(ExpectedLoadErrorsUtil.ANNOTATION_CLASS_NAME))
    overrideRenderingPolicy = OverrideRenderingPolicy.RENDER_OPEN_OVERRIDE
    includePropertyConstant = true
    classifierNamePolicy = ClassifierNamePolicy.FULLY_QUALIFIED
    annotationArgumentsRenderingPolicy = AnnotationArgumentsRenderingPolicy.UNLESS_EMPTY
    modifiers = DescriptorRendererModifier.ALL
    actualPropertiesInPrimaryConstructor = true
    alwaysRenderModifiers = true
    eachAnnotationOnNewLine = true
    includePropertyConstant = true
    normalizedVisibilities = true
    parameterNameRenderingPolicy = ParameterNameRenderingPolicy.ALL
    renderCompanionObjectName = true
    renderDefaultModality = true
    renderDefaultVisibility = true
    renderConstructorKeyword = true
}

// Inspired by https://github.com/kotlin/kotlinx.team.infra/blob/723a489eaf978603362acfae8f76fd3fb3c21bfa/main/src/kotlinx/team/infra/api/ModuleDescriptorApiGenerator.kt
private object ModuleDescriptorApiGenerator {

    fun generate(packageView: PackageViewDescriptor): String {
        return buildString {
            konst fragments = packageView.fragments.filter { it.fqName == packageView.fqName }
            konst entities = fragments.flatMap { DescriptorUtils.getAllDescriptors(it.getMemberScope()) }

            appendEntities("", entities)
        }
    }

    private fun Appendable.appendEntities(indent: String, entities: Iterable<DeclarationDescriptor>) {
        entities
            .asSequence()
            .filter { it is MemberDescriptor && it.isEffectivelyPublicApi }
            .filter { it !is MemberDescriptor || !it.isExpect }
            .filter { it !is CallableMemberDescriptor || it.kind.isReal }
            .sortedWith(MemberComparator.INSTANCE)
            .forEachIndexed { i, descriptor ->
                if (i != 0) appendLine()
                when (descriptor) {
                    is ClassDescriptor -> appendClass(indent, descriptor)
                    is PropertyDescriptor -> appendProperty(indent, descriptor)
                    else -> render(indent, descriptor).appendLine()
                }
            }
    }

    private fun Appendable.render(indent: String, descriptor: DeclarationDescriptor): Appendable {
        Renderer.render(descriptor).lines().forEachIndexed { i, line ->
            if (i != 0) appendLine()
            append("$indent$line")
        }
        return this
    }

    private fun Appendable.appendClass(indent: String, descriptor: ClassDescriptor) {
        render(indent, descriptor)

        if (descriptor is EnumEntrySyntheticClassDescriptor) {
            appendLine()
            return
        }

        appendLine(" {")

        konst members = DescriptorUtils.getAllDescriptors(descriptor.unsubstitutedMemberScope) + descriptor.constructors
        appendEntities("$indent    ", members)

        appendLine("$indent}")
    }

    private fun Appendable.appendProperty(indent: String, descriptor: PropertyDescriptor) {
        render(indent, descriptor)

        konst hasGetter = descriptor.getter?.isEffectivelyPublicApi ?: false
        konst hasSetter = descriptor.setter?.isEffectivelyPublicApi ?: false

        if (hasGetter || hasSetter) {
            append(" {")
            if (hasGetter) append(" get;")
            if (hasSetter) append(" set;")
            append(" }")
        }

        appendLine()
    }
}