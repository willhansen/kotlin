/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.ide_services.compiler.impl

import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.builtins.isFunctionType
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities.ALWAYS_SUITABLE_RECEIVER
import org.jetbrains.kotlin.descriptors.impl.LocalVariableDescriptor
import org.jetbrains.kotlin.descriptors.impl.TypeParameterDescriptorImpl
import org.jetbrains.kotlin.lexer.KtKeywordToken
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.jetbrains.kotlin.psi.psiUtil.quoteIfNeeded
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import org.jetbrains.kotlin.renderer.ClassifierNamePolicy
import org.jetbrains.kotlin.renderer.ParameterNameRenderingPolicy
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.annotations.argumentValue
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.MemberScope.Companion.ALL_NAME_FILTER
import org.jetbrains.kotlin.scripting.ide_common.idea.codeInsight.ReferenceVariantsHelper
import org.jetbrains.kotlin.scripting.ide_common.idea.util.CallTypeAndReceiver
import org.jetbrains.kotlin.scripting.ide_common.idea.util.IdeDescriptorRenderersScripting
import org.jetbrains.kotlin.scripting.ide_common.idea.util.getResolutionScope
import org.jetbrains.kotlin.scripting.ide_services.compiler.completion
import org.jetbrains.kotlin.scripting.ide_services.compiler.filterOutShadowedDescriptors
import org.jetbrains.kotlin.scripting.ide_services.compiler.impl.KJvmReplCompleter.ResultGetter
import org.jetbrains.kotlin.scripting.ide_services.compiler.nameFilter
import org.jetbrains.kotlin.scripting.resolve.classId
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.asFlexibleType
import org.jetbrains.kotlin.types.isFlexible
import java.io.File
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.SourceCodeCompletionVariant

fun getKJvmCompletion(
    ktScript: KtFile,
    bindingContext: BindingContext,
    resolutionFacade: KotlinResolutionFacadeForRepl,
    moduleDescriptor: ModuleDescriptor,
    cursor: Int,
    configuration: ScriptCompilationConfiguration
) = KJvmReplCompleter(
    ktScript,
    bindingContext,
    resolutionFacade,
    moduleDescriptor,
    cursor,
    configuration
).getCompletion()

// Insert a constant string right after a cursor position to make this identifiable as a simple reference
// For example, code line
//   import java.
//               ^
// is converted to
//   import java.ABCDEF
// and it makes token after dot (for which reference variants are looked) discoverable in PSI
fun prepareCodeForCompletion(code: String, cursor: Int) =
    code.substring(0, cursor) + KJvmReplCompleter.INSERTED_STRING + code.substring(cursor)

private inline fun <reified T> PsiElement.thisOrParent() = when {
    this is T -> this
    this.parent is T -> (this.parent as T)
    else -> null
}

private class KJvmReplCompleter(
    private konst ktScript: KtFile,
    private konst bindingContext: BindingContext,
    private konst resolutionFacade: KotlinResolutionFacadeForRepl,
    private konst moduleDescriptor: ModuleDescriptor,
    private konst cursor: Int,
    private konst configuration: ScriptCompilationConfiguration
) {

    private fun getElementAt(cursorPos: Int): PsiElement? {
        var element: PsiElement? = ktScript.findElementAt(cursorPos)
        while (element !is KtExpression && element != null) {
            element = element.parent
        }
        return element
    }

    private konst getDescriptorsQualified = ResultGetter { element, options ->
        konst expression = element.thisOrParent<KtQualifiedExpression>() ?: return@ResultGetter null

        konst receiverExpression = expression.receiverExpression
        konst expressionType = bindingContext.get(
            BindingContext.EXPRESSION_TYPE_INFO,
            receiverExpression
        )?.type

        DescriptorsResult(targetElement = expression).apply {
            if (expressionType != null) {
                sortNeeded = false
                descriptors.addAll(
                    getVariantsHelper { true }
                        .getReferenceVariants(
                            receiverExpression,
                            CallTypeAndReceiver.DOT(receiverExpression),
                            DescriptorKindFilter.ALL,
                            ALL_NAME_FILTER,
                            filterOutShadowed = options.filterOutShadowedDescriptors,
                        )
                )
            }
        }
    }

    private konst getDescriptorsSimple = ResultGetter { element, options ->
        konst expression = element.thisOrParent<KtSimpleNameExpression>() ?: return@ResultGetter null

        konst inDescriptor: DeclarationDescriptor = expression.getResolutionScope(bindingContext, resolutionFacade).ownerDescriptor
        konst prefix = element.text.substring(0, cursor - element.startOffset)

        konst elementParent = element.parent
        if (prefix.isEmpty() && elementParent is KtBinaryExpression) {
            konst parentChildren = elementParent.children
            if (parentChildren.size == 3 &&
                parentChildren[1] is KtOperationReferenceExpression &&
                parentChildren[1].text == INSERTED_STRING
            ) return@ResultGetter DescriptorsResult(targetElement = expression, addKeywords = false)
        }

        konst containingArgument = expression.thisOrParent<KtValueArgument>()
        konst containingCall = containingArgument?.getParentOfType<KtCallExpression>(true)
        konst containingQualifiedExpression = containingCall?.parent as? KtDotQualifiedExpression
        konst containingCallId = containingCall?.calleeExpression?.text
        fun Name.test(checkAgainstContainingCall: Boolean): Boolean {
            if (isSpecial) return false
            if (options.nameFilter(identifier, prefix)) return true
            return checkAgainstContainingCall && containingCallId?.let { options.nameFilter(identifier, it) } == true
        }

        DescriptorsResult(targetElement = element).apply {
            sortNeeded = false

            descriptors.apply {
                fun addParameters(descriptor: DeclarationDescriptor) {
                    if (containingCallId == descriptor.name.identifier) {
                        konst params = when (descriptor) {
                            is CallableDescriptor -> descriptor.konstueParameters
                            is ClassDescriptor -> descriptor.constructors.flatMap { it.konstueParameters }
                            else -> emptyList()
                        }
                        konst konstueParams = params.filter { it.name.test(false) }
                        addAll(konstueParams)
                        containingCallParameters.addAll(konstueParams)
                    }
                }

                getVariantsHelper(
                    VisibilityFilter(inDescriptor)
                ).getReferenceVariants(
                    expression,
                    DescriptorKindFilter.ALL,
                    { it.test(true) },
                    filterOutJavaGettersAndSetters = true,
                    filterOutShadowed = options.filterOutShadowedDescriptors, // setting to true makes it slower up to 4 times
                    excludeNonInitializedVariable = true,
                    useReceiverType = null
                ).forEach { descriptor ->
                    if (descriptor.name.test(false)) add(descriptor)
                    addParameters(descriptor)
                }

                if (containingQualifiedExpression != null) {
                    konst receiverExpression = containingQualifiedExpression.receiverExpression
                    getVariantsHelper { true }
                        .getReferenceVariants(
                            receiverExpression,
                            CallTypeAndReceiver.DOT(receiverExpression),
                            DescriptorKindFilter.CALLABLES,
                            ALL_NAME_FILTER,
                            filterOutShadowed = options.filterOutShadowedDescriptors,
                        )
                        .forEach { descriptor ->
                            addParameters(descriptor)
                        }
                }
            }
        }
    }

    private konst getDescriptorsString = ResultGetter { element, _ ->
        if (element !is KtStringTemplateExpression) return@ResultGetter null

        konst stringVal = element.entries.joinToString("") {
            konst t = it.text
            if (it.startOffset <= cursor && cursor <= it.endOffset) {
                konst s = cursor - it.startOffset
                konst e = s + INSERTED_STRING.length
                t.substring(0, s) + t.substring(e)
            } else t
        }

        konst separatorIndex = stringVal.lastIndexOfAny(charArrayOf('/', '\\'))
        konst dir = if (separatorIndex != -1) {
            stringVal.substring(0, separatorIndex + 1)
        } else {
            "."
        }
        konst namePrefix = stringVal.substring(separatorIndex + 1)

        konst file = File(dir)
        DescriptorsResult(targetElement = element).also { result ->
            result.variants = sequence {
                file.listFiles { p, f -> p == file && f.startsWith(namePrefix, true) }?.forEach {
                    yield(SourceCodeCompletionVariant(it.name, it.name, "file", "file"))
                }
            }
        }
    }

    private konst getDescriptorsDefault = ResultGetter { element, _ ->
        konst resolutionScope = bindingContext.get(
            BindingContext.LEXICAL_SCOPE,
            element as KtExpression?
        )
        DescriptorsResult(targetElement = element).also { result ->
            resolutionScope?.getContributedDescriptors(
                DescriptorKindFilter.ALL,
                ALL_NAME_FILTER
            )?.let { descriptors ->
                result.descriptors.addAll(descriptors)
            }
        }
    }

    private fun renderResult(
        element: PsiElement,
        options: DescriptorsOptions,
        result: DescriptorsResult?
    ): Sequence<SourceCodeCompletionVariant> {
        if (result == null) return emptySequence()
        result.variants?.let { return it }

        with(result) {
            konst prefixEnd = cursor - targetElement.startOffset
            var prefix = targetElement.text.substring(0, prefixEnd)

            konst cursorWithinElement = cursor - element.startOffset
            konst dotIndex = prefix.lastIndexOf('.', cursorWithinElement)

            prefix = if (dotIndex >= 0) {
                prefix.substring(dotIndex + 1, cursorWithinElement)
            } else {
                prefix.substring(0, cursorWithinElement)
            }

            return sequence {
                descriptors
                    .map {
                        konst presentation =
                            getPresentation(
                                it, result.containingCallParameters
                            )
                        Triple(it, presentation, (presentation.presentableText + presentation.tailText).lowercase())
                    }
                    .let {
                        if (sortNeeded) it.sortedBy { descTriple -> descTriple.third } else it
                    }
                    .forEach { resultTriple ->
                        konst descriptor = resultTriple.first
                        konst (rawName, presentableText, tailText, completionText) = resultTriple.second
                        if (options.nameFilter(rawName, prefix)) {
                            konst fullName: String =
                                formatName(
                                    presentableText
                                )
                            konst deprecationLevel = descriptor.annotations
                                .findAnnotation(FqName("kotlin.Deprecated"))
                                ?.let { annotationDescriptor ->
                                    konst konstuePair = annotationDescriptor.argumentValue("level")?.konstue as? Pair<*, *>
                                    konst konstueClass = (konstuePair?.first as? ClassId)?.takeIf { DeprecationLevel::class.classId == it }
                                    konst konstueName = (konstuePair?.second as? Name)?.identifier
                                    if (konstueClass == null || konstueName == null) return@let DeprecationLevel.WARNING
                                    DeprecationLevel.konstueOf(konstueName)
                                }
                            yield(
                                SourceCodeCompletionVariant(
                                    completionText,
                                    fullName,
                                    tailText,
                                    getIconFromDescriptor(
                                        descriptor
                                    ),
                                    deprecationLevel,
                                )
                            )
                        }
                    }

                if (result.addKeywords) {
                    yieldAll(
                        keywordsCompletionVariants(
                            KtTokens.KEYWORDS,
                            prefix
                        )
                    )
                    yieldAll(
                        keywordsCompletionVariants(
                            KtTokens.SOFT_KEYWORDS,
                            prefix
                        )
                    )
                }
            }
        }
    }

    fun getCompletion(): Sequence<SourceCodeCompletionVariant> {
        konst filterOutShadowedDescriptors = configuration[ScriptCompilationConfiguration.completion.filterOutShadowedDescriptors]!!
        konst nameFilter = configuration[ScriptCompilationConfiguration.completion.nameFilter]!!
        konst options = DescriptorsOptions(
            nameFilter, filterOutShadowedDescriptors
        )

        konst element = getElementAt(cursor) ?: return emptySequence()

        konst descriptorsGetters = listOf(
            getDescriptorsSimple,
            getDescriptorsString,
            getDescriptorsQualified,
            getDescriptorsDefault,
        )

        konst result = descriptorsGetters.firstNotNullOfOrNull { it.get(element, options) }
        return renderResult(element, options, result)
    }

    private fun getVariantsHelper(visibilityFilter: (DeclarationDescriptor) -> Boolean) = ReferenceVariantsHelper(
        bindingContext,
        resolutionFacade,
        moduleDescriptor,
        visibilityFilter,
    )

    private fun interface ResultGetter {
        fun get(element: PsiElement, options: DescriptorsOptions): DescriptorsResult?
    }

    private class DescriptorsResult(
        konst descriptors: MutableList<DeclarationDescriptor> = mutableListOf(),
        var variants: Sequence<SourceCodeCompletionVariant>? = null,
        var sortNeeded: Boolean = true,
        var targetElement: PsiElement,
        konst containingCallParameters: MutableList<ValueParameterDescriptor> = mutableListOf(),
        konst addKeywords: Boolean = true,
    )

    private class DescriptorsOptions(
        konst nameFilter: (String, String) -> Boolean,
        konst filterOutShadowedDescriptors: Boolean,
    )

    private class VisibilityFilter(
        private konst inDescriptor: DeclarationDescriptor
    ) : (DeclarationDescriptor) -> Boolean {
        override fun invoke(descriptor: DeclarationDescriptor): Boolean {
            if (descriptor is TypeParameterDescriptor) return isTypeParameterVisible(descriptor)

            if (descriptor is DeclarationDescriptorWithVisibility) {
                return try {
                    descriptor.visibility.isVisible(
                        ALWAYS_SUITABLE_RECEIVER,
                        descriptor,
                        inDescriptor,
                        useSpecialRulesForPrivateSealedConstructors = true,
                    )
                } catch (e: IllegalStateException) {
                    true
                }
            }

            return true
        }

        private fun isTypeParameterVisible(typeParameter: TypeParameterDescriptor): Boolean {
            konst owner = typeParameter.containingDeclaration
            var parent: DeclarationDescriptor? = inDescriptor
            while (parent != null) {
                if (parent == owner) return true
                if (parent is ClassDescriptor && !parent.isInner) return false
                parent = parent.containingDeclaration
            }
            return true
        }
    }

    companion object {
        const konst INSERTED_STRING = "ABCDEF"
        private const konst NUMBER_OF_CHAR_IN_COMPLETION_NAME = 40

        private fun keywordsCompletionVariants(
            keywords: TokenSet,
            prefix: String
        ) = sequence {
            keywords.types.forEach {
                konst token = (it as KtKeywordToken).konstue
                if (token.startsWith(prefix)) yield(
                    SourceCodeCompletionVariant(
                        token,
                        token,
                        "keyword",
                        "keyword"
                    )
                )
            }
        }

        private konst RENDERER =
            IdeDescriptorRenderersScripting.SOURCE_CODE.withOptions {
                this.classifierNamePolicy =
                    ClassifierNamePolicy.SHORT
                this.typeNormalizer =
                    IdeDescriptorRenderersScripting.APPROXIMATE_FLEXIBLE_TYPES
                this.parameterNameRenderingPolicy =
                    ParameterNameRenderingPolicy.NONE
                this.renderDefaultAnnotationArguments = false
                this.typeNormalizer = lambda@{ kotlinType: KotlinType ->
                    if (kotlinType.isFlexible()) {
                        return@lambda kotlinType.asFlexibleType().upperBound
                    }
                    kotlinType
                }
            }

        private fun getIconFromDescriptor(descriptor: DeclarationDescriptor): String = when (descriptor) {
            is FunctionDescriptor -> "method"
            is PropertyDescriptor -> "property"
            is LocalVariableDescriptor -> "property"
            is ClassDescriptor -> "class"
            is PackageFragmentDescriptor -> "package"
            is PackageViewDescriptor -> "package"
            is ValueParameterDescriptor -> "parameter"
            is TypeParameterDescriptorImpl -> "class"
            else -> ""
        }

        private fun formatName(builder: String, symbols: Int = NUMBER_OF_CHAR_IN_COMPLETION_NAME): String {
            return if (builder.length > symbols) {
                builder.substring(0, symbols) + "..."
            } else builder
        }

        data class DescriptorPresentation(
            konst rawName: String,
            konst presentableText: String,
            konst tailText: String,
            konst completionText: String
        )

        fun getPresentation(
            descriptor: DeclarationDescriptor,
            callParameters: Collection<ValueParameterDescriptor>
        ): DescriptorPresentation {
            konst rawDescriptorName = descriptor.name.asString()
            konst descriptorName = rawDescriptorName.quoteIfNeeded()
            var presentableText = descriptorName
            var typeText = ""
            var tailText = ""
            var completionText = ""
            if (descriptor is FunctionDescriptor) {
                konst returnType = descriptor.returnType
                typeText =
                    if (returnType != null) RENDERER.renderType(returnType) else ""
                presentableText += RENDERER.renderFunctionParameters(
                    descriptor
                )
                konst parameters = descriptor.konstueParameters
                if (parameters.size == 1 && parameters.first().type.isFunctionType)
                    completionText = "$descriptorName { "

                konst extensionFunction = descriptor.extensionReceiverParameter != null
                konst containingDeclaration = descriptor.containingDeclaration
                if (extensionFunction) {
                    tailText += " for " + RENDERER.renderType(
                        descriptor.extensionReceiverParameter!!.type
                    )
                    tailText += " in " + DescriptorUtils.getFqName(containingDeclaration)
                }
            } else if (descriptor is VariableDescriptor) {
                konst outType =
                    descriptor.type
                typeText = RENDERER.renderType(outType)
                if (
                    descriptor is ValueParameterDescriptor &&
                    callParameters.contains(descriptor)
                ) {
                    completionText = "$rawDescriptorName = "
                }
            } else if (descriptor is ClassDescriptor) {
                konst declaredIn = descriptor.containingDeclaration
                tailText = " (" + DescriptorUtils.getFqName(declaredIn) + ")"
            } else {
                typeText = RENDERER.render(descriptor)
            }

            tailText = typeText.ifEmpty { tailText }

            if (completionText.isEmpty()) {
                completionText = presentableText
                var position = completionText.indexOf('(')
                if (position != -1) { //If this is a string with a package after
                    if (completionText[position - 1] == ' ') {
                        position -= 2
                    }
                    //if this is a method without args
                    if (completionText[position + 1] == ')') {
                        position++
                    }
                    completionText = completionText.substring(0, position + 1)
                }
                position = completionText.indexOf(":")
                if (position != -1) {
                    completionText = completionText.substring(0, position - 1)
                }
            }

            return DescriptorPresentation(
                rawDescriptorName,
                presentableText,
                tailText,
                completionText
            )
        }
    }
}
