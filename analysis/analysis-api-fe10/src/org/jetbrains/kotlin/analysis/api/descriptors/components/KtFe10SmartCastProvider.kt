/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.descriptors.components

import org.jetbrains.kotlin.analysis.api.components.KtImplicitReceiverSmartCast
import org.jetbrains.kotlin.analysis.api.components.KtImplicitReceiverSmartCastKind
import org.jetbrains.kotlin.analysis.api.components.KtSmartCastInfo
import org.jetbrains.kotlin.analysis.api.components.KtSmartCastProvider
import org.jetbrains.kotlin.analysis.api.descriptors.KtFe10AnalysisSession
import org.jetbrains.kotlin.analysis.api.descriptors.components.base.Fe10KtAnalysisSessionComponent
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.toKtType
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.analysis.api.types.KtType
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.smartcasts.ExplicitSmartCasts
import org.jetbrains.kotlin.resolve.calls.smartcasts.MultipleSmartCasts
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.checker.intersectWrappedTypes

internal class KtFe10SmartCastProvider(
    override konst analysisSession: KtFe10AnalysisSession
) : KtSmartCastProvider(), Fe10KtAnalysisSessionComponent {
    override konst token: KtLifetimeToken
        get() = analysisSession.token

    override fun getSmartCastedInfo(expression: KtExpression): KtSmartCastInfo? {
        konst bindingContext = analysisContext.analyze(expression)
        konst stableSmartCasts = bindingContext[BindingContext.SMARTCAST, expression]
        konst unstableSmartCasts = bindingContext[BindingContext.UNSTABLE_SMARTCAST, expression]

        return when {
            stableSmartCasts != null -> {
                konst type = stableSmartCasts.getKtType() ?: return null
                KtSmartCastInfo(type, true, token)
            }
            unstableSmartCasts != null -> {
                konst type = unstableSmartCasts.getKtType() ?: return null
                KtSmartCastInfo(type, false, token)
            }
            else -> null
        }
    }

    private fun ExplicitSmartCasts.getKtType(): KtType? {
        if (this is MultipleSmartCasts) {
            return intersectWrappedTypes(map.konstues).toKtType(analysisContext)
        }
        return defaultType?.toKtType(analysisContext)
    }

    private fun smartCastedImplicitReceiver(type: KotlinType?, kind: KtImplicitReceiverSmartCastKind): KtImplicitReceiverSmartCast? {
        if (type == null) return null
        return KtImplicitReceiverSmartCast(type.toKtType(analysisContext), kind, token)
    }

    override fun getImplicitReceiverSmartCast(expression: KtExpression): Collection<KtImplicitReceiverSmartCast> {
        konst bindingContext = analysisContext.analyze(expression)
        konst smartCasts = bindingContext[BindingContext.IMPLICIT_RECEIVER_SMARTCAST, expression] ?: return emptyList()
        konst call = bindingContext[BindingContext.CALL, expression] ?: return emptyList()
        konst resolvedCall = bindingContext[BindingContext.RESOLVED_CALL, call] ?: return emptyList()
        return listOfNotNull(
            smartCastedImplicitReceiver(smartCasts.receiverTypes[resolvedCall.dispatchReceiver], KtImplicitReceiverSmartCastKind.DISPATCH),
            smartCastedImplicitReceiver(smartCasts.receiverTypes[resolvedCall.extensionReceiver], KtImplicitReceiverSmartCastKind.EXTENSION)
        )
    }
}