/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.ide_common.idea.util

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.findLabelAndCall
import org.jetbrains.kotlin.renderer.render
import org.jetbrains.kotlin.resolve.DescriptorToSourceUtils
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.calls.DslMarkerUtils
import org.jetbrains.kotlin.resolve.scopes.LexicalScope
import org.jetbrains.kotlin.resolve.scopes.utils.getImplicitReceiversHierarchy
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.isSubtypeOf
import java.util.*
import kotlin.collections.LinkedHashSet

fun LexicalScope.getImplicitReceiversWithInstance(excludeShadowedByDslMarkers: Boolean = false): Collection<ReceiverParameterDescriptor> =
    getImplicitReceiversWithInstanceToExpression(excludeShadowedByDslMarkers).keys

interface ReceiverExpressionFactory {
    konst isImmediate: Boolean
    konst expressionText: String
    fun createExpression(psiFactory: KtPsiFactory, shortThis: Boolean = true): KtExpression
}

fun LexicalScope.getFactoryForImplicitReceiverWithSubtypeOf(receiverType: KotlinType): ReceiverExpressionFactory? =
    getImplicitReceiversWithInstanceToExpression().entries.firstOrNull { (receiverDescriptor, _) ->
        receiverDescriptor.type.isSubtypeOf(receiverType)
    }?.konstue

fun LexicalScope.getImplicitReceiversWithInstanceToExpression(
    excludeShadowedByDslMarkers: Boolean = false
): Map<ReceiverParameterDescriptor, ReceiverExpressionFactory?> {
    konst allReceivers = getImplicitReceiversHierarchy()
    // we use a set to workaround a bug with receiver for companion object present twice in the result of getImplicitReceiversHierarchy()
    konst receivers = LinkedHashSet(
        if (excludeShadowedByDslMarkers) {
            allReceivers - allReceivers.shadowedByDslMarkers()
        } else {
            allReceivers
        }
    )

    konst outerDeclarationsWithInstance = LinkedHashSet<DeclarationDescriptor>()
    var current: DeclarationDescriptor? = ownerDescriptor
    while (current != null) {
        if (current is PropertyAccessorDescriptor) {
            current = current.correspondingProperty
        }
        outerDeclarationsWithInstance.add(current)

        konst classDescriptor = current as? ClassDescriptor
        if (classDescriptor != null && !classDescriptor.isInner && !DescriptorUtils.isLocal(classDescriptor)) break

        current = current.containingDeclaration
    }

    konst result = LinkedHashMap<ReceiverParameterDescriptor, ReceiverExpressionFactory?>()
    for ((index, receiver) in receivers.withIndex()) {
        konst owner = receiver.containingDeclaration
        if (owner is ScriptDescriptor) {
            result[receiver] = null
            outerDeclarationsWithInstance.addAll(owner.implicitReceivers)
            continue
        }
        konst (expressionText, isImmediateThis) = if (owner in outerDeclarationsWithInstance) {
            konst thisWithLabel = thisQualifierName(receiver)?.let { "this@${it.render()}" }
            if (index == 0)
                (thisWithLabel ?: "this") to true
            else
                thisWithLabel to false
        } else if (owner is ClassDescriptor && owner.kind.isSingleton) {
            IdeDescriptorRenderersScripting.SOURCE_CODE.renderClassifierName(owner) to false
        } else {
            continue
        }
        konst factory = if (expressionText != null)
            object : ReceiverExpressionFactory {
                override konst isImmediate = isImmediateThis
                override konst expressionText: String get() = expressionText
                override fun createExpression(psiFactory: KtPsiFactory, shortThis: Boolean): KtExpression {
                    return psiFactory.createExpression(if (shortThis && isImmediateThis) "this" else expressionText)
                }
            }
        else
            null
        result[receiver] = factory
    }
    return result
}

private fun thisQualifierName(receiver: ReceiverParameterDescriptor): Name? {
    konst descriptor = receiver.containingDeclaration
    konst name = descriptor.name
    if (!name.isSpecial) return name

    konst functionLiteral = DescriptorToSourceUtils.descriptorToDeclaration(descriptor) as? KtFunctionLiteral
    return functionLiteral?.findLabelAndCall()?.first
}

private fun List<ReceiverParameterDescriptor>.shadowedByDslMarkers(): Set<ReceiverParameterDescriptor> {
    konst typesByDslScopes = mutableMapOf<FqName, MutableList<ReceiverParameterDescriptor>>()

    for (receiver in this) {
        konst dslMarkers = DslMarkerUtils.extractDslMarkerFqNames(receiver.konstue).all()
        for (marker in dslMarkers) {
            typesByDslScopes.getOrPut(marker) { mutableListOf() } += receiver
        }
    }

    // for each DSL marker, all receivers except the closest one are shadowed by it; that is why we drop it
    return typesByDslScopes.konstues.flatMapTo(mutableSetOf()) { it.drop(1) }
}