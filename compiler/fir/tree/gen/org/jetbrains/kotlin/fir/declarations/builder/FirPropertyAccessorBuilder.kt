/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("DuplicatedCode")

package org.jetbrains.kotlin.fir.declarations.builder

import kotlin.contracts.*
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.fir.FirImplementationDetail
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.builder.FirAnnotationContainerBuilder
import org.jetbrains.kotlin.fir.builder.FirBuilderDsl
import org.jetbrains.kotlin.fir.builder.toMutableOrEmpty
import org.jetbrains.kotlin.fir.contracts.FirContractDescription
import org.jetbrains.kotlin.fir.contracts.impl.FirEmptyContractDescription
import org.jetbrains.kotlin.fir.declarations.DeprecationsProvider
import org.jetbrains.kotlin.fir.declarations.FirContextReceiver
import org.jetbrains.kotlin.fir.declarations.FirDeclarationAttributes
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.declarations.FirDeclarationStatus
import org.jetbrains.kotlin.fir.declarations.FirPropertyAccessor
import org.jetbrains.kotlin.fir.declarations.FirReceiverParameter
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.declarations.FirResolveState
import org.jetbrains.kotlin.fir.declarations.FirTypeParameter
import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.fir.declarations.ResolveStateAccess
import org.jetbrains.kotlin.fir.declarations.UnresolvedDeprecationProvider
import org.jetbrains.kotlin.fir.declarations.asResolveState
import org.jetbrains.kotlin.fir.declarations.builder.FirFunctionBuilder
import org.jetbrains.kotlin.fir.declarations.impl.FirPropertyAccessorImpl
import org.jetbrains.kotlin.fir.declarations.resolvePhase
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirBlock
import org.jetbrains.kotlin.fir.references.FirControlFlowGraphReference
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertyAccessorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.types.ConeSimpleKotlinType
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.visitors.*
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedContainerSource

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

@FirBuilderDsl
class FirPropertyAccessorBuilder : FirFunctionBuilder, FirAnnotationContainerBuilder {
    override var source: KtSourceElement? = null
    override var resolvePhase: FirResolvePhase = FirResolvePhase.RAW_FIR
    override lateinit var moduleData: FirModuleData
    override lateinit var origin: FirDeclarationOrigin
    override var attributes: FirDeclarationAttributes = FirDeclarationAttributes()
    override lateinit var status: FirDeclarationStatus
    override lateinit var returnTypeRef: FirTypeRef
    override var deprecationsProvider: DeprecationsProvider = UnresolvedDeprecationProvider
    override var containerSource: DeserializedContainerSource? = null
    override var dispatchReceiverType: ConeSimpleKotlinType? = null
    override konst contextReceivers: MutableList<FirContextReceiver> = mutableListOf()
    override konst konstueParameters: MutableList<FirValueParameter> = mutableListOf()
    override var body: FirBlock? = null
    var contractDescription: FirContractDescription = FirEmptyContractDescription
    lateinit var symbol: FirPropertyAccessorSymbol
    lateinit var propertySymbol: FirPropertySymbol
    var isGetter: Boolean by kotlin.properties.Delegates.notNull<Boolean>()
    override konst annotations: MutableList<FirAnnotation> = mutableListOf()
    konst typeParameters: MutableList<FirTypeParameter> = mutableListOf()

    @OptIn(FirImplementationDetail::class)
    override fun build(): FirPropertyAccessor {
        return FirPropertyAccessorImpl(
            source,
            resolvePhase,
            moduleData,
            origin,
            attributes,
            status,
            returnTypeRef,
            deprecationsProvider,
            containerSource,
            dispatchReceiverType,
            contextReceivers.toMutableOrEmpty(),
            konstueParameters,
            body,
            contractDescription,
            symbol,
            propertySymbol,
            isGetter,
            annotations.toMutableOrEmpty(),
            typeParameters,
        )
    }

}

@OptIn(ExperimentalContracts::class)
inline fun buildPropertyAccessor(init: FirPropertyAccessorBuilder.() -> Unit): FirPropertyAccessor {
    contract {
        callsInPlace(init, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
    }
    return FirPropertyAccessorBuilder().apply(init).build()
}

@OptIn(ExperimentalContracts::class)
inline fun buildPropertyAccessorCopy(original: FirPropertyAccessor, init: FirPropertyAccessorBuilder.() -> Unit): FirPropertyAccessor {
    contract {
        callsInPlace(init, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
    }
    konst copyBuilder = FirPropertyAccessorBuilder()
    copyBuilder.source = original.source
    copyBuilder.resolvePhase = original.resolvePhase
    copyBuilder.moduleData = original.moduleData
    copyBuilder.origin = original.origin
    copyBuilder.attributes = original.attributes.copy()
    copyBuilder.status = original.status
    copyBuilder.returnTypeRef = original.returnTypeRef
    copyBuilder.deprecationsProvider = original.deprecationsProvider
    copyBuilder.containerSource = original.containerSource
    copyBuilder.dispatchReceiverType = original.dispatchReceiverType
    copyBuilder.contextReceivers.addAll(original.contextReceivers)
    copyBuilder.konstueParameters.addAll(original.konstueParameters)
    copyBuilder.body = original.body
    copyBuilder.contractDescription = original.contractDescription
    copyBuilder.symbol = original.symbol
    copyBuilder.propertySymbol = original.propertySymbol
    copyBuilder.isGetter = original.isGetter
    copyBuilder.annotations.addAll(original.annotations)
    copyBuilder.typeParameters.addAll(original.typeParameters)
    return copyBuilder.apply(init).build()
}
