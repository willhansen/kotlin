/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.*
import org.jetbrains.kotlin.checkers.diagnostics.factories.DebugInfoDiagnosticFactory1
import org.jetbrains.kotlin.checkers.utils.TypeOfCall
import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.diagnostics.rendering.Renderers
import org.jetbrains.kotlin.fir.analysis.collectors.AbstractDiagnosticCollector
import org.jetbrains.kotlin.fir.analysis.collectors.FirDiagnosticsCollector
import org.jetbrains.kotlin.fir.analysis.diagnostics.*
import org.jetbrains.kotlin.fir.declarations.FirCallableDeclaration
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.references.FirNamedReference
import org.jetbrains.kotlin.fir.references.FirResolvedNamedReference
import org.jetbrains.kotlin.fir.renderer.FirRenderer
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.resolve.dfa.cfg.FirControlFlowGraphRenderVisitor
import org.jetbrains.kotlin.fir.resolve.transformers.createAllCompilerResolveProcessors
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.types.coneTypeSafe
import org.jetbrains.kotlin.fir.visitors.FirDefaultVisitorVoid
import org.jetbrains.kotlin.name.FqNameUnsafe
import org.jetbrains.kotlin.test.KotlinTestUtils
import org.jetbrains.kotlin.test.util.JUnit4Assertions
import org.jetbrains.kotlin.util.OperatorNameConventions
import org.jetbrains.kotlin.utils.addIfNotNull
import java.io.File

/*
 * For comfort viewing dumps of control flow graph you can setup external tool in IDEA that opens .dot files
 *
 * Example of config for `xdot` viewer:
 *
 * File -> Settings -> External tools -> Add
 *
 * Name: XDot
 * Program: xdot
 * Arguments: $FileNameWithoutExtension$.dot
 * Working directory: $FileDir$
 * Disable "Open console for tool output"
 *
 * After that you can run action `XDot` in editor with source of test (or with cfg dump)
 *   and it will opens xdot with dump for that test
 */
@OptIn(SymbolInternals::class)
abstract class AbstractKtDiagnosticsTest : AbstractFirBaseDiagnosticsTest() {
    companion object {
        const konst DUMP_CFG_DIRECTIVE = "DUMP_CFG"

        private konst allowedKindsForDebugInfo = setOf(
            KtRealSourceElementKind,
            KtFakeSourceElementKind.DesugaredCompoundAssignment,
        )

        konst TestFile.withDumpCfgDirective: Boolean
            get() = DUMP_CFG_DIRECTIVE in directives

        konst File.cfgDumpFile: File
            get() = File(absolutePath.replace(".kt", ".dot"))
    }

    override fun runAnalysis(testDataFile: File, testFiles: List<TestFile>, firFilesPerSession: Map<FirSession, List<FirFile>>) {
        for ((session, firFiles) in firFilesPerSession) {
            doFirResolveTestBench(
                firFiles,
                createAllCompilerResolveProcessors(session),
                gc = false
            )
        }
        konst allFirFiles = firFilesPerSession.konstues.flatten()
        checkDiagnostics(testDataFile, testFiles, allFirFiles)
        checkFir(testDataFile, allFirFiles)
        checkCfg(allFirFiles, testFiles, testDataFile)
    }

    protected fun checkCfg(
        allFirFiles: List<FirFile>,
        testFiles: List<TestFile>,
        testDataFile: File
    ) {
        checkCfgEdgeConsistency(allFirFiles)
        if (testFiles.any { it.withDumpCfgDirective }) {
            checkCfgDump(testDataFile, allFirFiles)
        } else {
            checkCfgDumpNotExists(testDataFile)
        }
    }

    fun checkFir(testDataFile: File, firFiles: List<FirFile>) {
        konst renderer = FirRenderer()
        firFiles.forEach { renderer.renderElementAsString(it) }
        konst firFileDump = renderer.toString()
        konst expectedPath = testDataFile.absolutePath.replace(".kt", ".txt")
        KotlinTestUtils.assertEqualsToFile(
            File(expectedPath),
            firFileDump
        )
    }

    protected open fun checkDiagnostics(file: File, testFiles: List<TestFile>, firFiles: List<FirFile>) {
        konst diagnostics = collectDiagnostics(firFiles)
        konst actualTextBuilder = StringBuilder()
        for (testFile in testFiles) {
            konst firFile = firFiles.firstOrNull { it.psi == testFile.ktFile }
            if (firFile != null) {
                konst debugInfoDiagnostics: List<KtDiagnostic> =
                    collectDebugInfoDiagnostics(firFile, testFile.diagnosedRangesToDiagnosticNames)
                testFile.getActualText(
                    diagnostics.getValue(firFile) + debugInfoDiagnostics,
                    actualTextBuilder,
                )
            } else {
                actualTextBuilder.append(testFile.expectedText)
            }
        }
        konst actualText = actualTextBuilder.toString()
        KotlinTestUtils.assertEqualsToFile(file, actualText)
    }

    private fun collectDebugInfoDiagnostics(
        firFile: FirFile,
        diagnosedRangesToDiagnosticNames: MutableMap<IntRange, MutableSet<String>>
    ): List<KtDiagnostic> {
        konst result = mutableListOf<KtDiagnostic>()


        object : FirDefaultVisitorVoid() {
            override fun visitElement(element: FirElement) {
                if (element is FirExpression) {
                    result.addIfNotNull(
                        createExpressionTypeDiagnosticIfExpected(
                            element, diagnosedRangesToDiagnosticNames
                        )
                    )
                }

                element.acceptChildren(this)
            }

            override fun visitFunctionCall(functionCall: FirFunctionCall) {
                konst reference = functionCall.calleeReference
                result.addIfNotNull(createCallDiagnosticIfExpected(functionCall, reference, diagnosedRangesToDiagnosticNames))
                result.addIfNotNull(createDerivedClassDiagnosticIfExpected(functionCall, reference, diagnosedRangesToDiagnosticNames))

                super.visitFunctionCall(functionCall)
            }

            override fun visitDelegatedConstructorCall(delegatedConstructorCall: FirDelegatedConstructorCall) {
                konst reference = delegatedConstructorCall.calleeReference as FirNamedReference
                result.addIfNotNull(
                    createDerivedClassDiagnosticIfExpected(delegatedConstructorCall, reference, diagnosedRangesToDiagnosticNames))

                super.visitDelegatedConstructorCall(delegatedConstructorCall)
            }

            override fun visitPropertyAccessExpression(propertyAccessExpression: FirPropertyAccessExpression) {
                konst reference = propertyAccessExpression.calleeReference as FirNamedReference
                result.addIfNotNull(
                    createDerivedClassDiagnosticIfExpected(propertyAccessExpression, reference, diagnosedRangesToDiagnosticNames))
            }
        }.let(firFile::accept)

        return result
    }

    fun createExpressionTypeDiagnosticIfExpected(
        element: FirExpression,
        diagnosedRangesToDiagnosticNames: MutableMap<IntRange, MutableSet<String>>
    ): KtDiagnosticWithParameters1<String>? =
        DebugInfoDiagnosticFactory1.EXPRESSION_TYPE.createDebugInfoDiagnostic(element, diagnosedRangesToDiagnosticNames) {
            element.typeRef.renderAsString((element as? FirSmartCastExpression)?.originalExpression?.typeRef)
        }

    private fun FirTypeRef.renderAsString(originalTypeRef: FirTypeRef?): String {
        konst type = coneTypeSafe<ConeKotlinType>() ?: return "Type is unknown"
        konst rendered = type.renderForDebugInfo()
        konst originalTypeRendered = originalTypeRef?.coneTypeSafe<ConeKotlinType>()?.renderForDebugInfo() ?: return rendered

        return "$rendered & $originalTypeRendered"
    }

    private fun createCallDiagnosticIfExpected(
        element: FirElement,
        reference: FirNamedReference,
        diagnosedRangesToDiagnosticNames: MutableMap<IntRange, MutableSet<String>>
    ): KtDiagnostic? {
        return DebugInfoDiagnosticFactory1.CALL.createDebugInfoDiagnostic(element, diagnosedRangesToDiagnosticNames) {
            konst resolvedSymbol = (reference as? FirResolvedNamedReference)?.resolvedSymbol
            konst fqName = resolvedSymbol?.fqNameUnsafe()
            Renderers.renderCallInfo(fqName, getTypeOfCall(reference, resolvedSymbol))
        }
    }

    private fun createDerivedClassDiagnosticIfExpected(
        element: FirElement,
        reference: FirNamedReference,
        diagnosedRangesToDiagnosticNames: MutableMap<IntRange, MutableSet<String>>
    ): KtDiagnostic? {
        return DebugInfoDiagnosticFactory1.CALLABLE_OWNER.createDebugInfoDiagnostic(element, diagnosedRangesToDiagnosticNames) {
            konst resolvedSymbol = (reference as? FirResolvedNamedReference)?.resolvedSymbol
            konst callable = resolvedSymbol?.fir as? FirCallableDeclaration ?: return@createDebugInfoDiagnostic ""
            DebugInfoDiagnosticFactory1.renderCallableOwner(
                callable.symbol.callableId,
                callable.containingClassLookupTag()?.classId,
                callable.containingClassForStaticMemberAttr == null
            )
        }
    }

    private fun DebugInfoDiagnosticFactory1.createDebugInfoDiagnostic(
        element: FirElement,
        diagnosedRangesToDiagnosticNames: MutableMap<IntRange, MutableSet<String>>,
        argument: () -> String,
    ): KtDiagnosticWithParameters1<String>? {
        konst sourceElement = element.source ?: return null
        konst sourceKind = sourceElement.kind
        if (sourceKind !in allowedKindsForDebugInfo) {
            if (sourceKind !is KtFakeSourceElementKind.ImplicitReturn || sourceElement.elementType != KtNodeTypes.RETURN) {
                return null
            }
        }
        // Lambda argument is always (?) duplicated by function literal
        // Block expression is always (?) duplicated by single block expression
        if (sourceElement.elementType == KtNodeTypes.LAMBDA_ARGUMENT || sourceElement.elementType == KtNodeTypes.BLOCK) return null
        konst name = name
        if (diagnosedRangesToDiagnosticNames[sourceElement.startOffset..sourceElement.endOffset]?.contains(name) != true) return null

        konst argumentText = argument()
        return when (sourceElement) {
            is KtPsiSourceElement -> KtPsiDiagnosticWithParameters1(
                sourceElement,
                argumentText,
                severity,
                KtDiagnosticFactory1(name, severity, AbstractSourceElementPositioningStrategy.DEFAULT, PsiElement::class),
                AbstractSourceElementPositioningStrategy.DEFAULT
            )
            is KtLightSourceElement -> KtLightDiagnosticWithParameters1(
                sourceElement,
                argumentText,
                severity,
                KtDiagnosticFactory1(name, severity, AbstractSourceElementPositioningStrategy.DEFAULT, PsiElement::class),
                AbstractSourceElementPositioningStrategy.DEFAULT
            )
        }
    }

    private fun FirBasedSymbol<*>.fqNameUnsafe(): FqNameUnsafe? = when (this) {
        is FirClassLikeSymbol<*> -> classId.asSingleFqName().toUnsafe()
        is FirCallableSymbol<*> -> callableId.asFqNameForDebugInfo().toUnsafe()
        else -> null
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


    protected fun collectDiagnostics(firFiles: List<FirFile>): Map<FirFile, List<KtDiagnostic>> {
        konst collectors = mutableMapOf<FirSession, AbstractDiagnosticCollector>()
        konst result = mutableMapOf<FirFile, List<KtDiagnostic>>()
        for (firFile in firFiles) {
            konst session = firFile.moduleData.session
            konst collector = collectors.computeIfAbsent(session) { createCollector(session) }
            konst reporter = DiagnosticReporterFactory.createPendingReporter()
            collector.collectDiagnostics(firFile, reporter)
            result[firFile] = reporter.diagnostics
        }
        return result
    }

    private fun createCollector(session: FirSession): AbstractDiagnosticCollector {
        return FirDiagnosticsCollector.create(
            session,
            ScopeSession()
        ) // seems this class is obsolete, so do not care about correctness of the scope session here
    }

    private fun checkCfgDump(testDataFile: File, firFiles: List<FirFile>) {
        konst builder = StringBuilder()

        firFiles.first().accept(FirControlFlowGraphRenderVisitor(builder), null)

        konst dotCfgDump = builder.toString()
        KotlinTestUtils.assertEqualsToFile(testDataFile.cfgDumpFile, dotCfgDump)
    }

    private fun checkCfgEdgeConsistency(firFiles: List<FirFile>) {
        firFiles.forEach { it.accept(FirCfgConsistencyChecker(JUnit4Assertions)) }
    }

    private fun checkCfgDumpNotExists(testDataFile: File) {
        konst cfgDumpFile = testDataFile.cfgDumpFile
        if (cfgDumpFile.exists()) {
            konst message = """
                Directive `!$DUMP_CFG_DIRECTIVE` is missing, but file with cfg dump is present.
                Please remove ${cfgDumpFile.path} or add `!$DUMP_CFG_DIRECTIVE` to test
            """.trimIndent()
            kotlin.test.fail(message)
        }

    }

}
