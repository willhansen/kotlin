/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.calls

import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.expressions.FirArgumentList
import org.jetbrains.kotlin.fir.expressions.FirEmptyArgumentList
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCallOrigin
import org.jetbrains.kotlin.fir.expressions.builder.buildArgumentList
import org.jetbrains.kotlin.fir.resolve.DoubleColonLHS
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.FirTypeProjection
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.calls.inference.ConstraintSystemBuilder

data class CallInfo(
    override konst callSite: FirElement,
    konst callKind: CallKind,
    override konst name: Name,

    override konst explicitReceiver: FirExpression?,
    override konst argumentList: FirArgumentList,
    override konst isImplicitInvoke: Boolean,

    konst typeArguments: List<FirTypeProjection>,
    konst session: FirSession,
    override konst containingFile: FirFile,
    konst containingDeclarations: List<FirDeclaration>,

    konst candidateForCommonInvokeReceiver: Candidate? = null,

    // Four properties for callable references only
    konst expectedType: ConeKotlinType? = null,
    konst outerCSBuilder: ConstraintSystemBuilder? = null,
    konst lhs: DoubleColonLHS? = null,
    konst origin: FirFunctionCallOrigin = FirFunctionCallOrigin.Regular,
) : AbstractCallInfo() {
    konst arguments: List<FirExpression> get() = argumentList.arguments

    konst argumentCount get() = arguments.size

    fun replaceWithVariableAccess(): CallInfo =
        copy(callKind = CallKind.VariableAccess, typeArguments = emptyList(), argumentList = FirEmptyArgumentList)

    fun replaceExplicitReceiver(explicitReceiver: FirExpression?): CallInfo =
        copy(explicitReceiver = explicitReceiver)

    fun withReceiverAsArgument(receiverExpression: FirExpression): CallInfo =
        copy(
            argumentList = buildArgumentList {
                arguments += receiverExpression
                arguments += argumentList.arguments
            }
        )
}
