/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("DuplicatedCode")

package org.jetbrains.kotlin.fir.declarations.builder

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.builder.FirAnnotationContainerBuilder
import org.jetbrains.kotlin.fir.builder.FirBuilderDsl
import org.jetbrains.kotlin.fir.declarations.DeprecationsProvider
import org.jetbrains.kotlin.fir.declarations.FirContextReceiver
import org.jetbrains.kotlin.fir.declarations.FirDeclarationAttributes
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.declarations.FirDeclarationStatus
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.fir.declarations.builder.FirDeclarationBuilder
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirBlock
import org.jetbrains.kotlin.fir.types.ConeSimpleKotlinType
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.visitors.*
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedContainerSource

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

@FirBuilderDsl
interface FirFunctionBuilder : FirDeclarationBuilder, FirAnnotationContainerBuilder {
    abstract override var source: KtSourceElement?
    abstract override var resolvePhase: FirResolvePhase
    abstract override konst annotations: MutableList<FirAnnotation>
    abstract override var moduleData: FirModuleData
    abstract override var origin: FirDeclarationOrigin
    abstract override var attributes: FirDeclarationAttributes
    abstract var status: FirDeclarationStatus
    abstract var returnTypeRef: FirTypeRef
    abstract var deprecationsProvider: DeprecationsProvider
    abstract var containerSource: DeserializedContainerSource?
    abstract var dispatchReceiverType: ConeSimpleKotlinType?
    abstract konst contextReceivers: MutableList<FirContextReceiver>
    abstract konst konstueParameters: MutableList<FirValueParameter>
    abstract var body: FirBlock?
    override fun build(): FirFunction
}
