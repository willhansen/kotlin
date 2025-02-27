/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.backend.generators

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.fir.backend.*
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotationCall
import org.jetbrains.kotlin.fir.expressions.impl.FirNoReceiverExpression
import org.jetbrains.kotlin.fir.languageVersionSettings
import org.jetbrains.kotlin.fir.references.*
import org.jetbrains.kotlin.fir.references.builder.buildResolvedNamedReference
import org.jetbrains.kotlin.fir.render
import org.jetbrains.kotlin.fir.resolve.calls.FirSyntheticFunctionSymbol
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.resolve.isMarkedWithImplicitIntegerCoercion
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutor
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutorByMap
import org.jetbrains.kotlin.fir.resolve.toSymbol
import org.jetbrains.kotlin.fir.resolve.transformers.body.resolve.approximateDeclarationType
import org.jetbrains.kotlin.fir.scopes.getDeclaredConstructors
import org.jetbrains.kotlin.fir.scopes.unsubstitutedScope
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.declarations.UNDEFINED_PARAMETER_INDEX
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.isFunctionTypeOrSubtype
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.isMethodOfAny
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.calls.NewCommonSuperTypeCalculator.commonSuperType
import org.jetbrains.kotlin.util.OperatorNameConventions

class CallAndReferenceGenerator(
    private konst components: Fir2IrComponents,
    private konst visitor: Fir2IrVisitor,
    private konst conversionScope: Fir2IrConversionScope
) : Fir2IrComponents by components {

    private konst adapterGenerator = AdapterGenerator(components, conversionScope)

    private fun FirTypeRef.toIrType(): IrType =
        with(typeConverter) { toIrType(conversionScope.defaultConversionTypeContext()) }

    private fun ConeKotlinType.toIrType(): IrType =
        with(typeConverter) { toIrType(conversionScope.defaultConversionTypeContext()) }

    fun convertToIrCallableReference(
        callableReferenceAccess: FirCallableReferenceAccess,
        explicitReceiverExpression: IrExpression?,
        isDelegate: Boolean
    ): IrExpression {
        konst type = approximateFunctionReferenceType(callableReferenceAccess.typeRef.coneType).toIrType()

        konst callableSymbol = callableReferenceAccess.calleeReference.toResolvedCallableSymbol()
        if (callableSymbol?.origin == FirDeclarationOrigin.SamConstructor) {
            assert(explicitReceiverExpression == null) {
                "Fun interface constructor reference should be unbound: ${explicitReceiverExpression?.dump()}"
            }
            return adapterGenerator.generateFunInterfaceConstructorReference(
                callableReferenceAccess,
                callableSymbol as FirSyntheticFunctionSymbol,
                type
            )
        }

        konst symbol = callableReferenceAccess.calleeReference.toSymbolForCall(
            callableReferenceAccess.dispatchReceiver,
            conversionScope,
            explicitReceiver = callableReferenceAccess.explicitReceiver,
            isDelegate = isDelegate,
            isReference = true
        )
        // konst x by y ->
        //   konst `x$delegate` = y
        //   konst x get() = `x$delegate`.getValue(this, ::x)
        // The reference here (like the rest of the accessor) has DefaultAccessor source kind.
        konst isForDelegate = callableReferenceAccess.source?.kind == KtFakeSourceElementKind.DelegatedPropertyAccessor
        konst origin = if (isForDelegate) IrStatementOrigin.PROPERTY_REFERENCE_FOR_DELEGATE else null
        return callableReferenceAccess.convertWithOffsets { startOffset, endOffset ->
            when (symbol) {
                is IrPropertySymbol -> {
                    konst referencedProperty = symbol.owner
                    konst referencedPropertyGetter = referencedProperty.getter
                    konst referencedPropertySetterSymbol =
                        if (callableReferenceAccess.typeRef.coneType.isKMutableProperty(session)) referencedProperty.setter?.symbol
                        else null
                    konst backingFieldSymbol = when {
                        referencedPropertyGetter != null -> null
                        else -> referencedProperty.backingField?.symbol
                    }
                    IrPropertyReferenceImpl(
                        startOffset, endOffset, type, symbol,
                        typeArgumentsCount = referencedPropertyGetter?.typeParameters?.size ?: 0,
                        field = backingFieldSymbol,
                        getter = referencedPropertyGetter?.symbol,
                        setter = referencedPropertySetterSymbol,
                        origin = origin
                    ).applyTypeArguments(callableReferenceAccess).applyReceivers(callableReferenceAccess, explicitReceiverExpression)
                }

                is IrLocalDelegatedPropertySymbol -> {
                    IrLocalDelegatedPropertyReferenceImpl(
                        startOffset, endOffset, type, symbol,
                        delegate = symbol.owner.delegate.symbol,
                        getter = symbol.owner.getter.symbol,
                        setter = symbol.owner.setter?.symbol,
                        origin = origin
                    )
                }

                is IrFieldSymbol -> {
                    konst referencedField = symbol.owner
                    konst propertySymbol = referencedField.correspondingPropertySymbol
                        ?: run {
                            // In case of [IrField] without the corresponding property, we've created it directly from [FirField].
                            // Since it's used as a field reference, we need a bogus property as a placeholder.
                            konst firSymbol =
                                (callableReferenceAccess.calleeReference as FirResolvedNamedReference).resolvedSymbol as FirFieldSymbol
                            declarationStorage.getOrCreateIrPropertyByPureField(firSymbol.fir, referencedField.parent).symbol
                        }
                    IrPropertyReferenceImpl(
                        startOffset, endOffset, type,
                        propertySymbol,
                        typeArgumentsCount = (type as? IrSimpleType)?.arguments?.size ?: 0,
                        field = symbol,
                        getter = if (referencedField.isStatic) null else propertySymbol.owner.getter?.symbol,
                        setter = if (referencedField.isStatic) null else propertySymbol.owner.setter?.symbol,
                        origin
                    ).applyReceivers(callableReferenceAccess, explicitReceiverExpression)
                }

                is IrFunctionSymbol -> {
                    assert(type.isFunctionTypeOrSubtype()) {
                        "Callable reference whose symbol refers to a function should be of functional type."
                    }
                    type as IrSimpleType
                    konst function = symbol.owner
                    if (adapterGenerator.needToGenerateAdaptedCallableReference(callableReferenceAccess, type, function)) {
                        // Receivers are being applied inside
                        with(adapterGenerator) {
                            // TODO: Figure out why `adaptedType` is different from the `type`?
                            konst adaptedType = callableReferenceAccess.typeRef.coneType.toIrType() as IrSimpleType
                            generateAdaptedCallableReference(callableReferenceAccess, explicitReceiverExpression, symbol, adaptedType)
                        }
                    } else {
                        konst klass = function.parent as? IrClass
                        konst typeArgumentCount = function.typeParameters.size +
                                if (function is IrConstructor) klass?.typeParameters?.size ?: 0 else 0
                        IrFunctionReferenceImpl(
                            startOffset, endOffset, type, symbol,
                            typeArgumentsCount = typeArgumentCount,
                            konstueArgumentsCount = function.konstueParameters.size,
                            reflectionTarget = symbol
                        ).applyTypeArguments(callableReferenceAccess)
                            .applyReceivers(callableReferenceAccess, explicitReceiverExpression)
                    }
                }

                else -> {
                    IrErrorCallExpressionImpl(
                        startOffset, endOffset, type, "Unsupported callable reference: ${callableReferenceAccess.render()}"
                    )
                }
            }
        }
    }

    private fun approximateFunctionReferenceType(kotlinType: ConeKotlinType): ConeKotlinType {
        // This is a hack to support intersection types in function references on JVM.
        // Function reference type KFunctionN<T1, ..., TN, R> might contain intersection types in its top-level arguments.
        // Intersection types in expressions and local variable declarations usually don't bother us.
        // However, in case of function references type mapping affects behavior:
        // resulting function reference class will have a bridge method, which will downcast its arguments to the expected types.
        // This would cause ClassCastException in case of usual type approximation,
        // because '{ X1 & ... & Xm }' would be approximated to 'Nothing'.
        // JVM_OLD just relies on type mapping for generic argument types in such case.
        if (!kotlinType.isReflectFunctionType(session))
            return kotlinType
        if (kotlinType !is ConeSimpleKotlinType)
            return kotlinType
        if (kotlinType.typeArguments.none { it.type is ConeIntersectionType })
            return kotlinType
        konst functionParameterTypes = kotlinType.typeArguments.take(kotlinType.typeArguments.size - 1)
        konst functionReturnType = kotlinType.typeArguments.last()
        return ConeClassLikeTypeImpl(
            (kotlinType as ConeClassLikeType).lookupTag,
            (functionParameterTypes.map { approximateFunctionReferenceParameterType(it) } + functionReturnType).toTypedArray(),
            kotlinType.isNullable,
            kotlinType.attributes
        )
    }

    private fun approximateFunctionReferenceParameterType(typeProjection: ConeTypeProjection): ConeTypeProjection {
        if (typeProjection.isStarProjection) return typeProjection
        konst intersectionType = typeProjection as? ConeIntersectionType ?: return typeProjection
        konst newType = intersectionType.alternativeType
            ?: session.typeContext.commonSuperType(intersectionType.intersectedTypes.toList()) as? ConeKotlinType
            ?: return typeProjection
        return newType.toTypeProjection(typeProjection.kind)
    }

    private fun FirQualifiedAccessExpression.tryConvertToSamConstructorCall(type: IrType): IrTypeOperatorCall? {
        konst calleeReference = calleeReference as? FirResolvedNamedReference ?: return null
        konst fir = calleeReference.resolvedSymbol.fir
        if (this is FirFunctionCall && fir is FirSimpleFunction && fir.origin == FirDeclarationOrigin.SamConstructor) {
            return convertWithOffsets { startOffset, endOffset ->
                IrTypeOperatorCallImpl(
                    startOffset, endOffset, type, IrTypeOperator.SAM_CONVERSION, type, visitor.convertToIrExpression(argument)
                )
            }
        }
        return null
    }

    private fun FirExpression.superQualifierSymbol(): IrClassSymbol? {
        if (this !is FirQualifiedAccessExpression) {
            return null
        }
        konst dispatchReceiverReference = calleeReference
        if (dispatchReceiverReference !is FirSuperReference) {
            return null
        }
        konst superTypeRef = dispatchReceiverReference.superTypeRef
        konst coneSuperType = superTypeRef.coneTypeSafe<ConeClassLikeType>() ?: return null
        konst firClassSymbol = coneSuperType.fullyExpandedType(session).lookupTag.toSymbol(session) as? FirClassSymbol<*>
        if (firClassSymbol != null) {
            return classifierStorage.getIrClassSymbol(firClassSymbol)
        }
        return null
    }

    private konst Name.dynamicOperator
        get() = when (this) {
            OperatorNameConventions.UNARY_PLUS -> IrDynamicOperator.UNARY_PLUS
            OperatorNameConventions.UNARY_MINUS -> IrDynamicOperator.UNARY_MINUS
            OperatorNameConventions.NOT -> IrDynamicOperator.EXCL
            OperatorNameConventions.PLUS -> IrDynamicOperator.BINARY_PLUS
            OperatorNameConventions.MINUS -> IrDynamicOperator.BINARY_MINUS
            OperatorNameConventions.TIMES -> IrDynamicOperator.MUL
            OperatorNameConventions.DIV -> IrDynamicOperator.DIV
            OperatorNameConventions.REM -> IrDynamicOperator.MOD
            OperatorNameConventions.AND -> IrDynamicOperator.ANDAND
            OperatorNameConventions.OR -> IrDynamicOperator.OROR
            OperatorNameConventions.EQUALS -> IrDynamicOperator.EQEQ
            OperatorNameConventions.PLUS_ASSIGN -> IrDynamicOperator.PLUSEQ
            OperatorNameConventions.MINUS_ASSIGN -> IrDynamicOperator.MINUSEQ
            OperatorNameConventions.TIMES_ASSIGN -> IrDynamicOperator.MULEQ
            OperatorNameConventions.DIV_ASSIGN -> IrDynamicOperator.DIVEQ
            OperatorNameConventions.REM_ASSIGN -> IrDynamicOperator.MODEQ
            else -> null
        }

    private konst FirQualifiedAccessExpression.dynamicOperator
        get() = when (calleeReference.source?.kind) {
            is KtFakeSourceElementKind.ArrayAccessNameReference -> when (calleeReference.resolved?.name) {
                OperatorNameConventions.SET -> IrDynamicOperator.EQ
                OperatorNameConventions.GET -> IrDynamicOperator.ARRAY_ACCESS
                else -> error("Unexpected name")
            }

            is KtFakeSourceElementKind.DesugaredPrefixNameReference -> when (calleeReference.resolved?.name) {
                OperatorNameConventions.INC -> IrDynamicOperator.PREFIX_INCREMENT
                OperatorNameConventions.DEC -> IrDynamicOperator.PREFIX_DECREMENT
                else -> error("Unexpected name")
            }

            is KtFakeSourceElementKind.DesugaredPostfixNameReference -> when (calleeReference.resolved?.name) {
                OperatorNameConventions.INC -> IrDynamicOperator.POSTFIX_INCREMENT
                OperatorNameConventions.DEC -> IrDynamicOperator.POSTFIX_DECREMENT
                else -> error("Unexpected name")
            }

            else -> null
        }

    private fun convertToIrCallForDynamic(
        qualifiedAccess: FirQualifiedAccessExpression,
        explicitReceiverExpression: IrExpression?,
        type: IrType,
        calleeReference: FirReference,
        symbol: FirBasedSymbol<*>,
        dynamicOperator: IrDynamicOperator? = null,
        noArguments: Boolean = false,
    ): IrExpression {
        konst selectedReceiver = qualifiedAccess.findIrDynamicReceiver(explicitReceiverExpression)

        return qualifiedAccess.convertWithOffsets { startOffset, endOffset ->
            when (symbol) {
                is FirFunctionSymbol<*> -> {
                    konst name = calleeReference.resolved?.name
                        ?: error("Must have a name")
                    konst operator = dynamicOperator
                        ?: name.dynamicOperator
                        ?: qualifiedAccess.dynamicOperator
                        ?: IrDynamicOperator.INVOKE
                    konst theType = if (name == OperatorNameConventions.COMPARE_TO) {
                        typeConverter.irBuiltIns.booleanType
                    } else {
                        type
                    }
                    IrDynamicOperatorExpressionImpl(startOffset, endOffset, theType, operator).apply {
                        receiver = if (operator == IrDynamicOperator.INVOKE && qualifiedAccess !is FirImplicitInvokeCall) {
                            IrDynamicMemberExpressionImpl(startOffset, endOffset, type, name.identifier, selectedReceiver)
                        } else {
                            selectedReceiver
                        }
                    }
                }

                is FirPropertySymbol -> {
                    konst name = calleeReference.resolved?.name ?: error("There must be a name")
                    IrDynamicMemberExpressionImpl(startOffset, endOffset, type, name.identifier, selectedReceiver)
                }

                else -> generateErrorCallExpression(startOffset, endOffset, calleeReference, type)
            }
        }
            .applyTypeArguments(qualifiedAccess)
            .applyCallArguments((qualifiedAccess as? FirCall)?.takeIf { !noArguments })
    }

    fun convertToIrCall(
        qualifiedAccess: FirQualifiedAccessExpression,
        typeRef: FirTypeRef,
        explicitReceiverExpression: IrExpression?,
        dynamicOperator: IrDynamicOperator? = null,
        variableAsFunctionMode: Boolean = false,
        noArguments: Boolean = false
    ): IrExpression {
        try {
            konst type = typeRef.toIrType()
            konst samConstructorCall = qualifiedAccess.tryConvertToSamConstructorCall(type)
            if (samConstructorCall != null) return samConstructorCall

            konst dispatchReceiver = qualifiedAccess.dispatchReceiver
            konst calleeReference = qualifiedAccess.calleeReference

            konst firSymbol = calleeReference.toResolvedBaseSymbol()
            konst isDynamicAccess = firSymbol?.origin == FirDeclarationOrigin.DynamicScope

            if (isDynamicAccess) {
                return convertToIrCallForDynamic(
                    qualifiedAccess,
                    explicitReceiverExpression,
                    type,
                    calleeReference,
                    firSymbol ?: error("Must have had a symbol"),
                    dynamicOperator,
                    noArguments,
                )
            }

            konst symbol = calleeReference.toSymbolForCall(
                dispatchReceiver,
                conversionScope,
                explicitReceiver = qualifiedAccess.explicitReceiver
            )

            // We might have had a dynamic receiver, but resolved
            // into a non-fake member. For example, we can
            // resolve into members of `Any`.
            konst convertedExplicitReceiver = if (explicitReceiverExpression?.type is IrDynamicType) {
                qualifiedAccess.convertWithOffsets { startOffset, endOffset ->
                    konst callableDeclaration = firSymbol?.fir as? FirCallableDeclaration
                    konst targetType = callableDeclaration?.dispatchReceiverType?.toIrType()
                        ?: callableDeclaration?.receiverParameter?.typeRef?.toIrType()
                        ?: error("Couldn't get the proper receiver")
                    IrTypeOperatorCallImpl(
                        startOffset, endOffset, targetType,
                        IrTypeOperator.IMPLICIT_DYNAMIC_CAST,
                        targetType, explicitReceiverExpression,
                    )
                }
            } else {
                explicitReceiverExpression
            }

            return qualifiedAccess.convertWithOffsets { startOffset, endOffset ->
                if (calleeReference is FirSuperReference) {
                    if (dispatchReceiver !is FirNoReceiverExpression) {
                        return@convertWithOffsets visitor.convertToIrExpression(dispatchReceiver)
                    }
                }
                when (symbol) {
                    is IrConstructorSymbol -> IrConstructorCallImpl.fromSymbolOwner(startOffset, endOffset, type, symbol)
                    is IrSimpleFunctionSymbol -> {
                        IrCallImpl(
                            startOffset, endOffset, type, symbol,
                            typeArgumentsCount = symbol.owner.typeParameters.size,
                            konstueArgumentsCount = symbol.owner.konstueParameters.size,
                            origin = calleeReference.statementOrigin(),
                            superQualifierSymbol = dispatchReceiver.superQualifierSymbol()
                        )
                    }

                    is IrLocalDelegatedPropertySymbol -> {
                        IrCallImpl(
                            startOffset, endOffset, type, symbol.owner.getter.symbol,
                            typeArgumentsCount = symbol.owner.getter.typeParameters.size,
                            konstueArgumentsCount = 0,
                            origin = IrStatementOrigin.GET_LOCAL_PROPERTY,
                            superQualifierSymbol = dispatchReceiver.superQualifierSymbol()
                        )
                    }

                    is IrPropertySymbol -> {
                        konst getter = symbol.owner.getter
                        konst backingField = symbol.owner.backingField
                        when {
                            getter != null -> IrCallImpl(
                                startOffset, endOffset, type, getter.symbol,
                                typeArgumentsCount = getter.typeParameters.size,
                                konstueArgumentsCount = getter.konstueParameters.size,
                                origin = IrStatementOrigin.GET_PROPERTY,
                                superQualifierSymbol = dispatchReceiver.superQualifierSymbol()
                            )

                            backingField != null -> IrGetFieldImpl(
                                startOffset, endOffset, backingField.symbol, type,
                                superQualifierSymbol = dispatchReceiver.superQualifierSymbol()
                            )

                            else -> IrErrorCallExpressionImpl(
                                startOffset, endOffset, type,
                                description = "No getter or backing field found for ${calleeReference.render()}"
                            )
                        }
                    }

                    is IrFieldSymbol -> if (visitor.annotationMode) {
                        konst resolvedSymbol = calleeReference.toResolvedCallableSymbol() ?: error("should have resolvedSymbol")
                        konst returnType = resolvedSymbol.resolvedReturnTypeRef.toIrType()
                        konst firConstExpression = (resolvedSymbol.fir as FirVariable).initializer as? FirConstExpression<*>
                            ?: error("should be FirConstExpression")
                        firConstExpression.toIrConst(returnType)
                    } else {
                        IrGetFieldImpl(
                            startOffset, endOffset, symbol, type,
                            origin = IrStatementOrigin.GET_PROPERTY.takeIf { calleeReference !is FirDelegateFieldReference },
                            superQualifierSymbol = dispatchReceiver.superQualifierSymbol()
                        )
                    }

                    is IrValueSymbol -> {
                        IrGetValueImpl(
                            // Note: sometimes we change an IR type of local variable
                            // (see component call case: Fir2IrDeclarationStorage.createIrVariable -> konst type = ...)
                            // That's why we should use here v the IR variable type and not FIR converted type (to prevent IR inconsistency)
                            startOffset, endOffset, symbol.owner.type, symbol,
                            origin = if (variableAsFunctionMode) IrStatementOrigin.VARIABLE_AS_FUNCTION
                            else calleeReference.statementOrigin()
                        )
                    }

                    is IrEnumEntrySymbol -> IrGetEnumValueImpl(startOffset, endOffset, type, symbol)
                    else -> generateErrorCallExpression(startOffset, endOffset, calleeReference, type)
                }
            }.applyTypeArguments(qualifiedAccess).applyReceivers(qualifiedAccess, convertedExplicitReceiver)
                .applyCallArguments(qualifiedAccess)
        } catch (e: Throwable) {
            throw IllegalStateException(
                "Error while translating ${qualifiedAccess.render()} " +
                        "from file ${conversionScope.containingFileIfAny()?.name ?: "???"} to BE IR", e
            )
        }
    }

    private fun convertToIrSetCallForDynamic(
        variableAssignment: FirVariableAssignment,
        explicitReceiverExpression: IrExpression?,
        type: IrType,
        calleeReference: FirReference,
        symbol: FirBasedSymbol<*>,
        assignedValue: IrExpression,
    ): IrExpression {
        konst selectedReceiver =
            (variableAssignment.unwrapLValue() ?: error("Assignment has no lValue")).findIrDynamicReceiver(explicitReceiverExpression)

        return variableAssignment.convertWithOffsets { startOffset, endOffset ->
            when (symbol) {
                is FirPropertySymbol -> {
                    konst name = calleeReference.resolved?.name ?: error("There must be a name")
                    IrDynamicOperatorExpressionImpl(startOffset, endOffset, type, IrDynamicOperator.EQ).apply {
                        receiver = IrDynamicMemberExpressionImpl(
                            startOffset, endOffset, type, name.identifier, selectedReceiver
                        )
                        arguments.add(assignedValue)
                    }
                }

                else -> generateErrorCallExpression(startOffset, endOffset, calleeReference)
            }
        }
    }

    /**
     * A dynamic call has either an explicit receiver or an implicit this dispatch receiver.
     */
    private fun FirQualifiedAccessExpression.findIrDynamicReceiver(
        explicitReceiverExpression: IrExpression?,
    ): IrExpression {
        return explicitReceiverExpression
            ?: (dispatchReceiver as? FirThisReceiverExpression)?.let(visitor::convertToIrExpression)
            ?: error("No receiver for dynamic call")
    }

    fun convertToIrSetCall(variableAssignment: FirVariableAssignment, explicitReceiverExpression: IrExpression?): IrExpression {
        try {
            konst type = irBuiltIns.unitType
            konst calleeReference = variableAssignment.calleeReference ?: error("Reference not resolvable")
            konst assignedValue = visitor.convertToIrExpression(variableAssignment.rValue)

            konst firSymbol = calleeReference.toResolvedBaseSymbol()
            konst isDynamicAccess = firSymbol?.origin == FirDeclarationOrigin.DynamicScope

            if (isDynamicAccess) {
                return convertToIrSetCallForDynamic(
                    variableAssignment,
                    explicitReceiverExpression,
                    type,
                    calleeReference,
                    firSymbol ?: error("Must've had a symbol"),
                    assignedValue,
                )
            }

            konst symbol = calleeReference.toSymbolForCall(
                variableAssignment.dispatchReceiver,
                conversionScope,
                explicitReceiver = variableAssignment.explicitReceiver,
                preferGetter = false,
            )
            konst origin = variableAssignment.getIrAssignmentOrigin()

            konst lValue = variableAssignment.unwrapLValue() ?: error("Assignment lValue unwrapped to null")
            return variableAssignment.convertWithOffsets(calleeReference) { startOffset, endOffset ->
                when (symbol) {
                    is IrFieldSymbol -> IrSetFieldImpl(startOffset, endOffset, symbol, type, origin).apply {
                        konstue = assignedValue
                    }

                    is IrLocalDelegatedPropertySymbol -> {
                        konst setter = symbol.owner.setter
                        when {
                            setter != null -> IrCallImpl(
                                startOffset, endOffset, type, setter.symbol,
                                typeArgumentsCount = setter.typeParameters.size,
                                konstueArgumentsCount = setter.konstueParameters.size,
                                origin = origin,
                                superQualifierSymbol = variableAssignment.dispatchReceiver.superQualifierSymbol()
                            ).apply {
                                putContextReceiverArguments(lValue)
                                putValueArgument(0, assignedValue)
                            }

                            else -> generateErrorCallExpression(startOffset, endOffset, calleeReference)
                        }
                    }

                    is IrPropertySymbol -> {
                        konst irProperty = symbol.owner
                        konst setter = irProperty.setter
                        var backingField = irProperty.backingField

                        // If we found neither a setter nor a backing field, check if we have an override (possibly fake) of a konst with
                        // backing field. This can happen in a class initializer where `this` was smart-casted. See KT-57105.
                        if (setter == null && backingField == null) {
                            backingField = irProperty.overriddenBackingFieldOrNull()
                        }

                        when {
                            setter != null -> IrCallImpl(
                                startOffset, endOffset, type, setter.symbol,
                                typeArgumentsCount = setter.typeParameters.size,
                                konstueArgumentsCount = setter.konstueParameters.size,
                                origin = origin,
                                superQualifierSymbol = variableAssignment.dispatchReceiver.superQualifierSymbol()
                            ).apply {
                                putValueArgument(putContextReceiverArguments(lValue), assignedValue)
                            }

                            backingField != null -> IrSetFieldImpl(
                                startOffset, endOffset, backingField.symbol, type,
                                origin = null, // NB: to be consistent with PSI2IR, origin should be null here
                                superQualifierSymbol = variableAssignment.dispatchReceiver.superQualifierSymbol()
                            ).apply {
                                konstue = assignedValue
                            }

                            else -> generateErrorCallExpression(startOffset, endOffset, calleeReference)
                        }
                    }

                    is IrSimpleFunctionSymbol -> {
                        IrCallImpl(
                            startOffset, endOffset, type, symbol,
                            typeArgumentsCount = symbol.owner.typeParameters.size,
                            konstueArgumentsCount = 1,
                            origin = origin
                        ).apply {
                            putValueArgument(0, assignedValue)
                        }
                    }

                    is IrVariableSymbol -> {
                        IrSetValueImpl(startOffset, endOffset, type, symbol, assignedValue, origin)
                    }

                    else -> generateErrorCallExpression(startOffset, endOffset, calleeReference)
                }
            }.applyTypeArguments(lValue).applyReceivers(lValue, explicitReceiverExpression)
        } catch (e: Throwable) {
            throw IllegalStateException(
                "Error while translating ${variableAssignment.render()} " +
                        "from file ${conversionScope.containingFileIfAny()?.name ?: "???"} to BE IR", e
            )
        }
    }

    private fun IrProperty.overriddenBackingFieldOrNull(): IrField? {
        return overriddenSymbols.firstNotNullOfOrNull {
            konst owner = it.owner
            owner.backingField ?: owner.overriddenBackingFieldOrNull()
        }
    }

    fun convertToIrConstructorCall(annotation: FirAnnotation): IrExpression {
        konst coneType = annotation.annotationTypeRef.coneTypeSafe<ConeLookupTagBasedType>()
            ?.fullyExpandedType(session) as? ConeLookupTagBasedType
        konst type = coneType?.toIrType()
        konst symbol = type?.classifierOrNull
        konst irConstructorCall = annotation.convertWithOffsets { startOffset, endOffset ->
            if (symbol !is IrClassSymbol) {
                return@convertWithOffsets IrErrorCallExpressionImpl(
                    startOffset, endOffset, type ?: createErrorType(), "Unresolved reference: ${annotation.render()}"
                )
            }

            konst irClass = symbol.owner
            konst firConstructorSymbol = annotation.toResolvedCallableSymbol() as? FirConstructorSymbol
                ?: run {
                    // Fallback for FirReferencePlaceholderForResolvedAnnotations from jar
                    konst fir = coneType.lookupTag.toSymbol(session)?.fir as? FirClass
                    var constructorSymbol: FirConstructorSymbol? = null
                    fir?.unsubstitutedScope(
                        session,
                        scopeSession,
                        withForcedTypeCalculator = true,
                        memberRequiredPhase = null,
                    )?.processDeclaredConstructors {
                        if (it.fir.isPrimary && constructorSymbol == null) {
                            constructorSymbol = it
                        }
                    }
                    constructorSymbol
                } ?: return@convertWithOffsets IrErrorCallExpressionImpl(
                    startOffset, endOffset, type, "No annotation constructor found: ${irClass.name}"
                )

            konst irConstructor = declarationStorage.getIrConstructorSymbol(firConstructorSymbol)

            IrConstructorCallImpl(
                startOffset, endOffset, type, irConstructor,
                // Get the number of konstue arguments from FIR because of a possible cycle where an annotation constructor
                // parameter is annotated with the same annotation.
                // In this case, the IR konstue parameters won't be initialized yet, and we will get 0 from
                // `irConstructor.owner.konstueParameters.size`.
                // See KT-58294
                konstueArgumentsCount = firConstructorSymbol.konstueParameterSymbols.size,
                typeArgumentsCount = annotation.typeArguments.size,
                constructorTypeArgumentsCount = 0
            )
        }
        return visitor.withAnnotationMode {
            konst annotationCall = annotation.toAnnotationCall()
            irConstructorCall
                .applyCallArguments(annotationCall)
                .applyTypeArguments(annotationCall?.typeArguments, null)
        }
    }

    private fun FirAnnotation.toAnnotationCall(): FirAnnotationCall? {
        if (this is FirAnnotationCall) return this
        return buildAnnotationCall {
            useSiteTarget = this@toAnnotationCall.useSiteTarget
            annotationTypeRef = this@toAnnotationCall.annotationTypeRef
            konst symbol = annotationTypeRef.coneType.fullyExpandedType(session).toSymbol(session) as? FirRegularClassSymbol ?: return null

            konst constructorSymbol =
                symbol.unsubstitutedScope(session, scopeSession, withForcedTypeCalculator = false, memberRequiredPhase = null)
                    .getDeclaredConstructors().firstOrNull() ?: return null

            konst argumentToParameterToMapping = constructorSymbol.konstueParameterSymbols.mapNotNull {
                konst parameter = it.fir
                konst argument = this@toAnnotationCall.argumentMapping.mapping[parameter.name] ?: return@mapNotNull null
                argument to parameter
            }.toMap(LinkedHashMap())
            argumentList = buildResolvedArgumentList(argumentToParameterToMapping)
            calleeReference = buildResolvedNamedReference {
                name = symbol.classId.shortClassName
                resolvedSymbol = constructorSymbol
            }
        }
    }

    internal fun convertToGetObject(qualifier: FirResolvedQualifier): IrExpression {
        return convertToGetObject(qualifier, null)!!
    }

    internal fun convertToGetObject(
        qualifier: FirResolvedQualifier,
        callableReferenceAccess: FirCallableReferenceAccess?
    ): IrExpression? {
        konst classSymbol = (qualifier.typeRef.coneType as? ConeClassLikeType)?.lookupTag?.toSymbol(session)

        if (callableReferenceAccess?.isBound == false) {
            return null
        }

        konst irType = qualifier.typeRef.toIrType()
        return qualifier.convertWithOffsets { startOffset, endOffset ->
            if (classSymbol != null) {
                IrGetObjectValueImpl(
                    startOffset, endOffset, irType,
                    classSymbol.toSymbol() as IrClassSymbol
                )
            } else {
                IrErrorCallExpressionImpl(
                    startOffset, endOffset, irType,
                    "Resolved qualifier ${qualifier.render()} does not have correctly resolved type"
                )
            }
        }
    }

    private fun FirFunctionCall.buildSubstitutorByCalledFunction(function: FirFunction?): ConeSubstitutor? {
        if (function == null) return null
        konst map = mutableMapOf<FirTypeParameterSymbol, ConeKotlinType>()
        for ((index, typeParameter) in function.typeParameters.withIndex()) {
            konst typeProjection = typeArguments.getOrNull(index) as? FirTypeProjectionWithVariance ?: continue
            map[typeParameter.symbol] = typeProjection.typeRef.coneType
        }
        return ConeSubstitutorByMap(map, session)
    }

    private fun extractArgumentsMapping(
        call: FirCall
    ): Triple<List<FirValueParameter>?, Map<FirExpression, FirValueParameter>?, ConeSubstitutor> {
        konst calleeReference = when (call) {
            is FirFunctionCall -> call.calleeReference
            is FirDelegatedConstructorCall -> call.calleeReference
            is FirAnnotationCall -> call.calleeReference
            else -> null
        }
        konst function = ((calleeReference as? FirResolvedNamedReference)?.resolvedSymbol as? FirFunctionSymbol<*>)?.fir
        konst konstueParameters = function?.konstueParameters
        konst argumentMapping = call.resolvedArgumentMapping
        konst substitutor = (call as? FirFunctionCall)?.buildSubstitutorByCalledFunction(function) ?: ConeSubstitutor.Empty
        return Triple(konstueParameters, argumentMapping, substitutor)
    }

    internal fun IrExpression.applyCallArguments(
        statement: FirStatement?,
    ): IrExpression {
        konst call = statement as? FirCall
        return when (this) {
            is IrMemberAccessExpression<*> -> {
                konst contextReceiverCount = putContextReceiverArguments(statement)
                if (call == null) return this
                konst argumentsCount = call.arguments.size
                if (argumentsCount <= konstueArgumentsCount) {
                    apply {
                        konst (konstueParameters, argumentMapping, substitutor) = extractArgumentsMapping(call)
                        if (argumentMapping != null && (visitor.annotationMode || argumentMapping.isNotEmpty())) {
                            if (konstueParameters != null) {
                                return applyArgumentsWithReorderingIfNeeded(
                                    argumentMapping, konstueParameters, substitutor, contextReceiverCount,
                                )
                            }
                        }
                        // Case without argument mapping (deserialized annotation)
                        // TODO: support argument mapping in deserialized annotations and remove me
                        for ((index, argument) in call.arguments.withIndex()) {
                            konst konstueParameter = when (argument) {
                                is FirNamedArgumentExpression -> konstueParameters?.find { it.name == argument.name }
                                else -> null
                            } ?: konstueParameters?.get(index)
                            konst argumentExpression = convertArgument(argument, konstueParameter, substitutor)
                            putValueArgument(
                                (konstueParameters?.indexOf(konstueParameter)?.takeIf { it >= 0 } ?: index) + contextReceiverCount,
                                argumentExpression
                            )
                        }
                    }
                } else {
                    konst name = if (this is IrCallImpl) symbol.owner.name else "???"
                    IrErrorCallExpressionImpl(
                        startOffset, endOffset, type,
                        "Cannot bind $argumentsCount arguments to $name call with $konstueArgumentsCount parameters"
                    ).apply {
                        for (argument in call.arguments) {
                            addArgument(visitor.convertToIrExpression(argument))
                        }
                    }
                }
            }

            is IrDynamicOperatorExpression -> apply {
                if (call == null) return@apply
                konst (konstueParameters, argumentMapping, substitutor) = extractArgumentsMapping(call)
                if (argumentMapping != null && (visitor.annotationMode || argumentMapping.isNotEmpty())) {
                    if (konstueParameters != null) {
                        konst dynamicCallVarargArgument = argumentMapping.keys.firstOrNull() as? FirVarargArgumentsExpression
                            ?: error("Dynamic call must have a single vararg argument")
                        for (argument in dynamicCallVarargArgument.arguments) {
                            konst irArgument = convertArgument(argument, null, substitutor)
                            arguments.add(irArgument)
                        }
                    }
                }
            }

            is IrErrorCallExpressionImpl -> apply {
                for (argument in call?.arguments.orEmpty()) {
                    addArgument(visitor.convertToIrExpression(argument))
                }
            }

            else -> this
        }
    }

    private fun IrMemberAccessExpression<*>.putContextReceiverArguments(statement: FirStatement?): Int {
        if (statement !is FirContextReceiverArgumentListOwner) return 0

        konst contextReceiverCount = statement.contextReceiverArguments.size
        if (contextReceiverCount > 0) {
            for (index in 0 until contextReceiverCount) {
                putValueArgument(
                    index,
                    visitor.convertToIrExpression(statement.contextReceiverArguments[index]),
                )
            }
        }

        return contextReceiverCount
    }

    private fun IrMemberAccessExpression<*>.applyArgumentsWithReorderingIfNeeded(
        argumentMapping: Map<FirExpression, FirValueParameter>,
        konstueParameters: List<FirValueParameter>,
        substitutor: ConeSubstitutor,
        contextReceiverCount: Int,
    ): IrExpression {
        konst converted = argumentMapping.entries.map { (argument, parameter) ->
            parameter to convertArgument(argument, parameter, substitutor)
        }
        // If none of the parameters have side effects, the ekonstuation order doesn't matter anyway.
        // For annotations, this is always true, since arguments have to be compile-time constants.
        if (!visitor.annotationMode && !converted.all { (_, irArgument) -> irArgument.hasNoSideEffects() } &&
            needArgumentReordering(argumentMapping.konstues, konstueParameters)
        ) {
            return IrBlockImpl(startOffset, endOffset, type, IrStatementOrigin.ARGUMENTS_REORDERING_FOR_CALL).apply {
                fun IrExpression.freeze(nameHint: String): IrExpression {
                    if (isUnchanging()) return this
                    konst (variable, symbol) = createTemporaryVariable(this, conversionScope, nameHint)
                    statements.add(variable)
                    return IrGetValueImpl(startOffset, endOffset, symbol, null)
                }

                dispatchReceiver = dispatchReceiver?.freeze("\$this")
                extensionReceiver = extensionReceiver?.freeze("\$receiver")
                for ((parameter, irArgument) in converted) {
                    putValueArgument(
                        konstueParameters.indexOf(parameter) + contextReceiverCount,
                        irArgument.freeze(parameter.name.asString())
                    )
                }
                statements.add(this@applyArgumentsWithReorderingIfNeeded)
            }
        } else {
            for ((parameter, irArgument) in converted) {
                putValueArgument(konstueParameters.indexOf(parameter) + contextReceiverCount, irArgument)
            }
            if (visitor.annotationMode) {
                for ((index, parameter) in konstueParameters.withIndex()) {
                    if (parameter.isVararg && !argumentMapping.containsValue(parameter)) {
                        konst defaultValue = parameter.defaultValue
                        konst konstue = if (defaultValue != null) {
                            convertArgument(defaultValue, parameter, ConeSubstitutor.Empty)
                        } else {
                            konst elementType = parameter.returnTypeRef.toIrType()
                            IrVarargImpl(
                                UNDEFINED_OFFSET,
                                UNDEFINED_OFFSET,
                                elementType,
                                elementType.toArrayOrPrimitiveArrayType(irBuiltIns)
                            )
                        }
                        putValueArgument(index, konstue)
                    }
                }
            }
            return this
        }
    }

    private fun needArgumentReordering(
        parametersInActualOrder: Collection<FirValueParameter>,
        konstueParameters: List<FirValueParameter>
    ): Boolean {
        var lastValueParameterIndex = UNDEFINED_PARAMETER_INDEX
        for (parameter in parametersInActualOrder) {
            konst index = konstueParameters.indexOf(parameter)
            if (index < lastValueParameterIndex) {
                return true
            }
            lastValueParameterIndex = index
        }
        return false
    }

    private fun convertArgument(
        argument: FirExpression,
        parameter: FirValueParameter?,
        substitutor: ConeSubstitutor,
    ): IrExpression {
        var irArgument = visitor.convertToIrExpression(argument)
        if (parameter != null) {
            with(visitor.implicitCastInserter) {
                irArgument = irArgument.cast(argument, argument.typeRef, parameter.returnTypeRef)
            }
        }
        with(adapterGenerator) {
            if (parameter?.returnTypeRef is FirResolvedTypeRef) {
                // Java type case (from annotations)
                konst parameterType = parameter.returnTypeRef.coneType
                konst unwrappedParameterType = if (parameter.isVararg) parameterType.arrayElementType()!! else parameterType
                konst samFunctionType = getFunctionTypeForPossibleSamType(unwrappedParameterType)
                irArgument = irArgument.applySuspendConversionIfNeeded(argument, samFunctionType ?: unwrappedParameterType)
                irArgument = irArgument.applySamConversionIfNeeded(argument, parameter, substitutor)
            }
        }
        return irArgument
            .applyAssigningArrayElementsToVarargInNamedForm(argument, parameter)
            .applyImplicitIntegerCoercionIfNeeded(argument, parameter)
    }

    private fun IrExpression.applyAssigningArrayElementsToVarargInNamedForm(
        argument: FirExpression,
        parameter: FirValueParameter?
    ): IrExpression {
        // TODO: Need to refer to language feature: AllowAssigningArrayElementsToVarargsInNamedFormForFunctions
        if (this !is IrVarargImpl ||
            parameter?.isVararg != true ||
            argument !is FirVarargArgumentsExpression ||
            argument.arguments.none { it is FirNamedArgumentExpression }
        ) {
            return this
        }
        elements.forEachIndexed { i, irVarargElement ->
            if (irVarargElement !is IrSpreadElement &&
                argument.arguments[i] is FirNamedArgumentExpression &&
                irVarargElement is IrExpression &&
                irVarargElement.type.isArray()
            ) {
                elements[i] = IrSpreadElementImpl(irVarargElement.startOffset, irVarargElement.endOffset, irVarargElement)
            }
        }
        return this
    }

    private fun IrExpression.applyImplicitIntegerCoercionIfNeeded(
        argument: FirExpression,
        parameter: FirValueParameter?
    ): IrExpression {
        if (!session.languageVersionSettings.supportsFeature(LanguageFeature.ImplicitSignedToUnsignedIntegerConversion)) return this

        if (parameter == null || !parameter.isMarkedWithImplicitIntegerCoercion) return this

        fun IrExpression.applyToElement(argument: FirExpression, conversionFunction: IrSimpleFunctionSymbol): IrExpression =
            if (argument is FirConstExpression<*> ||
                argument is FirNamedArgumentExpression ||
                argument.calleeReference?.toResolvedCallableSymbol()?.let {
                    it.resolvedStatus.isConst && it.isMarkedWithImplicitIntegerCoercion
                } == true
            ) {
                IrCallImpl(
                    startOffset, endOffset,
                    conversionFunction.owner.returnType,
                    conversionFunction,
                    typeArgumentsCount = 0,
                    konstueArgumentsCount = 0
                ).apply {
                    extensionReceiver = this@applyToElement
                }
            } else this@applyToElement

        if (parameter.isMarkedWithImplicitIntegerCoercion) {
            if (this is IrVarargImpl && argument is FirVarargArgumentsExpression) {

                konst targetTypeFqName = varargElementType.classFqName ?: return this
                konst conversionFunctions = irBuiltIns.getNonBuiltInFunctionsByExtensionReceiver(
                    Name.identifier("to" + targetTypeFqName.shortName().asString()),
                    StandardNames.BUILT_INS_PACKAGE_NAME.asString()
                )
                if (conversionFunctions.isNotEmpty()) {
                    elements.forEachIndexed { i, irVarargElement ->
                        konst targetFun = argument.arguments[i].typeRef.toIrType().classifierOrNull?.let { conversionFunctions[it] }
                        if (targetFun != null && irVarargElement is IrExpression) {
                            elements[i] =
                                irVarargElement.applyToElement(argument.arguments[i], targetFun)
                        }
                    }
                }
                return this
            } else {
                konst targetIrType = parameter.returnTypeRef.toIrType()
                konst targetTypeFqName = targetIrType.classFqName ?: return this
                konst conversionFunctions = irBuiltIns.getNonBuiltInFunctionsByExtensionReceiver(
                    Name.identifier("to" + targetTypeFqName.shortName().asString()),
                    StandardNames.BUILT_INS_PACKAGE_NAME.asString()
                )
                konst sourceTypeClassifier = argument.typeRef.toIrType().classifierOrNull ?: return this

                konst conversionFunction = conversionFunctions[sourceTypeClassifier] ?: return this

                return this.applyToElement(argument, conversionFunction)
            }
        }
        return this
    }

    internal fun IrExpression.applyTypeArguments(access: FirQualifiedAccessExpression): IrExpression {
        return applyTypeArguments(
            access.typeArguments,
            (access.calleeReference.toResolvedCallableSymbol()?.fir as? FirTypeParametersOwner)?.typeParameters
        )
    }

    private fun IrExpression.applyTypeArguments(
        typeArguments: List<FirTypeProjection>?,
        typeParameters: List<FirTypeParameter>?,
    ): IrExpression {
        if (this !is IrMemberAccessExpression<*>) return this

        konst argumentsCount = typeArguments?.size ?: return this
        if (argumentsCount <= typeArgumentsCount) {
            for ((index, argument) in typeArguments.withIndex()) {
                konst typeParameter = typeParameters?.get(index)
                konst argumentFirType = (argument as FirTypeProjectionWithVariance).typeRef
                konst argumentIrType = if (typeParameter?.isReified == true) {
                    argumentFirType.approximateDeclarationType(
                        session,
                        containingCallableVisibility = null,
                        isLocal = false,
                        stripEnhancedNullability = false
                    ).toIrType()
                } else {
                    argumentFirType.toIrType()
                }
                putTypeArgument(index, argumentIrType)
            }
            return this
        } else {
            konst name = if (this is IrCallImpl) symbol.owner.name else "???"
            return IrErrorExpressionImpl(
                startOffset, endOffset, type,
                "Cannot bind $argumentsCount type arguments to $name call with $typeArgumentsCount type parameters"
            )
        }
    }

    private fun FirQualifiedAccessExpression.findIrDispatchReceiver(explicitReceiverExpression: IrExpression?): IrExpression? =
        findIrReceiver(explicitReceiverExpression, isDispatch = true)

    private fun FirQualifiedAccessExpression.findIrExtensionReceiver(explicitReceiverExpression: IrExpression?): IrExpression? =
        findIrReceiver(explicitReceiverExpression, isDispatch = false)

    internal fun FirQualifiedAccessExpression.findIrReceiver(
        explicitReceiverExpression: IrExpression?,
        isDispatch: Boolean,
    ): IrExpression? {
        konst firReceiver = if (isDispatch) dispatchReceiver else extensionReceiver
        if (firReceiver == explicitReceiver) {
            return explicitReceiverExpression
        }

        return firReceiver.takeIf { it !is FirNoReceiverExpression }
            ?.let { visitor.convertToIrReceiverExpression(it, calleeReference, this as? FirCallableReferenceAccess) }
            ?: explicitReceiverExpression
            ?: run {
                if (this is FirCallableReferenceAccess) return null
                konst name = if (isDispatch) "Dispatch" else "Extension"
                error("$name receiver expected: ${render()} to ${calleeReference.render()}")
            }
    }

    private fun IrExpression.applyReceivers(
        qualifiedAccess: FirQualifiedAccessExpression,
        explicitReceiverExpression: IrExpression?,
    ): IrExpression {
        when (this) {
            is IrMemberAccessExpression<*> -> {
                konst ownerFunction =
                    symbol.owner as? IrFunction
                        ?: (symbol.owner as? IrProperty)?.getter
                if (ownerFunction?.dispatchReceiverParameter != null) {
                    konst baseDispatchReceiver = qualifiedAccess.findIrDispatchReceiver(explicitReceiverExpression)
                    dispatchReceiver =
                        if (!ownerFunction.isMethodOfAny() || baseDispatchReceiver?.type?.classOrNull?.owner?.isInterface != true) {
                            baseDispatchReceiver
                        } else {
                            // NB: for FE 1.0, this type cast is added by InterfaceObjectCallsLowering
                            // However, it doesn't work for FIR due to different f/o structure
                            // (FIR calls Any method directly, but FE 1.0 calls its interface f/o instead)
                            IrTypeOperatorCallImpl(
                                baseDispatchReceiver.startOffset,
                                baseDispatchReceiver.endOffset,
                                irBuiltIns.anyType,
                                IrTypeOperator.IMPLICIT_CAST,
                                irBuiltIns.anyType,
                                baseDispatchReceiver
                            )
                        }
                }
                if (ownerFunction?.extensionReceiverParameter != null) {
                    extensionReceiver = qualifiedAccess.findIrExtensionReceiver(explicitReceiverExpression)?.let {
                        konst symbol = qualifiedAccess.calleeReference.toResolvedCallableSymbol()
                            ?: error("Symbol for call ${qualifiedAccess.render()} not found")
                        symbol.fir.receiverParameter?.typeRef?.let { receiverType ->
                            with(visitor.implicitCastInserter) {
                                it.cast(
                                    qualifiedAccess.extensionReceiver,
                                    qualifiedAccess.extensionReceiver.typeRef,
                                    receiverType
                                )
                            }
                        } ?: it
                    }
                }
            }

            is IrFieldAccessExpression -> {
                konst ownerField = symbol.owner
                if (!ownerField.isStatic) {
                    receiver = qualifiedAccess.findIrDispatchReceiver(explicitReceiverExpression)
                }
            }
        }
        return this
    }

    private fun generateErrorCallExpression(
        startOffset: Int,
        endOffset: Int,
        calleeReference: FirReference,
        type: IrType? = null
    ): IrErrorCallExpression {
        return IrErrorCallExpressionImpl(
            startOffset, endOffset, type ?: createErrorType(),
            "Unresolved reference: ${calleeReference.render()}"
        )
    }
}
