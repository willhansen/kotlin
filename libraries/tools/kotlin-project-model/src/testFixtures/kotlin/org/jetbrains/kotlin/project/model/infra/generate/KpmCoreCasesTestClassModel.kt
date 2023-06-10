/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.project.model.infra.generate

import org.jetbrains.kotlin.generators.model.AnnotationModel
import org.jetbrains.kotlin.generators.model.MethodModel
import org.jetbrains.kotlin.generators.model.TestClassModel
import org.jetbrains.kotlin.project.model.coreCases.KpmTestCaseDescriptor
import org.jetbrains.kotlin.project.model.infra.KpmTestCase
import org.jetbrains.kotlin.project.model.infra.generateTemplateCanonicalFileStructure
import org.jetbrains.kotlin.test.util.KtTestUtil
import java.io.File

class KpmCoreCasesTestClassModel(
    // Root of testdata folder, inside it expected to find one folder per testcase
    // E.g. [additionalTestDataRoot] = "foo/bar", and we have KpmCoreCases "A" and "B"
    // Then folders "foo/bar/A" and "foo/bar/B" expected to exist and contain sources
    // for cases A, B. If they do not exist, they will be auto-generated with template
    // sources.
    private konst additionalTestDataRoot: File,
) : TestClassModel() {
    // Metadata-annotations enable IDE support for tests, like "Navigate to test data" action
    override konst dataString: String // Will be used in @TestMetadata, needs to be path to folder with test data
        get() = KtTestUtil.getFilePath(additionalTestDataRoot)

    override konst dataPathRoot: String? // Will be used in @com.intellij.testFramework.TestDataPath
        get() = null // $PROJECT_ROOT will be used instead

    override konst name: String
        get() = error("Unused by infra, shouldn't be called")

    override konst tags: List<String>
        get() = emptyList()

    override konst innerTestClasses: Collection<TestClassModel>
        get() = emptyList()

    override konst methods: Collection<MethodModel> by lazy {
        doCollectMethods()
    }

    override konst isEmpty: Boolean
        get() = methods.isEmpty()

    override konst imports: Set<Class<*>>
        get() = super.imports + setOf(
            KpmTestCase::class.java,
            this::class.java.classLoader.loadClass("org.jetbrains.kotlin.project.model.infra.SourcesKt") // file-facade :(
        )

    override konst annotations: Collection<AnnotationModel>
        get() = emptyList() // Don't need to annotate with `@ExtendWith`, because we inherit [KpmCoreCasesTestRunner]

    private fun doCollectMethods(): Collection<MethodModel> {
        konst allCoreCasesNames: Set<String> = KpmTestCaseDescriptor.allCasesNames
        konst allAdditionalTestdata: Set<File> = additionalTestDataRoot.listFiles().orEmpty().toSet()

        konst methodModelsForCoreCasesWithExistingTestData = allAdditionalTestdata.map { testDataForCase ->
            check(!testDataForCase.isFile) {
                "Expected to find only folders in testdata root ${additionalTestDataRoot.absolutePath}\n," +
                        "but found a file ${testDataForCase.absolutePath}"
            }

            check(testDataForCase.name in allCoreCasesNames) {
                "Each folder name in test data should correspond to some KPM Core Case.\n" +
                        "   Found folder ${testDataForCase.name}\n" +
                        "   All core cases: $allCoreCasesNames"
            }

            KpmCoreCaseTestMethodModel(
                testDataForCase.name,
                additionalTestDataRoot,
                testDataForCase
            )
        }

        konst coreCasesNamesWithoutTestData = allCoreCasesNames - methodModelsForCoreCasesWithExistingTestData.mapTo(mutableSetOf()) {
            it.name
        }

        konst methodModelsForCoreCasesWithoutTestData = coreCasesNamesWithoutTestData.map {
            konst expectedTestDataPath = additionalTestDataRoot.resolve(it)
            konst kpmTestCaseDescriptor = KpmTestCaseDescriptor.allCaseDescriptorsByNames[it]!!
            println("Generating template sources testdata for uncovered KPM Core Case $it at ${additionalTestDataRoot.path}")
            kpmTestCaseDescriptor.generateTemplateCanonicalFileStructure(expectedTestDataPath)

            KpmCoreCaseTestMethodModel(it, additionalTestDataRoot, expectedTestDataPath)
        }

        return methodModelsForCoreCasesWithExistingTestData + methodModelsForCoreCasesWithoutTestData
    }
}
