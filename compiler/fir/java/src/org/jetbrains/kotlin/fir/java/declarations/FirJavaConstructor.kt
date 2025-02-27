/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.java.declarations

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.fir.FirImplementationDetail
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.builder.FirBuilderDsl
import org.jetbrains.kotlin.fir.contracts.FirContractDescription
import org.jetbrains.kotlin.fir.contracts.impl.FirEmptyContractDescription
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.builder.FirConstructorBuilder
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirBlock
import org.jetbrains.kotlin.fir.expressions.FirDelegatedConstructorCall
import org.jetbrains.kotlin.fir.references.FirControlFlowGraphReference
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.types.ConeSimpleKotlinType
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.visitors.FirTransformer
import org.jetbrains.kotlin.fir.visitors.FirVisitor
import org.jetbrains.kotlin.fir.visitors.transformInplace
import org.jetbrains.kotlin.fir.visitors.transformSingle
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedContainerSource
import kotlin.properties.Delegates

@OptIn(FirImplementationDetail::class)
class FirJavaConstructor @FirImplementationDetail constructor(
    override konst source: KtSourceElement?,
    override konst moduleData: FirModuleData,
    override konst symbol: FirConstructorSymbol,
    override konst origin: FirDeclarationOrigin.Java,
    override konst isPrimary: Boolean,
    override var returnTypeRef: FirTypeRef,
    override konst konstueParameters: MutableList<FirValueParameter>,
    override konst typeParameters: MutableList<FirTypeParameterRef>,
    annotationBuilder: () -> List<FirAnnotation>,
    override var status: FirDeclarationStatus,
    resolvePhase: FirResolvePhase,
    override konst dispatchReceiverType: ConeSimpleKotlinType?,
) : FirConstructor() {
    override konst receiverParameter: FirReceiverParameter? get() = null
    override var deprecationsProvider: DeprecationsProvider = UnresolvedDeprecationProvider
    override konst contractDescription: FirContractDescription get() = FirEmptyContractDescription

    init {
        symbol.bind(this)

        @OptIn(ResolveStateAccess::class)
        this.resolveState = resolvePhase.asResolveState()
    }

    override konst delegatedConstructor: FirDelegatedConstructorCall?
        get() = null

    override konst body: FirBlock?
        get() = null

    override konst attributes: FirDeclarationAttributes = FirDeclarationAttributes()

    override konst controlFlowGraphReference: FirControlFlowGraphReference? get() = null

    override konst annotations: List<FirAnnotation> by lazy { annotationBuilder() }

    override konst contextReceivers: List<FirContextReceiver>
        get() = emptyList()

    override fun <D> transformValueParameters(transformer: FirTransformer<D>, data: D): FirJavaConstructor {
        konstueParameters.transformInplace(transformer, data)
        return this
    }

    override fun <D> transformReturnTypeRef(transformer: FirTransformer<D>, data: D): FirConstructor {
        returnTypeRef = returnTypeRef.transformSingle(transformer, data)
        return this
    }

    override fun <R, D> acceptChildren(visitor: FirVisitor<R, D>, data: D) {
        returnTypeRef.accept(visitor, data)
        controlFlowGraphReference?.accept(visitor, data)
        typeParameters.forEach { it.accept(visitor, data) }
        konstueParameters.forEach { it.accept(visitor, data) }
        status.accept(visitor, data)
        annotations.forEach { it.accept(visitor, data) }
    }

    override fun <D> transformChildren(transformer: FirTransformer<D>, data: D): FirJavaConstructor {
        transformReturnTypeRef(transformer, data)
        transformTypeParameters(transformer, data)
        transformValueParameters(transformer, data)
        status = status.transformSingle(transformer, data)
        transformAnnotations(transformer, data)
        return this
    }

    override fun <D> transformReceiverParameter(transformer: FirTransformer<D>, data: D): FirJavaConstructor {
        return this
    }

    override fun <D> transformContractDescription(transformer: FirTransformer<D>, data: D): FirConstructor {
        return this
    }

    override fun <D> transformStatus(transformer: FirTransformer<D>, data: D): FirJavaConstructor {
        status = status.transformSingle(transformer, data)
        return this
    }

    override fun replaceAnnotations(newAnnotations: List<FirAnnotation>) {
        throw AssertionError("Mutating annotations for FirJava* is not supported")
    }

    override fun <D> transformAnnotations(transformer: FirTransformer<D>, data: D): FirJavaConstructor {
        return this
    }

    override fun <D> transformDelegatedConstructor(transformer: FirTransformer<D>, data: D): FirJavaConstructor {
        return this
    }

    override fun <D> transformBody(transformer: FirTransformer<D>, data: D): FirConstructor {
        return this
    }

    override fun <D> transformTypeParameters(transformer: FirTransformer<D>, data: D): FirJavaConstructor {
        typeParameters.transformInplace(transformer, data)
        return this
    }

    override var containerSource: DeserializedContainerSource? = null

    override fun replaceReturnTypeRef(newReturnTypeRef: FirTypeRef) {
        returnTypeRef = newReturnTypeRef
    }

    override fun replaceValueParameters(newValueParameters: List<FirValueParameter>) {
        konstueParameters.clear()
        konstueParameters += newValueParameters
    }

    override fun replaceDelegatedConstructor(newDelegatedConstructor: FirDelegatedConstructorCall?) {
        error("Delegated constructor cannot be replaced for FirJavaConstructor")
    }

    override fun replaceReceiverParameter(newReceiverParameter: FirReceiverParameter?) {}

    override fun replaceDeprecationsProvider(newDeprecationsProvider: DeprecationsProvider) {
        deprecationsProvider = newDeprecationsProvider
    }

    override fun replaceContextReceivers(newContextReceivers: List<FirContextReceiver>) {
        error("Context receivers cannot be replaced for FirJavaConstructor")
    }

    override fun replaceControlFlowGraphReference(newControlFlowGraphReference: FirControlFlowGraphReference?) {}
    override fun replaceContractDescription(newContractDescription: FirContractDescription) {
        error("Contract description cannot be replaced for FirJavaConstructor")
    }

    override fun replaceBody(newBody: FirBlock?) {
        error("Body cannot be replaced for FirJavaConstructor")
    }

    override fun replaceStatus(newStatus: FirDeclarationStatus) {
        status = newStatus
    }
}

@FirBuilderDsl
class FirJavaConstructorBuilder : FirConstructorBuilder() {
    lateinit var visibility: Visibility
    var isInner: Boolean by Delegates.notNull()
    var isPrimary: Boolean by Delegates.notNull()
    var isFromSource: Boolean by Delegates.notNull()
    lateinit var annotationBuilder: () -> List<FirAnnotation>

    @OptIn(FirImplementationDetail::class)
    override fun build(): FirJavaConstructor {
        return FirJavaConstructor(
            source,
            moduleData,
            symbol,
            origin = javaOrigin(isFromSource),
            isPrimary,
            returnTypeRef,
            konstueParameters,
            typeParameters,
            annotationBuilder,
            status,
            resolvePhase = FirResolvePhase.ANALYZED_DEPENDENCIES,
            dispatchReceiverType
        )
    }

    @Deprecated("Modification of 'body' has no impact for FirJavaConstructorBuilder", level = DeprecationLevel.HIDDEN)
    override var body: FirBlock?
        get() = throw IllegalStateException()
        set(@Suppress("UNUSED_PARAMETER") konstue) {
            throw IllegalStateException()
        }

    @Deprecated("Modification of 'delegatedConstructor' has no impact for FirJavaConstructorBuilder", level = DeprecationLevel.HIDDEN)
    override var delegatedConstructor: FirDelegatedConstructorCall?
        get() = throw IllegalStateException()
        set(@Suppress("UNUSED_PARAMETER") konstue) {
            throw IllegalStateException()
        }

    @Deprecated("Modification of 'resolvePhase' has no impact for FirJavaConstructorBuilder", level = DeprecationLevel.HIDDEN)
    override var resolvePhase: FirResolvePhase
        get() = throw IllegalStateException()
        set(@Suppress("UNUSED_PARAMETER") konstue) {
            throw IllegalStateException()
        }

    @Deprecated("Modification of 'receiverParameter' has no impact for FirJavaConstructorBuilder", level = DeprecationLevel.HIDDEN)
    override var receiverParameter: FirReceiverParameter?
        get() = throw IllegalStateException()
        set(@Suppress("UNUSED_PARAMETER") konstue) {
            throw IllegalStateException()
        }

    @Deprecated("Modification of 'origin' has no impact for FirJavaConstructorBuilder", level = DeprecationLevel.HIDDEN)
    override var origin: FirDeclarationOrigin
        get() = throw IllegalStateException()
        set(@Suppress("UNUSED_PARAMETER") konstue) {
            throw IllegalStateException()
        }
}

inline fun buildJavaConstructor(init: FirJavaConstructorBuilder.() -> Unit): FirJavaConstructor {
    return FirJavaConstructorBuilder().apply(init).build()
}

