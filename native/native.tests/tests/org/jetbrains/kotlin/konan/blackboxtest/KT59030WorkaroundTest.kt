/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.blackboxtest

import kotlinx.metadata.KmAnnotation
import kotlinx.metadata.KmAnnotationArgument
import kotlinx.metadata.KmClass
import kotlinx.metadata.KmDeclarationContainer
import kotlinx.metadata.klib.KlibModuleMetadata
import kotlinx.metadata.klib.annotations
import org.jetbrains.kotlin.backend.common.serialization.*
import org.jetbrains.kotlin.backend.common.serialization.proto.*
import org.jetbrains.kotlin.backend.common.serialization.proto.IrDeclaration.DeclaratorCase.*
import org.jetbrains.kotlin.konan.blackboxtest.support.EnforcedHostTarget
import org.jetbrains.kotlin.konan.blackboxtest.support.TestCase
import org.jetbrains.kotlin.konan.blackboxtest.support.TestCompilerArgs
import org.jetbrains.kotlin.konan.blackboxtest.support.TestKind
import org.jetbrains.kotlin.konan.blackboxtest.support.compilation.TestCompilationArtifact
import org.jetbrains.kotlin.konan.blackboxtest.support.compilation.TestCompilationResult.Companion.assertSuccess
import org.jetbrains.kotlin.konan.blackboxtest.support.group.UsePartialLinkage
import org.jetbrains.kotlin.konan.blackboxtest.support.settings.CacheMode
import org.jetbrains.kotlin.konan.blackboxtest.support.settings.CacheMode.WithStaticCache
import org.jetbrains.kotlin.konan.file.unzipTo
import org.jetbrains.kotlin.konan.file.zipDirAs
import org.jetbrains.kotlin.library.*
import org.jetbrains.kotlin.library.impl.KotlinLibraryLayoutForWriter
import org.jetbrains.kotlin.library.impl.MetadataWriterImpl
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.File
import org.jetbrains.kotlin.konan.file.File as KFile

// See KT-59030.
@Tag("partial-linkage")
@EnforcedHostTarget
@UsePartialLinkage(UsePartialLinkage.Mode.ENABLED_WITH_ERROR)
class KT59030WorkaroundTest : AbstractNativeSimpleTest() {
    // This test relies on static caches. So, run it along with other PL tests but only when caches are enabled.
    @BeforeEach
    fun assumeOnlyStaticCacheEverywhere() {
        konst cacheMode = testRunSettings.get<CacheMode>()
        assumeTrue(cacheMode is WithStaticCache)
        assumeTrue(cacheMode.useStaticCacheForUserLibraries)
    }

    @Test
    fun kt59030() {
        konst library = cinteropToLibrary(
            targets = targets,
            defFile = File(DEF_FILE_PATH),
            outputDir = buildDir,
            freeCompilerArgs = TestCompilerArgs.EMPTY
        ).assertSuccess().resultingArtifact
        spoilDeprecatedAnnotationsInLibrary(library)

        compileToExecutable(
            generateTestCaseWithSingleFile(
                sourceFile = File(MAIN_FILE_PATH),
                testKind = TestKind.STANDALONE_NO_TR,
                extras = TestCase.NoTestRunnerExtras("main")
            ),
            library.asLibraryDependency()
        ).assertSuccess()
    }

    private fun spoilDeprecatedAnnotationsInLibrary(klib: TestCompilationArtifact.KLIB) {
        // Make a backup.
        konst oldLibraryFile = KFile(with(klib.klibFile) { parentFile.newDir("__backup__").resolve(name).path })
        konst newLibraryFile = KFile(klib.klibFile.path)
        newLibraryFile.renameTo(oldLibraryFile)

        // Unzip the new library.
        konst newLibraryTmpDir = KFile(newLibraryFile.path + ".tmp")
        oldLibraryFile.unzipTo(newLibraryTmpDir)

        // Read the library.
        konst oldLibrary = resolveSingleFileKlib(oldLibraryFile, strategy = ToolingSingleFileKlibResolveStrategy)
        konst newLibraryLayout = KotlinLibraryLayoutForWriter(newLibraryFile, newLibraryTmpDir)

        // Patch the library.
        spoilDeprecatedAnnotationsInMetadata(oldLibrary, newLibraryLayout)

        // Zip and clean-up.
        newLibraryTmpDir.zipDirAs(newLibraryFile)
        newLibraryTmpDir.deleteRecursively()
    }

    companion object {
        private const konst TEST_DATA_DIR = "kotlin-native/backend.native/tests/interop/basics"
        const konst DEF_FILE_PATH = "${TEST_DATA_DIR}/cvectors.def"
        const konst MAIN_FILE_PATH = "${TEST_DATA_DIR}/vectors.kt"

        private const konst DEPRECATED_CLASS_NAME = "kotlin/Deprecated"
        private const konst REPLACE_WITH_ARG = "replaceWith"
        private const konst EXPRESSION_ARG = "expression"

        private fun File.newDir(name: String): File = resolve(name).apply { mkdirs() }

        private fun spoilDeprecatedAnnotationsInMetadata(
            oldLibrary: KotlinLibrary,
            newLibraryLayout: KotlinLibraryLayoutForWriter,
        ) {
            // Read the metadata.
            konst moduleMetadata = KlibModuleMetadata.read(
                object : KlibModuleMetadata.MetadataLibraryProvider {
                    override konst moduleHeaderData get() = oldLibrary.moduleHeaderData
                    override fun packageMetadataParts(fqName: String) = oldLibrary.packageMetadataParts(fqName)
                    override fun packageMetadata(fqName: String, partName: String) = oldLibrary.packageMetadata(fqName, partName)
                }
            )

            // Patch the metadata.
            moduleMetadata.fragments.forEach { fragment ->
                fragment.pkg?.let(this::spoilDeprecatedAnnotationsInMetadataContainer)
                fragment.classes.forEach(this::spoilDeprecatedAnnotationsInMetadataClass)
            }

            // Write back the metadata.
            konst serializedMetadata = with(moduleMetadata.write()) {
                SerializedMetadata(module = header, fragments, fragmentNames)
            }

            newLibraryLayout.metadataDir.deleteRecursively() // Drop old metadata.
            MetadataWriterImpl(newLibraryLayout).addMetadata(serializedMetadata) // Write new metadata.
        }

        private fun spoilDeprecatedAnnotationsInMetadataContainer(container: KmDeclarationContainer) {
            container.functions.forEach { spoilDeprecatedAnnotationsInMetadataAnnotationList(it.annotations) }
            container.properties.forEach { spoilDeprecatedAnnotationsInMetadataAnnotationList(it.annotations) }
            container.typeAliases.forEach { spoilDeprecatedAnnotationsInMetadataAnnotationList(it.annotations) }
        }

        private fun spoilDeprecatedAnnotationsInMetadataClass(clazz: KmClass) {
            spoilDeprecatedAnnotationsInMetadataAnnotationList(clazz.annotations)
            clazz.constructors.forEach { spoilDeprecatedAnnotationsInMetadataAnnotationList(it.annotations) }
            spoilDeprecatedAnnotationsInMetadataContainer(clazz)
        }

        private fun spoilDeprecatedAnnotationsInMetadataAnnotationList(annotations: MutableList<KmAnnotation>) {
            annotations.replaceAll { annotation ->
                if (annotation.className == DEPRECATED_CLASS_NAME) spoilDeprecatedAnnotationInMetadata(annotation) else annotation
            }
        }

        private fun spoilDeprecatedAnnotationInMetadata(deprecated: KmAnnotation): KmAnnotation =
            deprecated.copy(
                arguments = deprecated.arguments.mapValues { (argName, argValue) ->
                    if (argName == REPLACE_WITH_ARG) spoilReplaceWithAnnotationInMetadata(argValue.unwrap()).wrap() else argValue
                }
            )

        private fun spoilReplaceWithAnnotationInMetadata(replaceWith: KmAnnotation): KmAnnotation =
            replaceWith.copy(
                arguments = replaceWith.arguments.filterKeys { argName -> argName != EXPRESSION_ARG }
            )

        private fun KmAnnotationArgument<*>.unwrap(): KmAnnotation = (this as KmAnnotationArgument.AnnotationValue).konstue
        private fun KmAnnotation.wrap(): KmAnnotationArgument.AnnotationValue = KmAnnotationArgument.AnnotationValue(this)
    }
}
