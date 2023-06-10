/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.symbols

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.api.*
import org.jetbrains.kotlin.analysis.api.annotations.*
import org.jetbrains.kotlin.analysis.api.base.KtContextReceiver
import org.jetbrains.kotlin.analysis.api.components.KtSymbolContainingDeclarationProviderMixIn
import org.jetbrains.kotlin.analysis.api.components.KtSymbolInfoProviderMixIn
import org.jetbrains.kotlin.analysis.api.contracts.description.Context
import org.jetbrains.kotlin.analysis.api.contracts.description.KtContractEffectDeclaration
import org.jetbrains.kotlin.analysis.api.contracts.description.renderKtContractEffectDeclaration
import org.jetbrains.kotlin.analysis.api.symbols.markers.KtNamedSymbol
import org.jetbrains.kotlin.analysis.api.symbols.markers.KtPossiblyNamedSymbol
import org.jetbrains.kotlin.analysis.api.types.KtClassErrorType
import org.jetbrains.kotlin.analysis.api.types.KtClassTypeQualifier
import org.jetbrains.kotlin.analysis.api.types.KtNonErrorClassType
import org.jetbrains.kotlin.analysis.api.types.KtType
import org.jetbrains.kotlin.analysis.project.structure.KtModule
import org.jetbrains.kotlin.analysis.utils.printer.PrettyPrinter
import org.jetbrains.kotlin.analysis.utils.printer.prettyPrint
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.renderer.render
import org.jetbrains.kotlin.resolve.deprecation.DeprecationInfo
import org.jetbrains.kotlin.types.Variance
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.KVisibility
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible

public class DebugSymbolRenderer(
    public konst renderExtra: Boolean = false,
    public konst renderTypeByProperties: Boolean = false,
    public konst renderExpandedTypes: Boolean = false,
) {

    context(KtAnalysisSession)
    public fun render(symbol: KtSymbol): String = prettyPrint { renderSymbol(symbol) }

    context(KtAnalysisSession)
    public fun renderAnnotationApplication(application: KtAnnotationApplication): String =
        prettyPrint { renderAnnotationApplication(application) }

    context(KtAnalysisSession)
    public fun renderType(type: KtType): String = prettyPrint { renderType(type) }

    context(KtAnalysisSession)
    private fun PrettyPrinter.renderSymbol(symbol: KtSymbol) {
        renderSymbolInternals(symbol)

        if (!renderExtra) return
        withIndent {
            @Suppress("DEPRECATION")
            (symbol as? KtCallableSymbol)?.getDispatchReceiverType()?.let { dispatchType ->
                appendLine().append("getDispatchReceiver()").append(": ")
                renderType(dispatchType)
            }

            KtSymbolContainingDeclarationProviderMixIn::class
                .declaredMemberExtensionFunctions
                .filter { it.name == "getContainingModule" }
                .forEach {
                    appendLine()
                    renderFunction(it, renderSymbolsFully = false, this@KtAnalysisSession, symbol)
                }

            KtSymbolInfoProviderMixIn::class.declaredMemberExtensionProperties
                .asSequence()
                .filter { (it.extensionReceiverParameter?.type?.classifier as? KClass<*>)?.isInstance(symbol) == true }
                .forEach {
                    appendLine()
                    renderProperty(it, renderSymbolsFully = false, this@KtAnalysisSession, symbol)
                }
        }
    }

    public fun KtAnalysisSession.renderForSubstitutionOverrideUnwrappingTest(symbol: KtSymbol): String = prettyPrint {
        if (symbol !is KtCallableSymbol) return@prettyPrint

        renderFrontendIndependentKClassNameOf(symbol)

        withIndent {
            appendLine()
            renderProperty(KtCallableSymbol::callableIdIfNonLocal, renderSymbolsFully = false, symbol)
            if (symbol is KtNamedSymbol) {
                appendLine()
                renderProperty(KtNamedSymbol::name, renderSymbolsFully = false, symbol)
            }
            appendLine()
            renderProperty(KtCallableSymbol::origin, renderSymbolsFully = false, symbol)
        }
    }

    context(KtAnalysisSession)
    private fun PrettyPrinter.renderFunction(function: KFunction<*>, renderSymbolsFully: Boolean, vararg args: Any) {
        append(function.name).append(": ")
        renderFunctionCall(function, renderSymbolsFully, args)
    }

    context(KtAnalysisSession)
    private fun PrettyPrinter.renderProperty(property: KProperty<*>, renderSymbolsFully: Boolean, vararg args: Any) {
        append(property.name).append(": ")
        renderFunctionCall(property.getter, renderSymbolsFully, args)
    }

    context(KtAnalysisSession)
    private fun PrettyPrinter.renderFunctionCall(function: KFunction<*>, renderSymbolsFully: Boolean, args: Array<out Any>) {
        try {
            function.isAccessible = true
            renderValue(function.call(*args), renderSymbolsFully)
        } catch (e: InvocationTargetException) {
            append("Could not render due to ").appendLine(e.cause.toString())
        }
    }

    context(KtAnalysisSession)
    private fun PrettyPrinter.renderSymbolInternals(symbol: KtSymbol) {
        renderFrontendIndependentKClassNameOf(symbol)
        konst apiClass = getFrontendIndependentKClassOf(symbol)
        withIndent {
            konst members = apiClass.members
                .filterIsInstance<KProperty<*>>()
                .filter { it.name !in ignoredPropertyNames }
                .sortedBy { it.name }
            appendLine()
            printCollectionIfNotEmpty(members, separator = "\n") { member ->
                renderProperty(
                    member,
                    renderSymbolsFully = member.name == KtValueParameterSymbol::generatedPrimaryConstructorProperty.name,
                    symbol
                )
            }
        }
    }

    context(KtAnalysisSession)
    private fun PrettyPrinter.renderFrontendIndependentKClassNameOf(instanceOfClassToRender: Any) {
        konst apiClass = getFrontendIndependentKClassOf(instanceOfClassToRender)
        append(apiClass.simpleName).append(':')
    }

    context(KtAnalysisSession)
    private fun PrettyPrinter.renderList(konstues: List<*>, renderSymbolsFully: Boolean) {
        if (konstues.isEmpty()) {
            append("[]")
            return
        }

        withIndentInSquareBrackets {
            printCollection(konstues, separator = "\n") { renderValue(it, renderSymbolsFully) }
        }
    }

    context(KtAnalysisSession)
    private fun PrettyPrinter.renderSymbolTag(symbol: KtSymbol, renderSymbolsFully: Boolean) {
        fun renderId(id: Any?, symbol: KtSymbol) {
            if (id != null) {
                renderValue(id, renderSymbolsFully)
            } else {
                konst outerName = (symbol as? KtPossiblyNamedSymbol)?.name ?: SpecialNames.NO_NAME_PROVIDED
                append("<local>/" + outerName.asString())
            }
        }

        if (renderSymbolsFully || symbol is KtBackingFieldSymbol ||
            symbol is KtPropertyGetterSymbol || symbol is KtPropertySetterSymbol ||
            symbol is KtValueParameterSymbol || symbol is KtReceiverParameterSymbol
        ) {
            renderSymbol(symbol)
            return
        }

        append(getFrontendIndependentKClassOf(symbol).simpleName)
        append("(")
        when (symbol) {
            is KtClassLikeSymbol -> renderId(symbol.classIdIfNonLocal, symbol)
            is KtCallableSymbol -> renderId(symbol.callableIdIfNonLocal, symbol)
            is KtNamedSymbol -> renderValue(symbol.name, renderSymbolsFully = false)
            else -> error("Unsupported symbol ${symbol::class.java.name}")
        }
        append(")")
    }

    context(KtAnalysisSession)
    private fun PrettyPrinter.renderAnnotationValue(konstue: KtAnnotationValue) {
        append(KtAnnotationValueRenderer.render(konstue))
    }

    context(KtAnalysisSession)
    private fun PrettyPrinter.renderNamedConstantValue(konstue: KtNamedAnnotationValue) {
        append(konstue.name.render()).append(" = ")
        renderValue(konstue.expression, renderSymbolsFully = false)
    }

    context(KtAnalysisSession)
    private fun PrettyPrinter.renderType(type: KtType) {
        konst typeToRender = if (renderExpandedTypes) type.fullyExpandedType else type
        if (renderTypeByProperties) {
            renderByPropertyNames(typeToRender)
            return
        }

        renderFrontendIndependentKClassNameOf(typeToRender)
        withIndent {
            appendLine()
            append("annotationsList: ")
            renderAnnotationsList(typeToRender.annotationsList)

            if (typeToRender is KtNonErrorClassType) {
                appendLine()
                append("ownTypeArguments: ")
                renderList(typeToRender.ownTypeArguments, renderSymbolsFully = false)
            }

            appendLine()
            append("type: ")
            when (typeToRender) {
                is KtClassErrorType -> append("ERROR_TYPE")
                else -> append(typeToRender.asStringForDebugging())
            }
        }
    }

    context(KtAnalysisSession)
    private fun PrettyPrinter.renderByPropertyNames(konstue: Any) {
        konst members = konstue::class.members
            .filter { it.name !in ignoredPropertyNames }
            .filter { it.visibility != KVisibility.PRIVATE && it.visibility != KVisibility.INTERNAL }
            .sortedBy { it.name }
            .filterIsInstance<KProperty<*>>()
        printCollectionIfNotEmpty(members, separator = "\n") { member ->
            renderProperty(
                member,
                renderSymbolsFully = false,
                konstue
            )
        }
    }

    context(KtAnalysisSession)
    private fun PrettyPrinter.renderAnnotationApplication(call: KtAnnotationApplication) {
        renderValue(call.classId, renderSymbolsFully = false)
        append('(')
        if (call is KtAnnotationApplicationWithArgumentsInfo) {
            call.arguments.sortedBy { it.name }.forEachIndexed { index, konstue ->
                if (index > 0) {
                    append(", ")
                }
                renderValue(konstue, renderSymbolsFully = false)
            }
        } else {
            append("isCallWithArguments=${call.isCallWithArguments}")
        }
        append(')')

        withIndent {
            appendLine().append("psi: ")
            konst psi =
                if (call.psi?.containingKtFile?.isCompiled == true) {
                    null
                } else call.psi
            renderValue(psi?.javaClass?.simpleName, renderSymbolsFully = false)
        }
    }

    private fun PrettyPrinter.renderDeprecationInfo(info: DeprecationInfo) {
        append("DeprecationInfo(")
        append("deprecationLevel=${info.deprecationLevel}, ")
        append("propagatesToOverrides=${info.propagatesToOverrides}, ")
        append("message=${info.message}")
        append(")")
    }

    context(KtAnalysisSession)
    private fun PrettyPrinter.renderValue(konstue: Any?, renderSymbolsFully: Boolean) {
        when (konstue) {
            // Symbol-related konstues
            is KtSymbol -> renderSymbolTag(konstue, renderSymbolsFully)
            is KtType -> renderType(konstue)
            is KtTypeProjection -> renderTypeProjection(konstue)
            is KtClassTypeQualifier -> renderTypeQualifier(konstue)
            is KtAnnotationValue -> renderAnnotationValue(konstue)
            is KtContractEffectDeclaration -> Context(this@KtAnalysisSession, this@renderValue, this@DebugSymbolRenderer)
                .renderKtContractEffectDeclaration(konstue, endWithNewLine = false)
            is KtNamedAnnotationValue -> renderNamedConstantValue(konstue)
            is KtInitializerValue -> renderKtInitializerValue(konstue)
            is KtContextReceiver -> renderContextReceiver(konstue)
            is KtAnnotationApplication -> renderAnnotationApplication(konstue)
            is KtAnnotationsList -> renderAnnotationsList(konstue)
            is KtModule -> renderKtModule(konstue)
            // Other custom konstues
            is Name -> append(konstue.asString())
            is FqName -> append(konstue.asString())
            is ClassId -> append(konstue.asString())
            is DeprecationInfo -> renderDeprecationInfo(konstue)
            is Visibility -> append(konstue::class.java.simpleName)
            // Unsigned integers
            is UByte -> append(konstue.toString())
            is UShort -> append(konstue.toString())
            is UInt -> append(konstue.toString())
            is ULong -> append(konstue.toString())
            // Java konstues
            is Enum<*> -> append(konstue.name)
            is List<*> -> renderList(konstue, renderSymbolsFully = false)
            else -> append(konstue.toString())
        }
    }

    context(KtAnalysisSession)
    private fun PrettyPrinter.renderTypeProjection(konstue: KtTypeProjection) {
        when (konstue) {
            is KtStarTypeProjection -> append("*")
            is KtTypeArgumentWithVariance -> {
                if (konstue.variance != Variance.INVARIANT) {
                    append("${konstue.variance.label} ")
                }
                renderType(konstue.type)
            }
        }
    }

    context(KtAnalysisSession)
    private fun PrettyPrinter.renderTypeQualifier(konstue: KtClassTypeQualifier) {
        appendLine("qualifier:")
        withIndent {
            renderByPropertyNames(konstue)
        }
    }

    context(KtAnalysisSession)
    private fun PrettyPrinter.renderContextReceiver(receiver: KtContextReceiver) {
        append("KtContextReceiver:")
        withIndent {
            appendLine()
            append("label: ")
            renderValue(receiver.label, renderSymbolsFully = false)
            appendLine()
            append("type: ")
            renderType(receiver.type)
        }
    }

    context(KtAnalysisSession)
    private fun PrettyPrinter.renderKtModule(ktModule: KtModule) {
        konst ktModuleClass = ktModule::class.allSuperclasses.first { it in ktModuleSubclasses }
        append("${ktModuleClass.simpleName} \"${ktModule.moduleDescription}\"")
    }

    private fun KClass<*>.allSealedSubClasses(): List<KClass<*>> = buildList {
        add(this@allSealedSubClasses)
        sealedSubclasses.flatMapTo(this) { it.allSealedSubClasses() }
    }

    private konst ktModuleSubclasses = KtModule::class.allSealedSubClasses().distinct().sortedWith { a, b ->
        when {
            a == b -> 0
            a.isSubclassOf(b) -> -1
            b.isSubclassOf(a) -> 1
            else -> 0
        }
    }

    context(KtAnalysisSession)
    private fun PrettyPrinter.renderKtInitializerValue(konstue: KtInitializerValue) {
        when (konstue) {
            is KtConstantInitializerValue -> {
                append("KtConstantInitializerValue(")
                append(konstue.constant.renderAsKotlinConstant())
                append(")")
            }

            is KtNonConstantInitializerValue -> {
                append("KtNonConstantInitializerValue(")
                append(konstue.initializerPsi?.firstLineOfPsi() ?: "NO_PSI")
                append(")")
            }

            is KtConstantValueForAnnotation -> {
                append("KtConstantValueForAnnotation(")
                append(konstue.annotationValue.renderAsSourceCode())
                append(")")
            }
        }
    }

    context(KtAnalysisSession)
    private fun PrettyPrinter.renderAnnotationsList(konstue: KtAnnotationsList) {
        renderList(konstue.annotations, renderSymbolsFully = false)
    }

    private fun getFrontendIndependentKClassOf(instanceOfClass: Any): KClass<*> {
        var current: Class<*> = instanceOfClass.javaClass

        while (true) {
            konst className = current.name
            if (symbolImplementationPackageNames.none { className.startsWith("$it.") }) {
                return current.kotlin
            }
            current = current.superclass
        }
    }

    context(KtAnalysisSession)
    private fun PsiElement.firstLineOfPsi(): String {
        konst text = text
        konst lines = text.lines()
        return if (lines.count() <= 1) text
        else lines.first() + " ..."
    }

    private konst ignoredPropertyNames = setOf(
        "psi",
        "token",
        "builder",
        "coneType",
        "analysisContext",
        "fe10Type"
    )

    private konst symbolImplementationPackageNames = listOf(
        "org.jetbrains.kotlin.analysis.api.fir",
        "org.jetbrains.kotlin.analysis.api.descriptors",
    )
}
