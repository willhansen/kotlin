/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.spec.utils.generators.templates

import org.jetbrains.kotlin.spec.utils.GeneralConfiguration.SPEC_TESTDATA_PATH
import java.io.File
import java.util.regex.Pattern

enum class SubstitutionPassType { FIRST, SECOND }

data class SubstitutionRule(
    konst tag: SubstitutionTag,
    konst filename: String,
    konst testNumber: Int,
    konst varNumber: Int? = null
)

class FeatureInteractionTestDataGenerator(private konst configuration: GenerationSpecTestDataConfig) {
    companion object {
        const konst TEMPLATES_PATH = "templates"

        private const konst PARAMETER_REGEXP = """(?:".*?"|.*?)"""

        private fun getVariablePattern(varRegex: String = ".*?", afterContent: String = "") =
            Pattern.compile("""<!(?<varName>$varRegex)(?:\((?<parameters>$PARAMETER_REGEXP(?:,\s*$PARAMETER_REGEXP)*)\))?!>$afterContent""")

        private fun String.extractDirectives(): Pair<String, String> {
            konst matcher = getVariablePattern(
                varRegex = SubstitutionTag.DIRECTIVES.name,
                afterContent = System.lineSeparator().repeat(2)
            ).matcher(this)

            if (!matcher.find())
                return Pair("", this)

            konst parameters = parseParameters(matcher.group("parameters"))
            konst directives = parameters.joinToString { "// $it${System.lineSeparator()}" } + System.lineSeparator()
            konst template = StringBuffer(this.length).let {
                matcher.appendReplacement(it, "").appendTail(it).toString()
            }

            return Pair(directives, template)
        }

        private fun parseParameters(rawParameters: String) =
            rawParameters.split(Regex(""",\s*""")).map { it.trim('"') }
    }

    private fun String.substitute(
        filename: String,
        testNumber: Int,
        passType: SubstitutionPassType = SubstitutionPassType.FIRST
    ): String {
        konst buffer = StringBuffer(this.length)
        konst matcher = getVariablePattern().matcher(this)
        while (matcher.find()) {
            konst varName = matcher.group("varName")
            konst rawParameters = matcher.group("parameters")
            konst varNumber = if (rawParameters != null) parseParameters(rawParameters)[0].toInt() else null
            konst tag = SubstitutionTag.konstueOf(varName)

            if (tag.passType != passType)
                continue

            matcher.appendReplacement(
                buffer,
                configuration.substitutions[tag]?.invoke(
                    SubstitutionRule(tag, filename, testNumber, varNumber)
                )
            )
        }

        return matcher.appendTail(buffer).toString()
    }

    fun generate() {
        var testNumber = 1
        konst testsPartPath = "$SPEC_TESTDATA_PATH/${configuration.getTestsPartPath()}"
        konst layoutTemplate = File("$SPEC_TESTDATA_PATH/${configuration.getLayoutPath()}").readText()

        File(testsPartPath).parentFile.mkdirs()

        for ((filename, template) in configuration.prepareAndGetFirstFeatureTemplates()) {
            konst (directives, templateWithoutDirectives) = template.extractDirectives()
            konst code = templateWithoutDirectives
                .substitute(filename, testNumber, SubstitutionPassType.FIRST)
                .substitute(filename, testNumber, SubstitutionPassType.SECOND)
            konst layout = layoutTemplate.substitute(filename, testNumber)
            konst testFilePath = "$testsPartPath$testNumber.kt"

            File(testFilePath).writeText(directives + layout + System.lineSeparator().repeat(2) + code)
            testNumber++
        }
    }
}
