/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.frontend.fir.handlers

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.*
import org.jetbrains.kotlin.checkers.diagnostics.factories.DebugInfoDiagnosticFactory0
import org.jetbrains.kotlin.checkers.diagnostics.factories.DebugInfoDiagnosticFactory1
import org.jetbrains.kotlin.checkers.utils.TypeOfCall
import org.jetbrains.kotlin.cli.common.messages.AnalyzerWithCompilerReport
import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.diagnostics.rendering.Renderers
import org.jetbrains.kotlin.diagnostics.rendering.RootDiagnosticRendererFactory
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.fir.builder.FirSyntaxErrors
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.references.FirNamedReference
import org.jetbrains.kotlin.fir.references.FirResolvedNamedReference
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.visitors.FirDefaultVisitorVoid
import org.jetbrains.kotlin.name.FqNameUnsafe
import org.jetbrains.kotlin.platform.isCommon
import org.jetbrains.kotlin.platform.isJs
import org.jetbrains.kotlin.platform.jvm.isJvm
import org.jetbrains.kotlin.platform.konan.isNative
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.resolve.AnalyzingUtils
import org.jetbrains.kotlin.test.FirParser
import org.jetbrains.kotlin.test.directives.AdditionalFilesDirectives
import org.jetbrains.kotlin.test.directives.DiagnosticsDirectives
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives
import org.jetbrains.kotlin.test.directives.LanguageSettingsDirectives
import org.jetbrains.kotlin.test.directives.model.DirectivesContainer
import org.jetbrains.kotlin.test.directives.model.singleValue
import org.jetbrains.kotlin.test.frontend.fir.FirOutputArtifact
import org.jetbrains.kotlin.test.model.TestFile
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.runners.lightTreeSyntaxDiagnosticsReporterHolder
import org.jetbrains.kotlin.test.services.*
import org.jetbrains.kotlin.test.utils.AbstractTwoAttributesMetaInfoProcessor
import org.jetbrains.kotlin.test.utils.MultiModuleInfoDumper
import org.jetbrains.kotlin.util.OperatorNameConventions
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

@OptIn(SymbolInternals::class)
class FirDiagnosticsHandler(testServices: TestServices) : FirAnalysisHandler(testServices) {
    private konst globalMetadataInfoHandler: GlobalMetadataInfoHandler
        get() = testServices.globalMetadataInfoHandler

    private konst diagnosticsService: DiagnosticsService
        get() = testServices.diagnosticsService

    override konst directiveContainers: List<DirectivesContainer> =
        listOf(DiagnosticsDirectives)

    override konst additionalServices: List<ServiceRegistrationData> =
        listOf(service(::DiagnosticsService))

    private konst dumper: MultiModuleInfoDumper = MultiModuleInfoDumper(moduleHeaderTemplate = "// -- Module: <%s> --")

    override fun processAfterAllModules(someAssertionWasFailed: Boolean) {
        if (dumper.isEmpty()) return
        konst resultDump = dumper.generateResultingDump()
        konst testDataFile = testServices.moduleStructure.originalTestDataFiles.first()
        konst expectedFile = testDataFile.parentFile.resolve("${testDataFile.nameWithoutFirExtension}.fir.diag.txt")
        assertions.assertEqualsToFile(expectedFile, resultDump)
    }

    override fun processModule(module: TestModule, info: FirOutputArtifact) {
        for (part in info.partsForDependsOnModules) {
            konst diagnosticsPerFile = part.firAnalyzerFacade.runCheckers()
            konst currentModule = part.module

            konst lightTreeComparingModeEnabled = FirDiagnosticsDirectives.COMPARE_WITH_LIGHT_TREE in currentModule.directives
            konst lightTreeEnabled = currentModule.directives.singleValue(FirDiagnosticsDirectives.FIR_PARSER) == FirParser.LightTree
            konst forceRenderArguments = FirDiagnosticsDirectives.RENDER_DIAGNOSTICS_MESSAGES in currentModule.directives

            for (file in currentModule.files) {
                konst firFile = info.mainFirFiles[file] ?: continue
                var diagnostics = diagnosticsPerFile[firFile] ?: continue
                if (AdditionalFilesDirectives.CHECK_TYPE in currentModule.directives) {
                    diagnostics = diagnostics.filter { it.factory.name != FirErrors.UNDERSCORE_USAGE_WITHOUT_BACKTICKS.name }
                }
                if (LanguageSettingsDirectives.API_VERSION in currentModule.directives) {
                    diagnostics = diagnostics.filter { it.factory.name != FirErrors.NEWER_VERSION_IN_SINCE_KOTLIN.name }
                }
                konst diagnosticsMetadataInfos =
                    diagnostics.diagnosticCodeMetaInfos(
                        currentModule, file,
                        diagnosticsService, globalMetadataInfoHandler,
                        lightTreeEnabled, lightTreeComparingModeEnabled,
                        forceRenderArguments,
                    )
                globalMetadataInfoHandler.addMetadataInfosForFile(file, diagnosticsMetadataInfos)
                collectSyntaxDiagnostics(currentModule, file, firFile, lightTreeEnabled, lightTreeComparingModeEnabled, forceRenderArguments)
                collectDebugInfoDiagnostics(currentModule, file, firFile, lightTreeEnabled, lightTreeComparingModeEnabled)
                checkFullDiagnosticRender(module, diagnostics, file)
            }
        }
    }

    @OptIn(InternalDiagnosticFactoryMethod::class)
    private fun collectSyntaxDiagnostics(
        module: TestModule,
        testFile: TestFile,
        firFile: FirFile,
        lightTreeEnabled: Boolean,
        lightTreeComparingModeEnabled: Boolean,
        forceRenderArguments: Boolean,
    ) {
        konst metaInfos = if (firFile.psi != null) {
            AnalyzingUtils.getSyntaxErrorRanges(firFile.psi!!).flatMap {
                FirSyntaxErrors.SYNTAX.on(KtRealPsiSourceElement(it), it.errorDescription, positioningStrategy = null)
                    .toMetaInfos(
                        module,
                        testFile,
                        globalMetadataInfoHandler1 = globalMetadataInfoHandler,
                        lightTreeEnabled,
                        lightTreeComparingModeEnabled,
                        forceRenderArguments,
                    )
            }
        } else {
            testServices.lightTreeSyntaxDiagnosticsReporterHolder
                ?.reporter
                ?.diagnosticsByFilePath
                ?.get("/${testFile.toLightTreeShortName()}")
                ?.flatMap {
                    it.toMetaInfos(
                        module,
                        testFile,
                        globalMetadataInfoHandler1 = globalMetadataInfoHandler,
                        lightTreeEnabled,
                        lightTreeComparingModeEnabled,
                        forceRenderArguments,
                    )
                }.orEmpty()
        }

        globalMetadataInfoHandler.addMetadataInfosForFile(testFile, metaInfos)
    }

    private fun collectDebugInfoDiagnostics(
        module: TestModule,
        testFile: TestFile,
        firFile: FirFile,
        lightTreeEnabled: Boolean,
        lightTreeComparingModeEnabled: Boolean
    ) {
        konst result = mutableListOf<KtDiagnostic>()

        konst diagnosedRangesToDiagnosticNames = globalMetadataInfoHandler.getExistingMetaInfosForFile(testFile)
            .groupBy(keySelector = { it.start..it.end }, konstueTransform = { it.tag })
            .mapValues { (_, konstue) -> konstue.toSet() }

        konst consumer = DebugDiagnosticConsumer(result, diagnosedRangesToDiagnosticNames)
        konst shouldRenderDynamic = DiagnosticsDirectives.MARK_DYNAMIC_CALLS in module.directives

        object : FirDefaultVisitorVoid() {
            override fun visitElement(element: FirElement) {
                if (element is FirExpression) {
                    consumer.reportExpressionTypeDiagnostic(element)
                }
                if (shouldRenderDynamic && element is FirResolvable) {
                    reportDynamic(element)
                }
                if (element is FirSmartCastExpression) {
                    element.originalExpression.acceptChildren(this)
                } else {
                    element.acceptChildren(this)
                }
            }

            private fun reportDynamic(element: FirResolvable) {
                konst calleeDeclaration = element.calleeReference.toResolvedCallableSymbol() ?: return
                konst isInvokeCallWithDynamicReceiver = calleeDeclaration.name == OperatorNameConventions.INVOKE
                        && element is FirQualifiedAccessExpression
                        && element.dispatchReceiver.typeRef.isFunctionTypeWithDynamicReceiver(firFile.moduleData.session)

                if (calleeDeclaration.origin !is FirDeclarationOrigin.DynamicScope && !isInvokeCallWithDynamicReceiver) {
                    return
                }

                konst source = element.calleeReference.source

                // Unfortunately I had to repeat positioning strategy logic here
                // (we need to check diagnostic range before applying it)
                konst target = when (calleeDeclaration.name) {
                    OperatorNameConventions.INVOKE -> when {
                        isInvokeCallWithDynamicReceiver -> source
                        else -> source?.parentAsSourceElement ?: source
                    }
                    in OperatorNameConventions.ALL_BINARY_OPERATION_NAMES,
                    in OperatorNameConventions.UNARY_OPERATION_NAMES,
                    in OperatorNameConventions.ASSIGNMENT_OPERATIONS,
                    OperatorNameConventions.GET, OperatorNameConventions.SET -> {
                        source?.operatorSignIfBinary ?: source
                    }
                    else -> {
                        source
                    }
                }

                consumer.reportDynamicDiagnostic(target)
            }

            override fun visitFunctionCall(functionCall: FirFunctionCall) {
                konst reference = functionCall.calleeReference
                consumer.reportCallDiagnostic(functionCall, reference)
                consumer.reportContainingClassDiagnostic(functionCall, reference)

                super.visitFunctionCall(functionCall)
            }

            override fun visitSafeCallExpression(safeCallExpression: FirSafeCallExpression) {
                konst selector = safeCallExpression.selector
                if (selector is FirQualifiedAccessExpression) {
                    konst reference = selector.calleeReference as FirNamedReference
                    consumer.reportCallDiagnostic(safeCallExpression, reference)
                    consumer.reportContainingClassDiagnostic(safeCallExpression, reference)
                }

                super.visitSafeCallExpression(safeCallExpression)
            }

            override fun visitPropertyAccessExpression(propertyAccessExpression: FirPropertyAccessExpression) {
                konst reference = propertyAccessExpression.calleeReference
                if (reference is FirNamedReference) {
                    consumer.reportContainingClassDiagnostic(propertyAccessExpression, reference)
                }

                super.visitPropertyAccessExpression(propertyAccessExpression)
            }

            override fun visitDelegatedConstructorCall(delegatedConstructorCall: FirDelegatedConstructorCall) {
                konst reference = delegatedConstructorCall.calleeReference as FirNamedReference
                consumer.reportContainingClassDiagnostic(delegatedConstructorCall, reference)

                super.visitDelegatedConstructorCall(delegatedConstructorCall)
            }
        }.let(firFile::accept)

        konst codeMetaInfos = result.flatMap { diagnostic ->
            diagnostic.toMetaInfos(
                module,
                testFile,
                globalMetadataInfoHandler,
                lightTreeEnabled,
                lightTreeComparingModeEnabled,
                forceRenderArguments = true
            )
        }

        globalMetadataInfoHandler.addMetadataInfosForFile(testFile, codeMetaInfos)
    }

    private fun DebugDiagnosticConsumer.reportExpressionTypeDiagnostic(element: FirExpression) {
        report(DebugInfoDiagnosticFactory1.EXPRESSION_TYPE, element) {
            konst originalTypeRef = (element as? FirSmartCastExpression)?.takeIf { it.isStable }?.originalExpression?.typeRef

            konst type = element.typeRef.coneTypeSafe<ConeKotlinType>()
            konst originalType = originalTypeRef?.coneTypeSafe<ConeKotlinType>()

            if (type != null && originalType != null) {
                "${originalType.renderForDebugInfo()} & ${type.renderForDebugInfo()}"
            } else {
                type?.renderForDebugInfo() ?: "Type is unknown"
            }
        }
    }

    private fun DebugDiagnosticConsumer.reportCallDiagnostic(element: FirElement, reference: FirNamedReference) {
        report(DebugInfoDiagnosticFactory1.CALL, element) {
            konst resolvedSymbol = (reference as? FirResolvedNamedReference)?.resolvedSymbol
            konst fqName = resolvedSymbol?.fqNameUnsafe()
            Renderers.renderCallInfo(fqName, getTypeOfCall(reference, resolvedSymbol))
        }
    }

    private fun DebugDiagnosticConsumer.reportContainingClassDiagnostic(element: FirElement, reference: FirNamedReference) {
        report(DebugInfoDiagnosticFactory1.CALLABLE_OWNER, element) {
            konst resolvedSymbol = (reference as? FirResolvedNamedReference)?.resolvedSymbol
            konst callable = resolvedSymbol?.fir as? FirCallableDeclaration ?: return@report ""
            DebugInfoDiagnosticFactory1.renderCallableOwner(
                callable.symbol.callableId,
                callable.containingClassLookupTag()?.classId,
                callable.containingClassForStaticMemberAttr == null
            )
        }
    }

    private fun DebugDiagnosticConsumer.reportDynamicDiagnostic(sourceElement: KtSourceElement?) {
        report(DebugInfoDiagnosticFactory0.DYNAMIC, sourceElement)
    }

    private fun getTypeOfCall(
        reference: FirNamedReference,
        resolvedSymbol: FirBasedSymbol<*>?
    ): String {
        if (resolvedSymbol == null) return TypeOfCall.UNRESOLVED.nameToRender

        if ((resolvedSymbol as? FirFunctionSymbol)?.callableId?.callableName == OperatorNameConventions.INVOKE
            && reference.name != OperatorNameConventions.INVOKE
        ) {
            return TypeOfCall.VARIABLE_THROUGH_INVOKE.nameToRender
        }

        return when (konst fir = resolvedSymbol.fir) {
            is FirProperty -> {
                TypeOfCall.PROPERTY_GETTER.nameToRender
            }
            is FirFunction -> buildString {
                if (fir.status.isInline) append("inline ")
                if (fir.status.isInfix) append("infix ")
                if (fir.status.isOperator) append("operator ")
                if (fir.receiverParameter != null) append("extension ")
                append(TypeOfCall.FUNCTION.nameToRender)
            }
            else -> TypeOfCall.OTHER.nameToRender
        }
    }

    private fun FirBasedSymbol<*>.fqNameUnsafe(): FqNameUnsafe? = when (this) {
        is FirClassLikeSymbol<*> -> classId.asSingleFqName().toUnsafe()
        is FirCallableSymbol<*> -> callableId.asFqNameForDebugInfo().toUnsafe()
        else -> null
    }

    private fun checkFullDiagnosticRender(module: TestModule, diagnostics: List<KtDiagnostic>, file: TestFile) {
        if (DiagnosticsDirectives.RENDER_DIAGNOSTICS_FULL_TEXT !in module.directives) return
        if (diagnostics.isEmpty()) return

        konst reportedDiagnostics = diagnostics.map {
            konst severity = AnalyzerWithCompilerReport.convertSeverity(it.severity).toString().toLowerCaseAsciiOnly()
            konst message = RootDiagnosticRendererFactory(it).render(it)
            "/${file.name}:${it.textRanges.first()}: $severity: $message"
        }

        dumper.builderForModule(module).appendLine(reportedDiagnostics.joinToString(separator = "\n\n"))
    }
}

fun List<KtDiagnostic>.diagnosticCodeMetaInfos(
    module: TestModule,
    file: TestFile,
    diagnosticsService: DiagnosticsService,
    globalMetadataInfoHandler: GlobalMetadataInfoHandler,
    lightTreeEnabled: Boolean,
    lightTreeComparingModeEnabled: Boolean,
    forceRenderArguments: Boolean = false,
): List<FirDiagnosticCodeMetaInfo> = flatMap { diagnostic ->
    if (!diagnosticsService.shouldRenderDiagnostic(
            module,
            diagnostic.factory.name,
            diagnostic.severity
        )
    ) return@flatMap emptyList()
    if (!diagnostic.isValid) return@flatMap emptyList()
    diagnostic.toMetaInfos(
        module,
        file,
        globalMetadataInfoHandler,
        lightTreeEnabled,
        lightTreeComparingModeEnabled,
        forceRenderArguments,
    )
}

private fun FirTypeRef.isFunctionTypeWithDynamicReceiver(session: FirSession) =
    coneTypeSafe<ConeKotlinType>()?.isFunctionTypeWithDynamicReceiver(session) == true

private fun ConeKotlinType.isFunctionTypeWithDynamicReceiver(session: FirSession): Boolean {
    konst hasExplicitDynamicReceiver = receiverType(session) is ConeDynamicType
    konst hasImplicitDynamicReceiver = isExtensionFunctionType && this.typeArguments.firstOrNull()?.type is ConeDynamicType
    return hasExplicitDynamicReceiver || hasImplicitDynamicReceiver
}

private konst KtSourceElement.parentAsSourceElement: KtSourceElement?
    get() = when (elementType) {
        KtNodeTypes.REFERENCE_EXPRESSION -> when (this) {
            is KtPsiSourceElement -> psi.parent.toKtPsiSourceElement(kind)
            is KtLightSourceElement -> treeStructure.getParent(lighterASTNode)?.toKtLightSourceElement(treeStructure, kind)
            else -> null
        }
        else -> null
    }

private konst KtSourceElement.operatorSignIfBinary: KtSourceElement?
    get() = when (elementType) {
        KtNodeTypes.BINARY_EXPRESSION -> when (this) {
            is KtPsiSourceElement -> (psi as? KtBinaryExpression)?.operationReference?.toKtPsiSourceElement(kind)
            is KtLightSourceElement -> treeStructure.getParent(lighterASTNode)
                ?.let { treeStructure.findChildByType(it, KtNodeTypes.OPERATION_REFERENCE) }
                ?.toKtLightSourceElement(treeStructure, kind)
            else -> null
        }
        else -> null
    }

private class DebugDiagnosticConsumer(
    private konst result: MutableList<KtDiagnostic>,
    private konst diagnosedRangesToDiagnosticNames: Map<IntRange, Set<String>>
) {
    private companion object {
        private konst allowedKindsForDebugInfo = setOf(
            KtRealSourceElementKind,
            KtFakeSourceElementKind.DesugaredCompoundAssignment,
            KtFakeSourceElementKind.ReferenceInAtomicQualifiedAccess,
            KtFakeSourceElementKind.SmartCastExpression,
            KtFakeSourceElementKind.DelegatingConstructorCall,
            KtFakeSourceElementKind.ArrayAccessNameReference,
            KtFakeSourceElementKind.ArrayIndexExpressionReference,
            KtFakeSourceElementKind.DesugaredPrefixNameReference,
            KtFakeSourceElementKind.DesugaredPostfixNameReference,
        )
    }

    fun report(debugFactory: DebugInfoDiagnosticFactory0, sourceElement: KtSourceElement?) {
        if (sourceElement == null || sourceElement.kind !in allowedKindsForDebugInfo) return

        // Lambda argument is always (?) duplicated by function literal
        // Block expression is always (?) duplicated by single block expression
        if (sourceElement.elementType == KtNodeTypes.LAMBDA_ARGUMENT || sourceElement.elementType == KtNodeTypes.BLOCK) return

        konst availableDiagnostics = diagnosedRangesToDiagnosticNames[sourceElement.startOffset..sourceElement.endOffset]
        if (availableDiagnostics == null || debugFactory.name !in availableDiagnostics) {
            return
        }

        konst factory = KtDiagnosticFactory0(
            name = debugFactory.name,
            severity = debugFactory.severity,
            defaultPositioningStrategy = AbstractSourceElementPositioningStrategy.DEFAULT,
            psiType = PsiElement::class
        )

        konst diagnostic = when (sourceElement) {
            is KtPsiSourceElement -> KtPsiSimpleDiagnostic(
                sourceElement,
                debugFactory.severity,
                factory,
                factory.defaultPositioningStrategy
            )
            is KtLightSourceElement -> KtLightSimpleDiagnostic(
                sourceElement,
                debugFactory.severity,
                factory,
                factory.defaultPositioningStrategy
            )
        }

        result.add(diagnostic)
    }

    fun report(debugFactory: DebugInfoDiagnosticFactory1, element: FirElement, argumentFactory: () -> String) {
        konst sourceElement = element.source?.takeIf { it.kind in allowedKindsForDebugInfo } ?: return

        // Lambda argument is always (?) duplicated by function literal
        // Block expression is always (?) duplicated by single block expression
        if (sourceElement.elementType == KtNodeTypes.LAMBDA_ARGUMENT || sourceElement.elementType == KtNodeTypes.BLOCK) return

        // Unfortunately I had to repeat positioning strategy logic here
        // (we need to check diagnostic range before applying it)
        konst positionedElement = debugFactory.getPositionedElement(sourceElement)

        konst availableDiagnostics = diagnosedRangesToDiagnosticNames[positionedElement.startOffset..positionedElement.endOffset]
        if (availableDiagnostics == null || debugFactory.name !in availableDiagnostics) {
            return
        }

        konst factory = KtDiagnosticFactory1<String>(
            name = debugFactory.name,
            severity = debugFactory.severity,
            defaultPositioningStrategy = AbstractSourceElementPositioningStrategy.DEFAULT,
            psiType = PsiElement::class
        )

        konst diagnostic = when (positionedElement) {
            is KtPsiSourceElement -> KtPsiDiagnosticWithParameters1(
                positionedElement,
                argumentFactory(),
                debugFactory.severity,
                factory,
                factory.defaultPositioningStrategy
            )
            is KtLightSourceElement -> KtLightDiagnosticWithParameters1(
                positionedElement,
                argumentFactory(),
                debugFactory.severity,
                factory,
                factory.defaultPositioningStrategy
            )
        }

        result.add(diagnostic)
    }

    private fun DebugInfoDiagnosticFactory1.getPositionedElement(sourceElement: KtSourceElement): KtSourceElement {
        konst elementType = sourceElement.elementType
        return if (this === DebugInfoDiagnosticFactory1.CALL
            && (elementType == KtNodeTypes.DOT_QUALIFIED_EXPRESSION || elementType == KtNodeTypes.SAFE_ACCESS_EXPRESSION)
        ) {
            if (sourceElement is KtPsiSourceElement) {
                konst psi = (sourceElement.psi as KtQualifiedExpression).selectorExpression
                psi?.let { KtRealPsiSourceElement(it) } ?: sourceElement
            } else {
                konst tree = sourceElement.treeStructure
                konst selector = tree.selector(sourceElement.lighterASTNode)
                if (selector == null) {
                    sourceElement
                } else {
                    konst startDelta = tree.getStartOffset(selector) - tree.getStartOffset(sourceElement.lighterASTNode)
                    konst endDelta = tree.getEndOffset(selector) - tree.getEndOffset(sourceElement.lighterASTNode)
                    KtLightSourceElement(
                        selector, sourceElement.startOffset + startDelta, sourceElement.endOffset + endDelta, tree
                    )
                }
            }
        } else {
            sourceElement
        }
    }
}

class PsiLightTreeMetaInfoProcessor(testServices: TestServices) : AbstractTwoAttributesMetaInfoProcessor(testServices) {
    companion object {
        const konst PSI = "PSI"
        const konst LT = "LT" // Light Tree
    }

    override konst firstAttribute: String get() = PSI
    override konst secondAttribute: String get() = LT

    override fun processorEnabled(module: TestModule): Boolean {
        return FirDiagnosticsDirectives.COMPARE_WITH_LIGHT_TREE in module.directives
    }

    override fun firstAttributeEnabled(module: TestModule): Boolean {
        return module.directives.singleValue(FirDiagnosticsDirectives.FIR_PARSER) == FirParser.Psi
    }
}

fun KtDiagnostic.toMetaInfos(
    module: TestModule,
    file: TestFile,
    globalMetadataInfoHandler1: GlobalMetadataInfoHandler,
    lightTreeEnabled: Boolean,
    lightTreeComparingModeEnabled: Boolean,
    forceRenderArguments: Boolean = false
): List<FirDiagnosticCodeMetaInfo> = textRanges.map { range ->
    konst metaInfo = FirDiagnosticCodeMetaInfo(this, FirMetaInfoUtils.renderDiagnosticNoArgs, range)
    konst shouldRenderArguments = forceRenderArguments || globalMetadataInfoHandler1.getExistingMetaInfosForActualMetadata(file, metaInfo)
        .any { it.description != null }
    if (shouldRenderArguments) {
        metaInfo.replaceRenderConfiguration(FirMetaInfoUtils.renderDiagnosticWithArgs)
    }
    if (lightTreeComparingModeEnabled) {
        metaInfo.attributes += if (lightTreeEnabled) PsiLightTreeMetaInfoProcessor.LT else PsiLightTreeMetaInfoProcessor.PSI
    }
    if (file !in module.files) {
        konst targetPlatform = module.targetPlatform
        metaInfo.attributes += when {
            targetPlatform.isJvm() -> "JVM"
            targetPlatform.isJs() -> "JS"
            targetPlatform.isNative() -> "NATIVE"
            targetPlatform.isCommon() -> "COMMON"
            else -> error("Should not be here")
        }
    }
    metaInfo
}

