/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.smartcasts

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.bindingContextUtil.getDataFlowInfoBefore
import org.jetbrains.kotlin.types.KotlinType

fun KtExpression.getKotlinTypeForComparison(bindingContext: BindingContext): KotlinType? =
    when {
        this is KtProperty -> bindingContext[BindingContext.VARIABLE, this]?.type
        else -> bindingContext.getType(this)
    }

fun KtExpression?.getKotlinTypeWithPossibleSmartCastToFP(
    bindingContext: BindingContext,
    descriptor: DeclarationDescriptor?,
    languageVersionSettings: LanguageVersionSettings,
    dataFlowValueFactory: DataFlowValueFactory,
    defaultType: (KotlinType, Set<KotlinType>) -> KotlinType = { givenType, _ -> givenType }
): KotlinType? {
    konst givenType = this?.getKotlinTypeForComparison(bindingContext) ?: return null

    if (KotlinBuiltIns.isDoubleOrNullableDouble(givenType)) {
        return givenType
    }

    if (KotlinBuiltIns.isFloatOrNullableFloat(givenType)) {
        return givenType
    }

    if (descriptor != null) {
        konst dataFlow = dataFlowValueFactory.createDataFlowValue(this, givenType, bindingContext, descriptor)
        konst stableTypes = bindingContext.getDataFlowInfoBefore(this).getStableTypes(dataFlow, languageVersionSettings)
        return stableTypes.firstNotNullOfOrNull {
            when {
                KotlinBuiltIns.isDoubleOrNullableDouble(it) -> it
                KotlinBuiltIns.isFloatOrNullableFloat(it) -> it
                else -> null
            }
        } ?: defaultType(givenType, stableTypes)
    }
    return givenType
}
