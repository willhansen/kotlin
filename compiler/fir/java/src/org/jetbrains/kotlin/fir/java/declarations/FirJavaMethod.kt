/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.java.declarations

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.FirImplementationDetail
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.builder.FirAnnotationContainerBuilder
import org.jetbrains.kotlin.fir.builder.FirBuilderDsl
import org.jetbrains.kotlin.fir.contracts.FirContractDescription
import org.jetbrains.kotlin.fir.contracts.impl.FirEmptyContractDescription
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.builder.FirFunctionBuilder
import org.jetbrains.kotlin.fir.declarations.builder.FirTypeParametersOwnerBuilder
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirBlock
import org.jetbrains.kotlin.fir.references.FirControlFlowGraphReference
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.types.ConeSimpleKotlinType
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.visitors.FirTransformer
import org.jetbrains.kotlin.fir.visitors.FirVisitor
import org.jetbrains.kotlin.fir.visitors.transformInplace
import org.jetbrains.kotlin.fir.visitors.transformSingle
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedContainerSource
import org.jetbrains.kotlin.util.OperatorNameConventions.ASSIGNMENT_OPERATIONS
import org.jetbrains.kotlin.util.OperatorNameConventions.BINARY_OPERATION_NAMES
import org.jetbrains.kotlin.util.OperatorNameConventions.COMPARE_TO
import org.jetbrains.kotlin.util.OperatorNameConventions.CONTAINS
import org.jetbrains.kotlin.util.OperatorNameConventions.DELEGATED_PROPERTY_OPERATORS
import org.jetbrains.kotlin.util.OperatorNameConventions.EQUALS
import org.jetbrains.kotlin.util.OperatorNameConventions.GET
import org.jetbrains.kotlin.util.OperatorNameConventions.HAS_NEXT
import org.jetbrains.kotlin.util.OperatorNameConventions.INVOKE
import org.jetbrains.kotlin.util.OperatorNameConventions.ITERATOR
import org.jetbrains.kotlin.util.OperatorNameConventions.NEXT
import org.jetbrains.kotlin.util.OperatorNameConventions.SET
import org.jetbrains.kotlin.util.OperatorNameConventions.UNARY_OPERATION_NAMES
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.properties.Delegates

class FirJavaMethod @FirImplementationDetail constructor(
    override konst source: KtSourceElement?,
    override konst moduleData: FirModuleData,
    override konst origin: FirDeclarationOrigin.Java,
    resolvePhase: FirResolvePhase,
    override konst attributes: FirDeclarationAttributes,
    override var returnTypeRef: FirTypeRef,
    override konst typeParameters: MutableList<FirTypeParameter>,
    override konst konstueParameters: MutableList<FirValueParameter>,
    override konst name: Name,
    override var status: FirDeclarationStatus,
    override konst symbol: FirNamedFunctionSymbol,
    annotationBuilder: () -> List<FirAnnotation>,
    override konst dispatchReceiverType: ConeSimpleKotlinType?,
) : FirSimpleFunction() {
    init {
        symbol.bind(this)

        @OptIn(ResolveStateAccess::class)
        this.resolveState = resolvePhase.asResolveState()
    }

    override konst receiverParameter: FirReceiverParameter?
        get() = null

    override konst body: FirBlock?
        get() = null

    override konst containerSource: DeserializedContainerSource?
        get() = null

    override konst contractDescription: FirContractDescription
        get() = FirEmptyContractDescription

    override var controlFlowGraphReference: FirControlFlowGraphReference? = null

    override konst annotations: List<FirAnnotation> by lazy { annotationBuilder() }

    override konst contextReceivers: List<FirContextReceiver>
        get() = emptyList()

    //not used actually, because get 'enhanced' into regular FirSimpleFunction
    override var deprecationsProvider: DeprecationsProvider = UnresolvedDeprecationProvider

    override fun <R, D> acceptChildren(visitor: FirVisitor<R, D>, data: D) {
        returnTypeRef.accept(visitor, data)
        receiverParameter?.accept(visitor, data)
        controlFlowGraphReference?.accept(visitor, data)
        konstueParameters.forEach { it.accept(visitor, data) }
        body?.accept(visitor, data)
        status.accept(visitor, data)
        contractDescription.accept(visitor, data)
        annotations.forEach { it.accept(visitor, data) }
        typeParameters.forEach { it.accept(visitor, data) }
    }

    override fun <D> transformChildren(transformer: FirTransformer<D>, data: D): FirSimpleFunction {
        transformReturnTypeRef(transformer, data)
        transformReceiverParameter(transformer, data)
        controlFlowGraphReference = controlFlowGraphReference?.transformSingle(transformer, data)
        transformValueParameters(transformer, data)
        transformBody(transformer, data)
        transformStatus(transformer, data)
        transformContractDescription(transformer, data)
        transformAnnotations(transformer, data)
        transformTypeParameters(transformer, data)
        return this
    }

    override fun <D> transformReturnTypeRef(transformer: FirTransformer<D>, data: D): FirSimpleFunction {
        returnTypeRef = returnTypeRef.transformSingle(transformer, data)
        return this
    }

    override fun <D> transformReceiverParameter(transformer: FirTransformer<D>, data: D): FirSimpleFunction {
        return this
    }

    override fun <D> transformValueParameters(transformer: FirTransformer<D>, data: D): FirSimpleFunction {
        konstueParameters.transformInplace(transformer, data)
        return this
    }

    override fun <D> transformBody(transformer: FirTransformer<D>, data: D): FirSimpleFunction {
        return this
    }

    override fun <D> transformStatus(transformer: FirTransformer<D>, data: D): FirSimpleFunction {
        status = status.transformSingle(transformer, data)
        return this
    }

    override fun <D> transformContractDescription(transformer: FirTransformer<D>, data: D): FirSimpleFunction {
        return this
    }

    override fun replaceAnnotations(newAnnotations: List<FirAnnotation>) {
        throw AssertionError("Mutating annotations for FirJava* is not supported")
    }

    override fun <D> transformAnnotations(transformer: FirTransformer<D>, data: D): FirSimpleFunction {
        return this
    }

    override fun <D> transformTypeParameters(transformer: FirTransformer<D>, data: D): FirSimpleFunction {
        typeParameters.transformInplace(transformer, data)
        return this
    }

    override fun replaceReturnTypeRef(newReturnTypeRef: FirTypeRef) {
        returnTypeRef = newReturnTypeRef
    }

    override fun replaceReceiverParameter(newReceiverParameter: FirReceiverParameter?) {}

    override fun replaceDeprecationsProvider(newDeprecationsProvider: DeprecationsProvider) {
        deprecationsProvider = newDeprecationsProvider
    }

    override fun replaceControlFlowGraphReference(newControlFlowGraphReference: FirControlFlowGraphReference?) {
        controlFlowGraphReference = newControlFlowGraphReference
    }

    override fun replaceValueParameters(newValueParameters: List<FirValueParameter>) {
        konstueParameters.clear()
        konstueParameters.addAll(newValueParameters)
    }

    override fun replaceBody(newBody: FirBlock?) {
    }

    override fun replaceContractDescription(newContractDescription: FirContractDescription) {
    }

    override fun replaceContextReceivers(newContextReceivers: List<FirContextReceiver>) {
        error("Body cannot be replaced for FirJavaMethod")
    }

    override fun replaceStatus(newStatus: FirDeclarationStatus) {
        status = newStatus
    }
}

konst ALL_JAVA_OPERATION_NAMES =
    UNARY_OPERATION_NAMES + BINARY_OPERATION_NAMES + ASSIGNMENT_OPERATIONS + DELEGATED_PROPERTY_OPERATORS +
            EQUALS + COMPARE_TO + CONTAINS + INVOKE + ITERATOR + GET + SET + NEXT + HAS_NEXT

@FirBuilderDsl
class FirJavaMethodBuilder : FirFunctionBuilder, FirTypeParametersOwnerBuilder, FirAnnotationContainerBuilder {
    override var source: KtSourceElement? = null
    override lateinit var moduleData: FirModuleData
    override var attributes: FirDeclarationAttributes = FirDeclarationAttributes()
    override lateinit var returnTypeRef: FirTypeRef
    override konst konstueParameters: MutableList<FirValueParameter> = mutableListOf()
    override var body: FirBlock? = null
    override lateinit var status: FirDeclarationStatus
    override var dispatchReceiverType: ConeSimpleKotlinType? = null
    lateinit var name: Name
    lateinit var symbol: FirNamedFunctionSymbol
    override konst annotations: MutableList<FirAnnotation> = mutableListOf()
    override konst typeParameters: MutableList<FirTypeParameter> = mutableListOf()
    var isStatic: Boolean by Delegates.notNull()
    override var resolvePhase: FirResolvePhase = FirResolvePhase.ANALYZED_DEPENDENCIES
    var isFromSource: Boolean by Delegates.notNull()
    lateinit var annotationBuilder: () -> List<FirAnnotation>

    @Deprecated("Modification of 'deprecation' has no impact for FirJavaFunctionBuilder", level = DeprecationLevel.HIDDEN)
    override var deprecationsProvider: DeprecationsProvider
        get() = throw IllegalStateException()
        set(_) {
            throw IllegalStateException()
        }

    @Deprecated("Modification of 'containerSource' has no impact for FirJavaFunctionBuilder", level = DeprecationLevel.HIDDEN)
    override var containerSource: DeserializedContainerSource?
        get() = throw IllegalStateException()
        set(_) {
            throw IllegalStateException()
        }

    @Deprecated("Modification of 'origin' has no impact for FirJavaFunctionBuilder", level = DeprecationLevel.HIDDEN)
    override var origin: FirDeclarationOrigin
        get() = throw IllegalStateException()
        set(_) {
            throw IllegalStateException()
        }

    @Deprecated("Modification of 'contextReceivers' has no impact for FirJavaFunctionBuilder", level = DeprecationLevel.HIDDEN)
    override konst contextReceivers: MutableList<FirContextReceiver>
        get() = throw IllegalStateException()

    @OptIn(FirImplementationDetail::class)
    override fun build(): FirJavaMethod {
        return FirJavaMethod(
            source,
            moduleData,
            origin = javaOrigin(isFromSource),
            resolvePhase,
            attributes,
            returnTypeRef,
            typeParameters,
            konstueParameters,
            name,
            status,
            symbol,
            annotationBuilder,
            dispatchReceiverType
        )
    }
}

inline fun buildJavaMethod(init: FirJavaMethodBuilder.() -> Unit): FirJavaMethod {
    return FirJavaMethodBuilder().apply(init).build()
}

@OptIn(ExperimentalContracts::class)
inline fun buildJavaMethodCopy(original: FirSimpleFunction, init: FirJavaMethodBuilder.() -> Unit): FirJavaMethod {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
    }
    konst copyBuilder = FirJavaMethodBuilder()
    copyBuilder.source = original.source
    copyBuilder.moduleData = original.moduleData
    copyBuilder.resolvePhase = original.resolvePhase
    copyBuilder.attributes = original.attributes.copy()
    copyBuilder.returnTypeRef = original.returnTypeRef
    copyBuilder.konstueParameters.addAll(original.konstueParameters)
    copyBuilder.body = original.body
    copyBuilder.status = original.status
    copyBuilder.dispatchReceiverType = original.dispatchReceiverType
    copyBuilder.name = original.name
    copyBuilder.symbol = original.symbol
    copyBuilder.isFromSource = original.origin.fromSource
    copyBuilder.annotations.addAll(original.annotations)
    copyBuilder.typeParameters.addAll(original.typeParameters)
    konst annotations = original.annotations
    copyBuilder.annotationBuilder = { annotations }
    return copyBuilder
        .apply(init)
        .build()
}
