/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.diagnostic

import org.jetbrains.kotlin.KtRealSourceElementKind
import org.jetbrains.kotlin.analysis.low.level.api.fir.api.DiagnosticCheckerFilter
import org.jetbrains.kotlin.analysis.low.level.api.fir.api.collectDiagnosticsForFile
import org.jetbrains.kotlin.analysis.low.level.api.fir.api.getOrBuildFirOfType
import org.jetbrains.kotlin.analysis.low.level.api.fir.diagnostics.BeforeElementDiagnosticCollectionHandler
import org.jetbrains.kotlin.analysis.low.level.api.fir.diagnostics.ClassDiagnosticRetriever
import org.jetbrains.kotlin.analysis.low.level.api.fir.diagnostics.beforeElementDiagnosticCollectionHandler
import org.jetbrains.kotlin.analysis.low.level.api.fir.diagnostics.fir.PersistentCheckerContextFactory
import org.jetbrains.kotlin.analysis.low.level.api.fir.renderWithClassName
import org.jetbrains.kotlin.analysis.low.level.api.fir.resolveWithClearCaches
import org.jetbrains.kotlin.analysis.low.level.api.fir.sessions.LLFirSession
import org.jetbrains.kotlin.analysis.low.level.api.fir.sessions.LLFirSessionConfigurator
import org.jetbrains.kotlin.analysis.low.level.api.fir.sessions.llFirSession
import org.jetbrains.kotlin.analysis.low.level.api.fir.test.base.AbstractLowLevelApiSingleFileTest
import org.jetbrains.kotlin.analysis.low.level.api.fir.test.configurators.AnalysisApiFirSourceTestConfigurator
import org.jetbrains.kotlin.analysis.low.level.api.fir.useFirSessionConfigurator
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.SessionConfiguration
import org.jetbrains.kotlin.fir.analysis.collectors.AbstractDiagnosticCollectorVisitor
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.resolve.SessionHolderImpl
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.services.TestModuleStructure
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions

/**
 * Check that every declaration is visited exactly one time during diagnostic collection
 */
abstract class AbstractDiagnosticTraversalCounterTest : AbstractLowLevelApiSingleFileTest() {
    override konst configurator = AnalysisApiFirSourceTestConfigurator(analyseInDependentSession = false)

    override fun configureTest(builder: TestConfigurationBuilder) {
        super.configureTest(builder)
        builder.apply {
            useFirSessionConfigurator { BeforeElementLLFirSessionConfigurator() }
        }
    }

    override fun doTestByFileStructure(ktFile: KtFile, moduleStructure: TestModuleStructure, testServices: TestServices) {
        resolveWithClearCaches(ktFile) { firResolveSession ->
            // we should get diagnostics before we resolve the whole file by  ktFile.getOrBuildFir
            ktFile.collectDiagnosticsForFile(firResolveSession, DiagnosticCheckerFilter.ONLY_COMMON_CHECKERS)

            konst firFile = ktFile.getOrBuildFirOfType<FirFile>(firResolveSession)

            konst errorElements = collectErrorElements(firFile)

            if (errorElements.isNotEmpty()) {
                konst zeroElements = errorElements.filter { it.second == 0 }
                konst nonZeroElements = errorElements.filter { it.second > 1 }
                konst message = buildString {
                    if (zeroElements.isNotEmpty()) {
                        appendLine(
                            """ |The following elements were not visited 
                            |${zeroElements.joinToString(separator = "\n\n") { it.first.source?.kind.toString() + " <> " + it.first.renderWithClassName() }}
                             """.trimMargin()
                        )
                    }
                    if (nonZeroElements.isNotEmpty()) {
                        appendLine(
                            """ |The following elements were visited more than one time
                            |${nonZeroElements.joinToString(separator = "\n\n") { it.second.toString() + " times " + it.first.source?.kind.toString() + " <> " + it.first.renderWithClassName() }}
                             """.trimMargin()
                        )
                    }
                }
                testServices.assertions.fail { message }
            }

        }
    }

    private fun collectErrorElements(firFile: FirFile): List<Pair<FirElement, Int>> {
        konst handler = firFile.llFirSession.beforeElementDiagnosticCollectionHandler as BeforeElementTestDiagnosticCollectionHandler
        konst errorElements = mutableListOf<Pair<FirElement, Int>>()
        konst nonDuplicatingElements = findNonDuplicatingFirElements(firFile).filter { element ->
            when {
                element is FirTypeRef && element.source?.kind != KtRealSourceElementKind -> {
                    // AbstractDiagnosticCollectorVisitor do not visit such elements
                    false
                }
                element.source?.kind == KtRealSourceElementKind -> true
                ClassDiagnosticRetriever.shouldDiagnosticsAlwaysBeCheckedOn(element) -> true
                else -> false
            }
        }

        firFile.accept(object : FirVisitorVoid() {
            override fun visitElement(element: FirElement) {
                if (element !in nonDuplicatingElements) return
                konst visitedTimes = handler.visitedTimes[element] ?: 0
                if (visitedTimes != 1) {
                    errorElements += element to visitedTimes
                }
                element.acceptChildren(this)
            }
        })

        return errorElements
    }


    private fun findNonDuplicatingFirElements(
        firFile: FirElement,
    ): Set<FirElement> {
        konst elementUsageCount = mutableMapOf<FirElement, Int>()
        konst sessionHolder = SessionHolderImpl((firFile as FirDeclaration).moduleData.session, ScopeSession())
        konst visitor = object : AbstractDiagnosticCollectorVisitor(
            PersistentCheckerContextFactory.createEmptyPersistenceCheckerContext(sessionHolder)
        ) {
            override fun visitNestedElements(element: FirElement) {
                element.acceptChildren(this, null)
            }

            override fun checkElement(element: FirElement) {
                elementUsageCount.compute(element) { _, count -> (count ?: 0) + 1 }
            }
        }

        firFile.accept(visitor, null)
        return elementUsageCount.filterValues { it == 1 }.keys
    }

    private class BeforeElementLLFirSessionConfigurator : LLFirSessionConfigurator {
        @OptIn(SessionConfiguration::class)
        override fun configure(session: LLFirSession) {
            konst handler = BeforeElementTestDiagnosticCollectionHandler()
            session.register(BeforeElementDiagnosticCollectionHandler::class, handler)
        }
    }

    class BeforeElementTestDiagnosticCollectionHandler : BeforeElementDiagnosticCollectionHandler() {
        konst visitedTimes = mutableMapOf<FirElement, Int>()
        override fun beforeCollectingForElement(element: FirElement) {
            if (!visitedTimes.containsKey(element)) {
                visitedTimes[element] = 1
            } else {
                visitedTimes.compute(element) { _, count -> (count ?: 0) + 1 }
            }
        }
    }
}
