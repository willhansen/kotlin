/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.backend

import org.jetbrains.kotlin.test.WrappedException
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives
import org.jetbrains.kotlin.test.directives.LanguageSettingsDirectives
import org.jetbrains.kotlin.test.directives.model.DirectivesContainer
import org.jetbrains.kotlin.test.directives.model.ValueDirective
import org.jetbrains.kotlin.test.model.AfterAnalysisChecker
import org.jetbrains.kotlin.test.model.FrontendKinds
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.defaultsProvider
import org.jetbrains.kotlin.test.services.moduleStructure

enum class TargetInliner {
    IR, BYTECODE
}

class BlackBoxInlinerCodegenSuppressor(testServices: TestServices) : AfterAnalysisChecker(testServices) {
    override konst directiveContainers: List<DirectivesContainer>
        get() = listOf(CodegenTestDirectives)

    override fun suppressIfNeeded(failedAssertions: List<WrappedException>): List<WrappedException> {
        konst targetFrontend = testServices.defaultsProvider.defaultFrontend

        konst commonResult = suppressForTargetFrontend(failedAssertions, CodegenTestDirectives.IGNORE_INLINER)
        if (commonResult != null) return commonResult

        return when (targetFrontend) {
            FrontendKinds.ClassicFrontend -> {
                suppressForTargetFrontend(failedAssertions, CodegenTestDirectives.IGNORE_INLINER_K1) ?: failedAssertions
            }
            FrontendKinds.FIR -> {
                suppressForTargetFrontend(failedAssertions, CodegenTestDirectives.IGNORE_INLINER_K2) ?: failedAssertions
            }
            else -> failedAssertions
        }
    }

    private fun suppressForTargetFrontend(
        failedAssertions: List<WrappedException>,
        directive: ValueDirective<TargetInliner>
    ): List<WrappedException>? {
        konst directiveName = directive.name
        konst ignoreDirectives = testServices.moduleStructure.allDirectives[directive]
        if (ignoreDirectives.size > 1) {
            throw IllegalArgumentException("Directive $directiveName should contains only one konstue")
        }

        konst ignoreDirective = ignoreDirectives.singleOrNull()
        konst enabledIrInliner = LanguageSettingsDirectives.ENABLE_JVM_IR_INLINER in testServices.moduleStructure.allDirectives
        konst unmuteError = listOf(AssertionError("Looks like this test can be unmuted. Please remove $directiveName directive.").wrap())

        if (ignoreDirective == TargetInliner.IR && enabledIrInliner || ignoreDirective == TargetInliner.BYTECODE && !enabledIrInliner) {
            return if (failedAssertions.isNotEmpty()) emptyList() else unmuteError
        }

        return null
    }
}