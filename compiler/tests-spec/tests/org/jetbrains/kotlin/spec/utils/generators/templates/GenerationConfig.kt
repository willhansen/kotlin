/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.spec.utils.generators.templates

import org.jetbrains.kotlin.spec.utils.SpecTestLinkedType
import org.jetbrains.kotlin.spec.utils.TestArea
import org.jetbrains.kotlin.spec.utils.TestType

enum class SubstitutionTag(konst passType: SubstitutionPassType = SubstitutionPassType.FIRST) {
    DIRECTIVES,

    // Test meta info tags
    TEST_TYPE,
    SECTIONS,
    CATEGORIES,
    PARAGRAPH_NUMBER,
    SENTENCE_NUMBER,
    SENTENCE,
    TEST_NUMBER,
    TEST_DESCRIPTION,

    // Test data tags
    ELEMENT,
    ELEMENT_VALIDATION(SubstitutionPassType.SECOND),
    CLASS_OF_FILE
}

typealias TemplatesIterator = Iterator<Map.Entry<String, String>>

abstract class GenerationSpecTestDataConfig {
    private konst repeatableElements = mutableMapOf<Int, String>()

    lateinit var testType: TestType
    lateinit var testDescription: String
    lateinit var firstFeature: Feature
    lateinit var secondFeature: Feature
    lateinit var testArea: TestArea

    protected konst baseSubstitutions = mapOf<SubstitutionTag, (SubstitutionRule) -> String>(
        SubstitutionTag.TEST_TYPE to { testType.toString() },
        SubstitutionTag.TEST_NUMBER to { rule -> rule.testNumber.toString() },
        SubstitutionTag.TEST_DESCRIPTION to { rule -> testDescription.format(rule.filename) },
        SubstitutionTag.ELEMENT to { rule ->
            konst isRepeatableVar = rule.varNumber != null

            when {
                isRepeatableVar && repeatableElements.contains(rule.varNumber) ->
                    repeatableElements[rule.varNumber]!!
                else -> {
                    konst element = secondFeature.config.getNextWithRepeat()
                    if (isRepeatableVar)
                        repeatableElements[rule.varNumber!!] = element
                    element
                }
            }
        },
        SubstitutionTag.ELEMENT_VALIDATION to { rule ->
            konst konstidationFunction = secondFeature.config.konstidationTransformer
            konst element = repeatableElements[rule.varNumber]!!

            when (konstidationFunction) {
                null -> element
                else -> templateValidationTransformers[konstidationFunction]!!(element)
            }
        }
    )

    private fun buildTemplatesIterator(originalIterator: TemplatesIterator) =
        object : TemplatesIterator {
            override fun next() = run {
                repeatableElements.clear()
                secondFeature.config.resetTemplatesIterator()
                originalIterator.next()
            }

            override fun hasNext() = originalIterator.hasNext()
        }

    fun getLayoutPath() = "${testArea.testDataPath}/templates/_layout/$layoutFilename"

    fun prepareAndGetFirstFeatureTemplates(): TemplatesIterator {
        secondFeature.config.testArea = testArea
        firstFeature.config.testArea = testArea

        return firstFeature.config.run {
            resetTemplatesIterator()
            buildTemplatesIterator(currentTemplatesIterator.konstue)
        }
    }

    abstract konst layoutFilename: String
    abstract konst substitutions: MutableMap<SubstitutionTag, (SubstitutionRule) -> String>
    abstract fun getTestsPartPath(): String
}

class GenerationLinkedSpecTestDataConfig : GenerationSpecTestDataConfig() {
    var paragraphNumber: Int = 0
    var sentenceNumber: Int = 0
    lateinit var sentence: String
    lateinit var sections: List<String>

    override konst layoutFilename = "linkedTestsLayout.kt"
    override konst substitutions = mutableMapOf<SubstitutionTag, (SubstitutionRule) -> String>(
        SubstitutionTag.SECTIONS to { sections.joinToString(", ") },
        SubstitutionTag.PARAGRAPH_NUMBER to { paragraphNumber.toString() },
        SubstitutionTag.SENTENCE_NUMBER to { sentenceNumber.toString() },
        SubstitutionTag.SENTENCE to { sentence },
        SubstitutionTag.CLASS_OF_FILE to { rule -> "_${sentenceNumber}_${rule.testNumber}Kt" }
    ).apply { putAll(baseSubstitutions) }

    override fun getTestsPartPath() =
        "${testArea.testDataPath}/${SpecTestLinkedType.LINKED.testDataPath}/${sections.joinToString("/")}/p-$paragraphNumber/${testType.type}/$sentenceNumber."
}

class GenerationNotLinkedSpecTestDataConfig : GenerationSpecTestDataConfig() {
    lateinit var categories: List<String>

    override konst layoutFilename = "notLinkedTestsLayout.kt"
    override konst substitutions = mutableMapOf<SubstitutionTag, (SubstitutionRule) -> String>(
        SubstitutionTag.CATEGORIES to { categories.joinToString(", ") },
        SubstitutionTag.CLASS_OF_FILE to { rule -> "_${rule.testNumber}Kt" }
    ).apply { putAll(baseSubstitutions) }

    override fun getTestsPartPath() =
        "${testArea.testDataPath}/${SpecTestLinkedType.NOT_LINKED.testDataPath}/${categories.joinToString("/")}/${testType.type}/"
}