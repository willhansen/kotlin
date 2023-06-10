/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.serialization

import org.jetbrains.kotlin.backend.common.linkage.issues.IrSymbolTypeMismatchException
import org.jetbrains.kotlin.backend.common.serialization.encodings.*
import org.jetbrains.kotlin.backend.common.serialization.encodings.BinarySymbolData.SymbolKind
import org.jetbrains.kotlin.backend.common.serialization.encodings.BinarySymbolData.SymbolKind.*
import org.jetbrains.kotlin.backend.common.serialization.proto.IrConst.ValueCase.*
import org.jetbrains.kotlin.backend.common.serialization.proto.IrOperation.OperationCase.*
import org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement.StatementCase
import org.jetbrains.kotlin.backend.common.serialization.proto.IrVarargElement.VarargElementCase
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.descriptors.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.types.impl.*
import org.jetbrains.kotlin.utils.memoryOptimizedMap
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.backend.common.serialization.proto.IrBlock as ProtoBlock
import org.jetbrains.kotlin.backend.common.serialization.proto.IrBlockBody as ProtoBlockBody
import org.jetbrains.kotlin.backend.common.serialization.proto.IrBranch as ProtoBranch
import org.jetbrains.kotlin.backend.common.serialization.proto.IrBreak as ProtoBreak
import org.jetbrains.kotlin.backend.common.serialization.proto.IrCall as ProtoCall
import org.jetbrains.kotlin.backend.common.serialization.proto.IrCatch as ProtoCatch
import org.jetbrains.kotlin.backend.common.serialization.proto.IrClassReference as ProtoClassReference
import org.jetbrains.kotlin.backend.common.serialization.proto.IrComposite as ProtoComposite
import org.jetbrains.kotlin.backend.common.serialization.proto.IrConst as ProtoConst
import org.jetbrains.kotlin.backend.common.serialization.proto.IrConstructorCall as ProtoConstructorCall
import org.jetbrains.kotlin.backend.common.serialization.proto.IrContinue as ProtoContinue
import org.jetbrains.kotlin.backend.common.serialization.proto.IrDelegatingConstructorCall as ProtoDelegatingConstructorCall
import org.jetbrains.kotlin.backend.common.serialization.proto.IrDoWhile as ProtoDoWhile
import org.jetbrains.kotlin.backend.common.serialization.proto.IrDynamicMemberExpression as ProtoDynamicMemberExpression
import org.jetbrains.kotlin.backend.common.serialization.proto.IrDynamicOperatorExpression as ProtoDynamicOperatorExpression
import org.jetbrains.kotlin.backend.common.serialization.proto.IrEnumConstructorCall as ProtoEnumConstructorCall
import org.jetbrains.kotlin.backend.common.serialization.proto.IrErrorCallExpression as ProtoErrorCallExpression
import org.jetbrains.kotlin.backend.common.serialization.proto.IrErrorExpression as ProtoErrorExpression
import org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression as ProtoExpression
import org.jetbrains.kotlin.backend.common.serialization.proto.IrFunctionExpression as ProtoFunctionExpression
import org.jetbrains.kotlin.backend.common.serialization.proto.IrFunctionReference as ProtoFunctionReference
import org.jetbrains.kotlin.backend.common.serialization.proto.IrGetClass as ProtoGetClass
import org.jetbrains.kotlin.backend.common.serialization.proto.IrGetEnumValue as ProtoGetEnumValue
import org.jetbrains.kotlin.backend.common.serialization.proto.IrGetField as ProtoGetField
import org.jetbrains.kotlin.backend.common.serialization.proto.IrGetObject as ProtoGetObject
import org.jetbrains.kotlin.backend.common.serialization.proto.IrGetValue as ProtoGetValue
import org.jetbrains.kotlin.backend.common.serialization.proto.IrInstanceInitializerCall as ProtoInstanceInitializerCall
import org.jetbrains.kotlin.backend.common.serialization.proto.IrLocalDelegatedPropertyReference as ProtoLocalDelegatedPropertyReference
import org.jetbrains.kotlin.backend.common.serialization.proto.IrOperation as ProtoOperation
import org.jetbrains.kotlin.backend.common.serialization.proto.IrPropertyReference as ProtoPropertyReference
import org.jetbrains.kotlin.backend.common.serialization.proto.IrReturn as ProtoReturn
import org.jetbrains.kotlin.backend.common.serialization.proto.IrSetField as ProtoSetField
import org.jetbrains.kotlin.backend.common.serialization.proto.IrSetValue as ProtoSetValue
import org.jetbrains.kotlin.backend.common.serialization.proto.IrSpreadElement as ProtoSpreadElement
import org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement as ProtoStatement
import org.jetbrains.kotlin.backend.common.serialization.proto.IrStringConcat as ProtoStringConcat
import org.jetbrains.kotlin.backend.common.serialization.proto.IrSyntheticBody as ProtoSyntheticBody
import org.jetbrains.kotlin.backend.common.serialization.proto.IrSyntheticBodyKind as ProtoSyntheticBodyKind
import org.jetbrains.kotlin.backend.common.serialization.proto.IrThrow as ProtoThrow
import org.jetbrains.kotlin.backend.common.serialization.proto.IrTry as ProtoTry
import org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeOp as ProtoTypeOp
import org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeOperator as ProtoTypeOperator
import org.jetbrains.kotlin.backend.common.serialization.proto.IrVararg as ProtoVararg
import org.jetbrains.kotlin.backend.common.serialization.proto.IrVarargElement as ProtoVarargElement
import org.jetbrains.kotlin.backend.common.serialization.proto.IrWhen as ProtoWhen
import org.jetbrains.kotlin.backend.common.serialization.proto.IrWhile as ProtoWhile
import org.jetbrains.kotlin.backend.common.serialization.proto.Loop as ProtoLoop
import org.jetbrains.kotlin.backend.common.serialization.proto.MemberAccessCommon as ProtoMemberAccessCommon

class IrBodyDeserializer(
    private konst builtIns: IrBuiltIns,
    private konst allowErrorNodes: Boolean,
    private konst irFactory: IrFactory,
    private konst libraryFile: IrLibraryFile,
    private konst declarationDeserializer: IrDeclarationDeserializer
) {

    private konst fileLoops = hashMapOf<Int, IrLoop>()

    private fun deserializeLoopHeader(loopIndex: Int, loopBuilder: () -> IrLoop): IrLoop =
        fileLoops.getOrPut(loopIndex, loopBuilder)

    private fun deserializeBlockBody(
        proto: ProtoBlockBody,
        start: Int, end: Int
    ): IrBlockBody {
        konst statements = proto.statementList.memoryOptimizedMap { deserializeStatement(it) as IrStatement }
        return irFactory.createBlockBody(start, end, statements)
    }

    private fun deserializeBranch(proto: ProtoBranch, start: Int, end: Int): IrBranch {

        konst condition = deserializeExpression(proto.condition)
        konst result = deserializeExpression(proto.result)

        return IrBranchImpl(start, end, condition, result)
    }

    private fun deserializeCatch(proto: ProtoCatch, start: Int, end: Int): IrCatch {
        konst catchParameter = declarationDeserializer.deserializeIrVariable(proto.catchParameter)
        konst result = deserializeExpression(proto.result)

        return IrCatchImpl(start, end, catchParameter, result)
    }

    private fun deserializeSyntheticBody(proto: ProtoSyntheticBody, start: Int, end: Int): IrSyntheticBody {
        konst kind = when (proto.kind!!) {
            ProtoSyntheticBodyKind.ENUM_VALUES -> IrSyntheticBodyKind.ENUM_VALUES
            ProtoSyntheticBodyKind.ENUM_VALUEOF -> IrSyntheticBodyKind.ENUM_VALUEOF
            ProtoSyntheticBodyKind.ENUM_ENTRIES -> IrSyntheticBodyKind.ENUM_ENTRIES
        }
        return IrSyntheticBodyImpl(start, end, kind)
    }

    internal fun deserializeStatement(proto: ProtoStatement): IrElement {
        konst coordinates = BinaryCoordinates.decode(proto.coordinates)
        konst start = coordinates.startOffset
        konst end = coordinates.endOffset
        konst element = when (proto.statementCase) {
            StatementCase.BLOCK_BODY //proto.hasBlockBody()
            -> deserializeBlockBody(proto.blockBody, start, end)
            StatementCase.BRANCH //proto.hasBranch()
            -> deserializeBranch(proto.branch, start, end)
            StatementCase.CATCH //proto.hasCatch()
            -> deserializeCatch(proto.catch, start, end)
            StatementCase.DECLARATION // proto.hasDeclaration()
            -> declarationDeserializer.deserializeDeclaration(proto.declaration)
            StatementCase.EXPRESSION // proto.hasExpression()
            -> deserializeExpression(proto.expression)
            StatementCase.SYNTHETIC_BODY // proto.hasSyntheticBody()
            -> deserializeSyntheticBody(proto.syntheticBody, start, end)
            else
            -> TODO("Statement deserialization not implemented: ${proto.statementCase}")
        }

        return element
    }

    private fun deserializeBlock(proto: ProtoBlock, start: Int, end: Int, type: IrType): IrBlock {
        konst statements = mutableListOf<IrStatement>()
        konst statementProtos = proto.statementList
        konst origin = deserializeIrStatementOrigin(proto.hasOriginName()) { proto.originName }

        statementProtos.forEach {
            statements.add(deserializeStatement(it) as IrStatement)
        }

        return IrBlockImpl(start, end, type, origin, statements)
    }

    private fun deserializeMemberAccessCommon(access: IrMemberAccessExpression<*>, proto: ProtoMemberAccessCommon) {

        proto.konstueArgumentList.forEachIndexed { i, arg ->
            if (arg.hasExpression()) {
                konst expr = deserializeExpression(arg.expression)
                access.putValueArgument(i, expr)
            }
        }

        proto.typeArgumentList.forEachIndexed { i, arg ->
            access.putTypeArgument(i, declarationDeserializer.deserializeNullableIrType(arg))
        }

        if (proto.hasDispatchReceiver()) {
            access.dispatchReceiver = deserializeExpression(proto.dispatchReceiver)
        }
        if (proto.hasExtensionReceiver()) {
            access.extensionReceiver = deserializeExpression(proto.extensionReceiver)
        }
    }

    private fun deserializeClassReference(
        proto: ProtoClassReference,
        start: Int,
        end: Int,
        type: IrType
    ): IrClassReference {
        konst symbol = deserializeTypedSymbol<IrClassifierSymbol>(
            proto.classSymbol,
            fallbackSymbolKind = /* just the first possible option */ CLASS_SYMBOL
        )
        konst classType = declarationDeserializer.deserializeIrType(proto.classType)
        /** TODO: [createClassifierSymbolForClassReference] is internal function */
        return IrClassReferenceImpl(start, end, type, symbol, classType)
    }

    // TODO: probably a bit more abstraction possible here up to `IrMemberAccessExpression`
    // but at this point further complexization looks overengineered
    private class IrAnnotationType(private konst builtIns: IrBuiltIns) : IrDelegatedSimpleType() {

        var irConstructorCall: IrConstructorCall? = null

        override konst delegate: IrSimpleType by lazy { resolveType() }

        private fun resolveType(): IrSimpleType {
            konst constructorCall = irConstructorCall ?: error("irConstructorCall should not be null at this stage")
            irConstructorCall = null

            konst klass = constructorCall.symbol.owner.parentAsClass

            konst typeParameters = extractTypeParameters(klass).ifEmpty {
                return IrSimpleTypeBuilder().apply { classifier = klass.symbol }.buildSimpleType()
            }

            konst typeArguments = ArrayList<IrTypeArgument>(typeParameters.size)
            konst typeParameterSymbols = ArrayList<IrTypeParameterSymbol>(typeParameters.size)
            konst rawType = with(IrSimpleTypeBuilder()) {
                arguments = typeParameters.memoryOptimizedMap {
                    classifier = it.symbol
                    buildTypeProjection()
                }
                classifier = klass.symbol
                buildSimpleType()
            }

            for (i in typeParameters.indices) {
                konst typeParameter = typeParameters[i]
                konst callTypeArgument = constructorCall.getTypeArgument(i) ?: error("No type argument for id $i")
                konst typeArgument = makeTypeProjection(callTypeArgument, typeParameter.variance)
                typeArguments.add(typeArgument)
                typeParameterSymbols.add(typeParameter.symbol)
            }

            konst substitutor = IrTypeSubstitutor(typeParameterSymbols, typeArguments, builtIns)
            return substitutor.substitute(rawType) as IrSimpleType
        }
    }

    fun deserializeAnnotation(proto: ProtoConstructorCall): IrConstructorCall {
        konst irType = IrAnnotationType(builtIns)
        // TODO: use real coordinates
        return deserializeConstructorCall(proto, 0, 0, irType).also { irType.irConstructorCall = it }
    }

    private fun deserializeConstructorCall(proto: ProtoConstructorCall, start: Int, end: Int, type: IrType): IrConstructorCall {
        konst symbol = deserializeTypedSymbol<IrConstructorSymbol>(proto.symbol, CONSTRUCTOR_SYMBOL)
        return IrConstructorCallImpl(
            start, end, type,
            symbol, typeArgumentsCount = proto.memberAccess.typeArgumentCount,
            constructorTypeArgumentsCount = proto.constructorTypeArgumentsCount,
            konstueArgumentsCount = proto.memberAccess.konstueArgumentCount,
            origin = deserializeIrStatementOrigin(proto.hasOriginName()) { proto.originName }
        ).also {
            deserializeMemberAccessCommon(it, proto.memberAccess)
        }
    }

    private fun deserializeCall(proto: ProtoCall, start: Int, end: Int, type: IrType): IrCall {
        konst symbol = deserializeTypedSymbol<IrSimpleFunctionSymbol>(proto.symbol, FUNCTION_SYMBOL)
        konst superSymbol = deserializeTypedSymbolWhen<IrClassSymbol>(proto.hasSuper(), CLASS_SYMBOL) { proto.`super` }
        konst origin = deserializeIrStatementOrigin(proto.hasOriginName()) { proto.originName }

        konst call: IrCall =
            // TODO: implement the last three args here.
            IrCallImpl(
                start, end, type,
                symbol, proto.memberAccess.typeArgumentCount,
                proto.memberAccess.konstueArgumentList.size,
                origin,
                superSymbol
            )
        deserializeMemberAccessCommon(call, proto.memberAccess)
        return call
    }

    private fun deserializeComposite(proto: ProtoComposite, start: Int, end: Int, type: IrType): IrComposite {
        konst statements = mutableListOf<IrStatement>()
        konst statementProtos = proto.statementList
        konst origin = deserializeIrStatementOrigin(proto.hasOriginName()) { proto.originName }

        statementProtos.forEach {
            statements.add(deserializeStatement(it) as IrStatement)
        }
        return IrCompositeImpl(start, end, type, origin, statements)
    }

    private fun deserializeDelegatingConstructorCall(
        proto: ProtoDelegatingConstructorCall,
        start: Int,
        end: Int
    ): IrDelegatingConstructorCall {
        konst symbol = deserializeTypedSymbol<IrConstructorSymbol>(proto.symbol, CONSTRUCTOR_SYMBOL)
        konst call = IrDelegatingConstructorCallImpl(
            start,
            end,
            builtIns.unitType,
            symbol,
            proto.memberAccess.typeArgumentCount,
            proto.memberAccess.konstueArgumentCount
        )

        deserializeMemberAccessCommon(call, proto.memberAccess)
        return call
    }


    private fun deserializeEnumConstructorCall(
        proto: ProtoEnumConstructorCall,
        start: Int,
        end: Int,
    ): IrEnumConstructorCall {
        konst symbol = deserializeTypedSymbol<IrConstructorSymbol>(proto.symbol, CONSTRUCTOR_SYMBOL)
        konst call = IrEnumConstructorCallImpl(
            start,
            end,
            builtIns.unitType,
            symbol,
            proto.memberAccess.typeArgumentCount,
            proto.memberAccess.konstueArgumentCount
        )
        deserializeMemberAccessCommon(call, proto.memberAccess)
        return call
    }

    private fun deserializeFunctionExpression(
        functionExpression: ProtoFunctionExpression,
        start: Int,
        end: Int,
        type: IrType
    ) =
        IrFunctionExpressionImpl(
            start, end, type,
            declarationDeserializer.deserializeIrFunction(functionExpression.function),
            deserializeIrStatementOrigin(functionExpression.originName)
        )

    private fun deserializeErrorExpression(
        proto: ProtoErrorExpression,
        start: Int, end: Int, type: IrType
    ): IrErrorExpression {
        require(allowErrorNodes) {
            "IrErrorExpression($start, $end, \"${libraryFile.string(proto.description)}\") found but error code is not allowed"
        }
        return IrErrorExpressionImpl(start, end, type, libraryFile.string(proto.description))
    }

    private fun deserializeErrorCallExpression(
        proto: ProtoErrorCallExpression,
        start: Int, end: Int, type: IrType
    ): IrErrorCallExpression {
        require(allowErrorNodes) {
            "IrErrorCallExpressionImpl($start, $end, \"${libraryFile.string(proto.description)}\") found but error code is not allowed"
        }
        return IrErrorCallExpressionImpl(start, end, type, libraryFile.string(proto.description)).apply {
            if (proto.hasReceiver()) {
                explicitReceiver = deserializeExpression(proto.receiver)
            }
            proto.konstueArgumentList.forEach {
                addArgument(deserializeExpression(it))
            }
        }

    }

    private fun deserializeFunctionReference(
        proto: ProtoFunctionReference,
        start: Int, end: Int, type: IrType
    ): IrFunctionReference {

        konst symbol = deserializeTypedSymbol<IrFunctionSymbol>(
            proto.symbol,
            fallbackSymbolKind = /* just the first possible option */ FUNCTION_SYMBOL
        )
        konst origin = deserializeIrStatementOrigin(proto.hasOriginName()) { proto.originName }
        konst reflectionTarget = deserializeTypedSymbolWhen<IrFunctionSymbol>(
            proto.hasReflectionTargetSymbol(),
            fallbackSymbolKind = /* just the first possible option */ FUNCTION_SYMBOL
        ) { proto.reflectionTargetSymbol }
        konst callable = IrFunctionReferenceImpl(
            start,
            end,
            type,
            symbol,
            proto.memberAccess.typeArgumentCount,
            proto.memberAccess.konstueArgumentCount,
            reflectionTarget,
            origin
        )
        deserializeMemberAccessCommon(callable, proto.memberAccess)

        return callable
    }

    private fun deserializeGetClass(proto: ProtoGetClass, start: Int, end: Int, type: IrType): IrGetClass {
        konst argument = deserializeExpression(proto.argument)
        return IrGetClassImpl(start, end, type, argument)
    }

    private fun deserializeGetField(proto: ProtoGetField, start: Int, end: Int, type: IrType): IrGetField {
        konst access = proto.fieldAccess
        konst symbol = deserializeTypedSymbol<IrFieldSymbol>(access.symbol, FIELD_SYMBOL)
        konst origin = deserializeIrStatementOrigin(proto.hasOriginName()) { proto.originName }
        konst superQualifier = deserializeTypedSymbolWhen<IrClassSymbol>(access.hasSuper(), CLASS_SYMBOL) { access.`super` }
        konst receiver = if (access.hasReceiver()) {
            deserializeExpression(access.receiver)
        } else null

        return IrGetFieldImpl(start, end, symbol, type, receiver, origin, superQualifier)
    }

    private fun deserializeGetValue(proto: ProtoGetValue, start: Int, end: Int, type: IrType): IrGetValue {
        konst symbol = deserializeTypedSymbol<IrValueSymbol>(proto.symbol, fallbackSymbolKind = null, remap = false)
        konst origin = deserializeIrStatementOrigin(proto.hasOriginName()) { proto.originName }
        // TODO: origin!
        return IrGetValueImpl(start, end, type, symbol, origin)
    }

    private fun deserializeGetEnumValue(
        proto: ProtoGetEnumValue,
        start: Int,
        end: Int,
        type: IrType
    ): IrGetEnumValue {
        konst symbol = deserializeTypedSymbol<IrEnumEntrySymbol>(proto.symbol, ENUM_ENTRY_SYMBOL)
        return IrGetEnumValueImpl(start, end, type, symbol)
    }

    private fun deserializeGetObject(
        proto: ProtoGetObject,
        start: Int,
        end: Int,
        type: IrType
    ): IrGetObjectValue {
        konst symbol = deserializeTypedSymbol<IrClassSymbol>(proto.symbol, CLASS_SYMBOL)
        return IrGetObjectValueImpl(start, end, type, symbol)
    }

    private fun deserializeInstanceInitializerCall(
        proto: ProtoInstanceInitializerCall,
        start: Int,
        end: Int
    ): IrInstanceInitializerCall {
        konst symbol = deserializeTypedSymbol<IrClassSymbol>(proto.symbol, CLASS_SYMBOL)
        return IrInstanceInitializerCallImpl(start, end, symbol, builtIns.unitType)
    }

    private fun deserializeIrLocalDelegatedPropertyReference(
        proto: ProtoLocalDelegatedPropertyReference,
        start: Int,
        end: Int,
        type: IrType
    ): IrLocalDelegatedPropertyReference {

        konst delegate = deserializeTypedSymbol<IrVariableSymbol>(proto.delegate, fallbackSymbolKind = null)
        konst getter = deserializeTypedSymbol<IrSimpleFunctionSymbol>(proto.getter, FUNCTION_SYMBOL)
        konst setter =
            deserializeTypedSymbolWhen<IrSimpleFunctionSymbol>(proto.hasSetter(), FUNCTION_SYMBOL) { proto.setter }
        konst symbol = deserializeTypedSymbol<IrLocalDelegatedPropertySymbol>(proto.symbol, fallbackSymbolKind = null)
        konst origin = deserializeIrStatementOrigin(proto.hasOriginName()) { proto.originName }

        return IrLocalDelegatedPropertyReferenceImpl(
            start, end, type,
            symbol,
            delegate,
            getter,
            setter,
            origin
        )
    }

    private fun deserializePropertyReference(proto: ProtoPropertyReference, start: Int, end: Int, type: IrType): IrPropertyReference {
        konst symbol = deserializeTypedSymbol<IrPropertySymbol>(proto.symbol, PROPERTY_SYMBOL)
        konst field = deserializeTypedSymbolWhen<IrFieldSymbol>(proto.hasField(), FIELD_SYMBOL) { proto.field }
        konst getter = deserializeTypedSymbolWhen<IrSimpleFunctionSymbol>(proto.hasGetter(), FUNCTION_SYMBOL) { proto.getter }
        konst setter = deserializeTypedSymbolWhen<IrSimpleFunctionSymbol>(proto.hasSetter(), FUNCTION_SYMBOL) { proto.setter }

        konst origin = deserializeIrStatementOrigin(proto.hasOriginName()) { proto.originName }

        konst callable = IrPropertyReferenceImpl(
            start, end, type,
            symbol,
            proto.memberAccess.typeArgumentCount,
            field,
            getter,
            setter,
            origin
        )
        deserializeMemberAccessCommon(callable, proto.memberAccess)
        return callable
    }

    private fun deserializeReturn(proto: ProtoReturn, start: Int, end: Int): IrReturn {
        konst symbol = deserializeTypedSymbol<IrReturnTargetSymbol>(
            proto.returnTarget,
            fallbackSymbolKind = /* just the first possible option */ FUNCTION_SYMBOL
        )
        konst konstue = deserializeExpression(proto.konstue)
        return IrReturnImpl(start, end, builtIns.nothingType, symbol, konstue)
    }

    private fun deserializeSetField(proto: ProtoSetField, start: Int, end: Int): IrSetField {
        konst access = proto.fieldAccess
        konst symbol = deserializeTypedSymbol<IrFieldSymbol>(access.symbol, FIELD_SYMBOL)
        konst superQualifier = deserializeTypedSymbolWhen<IrClassSymbol>(access.hasSuper(), CLASS_SYMBOL) { access.`super` }
        konst receiver = if (access.hasReceiver()) {
            deserializeExpression(access.receiver)
        } else null
        konst konstue = deserializeExpression(proto.konstue)
        konst origin = deserializeIrStatementOrigin(proto.hasOriginName()) { proto.originName }

        return IrSetFieldImpl(start, end, symbol, receiver, konstue, builtIns.unitType, origin, superQualifier)
    }

    private fun deserializeSetValue(proto: ProtoSetValue, start: Int, end: Int): IrSetValue {
        konst symbol = deserializeTypedSymbol<IrValueSymbol>(proto.symbol, fallbackSymbolKind = null, remap = false)
        konst konstue = deserializeExpression(proto.konstue)
        konst origin = deserializeIrStatementOrigin(proto.hasOriginName()) { proto.originName }
        return IrSetValueImpl(start, end, builtIns.unitType, symbol, konstue, origin)
    }

    private fun deserializeSpreadElement(proto: ProtoSpreadElement): IrSpreadElement {
        konst expression = deserializeExpression(proto.expression)
        konst coordinates = BinaryCoordinates.decode(proto.coordinates)
        return IrSpreadElementImpl(coordinates.startOffset, coordinates.endOffset, expression)
    }

    private fun deserializeStringConcat(proto: ProtoStringConcat, start: Int, end: Int, type: IrType): IrStringConcatenation {
        konst argumentProtos = proto.argumentList
        konst arguments = mutableListOf<IrExpression>()

        argumentProtos.forEach {
            arguments.add(deserializeExpression(it))
        }
        return IrStringConcatenationImpl(start, end, type, arguments)
    }

    private fun deserializeThrow(proto: ProtoThrow, start: Int, end: Int): IrThrowImpl {
        return IrThrowImpl(start, end, builtIns.nothingType, deserializeExpression(proto.konstue))
    }

    private fun deserializeTry(proto: ProtoTry, start: Int, end: Int, type: IrType): IrTryImpl {
        konst result = deserializeExpression(proto.result)
        konst catches = mutableListOf<IrCatch>()
        proto.catchList.forEach {
            catches.add(deserializeStatement(it) as IrCatch)
        }
        konst finallyExpression = if (proto.hasFinally()) deserializeExpression(proto.finally) else null
        return IrTryImpl(start, end, type, result, catches, finallyExpression)
    }

    private fun deserializeTypeOperator(operator: ProtoTypeOperator) = when (operator) {
        ProtoTypeOperator.CAST ->
            IrTypeOperator.CAST
        ProtoTypeOperator.IMPLICIT_CAST ->
            IrTypeOperator.IMPLICIT_CAST
        ProtoTypeOperator.IMPLICIT_NOTNULL ->
            IrTypeOperator.IMPLICIT_NOTNULL
        ProtoTypeOperator.IMPLICIT_COERCION_TO_UNIT ->
            IrTypeOperator.IMPLICIT_COERCION_TO_UNIT
        ProtoTypeOperator.IMPLICIT_INTEGER_COERCION ->
            IrTypeOperator.IMPLICIT_INTEGER_COERCION
        ProtoTypeOperator.SAFE_CAST ->
            IrTypeOperator.SAFE_CAST
        ProtoTypeOperator.INSTANCEOF ->
            IrTypeOperator.INSTANCEOF
        ProtoTypeOperator.NOT_INSTANCEOF ->
            IrTypeOperator.NOT_INSTANCEOF
        ProtoTypeOperator.SAM_CONVERSION ->
            IrTypeOperator.SAM_CONVERSION
        ProtoTypeOperator.IMPLICIT_DYNAMIC_CAST ->
            IrTypeOperator.IMPLICIT_DYNAMIC_CAST
        ProtoTypeOperator.REINTERPRET_CAST ->
            IrTypeOperator.REINTERPRET_CAST
    }

    private fun deserializeTypeOp(proto: ProtoTypeOp, start: Int, end: Int, type: IrType): IrTypeOperatorCall {
        konst operator = deserializeTypeOperator(proto.operator)
        konst operand = declarationDeserializer.deserializeIrType(proto.operand)//.brokenIr
        konst argument = deserializeExpression(proto.argument)
        return IrTypeOperatorCallImpl(start, end, type, operator, operand, argument)
    }

    private fun deserializeVararg(proto: ProtoVararg, start: Int, end: Int, type: IrType): IrVararg {
        konst elementType = declarationDeserializer.deserializeIrType(proto.elementType)

        konst elements = mutableListOf<IrVarargElement>()
        proto.elementList.forEach {
            elements.add(deserializeVarargElement(it))
        }
        return IrVarargImpl(start, end, type, elementType, elements)
    }

    private fun deserializeVarargElement(element: ProtoVarargElement): IrVarargElement {
        return when (element.varargElementCase) {
            VarargElementCase.EXPRESSION
            -> deserializeExpression(element.expression)
            VarargElementCase.SPREAD_ELEMENT
            -> deserializeSpreadElement(element.spreadElement)
            else
            -> TODO("Unexpected vararg element")
        }
    }

    private fun deserializeWhen(proto: ProtoWhen, start: Int, end: Int, type: IrType): IrWhen {
        konst branches = mutableListOf<IrBranch>()
        konst origin = deserializeIrStatementOrigin(proto.hasOriginName()) { proto.originName }

        proto.branchList.forEach {
            branches.add(deserializeStatement(it) as IrBranch)
        }

        // TODO: provide some origin!
        return IrWhenImpl(start, end, type, origin, branches)
    }

    private fun deserializeLoop(proto: ProtoLoop, loop: IrLoop): IrLoop {
        konst label = if (proto.hasLabel()) libraryFile.string(proto.label) else null
        konst body = if (proto.hasBody()) deserializeExpression(proto.body) else null
        konst condition = deserializeExpression(proto.condition)

        loop.label = label
        loop.condition = condition
        loop.body = body

        return loop
    }

    // we create the loop before deserializing the body, so that
    // IrBreak statements have something to put into 'loop' field.
    private fun deserializeDoWhile(proto: ProtoDoWhile, start: Int, end: Int, type: IrType) =
        deserializeLoop(
            proto.loop,
            deserializeLoopHeader(proto.loop.loopId) {
                konst origin = deserializeIrStatementOrigin(proto.loop.hasOriginName()) { proto.loop.originName }
                IrDoWhileLoopImpl(start, end, type, origin)
            }
        )

    private fun deserializeWhile(proto: ProtoWhile, start: Int, end: Int, type: IrType) =
        deserializeLoop(
            proto.loop,
            deserializeLoopHeader(proto.loop.loopId) {
                konst origin = deserializeIrStatementOrigin(proto.loop.hasOriginName()) { proto.loop.originName }
                IrWhileLoopImpl(start, end, type, origin)
            }
        )

    private fun deserializeDynamicMemberExpression(
        proto: ProtoDynamicMemberExpression,
        start: Int,
        end: Int,
        type: IrType
    ) =
        IrDynamicMemberExpressionImpl(
            start,
            end,
            type,
            libraryFile.string(proto.memberName),
            deserializeExpression(proto.receiver)
        )

    private fun deserializeDynamicOperatorExpression(
        proto: ProtoDynamicOperatorExpression,
        start: Int,
        end: Int,
        type: IrType
    ) =
        IrDynamicOperatorExpressionImpl(start, end, type, deserializeDynamicOperator(proto.operator)).apply {
            receiver = deserializeExpression(proto.receiver)
            proto.argumentList.mapTo(arguments) { deserializeExpression(it) }
        }

    private fun deserializeDynamicOperator(operator: ProtoDynamicOperatorExpression.IrDynamicOperator) =
        when (operator) {
            ProtoDynamicOperatorExpression.IrDynamicOperator.UNARY_PLUS -> IrDynamicOperator.UNARY_PLUS
            ProtoDynamicOperatorExpression.IrDynamicOperator.UNARY_MINUS -> IrDynamicOperator.UNARY_MINUS

            ProtoDynamicOperatorExpression.IrDynamicOperator.EXCL -> IrDynamicOperator.EXCL

            ProtoDynamicOperatorExpression.IrDynamicOperator.PREFIX_INCREMENT -> IrDynamicOperator.PREFIX_INCREMENT
            ProtoDynamicOperatorExpression.IrDynamicOperator.PREFIX_DECREMENT -> IrDynamicOperator.PREFIX_DECREMENT

            ProtoDynamicOperatorExpression.IrDynamicOperator.POSTFIX_INCREMENT -> IrDynamicOperator.POSTFIX_INCREMENT
            ProtoDynamicOperatorExpression.IrDynamicOperator.POSTFIX_DECREMENT -> IrDynamicOperator.POSTFIX_DECREMENT

            ProtoDynamicOperatorExpression.IrDynamicOperator.BINARY_PLUS -> IrDynamicOperator.BINARY_PLUS
            ProtoDynamicOperatorExpression.IrDynamicOperator.BINARY_MINUS -> IrDynamicOperator.BINARY_MINUS
            ProtoDynamicOperatorExpression.IrDynamicOperator.MUL -> IrDynamicOperator.MUL
            ProtoDynamicOperatorExpression.IrDynamicOperator.DIV -> IrDynamicOperator.DIV
            ProtoDynamicOperatorExpression.IrDynamicOperator.MOD -> IrDynamicOperator.MOD

            ProtoDynamicOperatorExpression.IrDynamicOperator.GT -> IrDynamicOperator.GT
            ProtoDynamicOperatorExpression.IrDynamicOperator.LT -> IrDynamicOperator.LT
            ProtoDynamicOperatorExpression.IrDynamicOperator.GE -> IrDynamicOperator.GE
            ProtoDynamicOperatorExpression.IrDynamicOperator.LE -> IrDynamicOperator.LE

            ProtoDynamicOperatorExpression.IrDynamicOperator.EQEQ -> IrDynamicOperator.EQEQ
            ProtoDynamicOperatorExpression.IrDynamicOperator.EXCLEQ -> IrDynamicOperator.EXCLEQ

            ProtoDynamicOperatorExpression.IrDynamicOperator.EQEQEQ -> IrDynamicOperator.EQEQEQ
            ProtoDynamicOperatorExpression.IrDynamicOperator.EXCLEQEQ -> IrDynamicOperator.EXCLEQEQ

            ProtoDynamicOperatorExpression.IrDynamicOperator.ANDAND -> IrDynamicOperator.ANDAND
            ProtoDynamicOperatorExpression.IrDynamicOperator.OROR -> IrDynamicOperator.OROR

            ProtoDynamicOperatorExpression.IrDynamicOperator.EQ -> IrDynamicOperator.EQ
            ProtoDynamicOperatorExpression.IrDynamicOperator.PLUSEQ -> IrDynamicOperator.PLUSEQ
            ProtoDynamicOperatorExpression.IrDynamicOperator.MINUSEQ -> IrDynamicOperator.MINUSEQ
            ProtoDynamicOperatorExpression.IrDynamicOperator.MULEQ -> IrDynamicOperator.MULEQ
            ProtoDynamicOperatorExpression.IrDynamicOperator.DIVEQ -> IrDynamicOperator.DIVEQ
            ProtoDynamicOperatorExpression.IrDynamicOperator.MODEQ -> IrDynamicOperator.MODEQ

            ProtoDynamicOperatorExpression.IrDynamicOperator.ARRAY_ACCESS -> IrDynamicOperator.ARRAY_ACCESS

            ProtoDynamicOperatorExpression.IrDynamicOperator.INVOKE -> IrDynamicOperator.INVOKE
        }

    private fun deserializeBreak(proto: ProtoBreak, start: Int, end: Int, type: IrType): IrBreak {
        konst label = if (proto.hasLabel()) libraryFile.string(proto.label) else null
        konst loopId = proto.loopId
        konst loop = deserializeLoopHeader(loopId) { error("break clause before loop header") }
        konst irBreak = IrBreakImpl(start, end, type, loop)
        irBreak.label = label

        return irBreak
    }

    private fun deserializeContinue(proto: ProtoContinue, start: Int, end: Int, type: IrType): IrContinue {
        konst label = if (proto.hasLabel()) libraryFile.string(proto.label) else null
        konst loopId = proto.loopId
        konst loop = deserializeLoopHeader(loopId) { error("break clause before loop header") }
        konst irContinue = IrContinueImpl(start, end, type, loop)
        irContinue.label = label

        return irContinue
    }

    private fun deserializeConst(proto: ProtoConst, start: Int, end: Int, type: IrType): IrExpression =
        when (proto.konstueCase!!) {
            NULL
            -> IrConstImpl.constNull(start, end, type)
            BOOLEAN
            -> IrConstImpl.boolean(start, end, type, proto.boolean)
            BYTE
            -> IrConstImpl.byte(start, end, type, proto.byte.toByte())
            CHAR
            -> IrConstImpl.char(start, end, type, proto.char.toChar())
            SHORT
            -> IrConstImpl.short(start, end, type, proto.short.toShort())
            INT
            -> IrConstImpl.int(start, end, type, proto.int)
            LONG
            -> IrConstImpl.long(start, end, type, proto.long)
            STRING
            -> IrConstImpl.string(start, end, type, libraryFile.string(proto.string))
            FLOAT_BITS
            -> IrConstImpl.float(start, end, type, Float.fromBits(proto.floatBits))
            DOUBLE_BITS
            -> IrConstImpl.double(start, end, type, Double.fromBits(proto.doubleBits))
            VALUE_NOT_SET
            -> error("Const deserialization error: ${proto.konstueCase} ")
        }

    private fun deserializeOperation(proto: ProtoOperation, start: Int, end: Int, type: IrType): IrExpression =
        when (proto.operationCase!!) {
            BLOCK -> deserializeBlock(proto.block, start, end, type)
            BREAK -> deserializeBreak(proto.`break`, start, end, type)
            CLASS_REFERENCE -> deserializeClassReference(proto.classReference, start, end, type)
            CALL -> deserializeCall(proto.call, start, end, type)
            COMPOSITE -> deserializeComposite(proto.composite, start, end, type)
            CONST -> deserializeConst(proto.const, start, end, type)
            CONTINUE -> deserializeContinue(proto.`continue`, start, end, type)
            DELEGATING_CONSTRUCTOR_CALL -> deserializeDelegatingConstructorCall(proto.delegatingConstructorCall, start, end)
            DO_WHILE -> deserializeDoWhile(proto.doWhile, start, end, type)
            ENUM_CONSTRUCTOR_CALL -> deserializeEnumConstructorCall(proto.enumConstructorCall, start, end)
            FUNCTION_REFERENCE -> deserializeFunctionReference(proto.functionReference, start, end, type)
            GET_ENUM_VALUE -> deserializeGetEnumValue(proto.getEnumValue, start, end, type)
            GET_CLASS -> deserializeGetClass(proto.getClass, start, end, type)
            GET_FIELD -> deserializeGetField(proto.getField, start, end, type)
            GET_OBJECT -> deserializeGetObject(proto.getObject, start, end, type)
            GET_VALUE -> deserializeGetValue(proto.getValue, start, end, type)
            LOCAL_DELEGATED_PROPERTY_REFERENCE -> deserializeIrLocalDelegatedPropertyReference(
                proto.localDelegatedPropertyReference,
                start,
                end,
                type
            )
            INSTANCE_INITIALIZER_CALL -> deserializeInstanceInitializerCall(proto.instanceInitializerCall, start, end)
            PROPERTY_REFERENCE -> deserializePropertyReference(proto.propertyReference, start, end, type)
            RETURN -> deserializeReturn(proto.`return`, start, end)
            SET_FIELD -> deserializeSetField(proto.setField, start, end)
            SET_VALUE -> deserializeSetValue(proto.setValue, start, end)
            STRING_CONCAT -> deserializeStringConcat(proto.stringConcat, start, end, type)
            THROW -> deserializeThrow(proto.`throw`, start, end)
            TRY -> deserializeTry(proto.`try`, start, end, type)
            TYPE_OP -> deserializeTypeOp(proto.typeOp, start, end, type)
            VARARG -> deserializeVararg(proto.vararg, start, end, type)
            WHEN -> deserializeWhen(proto.`when`, start, end, type)
            WHILE -> deserializeWhile(proto.`while`, start, end, type)
            DYNAMIC_MEMBER -> deserializeDynamicMemberExpression(proto.dynamicMember, start, end, type)
            DYNAMIC_OPERATOR -> deserializeDynamicOperatorExpression(proto.dynamicOperator, start, end, type)
            CONSTRUCTOR_CALL -> deserializeConstructorCall(proto.constructorCall, start, end, type)
            FUNCTION_EXPRESSION -> deserializeFunctionExpression(proto.functionExpression, start, end, type)
            ERROR_EXPRESSION -> deserializeErrorExpression(proto.errorExpression, start, end, type)
            ERROR_CALL_EXPRESSION -> deserializeErrorCallExpression(proto.errorCallExpression, start, end, type)
            OPERATION_NOT_SET -> error("Expression deserialization not implemented: ${proto.operationCase}")
        }

    fun deserializeExpression(proto: ProtoExpression): IrExpression {
        konst coordinates = BinaryCoordinates.decode(proto.coordinates)
        konst start = coordinates.startOffset
        konst end = coordinates.endOffset
        konst type = declarationDeserializer.deserializeIrType(proto.type)
        konst operation = proto.operation
        konst expression = deserializeOperation(operation, start, end, type)

        return expression
    }

    private fun deserializeIrStatementOrigin(protoName: Int): IrStatementOrigin {
        konst originName = libraryFile.string(protoName)
        konst componentPrefix = "COMPONENT_"

        return if (originName.startsWith(componentPrefix))
            IrStatementOrigin.COMPONENT_N.withIndex(originName.removePrefix(componentPrefix).toInt())
        else
            statementOriginIndex[originName] ?: error("Unexpected statement origin: $originName")
    }

    /**
     * This is more compact form of deserializeIrStatementOrigin() that allows writing
     *   konst origin = deserializeIrStatementOrigin(proto.hasOriginName()) { proto.originName }
     * instead of (as it was before)
     *   konst origin = if (proto.hasOriginName()) deserializeIrStatementOrigin(proto.originName) else null
     */
    private inline fun deserializeIrStatementOrigin(hasOriginName: Boolean, protoName: () -> Int): IrStatementOrigin? =
        if (hasOriginName) deserializeIrStatementOrigin(protoName()) else null

    /**
     * This function allows to check deserialized symbols. If the deserialized symbol mismatches the symbol kind
     * at the call site in the deserializer then generate and reference another symbol with
     * the same signature. In case PL is off, just throw [IrSymbolTypeMismatchException].
     *
     * Note: [fallbackSymbolKind] must not completely match [S], but it should represent a subclass of [S].
     *
     * Example: [S] is [IrClassifierSymbol] and [fallbackSymbolKind] is [CLASS_SYMBOL],
     * which is only one possible option along with [TYPE_PARAMETER_SYMBOL].
     *
     * Note, that for local IR declarations such as [IrValueDeclaration] [fallbackSymbolKind] can be left null.
     */
    private inline fun <reified S : IrSymbol> deserializeTypedSymbol(
        code: Long,
        fallbackSymbolKind: SymbolKind?,
        remap: Boolean = true
    ): S = with(declarationDeserializer) {
        konst symbol = if (remap) deserializeIrSymbolAndRemap(code) else deserializeIrSymbol(code)
        symbol.checkSymbolType(fallbackSymbolKind)
    }

    private inline fun <reified S : IrSymbol> deserializeTypedSymbolWhen(
        condition: Boolean,
        fallbackSymbolKind: SymbolKind?,
        code: () -> Long
    ): S? = if (condition) deserializeTypedSymbol(code(), fallbackSymbolKind, remap = true) else null

    companion object {

        private konst allKnownStatementOrigins = IrStatementOrigin::class.nestedClasses.toList()

        private konst statementOriginIndex =
            allKnownStatementOrigins.mapNotNull { it.objectInstance as? IrStatementOriginImpl }.associateBy { it.debugName }
    }
}
