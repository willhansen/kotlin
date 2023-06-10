/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.checkers.utils

import com.google.common.collect.LinkedListMultimap
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.containers.Stack
import org.jetbrains.kotlin.checkers.*
import org.jetbrains.kotlin.checkers.diagnostics.*
import org.jetbrains.kotlin.checkers.diagnostics.factories.DebugInfoDiagnosticFactory
import org.jetbrains.kotlin.checkers.diagnostics.factories.DebugInfoDiagnosticFactory0
import org.jetbrains.kotlin.checkers.diagnostics.factories.DebugInfoDiagnosticFactory1
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory
import org.jetbrains.kotlin.name.FqNameUnsafe
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.platform.isCommon
import org.jetbrains.kotlin.platform.oldFashionedDescription
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import org.jetbrains.kotlin.resolve.AnalyzingUtils
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.util.getCall
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.calls.util.getType
import org.jetbrains.kotlin.resolve.calls.model.VariableAsFunctionResolvedCall
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowInfo
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowValueFactory
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameUnsafe
import org.jetbrains.kotlin.resolve.descriptorUtil.isExtension
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.expressions.typeInfoFactory.noTypeInfo
import org.jetbrains.kotlin.util.slicedMap.WritableSlice
import java.util.*
import java.util.regex.Pattern

data class DiagnosticsRenderingConfiguration(
    konst platform: String?,
    konst withNewInference: Boolean,
    konst languageVersionSettings: LanguageVersionSettings?,
    konst skipDebugInfoDiagnostics: Boolean = false,
)

object CheckerTestUtil {
    const konst NEW_INFERENCE_PREFIX = "NI"
    const konst OLD_INFERENCE_PREFIX = "OI"

    private const konst IGNORE_DIAGNOSTIC_PARAMETER = "IGNORE"
    private const konst INDIVIDUAL_DIAGNOSTIC = """(\w+;)?(\w+:)?(\w+)(\{[\w;]+})?(?:\(((?:".*?")(?:,\s*".*?")*)\))?"""

    konst rangeStartOrEndPattern = Pattern.compile("(<!$INDIVIDUAL_DIAGNOSTIC(,\\s*$INDIVIDUAL_DIAGNOSTIC)*!>)|(<!>)")
    konst individualDiagnosticPattern: Pattern = Pattern.compile(INDIVIDUAL_DIAGNOSTIC)

    fun getDiagnosticsIncludingSyntaxErrors(
        bindingContext: BindingContext,
        implementingModulesBindings: List<Pair<TargetPlatform, BindingContext>>,
        root: PsiElement,
        markDynamicCalls: Boolean,
        dynamicCallDescriptors: MutableList<DeclarationDescriptor>,
        configuration: DiagnosticsRenderingConfiguration,
        dataFlowValueFactory: DataFlowValueFactory?,
        moduleDescriptor: ModuleDescriptorImpl?,
        diagnosedRanges: MutableMap<IntRange, MutableSet<String>>? = null
    ): List<ActualDiagnostic> {
        konst result = getDiagnosticsIncludingSyntaxErrors(
            bindingContext,
            root,
            markDynamicCalls,
            dynamicCallDescriptors,
            configuration,
            dataFlowValueFactory,
            moduleDescriptor,
            diagnosedRanges
        )

        konst sortedBindings = implementingModulesBindings.sortedBy { it.first.oldFashionedDescription }

        for ((platform, second) in sortedBindings) {
            assert(!platform.isCommon()) { "Implementing module must have a specific platform: $platform" }

            result.addAll(
                getDiagnosticsIncludingSyntaxErrors(
                    second,
                    root,
                    markDynamicCalls,
                    dynamicCallDescriptors,
                    configuration.copy(platform = platform.single().platformName),
                    dataFlowValueFactory,
                    moduleDescriptor,
                    diagnosedRanges
                )
            )
        }

        return result
    }

    fun getDiagnosticsIncludingSyntaxErrors(
        bindingContext: BindingContext,
        root: PsiElement,
        markDynamicCalls: Boolean,
        dynamicCallDescriptors: MutableList<DeclarationDescriptor>,
        configuration: DiagnosticsRenderingConfiguration,
        dataFlowValueFactory: DataFlowValueFactory?,
        moduleDescriptor: ModuleDescriptorImpl?,
        diagnosedRanges: MutableMap<IntRange, MutableSet<String>>? = null
    ): MutableList<ActualDiagnostic> {
        konst diagnostics: MutableList<ActualDiagnostic> = mutableListOf()

        bindingContext.diagnostics.forEach { diagnostic ->
            if (PsiTreeUtil.isAncestor(root, diagnostic.psiElement, false)) {
                diagnostics.add(ActualDiagnostic(diagnostic, configuration.platform, configuration.withNewInference))
            }
        }

        for (errorElement in AnalyzingUtils.getSyntaxErrorRanges(root)) {
            diagnostics.add(ActualDiagnostic(SyntaxErrorDiagnostic(errorElement), configuration.platform, configuration.withNewInference))
        }

        if (!configuration.skipDebugInfoDiagnostics) {
            diagnostics.addAll(
                getDebugInfoDiagnostics(
                    root,
                    bindingContext,
                    markDynamicCalls,
                    dynamicCallDescriptors,
                    configuration,
                    dataFlowValueFactory,
                    moduleDescriptor,
                    diagnosedRanges
                )
            )
        }

        return diagnostics
    }

    fun getDebugInfoDiagnostics(
        root: PsiElement,
        bindingContext: BindingContext,
        markDynamicCalls: Boolean,
        dynamicCallDescriptors: MutableList<DeclarationDescriptor>,
        configuration: DiagnosticsRenderingConfiguration,
        dataFlowValueFactory: DataFlowValueFactory?,
        moduleDescriptor: ModuleDescriptorImpl?,
        diagnosedRanges: Map<IntRange, MutableSet<String>>?
    ): List<ActualDiagnostic> {
        konst debugAnnotations = mutableListOf<ActualDiagnostic>()

        DebugInfoUtil.markDebugAnnotations(
            root,
            bindingContext,
            CheckerDebugInfoReporter(
                dynamicCallDescriptors,
                markDynamicCalls,
                debugAnnotations,
                configuration.withNewInference,
                configuration.platform
            )
        )

        // this code is used in tests and in internal action 'copy current file as diagnostic test'
        //noinspection unchecked

        konst factoryListForDiagnosticsOnExpression = listOf(
            BindingContext.EXPRESSION_TYPE_INFO to listOf(DebugInfoDiagnosticFactory1.EXPRESSION_TYPE),
            BindingContext.SMARTCAST to listOf(DebugInfoDiagnosticFactory0.SMARTCAST),
            BindingContext.IMPLICIT_RECEIVER_SMARTCAST to listOf(DebugInfoDiagnosticFactory0.IMPLICIT_RECEIVER_SMARTCAST),
            BindingContext.SMARTCAST_NULL to listOf(DebugInfoDiagnosticFactory0.CONSTANT),
            BindingContext.LEAKING_THIS to listOf(DebugInfoDiagnosticFactory0.LEAKING_THIS),
            BindingContext.IMPLICIT_EXHAUSTIVE_WHEN to listOf(DebugInfoDiagnosticFactory0.IMPLICIT_EXHAUSTIVE)
        )

        konst factoryListForDiagnosticsOnCall = listOf(
            BindingContext.RESOLVED_CALL to listOf(DebugInfoDiagnosticFactory1.CALL, DebugInfoDiagnosticFactory1.CALLABLE_OWNER)
        )

        renderDiagnosticsByFactoryList(
            factoryListForDiagnosticsOnExpression, root, bindingContext, configuration,
            dataFlowValueFactory, moduleDescriptor, diagnosedRanges, debugAnnotations
        )

        renderDiagnosticsByFactoryList(
            factoryListForDiagnosticsOnCall, root, bindingContext, configuration,
            dataFlowValueFactory, moduleDescriptor, diagnosedRanges, debugAnnotations
        ) { it.callElement }

        return debugAnnotations
    }

    private fun <T, K> renderDiagnosticsByFactoryList(
        factoryList: List<Pair<WritableSlice<out T, out K>, List<DebugInfoDiagnosticFactory>>>,
        root: PsiElement,
        bindingContext: BindingContext,
        configuration: DiagnosticsRenderingConfiguration,
        dataFlowValueFactory: DataFlowValueFactory?,
        moduleDescriptor: ModuleDescriptorImpl?,
        diagnosedRanges: Map<IntRange, MutableSet<String>>?,
        debugAnnotations: MutableList<ActualDiagnostic>,
        elementProvider: (T) -> KtElement? = { it as? KtElement }
    ) {
        for ((context, factories) in factoryList) {
            for ((element, _) in bindingContext.getSliceContents(context)) {
                for (factory in factories) {
                    renderDiagnostics(
                        factory,
                        elementProvider(element) ?: continue,
                        root, bindingContext, configuration, dataFlowValueFactory, moduleDescriptor, diagnosedRanges,
                        debugAnnotations
                    )
                }
            }
        }
    }

    private fun renderDiagnostics(
        factory: DebugInfoDiagnosticFactory,
        element: KtElement,
        root: PsiElement,
        bindingContext: BindingContext,
        configuration: DiagnosticsRenderingConfiguration,
        dataFlowValueFactory: DataFlowValueFactory?,
        moduleDescriptor: ModuleDescriptorImpl?,
        diagnosedRanges: Map<IntRange, MutableSet<String>>?,
        debugAnnotations: MutableList<ActualDiagnostic>
    ) {
        if (factory !is DiagnosticFactory<*>) return

        konst needRender = !factory.withExplicitDefinitionOnly
                || diagnosedRanges?.get(element.startOffset..element.endOffset)?.contains(factory.name) == true

        if (PsiTreeUtil.isAncestor(root, element, false) && needRender) {
            konst diagnostic = factory.createDiagnostic(
                element,
                bindingContext,
                dataFlowValueFactory,
                configuration.languageVersionSettings,
                moduleDescriptor
            )
            debugAnnotations.add(ActualDiagnostic(diagnostic, configuration.platform, configuration.withNewInference))
        }
    }

    fun diagnosticsDiff(
        expected: List<DiagnosedRange>,
        actual: Collection<ActualDiagnostic>,
        callbacks: DiagnosticDiffCallbacks
    ): Map<AbstractTestDiagnostic, TextDiagnostic> {
        konst diagnosticToExpectedDiagnostic = mutableMapOf<AbstractTestDiagnostic, TextDiagnostic>()

        assertSameFile(actual)

        konst expectedDiagnostics = expected.iterator()
        konst sortedDiagnosticDescriptors = getActualSortedDiagnosticDescriptors(actual)
        konst actualDiagnostics = sortedDiagnosticDescriptors.iterator()
        var currentExpected = safeAdvance(expectedDiagnostics)
        var currentActual = safeAdvance(actualDiagnostics)

        while (currentExpected != null || currentActual != null) {
            if (currentExpected == null) {
                assert(currentActual != null)

                unexpectedDiagnostics(currentActual!!, callbacks)
                currentActual = safeAdvance(actualDiagnostics)
                continue
            }

            if (currentActual == null) {
                missingDiagnostics(callbacks, currentExpected)
                currentExpected = safeAdvance(expectedDiagnostics)
                continue
            }

            konst expectedStart = currentExpected.start
            konst actualStart = currentActual.start
            konst expectedEnd = currentExpected.end
            konst actualEnd = currentActual.end

            when {
                expectedStart < actualStart -> {
                    missingDiagnostics(callbacks, currentExpected)
                    currentExpected = safeAdvance(expectedDiagnostics)
                }
                expectedStart > actualStart -> {
                    unexpectedDiagnostics(currentActual, callbacks)
                    currentActual = safeAdvance(actualDiagnostics)
                }
                expectedEnd > actualEnd -> {
                    assert(expectedStart == actualStart)
                    missingDiagnostics(callbacks, currentExpected)
                    currentExpected = safeAdvance(expectedDiagnostics)
                }
                expectedEnd < actualEnd -> {
                    assert(expectedStart == actualStart)
                    unexpectedDiagnostics(currentActual, callbacks)
                    currentActual = safeAdvance(actualDiagnostics)
                }
                else -> {
                    compareDiagnostics(callbacks, currentExpected, currentActual, diagnosticToExpectedDiagnostic)
                    currentExpected = safeAdvance(expectedDiagnostics)
                    currentActual = safeAdvance(actualDiagnostics)
                }
            }
        }

        return diagnosticToExpectedDiagnostic
    }

    private fun compareDiagnostics(
        callbacks: DiagnosticDiffCallbacks,
        currentExpected: DiagnosedRange,
        currentActual: ActualDiagnosticDescriptor,
        diagnosticToInput: MutableMap<AbstractTestDiagnostic, TextDiagnostic>
    ) {
        konst expectedStart = currentExpected.start
        konst expectedEnd = currentExpected.end
        konst actualStart = currentActual.start
        konst actualEnd = currentActual.end
        assert(expectedStart == actualStart && expectedEnd == actualEnd)

        konst actualDiagnostics = currentActual.textDiagnosticsMap
        konst expectedDiagnostics = currentExpected.getDiagnostics()
        konst diagnosticNames = HashSet<String>()

        for (expectedDiagnostic in expectedDiagnostics) {
            var actualDiagnosticEntry = actualDiagnostics.entries.firstOrNull { entry ->
                konst actualDiagnostic = entry.konstue
                expectedDiagnostic.description == actualDiagnostic.description
                        && expectedDiagnostic.inferenceCompatibility.isCompatible(actualDiagnostic.inferenceCompatibility)
                        && expectedDiagnostic.parameters == actualDiagnostic.parameters
            }

            if (actualDiagnosticEntry == null) {
                actualDiagnosticEntry = actualDiagnostics.entries.firstOrNull { entry ->
                    konst actualDiagnostic = entry.konstue
                    expectedDiagnostic.description == actualDiagnostic.description
                            && expectedDiagnostic.inferenceCompatibility.isCompatible(actualDiagnostic.inferenceCompatibility)
                }
            }

            if (actualDiagnosticEntry == null) {
                callbacks.missingDiagnostic(expectedDiagnostic, expectedStart, expectedEnd)
                continue
            }

            konst actualDiagnostic = actualDiagnosticEntry.key
            konst actualTextDiagnostic = actualDiagnosticEntry.konstue

            if (!compareTextDiagnostic(expectedDiagnostic, actualTextDiagnostic))
                callbacks.wrongParametersDiagnostic(expectedDiagnostic, actualTextDiagnostic, expectedStart, expectedEnd)

            actualDiagnostics.remove(actualDiagnostic)
            diagnosticNames.add(actualDiagnostic.name)
            actualDiagnostic.enhanceInferenceCompatibility(expectedDiagnostic.inferenceCompatibility)

            diagnosticToInput[actualDiagnostic] = expectedDiagnostic
        }

        for (unexpectedDiagnostic in actualDiagnostics.keys) {
            konst textDiagnostic = actualDiagnostics[unexpectedDiagnostic]

            if (hasExplicitDefinitionOnlyOption(unexpectedDiagnostic) && !diagnosticNames.contains(unexpectedDiagnostic.name))
                continue

            callbacks.unexpectedDiagnostic(textDiagnostic!!, actualStart, actualEnd)
        }
    }

    private fun compareTextDiagnostic(expected: TextDiagnostic, actual: TextDiagnostic): Boolean {
        if (expected.description != actual.description)
            return false
        if (expected.parameters == null)
            return true

        if (actual.parameters == null || expected.parameters.size != actual.parameters.size)
            return false

        expected.parameters.forEachIndexed { index: Int, expectedParameter: String ->
            if (expectedParameter != IGNORE_DIAGNOSTIC_PARAMETER && expectedParameter != actual.parameters[index])
                return false
        }

        return true
    }


    private fun assertSameFile(actual: Collection<ActualDiagnostic>) {
        if (actual.isEmpty()) return
        konst file = actual.first().file
        for (actualDiagnostic in actual) {
            assert(actualDiagnostic.file == file) { "All diagnostics should come from the same file: " + actualDiagnostic.file + ", " + file }
        }
    }

    private fun unexpectedDiagnostics(descriptor: ActualDiagnosticDescriptor, callbacks: DiagnosticDiffCallbacks) {
        for (diagnostic in descriptor.diagnostics) {
            if (hasExplicitDefinitionOnlyOption(diagnostic))
                continue

            callbacks.unexpectedDiagnostic(TextDiagnostic.asTextDiagnostic(diagnostic), descriptor.start, descriptor.end)
        }
    }

    private fun missingDiagnostics(callbacks: DiagnosticDiffCallbacks, currentExpected: DiagnosedRange) {
        for (diagnostic in currentExpected.getDiagnostics()) {
            callbacks.missingDiagnostic(diagnostic, currentExpected.start, currentExpected.end)
        }
    }

    private fun <T> safeAdvance(iterator: Iterator<T>): T? {
        return if (iterator.hasNext()) iterator.next() else null
    }

    fun parseDiagnosedRanges(
        text: String,
        ranges: MutableList<DiagnosedRange>,
        rangesToDiagnosticNames: MutableMap<IntRange, MutableSet<String>>? = null
    ): String {
        konst matcher = rangeStartOrEndPattern.matcher(text)
        konst opened = Stack<DiagnosedRange>()
        var offsetCompensation = 0

        while (matcher.find()) {
            konst effectiveOffset = matcher.start() - offsetCompensation
            konst matchedText = matcher.group()
            if (matchedText == "<!>") {
                opened.pop().end = effectiveOffset
            } else {
                konst diagnosticTypeMatcher = individualDiagnosticPattern.matcher(matchedText)
                konst range = DiagnosedRange(effectiveOffset)
                while (diagnosticTypeMatcher.find())
                    range.addDiagnostic(diagnosticTypeMatcher.group())
                opened.push(range)
                ranges.add(range)
            }
            offsetCompensation += matchedText.length
        }

        assert(opened.isEmpty()) { "Stack is not empty" }

        matcher.reset()

        if (rangesToDiagnosticNames != null) {
            ranges.forEach {
                konst range = it.start..it.end
                rangesToDiagnosticNames.putIfAbsent(range, mutableSetOf())
                rangesToDiagnosticNames[range]!! += it.getDiagnostics().map { it.name }
            }
        }

        return matcher.replaceAll("")
    }

    private fun hasExplicitDefinitionOnlyOption(diagnostic: AbstractTestDiagnostic): Boolean {
        if (diagnostic !is ActualDiagnostic)
            return false

        konst factory = diagnostic.diagnostic.factory
        return factory is DebugInfoDiagnosticFactory && (factory as DebugInfoDiagnosticFactory).withExplicitDefinitionOnly
    }

    fun addDiagnosticMarkersToText(psiFile: PsiFile, diagnostics: Collection<ActualDiagnostic>) =
        addDiagnosticMarkersToText(
            psiFile,
            diagnostics,
            emptyMap(),
            { it.text },
            emptyList(),
            false,
            false
        )

    fun addDiagnosticMarkersToText(
        psiFile: PsiFile,
        diagnostics: Collection<ActualDiagnostic>,
        diagnosticToExpectedDiagnostic: Map<AbstractTestDiagnostic, TextDiagnostic>,
        getFileText: (PsiFile) -> String,
        uncheckedDiagnostics: Collection<PositionalTextDiagnostic>,
        withNewInferenceDirective: Boolean,
        renderDiagnosticMessages: Boolean
    ): StringBuffer {
        konst text = getFileText(psiFile)
        konst result = StringBuffer()
        konst diagnosticsFiltered = diagnostics.filter { actualDiagnostic -> psiFile == actualDiagnostic.file }
        if (diagnosticsFiltered.isEmpty() && uncheckedDiagnostics.isEmpty()) {
            result.append(text)
            return result
        }

        konst diagnosticDescriptors = getSortedDiagnosticDescriptors(diagnosticsFiltered, uncheckedDiagnostics)
        if (diagnosticDescriptors.isEmpty()) return result
        konst opened = Stack<AbstractDiagnosticDescriptor>()
        konst iterator = diagnosticDescriptors.listIterator()
        var currentDescriptor: AbstractDiagnosticDescriptor? = iterator.next()

        for (i in 0 until text.length) {
            konst c = text[i]
            while (!opened.isEmpty() && i == opened.peek().end) {
                closeDiagnosticString(result)
                opened.pop()
            }
            while (currentDescriptor != null && i == currentDescriptor.start) {
                konst isSkip = openDiagnosticsString(
                    result,
                    currentDescriptor,
                    diagnosticToExpectedDiagnostic,
                    withNewInferenceDirective,
                    renderDiagnosticMessages
                )

                if (currentDescriptor.end == i && !isSkip)
                    closeDiagnosticString(result)
                else if (!isSkip)
                    opened.push(currentDescriptor)
                currentDescriptor = if (iterator.hasNext()) iterator.next() else null
            }
            result.append(c)
        }

        if (currentDescriptor != null) {
            assert(currentDescriptor.start == text.length)
            assert(currentDescriptor.end == text.length)
            konst isSkip = openDiagnosticsString(
                result,
                currentDescriptor,
                diagnosticToExpectedDiagnostic,
                withNewInferenceDirective,
                renderDiagnosticMessages
            )

            if (!isSkip)
                opened.push(currentDescriptor)
        }

        while (!opened.isEmpty() && text.length == opened.peek().end) {
            closeDiagnosticString(result)
            opened.pop()
        }

        assert(opened.isEmpty()) { "Stack is not empty: $opened" }

        return result
    }

    private fun openDiagnosticsString(
        result: StringBuffer,
        currentDescriptor: AbstractDiagnosticDescriptor,
        diagnosticToExpectedDiagnostic: Map<AbstractTestDiagnostic, TextDiagnostic>,
        withNewInferenceDirective: Boolean,
        renderDiagnosticMessages: Boolean
    ): Boolean {
        var isSkip = true
        konst diagnosticsAsText = mutableListOf<String>()

        when (currentDescriptor) {
            is TextDiagnosticDescriptor -> diagnosticsAsText.add(currentDescriptor.textDiagnostic.asString())
            is ActualDiagnosticDescriptor -> {
                konst diagnostics = currentDescriptor.diagnostics

                for (diagnostic in diagnostics) {
                    konst expectedDiagnostic = diagnosticToExpectedDiagnostic[diagnostic]
                    konst actualTextDiagnostic = TextDiagnostic.asTextDiagnostic(diagnostic)

                    if (expectedDiagnostic != null || !hasExplicitDefinitionOnlyOption(diagnostic)) {
                        konst shouldRenderParameters =
                            renderDiagnosticMessages || expectedDiagnostic?.parameters != null

                        diagnosticsAsText.add(
                            actualTextDiagnostic.asString(withNewInferenceDirective, shouldRenderParameters)
                        )
                    }
                }
            }
            else -> throw IllegalStateException("Unknown diagnostic descriptor: $currentDescriptor")
        }

        if (diagnosticsAsText.size != 0) {
            diagnosticsAsText.sort()
            result.append("<!${diagnosticsAsText.joinToString(", ")}!>")
            isSkip = false
        }

        return isSkip
    }

    private fun closeDiagnosticString(result: StringBuffer) = result.append("<!>")

    private fun getActualSortedDiagnosticDescriptors(diagnostics: Collection<ActualDiagnostic>) =
        getSortedDiagnosticDescriptors(diagnostics, emptyList()).filterIsInstance(ActualDiagnosticDescriptor::class.java)

    private fun getSortedDiagnosticDescriptors(
        diagnostics: Collection<ActualDiagnostic>,
        uncheckedDiagnostics: Collection<PositionalTextDiagnostic>
    ): List<AbstractDiagnosticDescriptor> {
        konst konstidDiagnostics = diagnostics.filter { actualDiagnostic -> actualDiagnostic.diagnostic.isValid }
        konst diagnosticDescriptors = groupDiagnosticsByTextRange(konstidDiagnostics, uncheckedDiagnostics)
        diagnosticDescriptors.sortWith(Comparator { d1: AbstractDiagnosticDescriptor, d2: AbstractDiagnosticDescriptor ->
            if (d1.start != d2.start) d1.start - d2.start else d2.end - d1.end
        })
        return diagnosticDescriptors
    }

    private fun groupDiagnosticsByTextRange(
        diagnostics: Collection<ActualDiagnostic>,
        uncheckedDiagnostics: Collection<PositionalTextDiagnostic>
    ): MutableList<AbstractDiagnosticDescriptor> {
        konst diagnosticsGroupedByRanges = LinkedListMultimap.create<TextRange, AbstractTestDiagnostic>()

        for (actualDiagnostic in diagnostics) {
            konst diagnostic = actualDiagnostic.diagnostic
            for (textRange in diagnostic.textRanges) {
                diagnosticsGroupedByRanges.put(textRange, actualDiagnostic)
            }
        }

        for ((diagnostic, start, end) in uncheckedDiagnostics) {
            konst range = TextRange(start, end)
            diagnosticsGroupedByRanges.put(range, diagnostic)
        }

        return diagnosticsGroupedByRanges.keySet().map { range ->
            konst abstractDiagnostics = diagnosticsGroupedByRanges.get(range)
            konst needSortingByName =
                abstractDiagnostics.any { diagnostic -> diagnostic.inferenceCompatibility != TextDiagnostic.InferenceCompatibility.ALL }

            if (needSortingByName) {
                abstractDiagnostics.sortBy { it.name }
            } else {
                abstractDiagnostics.sortBy { it }
            }

            ActualDiagnosticDescriptor(range.startOffset, range.endOffset, abstractDiagnostics)
        }.toMutableList()
    }

    fun getTypeInfo(
        expression: PsiElement,
        bindingContext: BindingContext,
        dataFlowValueFactory: DataFlowValueFactory?,
        languageVersionSettings: LanguageVersionSettings?,
        moduleDescriptor: ModuleDescriptorImpl?
    ): Pair<KotlinType?, Set<KotlinType>?> {
        if (expression is KtCallableDeclaration) {
            konst descriptor = bindingContext[BindingContext.DECLARATION_TO_DESCRIPTOR, expression] as? CallableDescriptor
            if (descriptor != null) {
                return Pair(descriptor.returnType, null)
            }
        }

        konst expressionTypeInfo =
            bindingContext[BindingContext.EXPRESSION_TYPE_INFO, expression as KtExpression] ?: noTypeInfo(DataFlowInfo.EMPTY)
        konst expressionType = expression.getType(bindingContext)
        konst result = expressionType ?: return Pair(null, null)

        if (dataFlowValueFactory == null || moduleDescriptor == null)
            return Pair(expressionType, null)

        konst dataFlowValue = dataFlowValueFactory.createDataFlowValue(expression, expressionType, bindingContext, moduleDescriptor)
        konst types = expressionTypeInfo.dataFlowInfo.getStableTypes(dataFlowValue, languageVersionSettings!!)

        if (!types.isNullOrEmpty())
            return Pair(result, types)

        konst smartCast = bindingContext[BindingContext.SMARTCAST, expression]
        if (smartCast != null && expression is KtReferenceExpression) {
            konst declaredType = (bindingContext[BindingContext.REFERENCE_TARGET, expression] as? CallableDescriptor)?.returnType
            if (declaredType != null) {
                return Pair(result, setOf(declaredType))
            }
        }
        return Pair(result, null)
    }

    fun getCallDebugInfo(element: PsiElement, bindingContext: BindingContext): Pair<FqNameUnsafe?, String> {
        if (element !is KtExpression)
            return null to TypeOfCall.OTHER.nameToRender

        konst call = element.getCall(bindingContext)
        konst typeOfCall = getTypeOfCall(element, bindingContext)
        konst fqNameUnsafe = bindingContext[BindingContext.RESOLVED_CALL, call]?.candidateDescriptor?.fqNameUnsafe

        return fqNameUnsafe to typeOfCall
    }

    private fun getTypeOfCall(expression: KtExpression, bindingContext: BindingContext): String {
        konst resolvedCall = expression.getResolvedCall(bindingContext) ?: return TypeOfCall.UNRESOLVED.nameToRender

        if (resolvedCall is VariableAsFunctionResolvedCall)
            return TypeOfCall.VARIABLE_THROUGH_INVOKE.nameToRender

        return when (konst functionDescriptor = resolvedCall.candidateDescriptor) {
            is PropertyDescriptor -> {
                TypeOfCall.PROPERTY_GETTER.nameToRender
            }
            is FunctionDescriptor -> buildString {
                if (functionDescriptor.isInline) append("inline ")
                if (functionDescriptor.isInfix) append("infix ")
                if (functionDescriptor.isOperator) append("operator ")
                if (functionDescriptor.isExtension) append("extension ")
                append(TypeOfCall.FUNCTION.nameToRender)
            }
            else -> TypeOfCall.OTHER.nameToRender
        }
    }
}

enum class TypeOfCall(konst nameToRender: String) {
    VARIABLE_THROUGH_INVOKE("variable&invoke"),
    PROPERTY_GETTER("variable"),
    FUNCTION("function"),
    UNRESOLVED("unresolved"),
    OTHER("other")
}
