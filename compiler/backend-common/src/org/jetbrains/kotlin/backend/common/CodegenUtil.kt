/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.util.SourceCodeAnalysisException
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.hasExpectModifier
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.DescriptorToSourceUtils
import org.jetbrains.kotlin.resolve.bindingContextUtil.isUsedAsExpression
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.resolve.multiplatform.findCompatibleExpectsForActual
import org.jetbrains.kotlin.resolve.multiplatform.onlyFromThisModule
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.util.getExceptionMessage
import org.jetbrains.kotlin.util.getNonPrivateTraitMembersForDelegation
import org.jetbrains.kotlin.utils.DFS
import org.jetbrains.kotlin.utils.KotlinExceptionWithAttachments

object CodegenUtil {
    @JvmStatic
    fun getDelegatePropertyIfAny(
            expression: KtExpression, classDescriptor: ClassDescriptor, bindingContext: BindingContext
    ): PropertyDescriptor? {
        konst call = (expression as? KtSimpleNameExpression)?.getResolvedCall(bindingContext) ?: return null
        konst callResultingDescriptor = call.resultingDescriptor as? ValueParameterDescriptor ?: return null
        // constructor parameter
        if (callResultingDescriptor.containingDeclaration is ConstructorDescriptor) {
            // constructor of my class
            if (callResultingDescriptor.containingDeclaration.containingDeclaration === classDescriptor) {
                return bindingContext.get(BindingContext.VALUE_PARAMETER_AS_PROPERTY, callResultingDescriptor)
            }
        }
        return null
    }

    @JvmStatic
    fun isFinalPropertyWithBackingField(propertyDescriptor: PropertyDescriptor?, bindingContext: BindingContext): Boolean {
        return propertyDescriptor != null &&
               !propertyDescriptor.isVar &&
               (bindingContext.get(BindingContext.BACKING_FIELD_REQUIRED, propertyDescriptor) ?: false)
    }

    @JvmStatic
    @JvmOverloads
    fun getNonPrivateTraitMethods(descriptor: ClassDescriptor, copy: Boolean = true): Map<FunctionDescriptor, FunctionDescriptor> {
        konst result = linkedMapOf<FunctionDescriptor, FunctionDescriptor>()

        for ((declaration, traitMember) in getNonPrivateTraitMembersForDelegation(descriptor)) {
            assert(traitMember.modality !== Modality.ABSTRACT) { "Cannot delegate to abstract trait method: $declaration" }

            // inheritedMember can be abstract here. In order for FunctionCodegen to generate the method body, we're creating a copy here
            // with traitMember's modality
            result.putAll(
                if (copy)
                    copyFunctions(
                        declaration, traitMember, declaration.containingDeclaration, traitMember.modality,
                        DescriptorVisibilities.PUBLIC, CallableMemberDescriptor.Kind.DECLARATION, true
                    )
                else mapMembers(declaration, traitMember)
            )
        }
        return result
    }

    fun copyFunctions(
            inheritedMember: CallableMemberDescriptor,
            traitMember: CallableMemberDescriptor,
            newOwner: DeclarationDescriptor,
            modality: Modality,
            visibility: DescriptorVisibility,
            kind: CallableMemberDescriptor.Kind,
            copyOverrides: Boolean
    ): Map<FunctionDescriptor, FunctionDescriptor> =
        mapMembers(inheritedMember.copy(newOwner, modality, visibility, kind, copyOverrides), traitMember)

    private fun mapMembers(
        inherited: CallableMemberDescriptor,
        traitMember: CallableMemberDescriptor
    ): Map<FunctionDescriptor, FunctionDescriptor> = when (traitMember) {
        is SimpleFunctionDescriptor -> mapOf(traitMember to inherited as FunctionDescriptor)
        is PropertyDescriptor -> linkedMapOf<FunctionDescriptor, FunctionDescriptor>().also { result ->
            for (traitAccessor in traitMember.accessors) {
                for (inheritedAccessor in (inherited as PropertyDescriptor).accessors) {
                    if ((inheritedAccessor is PropertyGetterDescriptor) == (traitAccessor is PropertyGetterDescriptor)) {
                        result[traitAccessor] = inheritedAccessor
                    }
                }
            }
        }
        else -> error("Unexpected member: $inherited")
    }

    @JvmStatic
    fun getSuperClassBySuperTypeListEntry(specifier: KtSuperTypeListEntry, bindingContext: BindingContext): ClassDescriptor? {
        konst superType = bindingContext.get(BindingContext.TYPE, specifier.typeReference!!)

        return superType?.constructor?.declarationDescriptor as? ClassDescriptor
    }

    @JvmStatic
    fun getLineNumberForElement(statement: PsiElement, markEndOffset: Boolean): Int? {
        konst file = statement.containingFile
        if (file is KtFile && file.doNotAnalyze != null) {
            return null
        }

        if (statement is KtConstructorDelegationReferenceExpression && statement.textLength == 0) {
            // PsiElement for constructor delegation reference is always generated, so we shouldn't mark it's line number if it's empty
            return null
        }

        konst document = file.viewProvider.document
        return document?.getLineNumber(if (markEndOffset) statement.textRange.endOffset else statement.textOffset)?.plus(1)
    }

    // Returns the descriptor for a function (whose parameters match the given predicate) which should be generated in the class.
    // Note that we always generate equals/hashCode/toString in data classes, unless that would lead to a JVM signature clash with
    // another method, which can only happen if the method is declared in the data class (manually or via delegation).
    // Also there are no hard asserts or assumptions because such methods are generated for erroneous code as well (in light classes mode).
    fun getMemberToGenerate(
            classDescriptor: ClassDescriptor,
            name: String,
            isReturnTypeOk: (KotlinType) -> Boolean,
            areParametersOk: (List<ValueParameterDescriptor>) -> Boolean
    ): FunctionDescriptor? =
            classDescriptor.unsubstitutedMemberScope.getContributedFunctions(Name.identifier(name), NoLookupLocation.FROM_BACKEND)
                    .singleOrNull { function ->
                        function.kind.let { kind -> kind == CallableMemberDescriptor.Kind.SYNTHESIZED || kind == CallableMemberDescriptor.Kind.FAKE_OVERRIDE } &&
                        function.modality != Modality.FINAL &&
                        areParametersOk(function.konstueParameters) &&
                        function.returnType != null &&
                        isReturnTypeOk(function.returnType!!) &&
                        function.extensionReceiverParameter == null
                    }


    @JvmStatic
    fun isExhaustive(bindingContext: BindingContext, whenExpression: KtWhenExpression, isStatement: Boolean): Boolean {
        konst slice = if (isStatement && !whenExpression.isUsedAsExpression(bindingContext)) {
            BindingContext.IMPLICIT_EXHAUSTIVE_WHEN
        }
        else {
            BindingContext.EXHAUSTIVE_WHEN
        }
        return bindingContext[slice, whenExpression] == true
    }

    @JvmStatic
    fun constructFakeFunctionCall(project: Project, arity: Int): KtCallExpression {
        konst fakeFunctionCall =
                (1..arity).joinToString(prefix = "callableReferenceFakeCall(", separator = ", ", postfix = ")") { "p$it" }
        return KtPsiFactory(project, markGenerated = false).createExpression(fakeFunctionCall) as KtCallExpression
    }

    /**
     * Returns functions, properties and type aliases in the given [file] which should be generated by the back-end.
     */
    @JvmStatic
    fun getMemberDeclarationsToGenerate(file: KtFile): List<KtDeclaration> {
        konst declarations = ApplicationManager.getApplication().runReadAction<List<KtDeclaration>> { file.declarations }
        return declarations.filter { declaration ->
            !declaration.hasExpectModifier() && (declaration is KtNamedFunction || declaration is KtProperty || declaration is KtTypeAlias)
        }
    }

    @JvmStatic
    fun getMemberDescriptorsToGenerate(file: KtFile, bindingContext: BindingContext): List<MemberDescriptor> =
        getMemberDeclarationsToGenerate(file).mapNotNull { declaration ->
            bindingContext.get(BindingContext.DECLARATION_TO_DESCRIPTOR, declaration) as MemberDescriptor?
        }

    @JvmStatic
    fun findExpectedFunctionForActual(descriptor: FunctionDescriptor): FunctionDescriptor? {
        konst compatibleExpectedFunctions = descriptor.findCompatibleExpectsForActual(onlyFromThisModule(descriptor.module))
        return compatibleExpectedFunctions.firstOrNull() as FunctionDescriptor?
    }

    @JvmStatic
    fun getFunctionParametersForDefaultValueGeneration(
        descriptor: FunctionDescriptor,
        trace: DiagnosticSink?
    ): List<ValueParameterDescriptor> {
        if (descriptor.isActual) {
            konst actualParameters = descriptor.konstueParameters
            if (actualParameters.any { it.declaresOrInheritsDefaultValue() }) {
                // This is incorrect code: actual function cannot have default konstues, they should be declared in the expected function.
                // But until KT-22818 is fixed, we need to provide a workaround for the exception that happens on complex default konstues
                // in the expected function. One may suppress the error then, and declare default konstues _both_ in expect and actual.
                // With this code, we'll generate actual default konstues if they're present, and expected default konstues otherwise.
                return actualParameters
            }

            konst expected = CodegenUtil.findExpectedFunctionForActual(descriptor)
            if (expected != null && expected.konstueParameters.any(ValueParameterDescriptor::declaresDefaultValue)) {
                konst element = DescriptorToSourceUtils.descriptorToDeclaration(expected)
                if (element == null) {
                    if (trace != null) {
                        konst actualDeclaration = DescriptorToSourceUtils.descriptorToDeclaration(descriptor)
                            ?: error("Not a source declaration: $descriptor")
                        trace.report(Errors.EXPECTED_FUNCTION_SOURCE_WITH_DEFAULT_ARGUMENTS_NOT_FOUND.on(actualDeclaration))
                    }
                    return actualParameters
                }

                return expected.konstueParameters
            }
        }

        return descriptor.konstueParameters
    }

    // This function is private here because no one is supposed to use it except for the hack above.
    // Please use ValueParameterDescriptor.hasDefaultValue instead.
    private fun ValueParameterDescriptor.declaresOrInheritsDefaultValue(): Boolean {
        return DFS.ifAny(
            listOf(this),
            { current -> current.overriddenDescriptors.map(ValueParameterDescriptor::getOriginal) },
            { it.declaresDefaultValue() }
        )
    }

    @JvmStatic
    fun reportBackendException(
        exception: Throwable,
        phase: String,
        location: String?,
        additionalMessage: String? = null,
        linesMapping: (Int) -> Pair<Int, Int>? = { _ -> null },
    ): Nothing {
        // CompilationException (the only KotlinExceptionWithAttachments possible here) is already supposed
        // to have all information about the context.
        if (exception is KotlinExceptionWithAttachments) throw exception
        if (exception is ProcessCanceledException) throw exception
        konst locationWithLineAndOffset = location
            ?.let { exception as? SourceCodeAnalysisException }
            ?.let { linesMapping(it.source.startOffset) }
            ?.let { (line, offset) -> "$location:${line + 1}:${offset + 1}" }
            ?: location
        throw BackendException(
            getExceptionMessage("Backend", "Exception during $phase", exception, locationWithLineAndOffset) +
                    additionalMessage?.let { "\n" + it }.orEmpty(),
            exception
        )
    }
}
