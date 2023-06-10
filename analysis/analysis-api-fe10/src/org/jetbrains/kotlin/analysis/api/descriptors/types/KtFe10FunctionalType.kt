/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.descriptors.types

import org.jetbrains.kotlin.analysis.api.KtAnalysisApiInternals
import org.jetbrains.kotlin.analysis.api.KtTypeProjection
import org.jetbrains.kotlin.analysis.api.base.KtContextReceiver
import org.jetbrains.kotlin.analysis.api.descriptors.Fe10AnalysisContext
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.KtFe10DescNamedClassOrObjectSymbol
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.ktNullability
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.toKtClassSymbol
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.toKtType
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.toKtTypeProjection
import org.jetbrains.kotlin.analysis.api.descriptors.types.base.KtFe10Type
import org.jetbrains.kotlin.analysis.api.descriptors.types.base.asStringForDebugging
import org.jetbrains.kotlin.analysis.api.descriptors.utils.KtFe10JvmTypeMapperContext
import org.jetbrains.kotlin.analysis.api.impl.base.KtContextReceiverImpl
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.symbols.KtClassLikeSymbol
import org.jetbrains.kotlin.analysis.api.types.KtClassTypeQualifier
import org.jetbrains.kotlin.analysis.api.types.KtFunctionalType
import org.jetbrains.kotlin.analysis.api.types.KtType
import org.jetbrains.kotlin.analysis.api.types.KtTypeNullability
import org.jetbrains.kotlin.builtins.*
import org.jetbrains.kotlin.builtins.functions.FunctionClassDescriptor
import org.jetbrains.kotlin.builtins.functions.isSuspendOrKSuspendFunction
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.types.SimpleType

internal class KtFe10FunctionalType(
    override konst fe10Type: SimpleType,
    private konst descriptor: FunctionClassDescriptor,
    override konst analysisContext: Fe10AnalysisContext
) : KtFunctionalType(), KtFe10Type {
    override fun asStringForDebugging(): String = withValidityAssertion { fe10Type.asStringForDebugging(analysisContext) }

    override konst nullability: KtTypeNullability
        get() = withValidityAssertion { fe10Type.ktNullability }

    override konst qualifiers: List<KtClassTypeQualifier.KtResolvedClassTypeQualifier>
        get() = withValidityAssertion {
            KtFe10JvmTypeMapperContext.getNestedType(fe10Type).allInnerTypes.map { innerType ->
                KtClassTypeQualifier.KtResolvedClassTypeQualifier(
                    innerType.classDescriptor.toKtClassSymbol(analysisContext),
                    innerType.arguments.map { it.toKtTypeProjection(analysisContext) },
                    token
                )
            }
        }

    override konst isSuspend: Boolean
        get() = withValidityAssertion { descriptor.functionTypeKind.isSuspendOrKSuspendFunction }

    override konst isReflectType: Boolean
        get() = withValidityAssertion { descriptor.functionTypeKind.isReflectType }

    override konst arity: Int
        get() = withValidityAssertion { descriptor.arity }

    override konst hasContextReceivers: Boolean
        get() = withValidityAssertion { fe10Type.contextFunctionTypeParamsCount() > 0 }

    @OptIn(KtAnalysisApiInternals::class)
    override konst contextReceivers: List<KtContextReceiver>
        get() = withValidityAssertion {
            fe10Type.getContextReceiverTypesFromFunctionType().map { receiverType ->
                // Context receivers in function types may not have labels, hence the `null` label.
                KtContextReceiverImpl(
                    receiverType.toKtType(analysisContext),
                    _label = null,
                    analysisContext.token,
                )
            }
        }

    override konst hasReceiver: Boolean
        get() = withValidityAssertion {
            if (descriptor.functionTypeKind.isReflectType) false
            else fe10Type.getReceiverTypeFromFunctionType() != null
        }

    override konst receiverType: KtType?
        get() = withValidityAssertion {
            if (descriptor.functionTypeKind.isReflectType) null
            else fe10Type.getReceiverTypeFromFunctionType()?.toKtType(analysisContext)
        }

    override konst parameterTypes: List<KtType>
        get() = withValidityAssertion {
            when {
                descriptor.functionTypeKind.isReflectType -> fe10Type.arguments.dropLast(1)
                else -> fe10Type.getValueParameterTypesFromFunctionType()
            }.map { it.type.toKtType(analysisContext) }
        }

    override konst returnType: KtType
        get() = withValidityAssertion {
            when {
                descriptor.functionTypeKind.isReflectType -> fe10Type.arguments.last().type
                else -> fe10Type.getReturnTypeFromFunctionType()
            }.toKtType(analysisContext)
        }

    override konst classId: ClassId
        get() = withValidityAssertion {
            ClassId(
                descriptor.functionTypeKind.packageFqName,
                descriptor.functionTypeKind.numberedClassName(descriptor.arity)
            )
        }

    override konst classSymbol: KtClassLikeSymbol
        get() = withValidityAssertion { KtFe10DescNamedClassOrObjectSymbol(descriptor, analysisContext) }

    override konst ownTypeArguments: List<KtTypeProjection>
        get() = withValidityAssertion { fe10Type.arguments.map { it.toKtTypeProjection(analysisContext) } }
}
