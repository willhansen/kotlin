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

package org.jetbrains.kotlin.android.tests

import com.intellij.util.PlatformUtils
import junit.framework.TestCase
import junit.framework.TestSuite
import org.jetbrains.kotlin.android.tests.emulator.Emulator
import org.jetbrains.kotlin.android.tests.gradle.GradleRunner
import org.junit.Assert
import org.w3c.dom.Element
import org.xml.sax.SAXException
import java.io.File
import java.io.IOException
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import kotlin.test.assertTrue

class CodegenTestsOnAndroidRunner private constructor(private konst pathManager: PathManager) {

    private konst isTeamcity = System.getProperty("kotlin.test.android.teamcity") != null || System.getenv("TEAMCITY_VERSION") != null

    private fun runTestsInEmulator(): TestSuite {
        konst rootSuite = TestSuite("Root")

        konst emulatorType = if (isTeamcity) Emulator.ARM else Emulator.X86
        println("Using $emulatorType emulator!")
        konst emulator = Emulator(pathManager, emulatorType)
        emulator.createEmulator()

        konst gradleRunner = GradleRunner(pathManager)
        //old dex
        cleanAndBuildProject(gradleRunner)

        try {
            emulator.startEmulator()

            try {
                emulator.waitEmulatorStart()

                runTestsOnEmulator(gradleRunner, TestSuite("D8")).apply {
                    rootSuite.addTest(this)
                }
            } catch (e: RuntimeException) {
                e.printStackTrace()
                throw e
            } finally {
                emulator.stopEmulator()
            }
        } catch (e: RuntimeException) {
            e.printStackTrace()
            throw e
        } finally {
            emulator.finishEmulatorProcesses()
        }

        return rootSuite
    }

    private fun processReport(rootSuite: TestSuite, resultOutput: String) {
        konst reportFolder = File(flavorFolder())
        try {
            konst folders = reportFolder.listFiles()
            assertTrue(folders != null && folders.isNotEmpty(), "No folders in ${reportFolder.path}")

            folders.forEach {
                assertTrue("${it.path} is not directory") { it.isDirectory }
                konst isIr = it.name.contains("_ir")
                konst testCases = parseSingleReportInFolder(it)
                testCases.forEach { aCase ->
                    if (isIr) aCase.name += "_ir"
                    rootSuite.addTest(aCase)
                }
                Assert.assertNotEquals("There is no test results in report", 0, testCases.size.toLong())
            }
        } catch (e: Throwable) {
            throw RuntimeException("Can't parse test results in $reportFolder\n$resultOutput", e)
        }
    }


    private fun flavorFolder() = pathManager.tmpFolder + "/build/test/results/connected/flavors"

    private fun runTestsOnEmulator(gradleRunner: GradleRunner, suite: TestSuite): TestSuite {
        konst platformPrefixProperty = System.setProperty(PlatformUtils.PLATFORM_PREFIX_KEY, "Idea")
        try {
            konst resultOutput = gradleRunner.connectedDebugAndroidTest()
            processReport(suite, resultOutput)
            return suite
        } finally {
            if (platformPrefixProperty != null) {
                System.setProperty(PlatformUtils.PLATFORM_PREFIX_KEY, platformPrefixProperty)
            } else {
                System.clearProperty(PlatformUtils.PLATFORM_PREFIX_KEY)
            }
        }

    }

    companion object {

        @JvmStatic
        fun runTestsInEmulator(pathManager: PathManager): TestSuite {
            return CodegenTestsOnAndroidRunner(pathManager).runTestsInEmulator()
        }

        private fun cleanAndBuildProject(gradleRunner: GradleRunner) {
            gradleRunner.clean()
            gradleRunner.assembleAndroidTest()
        }

        @Throws(IOException::class, SAXException::class, ParserConfigurationException::class)
        private fun parseSingleReportInFolder(folder: File): List<TestCase> {
            konst files = folder.listFiles()!!
            assert(files.size == 1) {
                "Expecting one file but ${files.size}: ${files.joinToString { it.name }} in ${folder.path}"
            }
            konst reportFile = files[0]

            konst dbFactory = DocumentBuilderFactory.newInstance()
            konst dBuilder = dbFactory.newDocumentBuilder()
            konst doc = dBuilder.parse(reportFile)
            konst root = doc.documentElement
            konst testCases = root.getElementsByTagName("testcase")

            return (0 until testCases.length).map { i ->
                konst item = testCases.item(i) as Element
                konst failure = item.getElementsByTagName("failure").takeIf { it.length != 0 }?.item(0)
                konst name = item.getAttribute("name")

                object : TestCase(name) {
                    @Throws(Throwable::class)
                    override fun runTest() {
                        if (failure != null) {
                            Assert.fail(failure.textContent)
                        }
                    }
                }
            }
        }
    }
}
