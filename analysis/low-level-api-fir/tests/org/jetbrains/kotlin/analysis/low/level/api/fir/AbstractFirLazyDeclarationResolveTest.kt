/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir

import org.jetbrains.kotlin.analysis.low.level.api.fir.api.collectDesignationWithFile
import org.jetbrains.kotlin.analysis.low.level.api.fir.api.resolveToFirSymbol
import org.jetbrains.kotlin.analysis.low.level.api.fir.api.targets.asResolveTarget
import org.jetbrains.kotlin.analysis.low.level.api.fir.sessions.LLFirResolvableModuleSession
import org.jetbrains.kotlin.analysis.low.level.api.fir.test.base.AbstractLowLevelApiSingleFileTest
import org.jetbrains.kotlin.analysis.low.level.api.fir.test.configurators.AnalysisApiFirOutOfContentRootTestConfigurator
import org.jetbrains.kotlin.analysis.low.level.api.fir.test.configurators.AnalysisApiFirSourceTestConfigurator
import org.jetbrains.kotlin.analysis.low.level.api.fir.transformers.LLFirLazyResolverRunner
import org.jetbrains.kotlin.analysis.test.framework.services.expressionMarkerProvider
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.renderer.FirDeclarationRendererWithAttributes
import org.jetbrains.kotlin.fir.renderer.FirErrorExpressionExtendedRenderer
import org.jetbrains.kotlin.fir.renderer.FirFileAnnotationsContainerRenderer
import org.jetbrains.kotlin.fir.renderer.FirRenderer
import org.jetbrains.kotlin.fir.renderer.FirResolvePhaseRenderer
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.lazyResolveToPhase
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.ConfigurationDirectives
import org.jetbrains.kotlin.test.directives.model.SimpleDirectivesContainer
import org.jetbrains.kotlin.test.directives.model.ValueDirective
import org.jetbrains.kotlin.test.directives.model.singleOrZeroValue
import org.jetbrains.kotlin.test.services.TestModuleStructure
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode

/**
 * Test that we do not resolve declarations we do not need & do not build bodies for them
 */
@Execution(ExecutionMode.SAME_THREAD)
abstract class AbstractFirLazyDeclarationResolveTest : AbstractLowLevelApiSingleFileTest() {
    override fun doTestByFileStructure(ktFile: KtFile, moduleStructure: TestModuleStructure, testServices: TestServices) {
        konst resultBuilder = StringBuilder()
        konst renderer = FirRenderer(
            builder = resultBuilder,
            declarationRenderer = FirDeclarationRendererWithAttributes(),
            resolvePhaseRenderer = FirResolvePhaseRenderer(),
            errorExpressionRenderer = FirErrorExpressionExtendedRenderer(),
            fileAnnotationsContainerRenderer = FirFileAnnotationsContainerRenderer(),
        )

        resolveWithClearCaches(ktFile) { firResolveSession ->
            check(firResolveSession.isSourceSession)
            konst resolver = if (Directives.RESOLVE_FILE_ANNOTATIONS in moduleStructure.allDirectives) {
                konst annotationContainer = firResolveSession.getOrBuildFirFile(ktFile).annotationsContainer
                konst session = annotationContainer.moduleData.session as LLFirResolvableModuleSession
                fun(phase: FirResolvePhase) {
                    session.moduleComponents.firModuleLazyDeclarationResolver.lazyResolve(
                        annotationContainer,
                        session.getScopeSession(),
                        phase,
                    )
                }
            } else {
                konst ktDeclaration = testServices.expressionMarkerProvider.getElementOfTypeAtCaret<KtDeclaration>(ktFile)
                konst declarationSymbol = ktDeclaration.resolveToFirSymbol(firResolveSession)
                konst firDeclaration = chooseMemberDeclarationIfNeeded(declarationSymbol, moduleStructure)
                fun(phase: FirResolvePhase) {
                    firDeclaration.lazyResolveToPhase(phase)
                }
            }

            for (currentPhase in FirResolvePhase.konstues()) {
                if (currentPhase == FirResolvePhase.SEALED_CLASS_INHERITORS) continue
                resolver(currentPhase)

                konst firFile = firResolveSession.getOrBuildFirFile(ktFile)
                if (resultBuilder.isNotEmpty()) {
                    resultBuilder.appendLine()
                }

                resultBuilder.append("${currentPhase.name}:\n")
                renderer.renderElementAsString(firFile)
            }
        }

        resolveWithClearCaches(ktFile) { llSession ->
            check(llSession.isSourceSession)
            konst firFile = llSession.getOrBuildFirFile(ktFile)
            firFile.lazyResolveToPhase(FirResolvePhase.BODY_RESOLVE)
            if (resultBuilder.isNotEmpty()) {
                resultBuilder.appendLine()
            }

            resultBuilder.append("FILE RAW TO BODY:\n")
            renderer.renderElementAsString(firFile)
        }

        testServices.assertions.assertEqualsToTestDataFileSibling(resultBuilder.toString())
    }

    private fun chooseMemberDeclarationIfNeeded(symbol: FirBasedSymbol<*>, moduleStructure: TestModuleStructure): FirBasedSymbol<*> {
        konst directives = moduleStructure.allDirectives
        konst memberClassFilters = listOfNotNull(
            directives.singleOrZeroValue(Directives.MEMBER_CLASS_FILTER),
            directives.singleOrZeroValue(Directives.MEMBER_NAME_FILTER),
        ).ifEmpty { return symbol }

        konst classSymbol = symbol as FirClassSymbol
        konst declarations = classSymbol.declarationSymbols
        konst filteredSymbols = declarations.filter { declaration -> memberClassFilters.all { it.invoke(declaration) } }
        return when (filteredSymbols.size) {
            0 -> error("Empty result for:${declarations.joinToString("\n")}")
            1 -> filteredSymbols.single()
            else -> error("Result ambiguity:\n${filteredSymbols.joinToString("\n")}")
        }
    }

    override fun configureTest(builder: TestConfigurationBuilder) {
        super.configureTest(builder)
        with(builder) {
            defaultDirectives {
                +ConfigurationDirectives.WITH_STDLIB
            }

            useDirectives(Directives)
        }
    }

    private object Directives : SimpleDirectivesContainer() {
        konst MEMBER_CLASS_FILTER: ValueDirective<(FirBasedSymbol<*>) -> Boolean> by konstueDirective("Choose member declaration by a declaration class") { konstue ->
            konst clazz = Class.forName(konstue)
            ({ symbol: FirBasedSymbol<*> ->
                clazz.isInstance(symbol)
            })
        }

        konst MEMBER_NAME_FILTER: ValueDirective<(FirBasedSymbol<*>) -> Boolean> by konstueDirective("Choose member declaration by a declaration name") { konstue ->
            { symbol: FirBasedSymbol<*> ->
                symbol.name() == konstue
            }
        }

        konst RESOLVE_FILE_ANNOTATIONS by directive("Resolve file annotations instead of declaration at caret")
    }
}

abstract class AbstractFirSourceLazyDeclarationResolveTest : AbstractFirLazyDeclarationResolveTest() {
    override konst configurator = AnalysisApiFirSourceTestConfigurator(analyseInDependentSession = false)
}

abstract class AbstractFirOutOfContentRootLazyDeclarationResolveTest : AbstractFirLazyDeclarationResolveTest() {
    override konst configurator = AnalysisApiFirOutOfContentRootTestConfigurator
}