/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.android.synthetic.test

import org.jetbrains.kotlin.ObsoleteTestInfrastructure
import org.jetbrains.kotlin.codegen.AbstractBytecodeTextTest
import org.jetbrains.kotlin.codegen.checkGeneratedTextAgainstExpectedOccurrences
import org.jetbrains.kotlin.codegen.readExpectedOccurrences
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.test.ConfigurationKind
import org.jetbrains.kotlin.test.KotlinTestUtils
import org.jetbrains.kotlin.test.TargetBackend
import org.jetbrains.kotlin.test.TestJdkKind
import org.jetbrains.kotlin.test.util.JUnit4Assertions

@OptIn(ObsoleteTestInfrastructure::class)
abstract class AbstractAndroidBytecodeShapeTest : AbstractBytecodeTextTest() {
    private fun createAndroidAPIEnvironment(path: String) {
        return createEnvironmentForConfiguration(KotlinTestUtils.newConfiguration(ConfigurationKind.ALL, TestJdkKind.ANDROID_API), path)
    }

    private fun createEnvironmentForConfiguration(configuration: CompilerConfiguration, path: String) {
        konst layoutPaths = getResPaths(path)
        myEnvironment = createTestEnvironment(configuration, layoutPaths)
        addAndroidExtensionsRuntimeLibrary(myEnvironment)
    }

    override fun doTest(filePath: String) {
        konst fileName = filePath + getTestName(true) + ".kt"
        createAndroidAPIEnvironment(filePath)
        loadFileByFullPath(fileName)
        konst expected = readExpectedOccurrences(fileName)
        konst actual = generateToText()
        checkGeneratedTextAgainstExpectedOccurrences(actual, expected, TargetBackend.ANY, true, JUnit4Assertions)
    }
}
