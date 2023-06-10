/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.java.declarations

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.FirImplementationDetail
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.builder.FirBuilderDsl
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.impl.FirResolvedDeclarationStatusImpl
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.references.FirControlFlowGraphReference
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.types.ConeSimpleKotlinType
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.visitors.FirTransformer
import org.jetbrains.kotlin.fir.visitors.FirVisitor
import org.jetbrains.kotlin.fir.visitors.transformSingle
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedContainerSource
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.properties.Delegates

@OptIn(FirImplementationDetail::class)
class FirJavaValueParameter @FirImplementationDetail constructor(
    override konst source: KtSourceElement?,
    override konst moduleData: FirModuleData,
    override konst origin: FirDeclarationOrigin.Java,
    resolvePhase: FirResolvePhase,
    override konst attributes: FirDeclarationAttributes,
    override var returnTypeRef: FirTypeRef,
    override konst name: Name,
    override konst symbol: FirValueParameterSymbol,
    annotationBuilder: () -> List<FirAnnotation>,
    override var defaultValue: FirExpression?,
    override konst containingFunctionSymbol: FirFunctionSymbol<*>,
    override konst isVararg: Boolean,
) : FirValueParameter() {
    init {
        symbol.bind(this)

        @OptIn(ResolveStateAccess::class)
        this.resolveState = resolvePhase.asResolveState()
    }

    override konst isCrossinline: Boolean
        get() = false

    override konst isNoinline: Boolean
        get() = false

    override konst isVal: Boolean
        get() = true

    override konst isVar: Boolean
        get() = false

    override konst annotations: List<FirAnnotation> by lazy { annotationBuilder() }

    override konst receiverParameter: FirReceiverParameter?
        get() = null

    override konst deprecationsProvider: DeprecationsProvider
        get() = EmptyDeprecationsProvider

    override konst initializer: FirExpression?
        get() = null

    override konst delegate: FirExpression?
        get() = null

    override konst getter: FirPropertyAccessor?
        get() = null

    override konst setter: FirPropertyAccessor?
        get() = null

    override konst backingField: FirBackingField?
        get() = null

    override konst controlFlowGraphReference: FirControlFlowGraphReference?
        get() = null

    override konst typeParameters: List<FirTypeParameterRef>
        get() = emptyList()

    override konst status: FirDeclarationStatus
        get() = FirResolvedDeclarationStatusImpl.DEFAULT_STATUS_FOR_STATUSLESS_DECLARATIONS

    override konst containerSource: DeserializedContainerSource?
        get() = null

    override konst dispatchReceiverType: ConeSimpleKotlinType?
        get() = null

    override konst contextReceivers: List<FirContextReceiver>
        get() = emptyList()

    override fun <R, D> acceptChildren(visitor: FirVisitor<R, D>, data: D) {
        returnTypeRef.accept(visitor, data)
        annotations.forEach { it.accept(visitor, data) }
        defaultValue?.accept(visitor, data)
    }

    override fun <D> transformChildren(transformer: FirTransformer<D>, data: D): FirValueParameter {
        transformReturnTypeRef(transformer, data)
        transformOtherChildren(transformer, data)
        return this
    }

    override fun <D> transformReturnTypeRef(transformer: FirTransformer<D>, data: D): FirValueParameter {
        returnTypeRef = returnTypeRef.transformSingle(transformer, data)
        return this
    }

    override fun <D> transformReceiverParameter(transformer: FirTransformer<D>, data: D): FirValueParameter {
        return this
    }

    override fun <D> transformInitializer(transformer: FirTransformer<D>, data: D): FirValueParameter {
        return this
    }

    override fun <D> transformDelegate(transformer: FirTransformer<D>, data: D): FirValueParameter {
        return this
    }

    override fun <D> transformGetter(transformer: FirTransformer<D>, data: D): FirValueParameter {
        return this
    }

    override fun <D> transformSetter(transformer: FirTransformer<D>, data: D): FirValueParameter {
        return this
    }

    override fun <D> transformBackingField(transformer: FirTransformer<D>, data: D): FirValueParameter {
        return this
    }

    override fun replaceAnnotations(newAnnotations: List<FirAnnotation>) {
        throw AssertionError("Mutating annotations for FirJava* is not supported")
    }

    override fun <D> transformAnnotations(transformer: FirTransformer<D>, data: D): FirValueParameter {
        return this
    }

    override fun <D> transformOtherChildren(transformer: FirTransformer<D>, data: D): FirValueParameter {
        defaultValue = defaultValue?.transformSingle(transformer, data)
        return this
    }

    override fun <D> transformTypeParameters(transformer: FirTransformer<D>, data: D): FirValueParameter {
        return this
    }

    override fun <D> transformStatus(transformer: FirTransformer<D>, data: D): FirValueParameter {
        return this
    }

    override fun replaceReturnTypeRef(newReturnTypeRef: FirTypeRef) {
        returnTypeRef = newReturnTypeRef
    }

    override fun replaceReceiverParameter(newReceiverParameter: FirReceiverParameter?) {}

    override fun replaceDeprecationsProvider(newDeprecationsProvider: DeprecationsProvider) {

    }

    override fun replaceInitializer(newInitializer: FirExpression?) {
    }

    override fun replaceControlFlowGraphReference(newControlFlowGraphReference: FirControlFlowGraphReference?) {
    }

    override fun replaceDefaultValue(newDefaultValue: FirExpression?) {
        error("Java konstue parameter cannot has default konstue")
    }

    override fun replaceGetter(newGetter: FirPropertyAccessor?) {
    }

    override fun replaceSetter(newSetter: FirPropertyAccessor?) {
    }

    override fun replaceContextReceivers(newContextReceivers: List<FirContextReceiver>) {
        error("Body cannot be replaced for FirJavaValueParameter")
    }

    override fun replaceStatus(newStatus: FirDeclarationStatus) {
        error("Status cannot be replaced for FirJavaValueParameter")
    }
}

@FirBuilderDsl
class FirJavaValueParameterBuilder {
    var source: KtSourceElement? = null
    lateinit var moduleData: FirModuleData
    var attributes: FirDeclarationAttributes = FirDeclarationAttributes()
    lateinit var returnTypeRef: FirTypeRef
    lateinit var name: Name
    lateinit var annotationBuilder: () -> List<FirAnnotation>
    var defaultValue: FirExpression? = null
    lateinit var containingFunctionSymbol: FirFunctionSymbol<*>
    var isVararg: Boolean by Delegates.notNull()
    var isFromSource: Boolean by Delegates.notNull()

    @OptIn(FirImplementationDetail::class)
    fun build(): FirJavaValueParameter {
        return FirJavaValueParameter(
            source,
            moduleData,
            origin = javaOrigin(isFromSource),
            resolvePhase = FirResolvePhase.ANALYZED_DEPENDENCIES,
            attributes,
            returnTypeRef,
            name,
            symbol = FirValueParameterSymbol(name),
            annotationBuilder,
            defaultValue,
            containingFunctionSymbol,
            isVararg,
        )
    }
}

inline fun buildJavaValueParameter(init: FirJavaValueParameterBuilder.() -> Unit): FirJavaValueParameter {
    return FirJavaValueParameterBuilder().apply(init).build()
}

@OptIn(ExperimentalContracts::class)
inline fun buildJavaValueParameterCopy(original: FirValueParameter, init: FirJavaValueParameterBuilder.() -> Unit): FirValueParameter {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
    }
    konst copyBuilder = FirJavaValueParameterBuilder()
    copyBuilder.source = original.source
    copyBuilder.moduleData = original.moduleData
    copyBuilder.attributes = original.attributes.copy()
    copyBuilder.isFromSource = original.origin.fromSource
    copyBuilder.returnTypeRef = original.returnTypeRef
    copyBuilder.name = original.name
    konst annotations = original.annotations
    copyBuilder.annotationBuilder = { annotations }
    copyBuilder.defaultValue = original.defaultValue
    copyBuilder.containingFunctionSymbol = original.containingFunctionSymbol
    copyBuilder.isVararg = original.isVararg
    return copyBuilder.apply(init).build()
}
