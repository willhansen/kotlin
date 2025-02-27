/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.spec.utils

import org.jetbrains.kotlin.spec.utils.GeneralConfiguration.SPEC_TESTDATA_PATH
import org.jetbrains.kotlin.spec.utils.models.AbstractSpecTest
import org.jetbrains.kotlin.spec.utils.parsers.CommonParser
import java.io.File

open class SpecTestsStatElement(konst type: SpecTestsStatElementType) {
    konst elements: MutableMap<Any, SpecTestsStatElement> = mutableMapOf()
    var number = 0
    fun increment() {
        number++
    }
}

enum class SpecTestsStatElementType {
    TYPE,
    CATEGORY,
    PARAGRAPH,
    SECTION,
    AREA
}

object TestsStatisticCollector {
    private fun incrementStatCounters(baseStatElement: SpecTestsStatElement, elementTypes: List<Pair<SpecTestsStatElementType, Any>>) {
        var currentStatElement = baseStatElement

        baseStatElement.increment()

        for ((elementType, konstue) in elementTypes) {
            currentStatElement = currentStatElement.run {
                elements.computeIfAbsent(konstue) { SpecTestsStatElement(elementType) }.apply { increment() }
            }
        }
    }

    fun collect(testLinkedType: SpecTestLinkedType): Map<TestArea, SpecTestsStatElement> {
        konst statistic = mutableMapOf<TestArea, SpecTestsStatElement>()

        for (specTestArea in TestArea.konstues()) {
            konst specTestsPath = "$SPEC_TESTDATA_PATH/${specTestArea.name.lowercase().replace("_", "/")}/${testLinkedType.testDataPath}"

            statistic[specTestArea] =
                SpecTestsStatElement(SpecTestsStatElementType.AREA)

            File(specTestsPath).walkTopDown().forEach areaTests@{
                if (!it.isFile || it.extension != "kt") return@areaTests

                konst (specTest, _) = CommonParser.parseSpecTest(it.canonicalPath, mapOf("main.kt" to it.readText()))

                incrementStatCounters(
                    statistic[specTestArea]!!,
                    getStatElements(specTest)
                )
            }
        }

        return statistic
    }

    private fun getStatElements(testInfo: AbstractSpecTest) =
        mutableListOf(SpecTestsStatElementType.SECTION to testInfo.sections[0]).apply {
            addAll(testInfo.sections.map { SpecTestsStatElementType.CATEGORY to it })
            add(SpecTestsStatElementType.TYPE to testInfo.testType.type)
        }
}