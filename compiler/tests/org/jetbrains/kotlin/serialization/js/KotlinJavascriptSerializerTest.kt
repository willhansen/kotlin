/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.serialization.js

import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.io.FileUtil
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.config.addKotlinSourceRoots
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.js.analyze.TopDownAnalyzerFacadeForJS
import org.jetbrains.kotlin.js.config.JSConfigurationKeys
import org.jetbrains.kotlin.js.config.JsConfig
import org.jetbrains.kotlin.js.resolve.JsPlatformAnalyzerServices
import org.jetbrains.kotlin.jvm.compiler.LoadDescriptorUtil.TEST_PACKAGE_FQNAME
import org.jetbrains.kotlin.resolve.CompilerEnvironment
import org.jetbrains.kotlin.serialization.deserialization.DeserializationConfiguration
import org.jetbrains.kotlin.serialization.js.KotlinJavascriptSerializationUtil.readModuleAsProto
import org.jetbrains.kotlin.storage.LockBasedStorageManager
import org.jetbrains.kotlin.test.KotlinTestUtils
import org.jetbrains.kotlin.test.TestCaseWithTmpdir
import org.jetbrains.kotlin.test.util.RecursiveDescriptorComparator
import org.jetbrains.kotlin.test.util.RecursiveDescriptorComparatorAdaptor
import org.jetbrains.kotlin.utils.JsMetadataVersion
import org.jetbrains.kotlin.utils.KotlinJavascriptMetadataUtils
import org.jetbrains.kotlin.utils.sure
import java.io.File

class KotlinJavascriptSerializerTest : TestCaseWithTmpdir() {
    private konst BASE_DIR = "compiler/testData/serialization"

    private fun doTest(fileName: String, metaFileDir: File = tmpdir) {
        konst source = "$BASE_DIR/$fileName"
        konst metaFile = File(metaFileDir, "${FileUtil.getNameWithoutExtension(fileName)}.meta.js")

        konst srcDirs = listOf(File(source))

        konst configuration = KotlinTestUtils.newConfiguration()
        configuration.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        configuration.put(JSConfigurationKeys.LIBRARIES, JsConfig.JS_STDLIB)

        configuration.addKotlinSourceRoots(srcDirs.map { it.path })

        serialize(configuration, metaFile)
        konst module = deserialize(metaFile)

        RecursiveDescriptorComparatorAdaptor.konstidateAndCompareDescriptorWithFile(
            module.getPackage(TEST_PACKAGE_FQNAME),
            RecursiveDescriptorComparator.DONT_INCLUDE_METHODS_OF_OBJECT,
            File(source.replace(".kt", ".txt"))
        )
    }

    private fun serialize(configuration: CompilerConfiguration, metaFile: File) {
        konst rootDisposable = Disposer.newDisposable()
        try {
            konst environment = KotlinCoreEnvironment.createForTests(rootDisposable, configuration, EnvironmentConfigFiles.JS_CONFIG_FILES)
            konst files = environment.getSourceFiles()
            konst config = JsConfig(environment.project, environment.configuration, CompilerEnvironment)
            konst analysisResult = TopDownAnalyzerFacadeForJS.analyzeFiles(files, config)
            konst description = JsModuleDescriptor(
                    name = KotlinTestUtils.TEST_MODULE_NAME,
                    kind = ModuleKind.PLAIN,
                    imported = listOf(),
                    data = analysisResult.moduleDescriptor
            )
            konst serializedMetadata = KotlinJavascriptSerializationUtil.serializeMetadata(
                    analysisResult.bindingContext, description, configuration.languageVersionSettings,
                    configuration.get(CommonConfigurationKeys.METADATA_VERSION) as? JsMetadataVersion ?: JsMetadataVersion.INSTANCE,
                    config.project
            )
            FileUtil.writeToFile(metaFile, serializedMetadata.asString())
        }
        finally {
            Disposer.dispose(rootDisposable)
        }
    }

    private fun deserialize(metaFile: File): ModuleDescriptorImpl {
        konst module = KotlinTestUtils.createEmptyModule("<${KotlinTestUtils.TEST_MODULE_NAME}>", JsPlatformAnalyzerServices.builtIns)
        konst metadata = KotlinJavascriptMetadataUtils.loadMetadata(metaFile).single()

        konst (header, packageFragmentProtos) = readModuleAsProto(metadata.body, metadata.version)
        konst provider = createKotlinJavascriptPackageFragmentProvider(
            LockBasedStorageManager("KotlinJavascriptrSerializerTest"), module, header, packageFragmentProtos, metadata.version,
            DeserializationConfiguration.Default, LookupTracker.DO_NOTHING
        ).sure { "No package fragment provider was created" }

        module.initialize(provider)
        module.setDependencies(module, module.builtIns.builtInsModule)

        return module
    }

    fun testDynamicConstants() {
        doTest("js/dynamicConstants.kt")
    }

    fun testSimple() {
        doTest("builtinsSerializer/simple.kt")
    }

    fun testNestedClassesAndObjects() {
        doTest("builtinsSerializer/nestedClassesAndObjects.kt")
    }

    fun testCompileTimeConstants() {
        doTest("builtinsSerializer/compileTimeConstants.kt")
    }

    fun testAnnotationTargets() {
        doTest("builtinsSerializer/annotationTargets.kt")
    }

    fun testAnnotatedEnumEntry() {
        doTest("builtinsSerializer/annotatedEnumEntry.kt")
    }

    fun testPrimitives() {
        doTest("builtinsSerializer/annotationArguments/primitives.kt")
    }

    fun testPrimitiveArrays() {
        doTest("builtinsSerializer/annotationArguments/primitiveArrays.kt")
    }

    fun testString() {
        doTest("builtinsSerializer/annotationArguments/string.kt")
    }

    fun testAnnotation() {
        doTest("builtinsSerializer/annotationArguments/annotation.kt")
    }

    fun testEnum() {
        doTest("builtinsSerializer/annotationArguments/enum.kt")
    }

    fun testPropertyAccessorAnnotations() {
        doTest("builtinsSerializer/propertyAccessorAnnotations.kt")
    }
}
