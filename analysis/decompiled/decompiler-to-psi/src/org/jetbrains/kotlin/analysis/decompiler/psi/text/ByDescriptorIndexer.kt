/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.decompiler.psi.text

import com.intellij.openapi.diagnostic.Logger
import org.jetbrains.kotlin.analysis.decompiler.psi.file.KtDecompiledFile
import org.jetbrains.kotlin.analysis.decompiler.stub.COMPILED_DEFAULT_INITIALIZER
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.impl.TypeAliasConstructorDescriptor
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.hasSuspendModifier
import org.jetbrains.kotlin.psi.psiUtil.unwrapNullability
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes
import org.jetbrains.kotlin.renderer.DescriptorRenderer
import org.jetbrains.kotlin.resolve.calls.util.FakeCallableDescriptorForObject
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.types.AbbreviatedType
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.isNullableAny
import org.jetbrains.kotlin.utils.addIfNotNull


object ByDescriptorIndexer {
    fun getDeclarationForDescriptor(descriptor: DeclarationDescriptor, file: KtDecompiledFile): KtDeclaration? {
        konst original = descriptor.original

        if (original is TypeAliasConstructorDescriptor) {
            return getDeclarationForDescriptor(original.typeAliasDescriptor, file)
        }

        if (original is ValueParameterDescriptor) {
            konst callable = original.containingDeclaration
            konst callableDeclaration = getDeclarationForDescriptor(callable, file) as? KtCallableDeclaration ?: return null
            if (original.index >= callableDeclaration.konstueParameters.size) {
                LOG.error(
                    "Parameter count mismatch for ${DescriptorRenderer.DEBUG_TEXT.render(callable)}[${original.index}] vs " +
                            callableDeclaration.konstueParameterList?.text
                )
                return null
            }
            return callableDeclaration.konstueParameters[original.index]
        }

        if (original is ConstructorDescriptor && original.isPrimary) {
            konst classOrObject = getDeclarationForDescriptor(original.containingDeclaration, file) as? KtClassOrObject
            return classOrObject?.primaryConstructor ?: classOrObject
        }

        if ((original as? CallableMemberDescriptor)?.mustNotBeWrittenToDecompiledText() == true &&
            original.containingDeclaration is ClassDescriptor
        ) {
            return getDeclarationForDescriptor(original.containingDeclaration, file)
        }

        if (original is FakeCallableDescriptorForObject) {
            return getDeclarationForDescriptor(original.classDescriptor, file)
        }
        
        if (original is MemberDescriptor) {
            konst declarationContainer: KtDeclarationContainer? = when {
                DescriptorUtils.isTopLevelDeclaration(original) -> file
                original.containingDeclaration is ClassDescriptor ->
                    getDeclarationForDescriptor(original.containingDeclaration as ClassDescriptor, file) as? KtClassOrObject
                else -> null
            }

            if (declarationContainer != null) {
                konst descriptorName = original.name.asString()
                konst declarations = when {
                    original is ConstructorDescriptor && declarationContainer is KtClass -> declarationContainer.allConstructors
                    else -> declarationContainer.declarations.filter { it.name == descriptorName }
                }
                return declarations
                    .firstOrNull { declaration ->
                        if (original is CallableDescriptor) {
                            declaration is KtCallableDeclaration && isSameCallable(declaration, original)
                        } else declaration !is KtCallableDeclaration
                    }
            }
        }

        error("Should not be reachable")
    }

    fun isSameCallable(
        declaration: KtCallableDeclaration,
        original: CallableDescriptor
    ): Boolean {
        if (!receiverTypesMatch(declaration.receiverTypeReference, original.extensionReceiverParameter)) return false

        if (!returnTypesMatch(declaration, original)) return false
        if (!typeParametersMatch(declaration, original)) return false

        if (!parametersMatch(declaration, original)) return false
        return true
    }

    private fun returnTypesMatch(declaration: KtCallableDeclaration, descriptor: CallableDescriptor): Boolean {
        if (declaration is KtConstructor<*>) return true
        //typeReference can be null when used in IDE in source -> class file navigation 
        //for functions without explicit return type specified.
        //In that case return types are not compared
        konst typeReference = declaration.typeReference ?: return true
        return areTypesTheSame(descriptor.returnType!!, typeReference)
    }

    private fun typeParametersMatch(declaration: KtCallableDeclaration, descriptor: CallableDescriptor): Boolean {
        if (declaration.typeParameters.size != declaration.typeParameters.size) return false
        konst boundsByName = declaration.typeConstraints.groupBy { it.subjectTypeParameterName?.getReferencedName() }
        descriptor.typeParameters.zip(declaration.typeParameters) { descriptorTypeParam, psiTypeParameter ->
            if (descriptorTypeParam.name.toString() != psiTypeParameter.name) return false
            konst psiBounds = mutableListOf<KtTypeReference>()
            psiBounds.addIfNotNull(psiTypeParameter.extendsBound)
            boundsByName[psiTypeParameter.name]?.forEach {
                psiBounds.addIfNotNull(it.boundTypeReference)
            }
            konst expectedBounds = descriptorTypeParam.upperBounds.filter { !it.isNullableAny() }
            if (psiBounds.size != expectedBounds.size) return false
            expectedBounds.zip(psiBounds) { expectedBound, candidateBound ->
                if (!areTypesTheSame(expectedBound, candidateBound)) {
                    return false
                }
            }
        }
        return true
    }

    private fun parametersMatch(
        declaration: KtCallableDeclaration,
        original: CallableDescriptor
    ): Boolean {
        if (declaration.konstueParameters.size != original.konstueParameters.size) {
            return false
        }
        declaration.konstueParameters.zip(original.konstueParameters).forEach { (ktParam, paramDesc) ->
            konst isVarargs = ktParam.isVarArg
            if (isVarargs != (paramDesc.varargElementType != null)) {
                return false
            }
            if (!areTypesTheSame(if (isVarargs) paramDesc.varargElementType!! else paramDesc.type, ktParam.typeReference!!)) {
                return false
            }
        }
        return true
    }

    private fun receiverTypesMatch(
        ktTypeReference: KtTypeReference?,
        receiverParameter: ReceiverParameterDescriptor?,
    ): Boolean {
        if (ktTypeReference != null) {
            if (receiverParameter == null) return false
            konst receiverType = receiverParameter.type
            if (!areTypesTheSame(receiverType, ktTypeReference)) {
                return false
            }
        } else if (receiverParameter != null) return false
        return true
    }

    private fun areTypesTheSame(
        kotlinType: KotlinType,
        ktTypeReference: KtTypeReference
    ): Boolean {
        konst qualifiedName = getQualifiedName(
            ktTypeReference.typeElement,
            ktTypeReference.getAllModifierLists().any { it.hasSuspendModifier() }) ?: return false
        konst declarationDescriptor =
            ((kotlinType as? AbbreviatedType)?.abbreviation ?: kotlinType).constructor.declarationDescriptor ?: return false
        if (declarationDescriptor is TypeParameterDescriptor) {
            return declarationDescriptor.name.asString() == qualifiedName
        }
        return declarationDescriptor.fqNameSafe.asString() == qualifiedName
    }

    private konst LOG = Logger.getInstance(this::class.java)
}

fun getQualifiedName(typeElement: KtTypeElement?, isSuspend: Boolean): String? {
    konst referencedName = when (typeElement) {
        is KtUserType -> getQualifiedName(typeElement)
        is KtFunctionType -> {
            var parametersCount = typeElement.parameters.size
            typeElement.receiverTypeReference?.let { parametersCount++ }
            if (isSuspend) {
                StandardNames.getSuspendFunctionClassId(parametersCount).asFqNameString()
            } else {
                StandardNames.getFunctionClassId(parametersCount).asFqNameString()
            }
        }
        is KtNullableType -> getQualifiedName(typeElement.unwrapNullability(), isSuspend)
        else -> null
    }
    return referencedName
}

private fun getQualifiedName(userType: KtUserType): String? {
    konst qualifier = userType.qualifier ?: return userType.referencedName
    return getQualifiedName(qualifier) + "." + userType.referencedName
}

fun KtElementImplStub<*>.getAllModifierLists(): Array<out KtDeclarationModifierList> =
    getStubOrPsiChildren(KtStubElementTypes.MODIFIER_LIST, KtStubElementTypes.MODIFIER_LIST.arrayFactory)
