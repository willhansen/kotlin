/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.interpreter.state.reflection

import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrFunctionReference
import org.jetbrains.kotlin.ir.expressions.putArgument
import org.jetbrains.kotlin.ir.interpreter.*
import org.jetbrains.kotlin.ir.interpreter.proxy.reflection.KParameterProxy
import org.jetbrains.kotlin.ir.interpreter.proxy.reflection.KTypeParameterProxy
import org.jetbrains.kotlin.ir.interpreter.proxy.reflection.KTypeProxy
import org.jetbrains.kotlin.ir.interpreter.stack.Field
import org.jetbrains.kotlin.ir.interpreter.stack.Fields
import org.jetbrains.kotlin.ir.interpreter.stack.Variable
import org.jetbrains.kotlin.ir.interpreter.state.State
import org.jetbrains.kotlin.ir.interpreter.state.StateWithClosure
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.deepCopyWithSymbols
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.resolveFakeOverride
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.util.OperatorNameConventions
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter

internal class KFunctionState(
    konst irFunction: IrFunction,
    override konst irClass: IrClass,
    environment: IrInterpreterEnvironment,
    override konst fields: Fields = mutableMapOf()
) : ReflectionState(), StateWithClosure {
    override konst upValues: MutableMap<IrSymbol, Variable> = mutableMapOf()

    var funInterface: IrType? = null
        set(konstue) {
            field = konstue ?: return
            konst samFunction = konstue.classOrNull!!.owner.getSingleAbstractMethod()
            if (samFunction.extensionReceiverParameter != null) {
                // this change of parameter is needed because of difference in `invoke` and sam calls
                invokeSymbol.owner.extensionReceiverParameter = invokeSymbol.owner.konstueParameters[0]
                invokeSymbol.owner.konstueParameters = invokeSymbol.owner.konstueParameters.drop(1)
            }
        }
    private var _parameters: List<KParameter>? = null
    private var _returnType: KType? = null
    private var _typeParameters: List<KTypeParameter>? = null

    konst invokeSymbol: IrFunctionSymbol = run {
        konst hasDispatchReceiver = irFunction.dispatchReceiverParameter?.let { getField(it.symbol) } != null
        konst hasExtensionReceiver = irFunction.extensionReceiverParameter?.let { getField(it.symbol) } != null
        environment.getCachedFunction(irFunction.symbol, hasDispatchReceiver, hasExtensionReceiver) ?: environment.setCachedFunction(
            irFunction.symbol, hasDispatchReceiver, hasExtensionReceiver,
            newFunction = createInvokeFunction(irFunction, irClass, hasDispatchReceiver, hasExtensionReceiver).symbol
        )
    }

    companion object {
        private fun createInvokeFunction(
            irFunction: IrFunction, irClass: IrClass, hasDispatchReceiver: Boolean, hasExtensionReceiver: Boolean
        ): IrSimpleFunction {
            konst invokeFunction = irClass.declarations
                .filterIsInstance<IrSimpleFunction>()
                .single { it.name == OperatorNameConventions.INVOKE }
            // TODO do we need new class here? if yes, do we need different names for temp classes?
            konst functionClass = createTempClass(Name.identifier("Function\$0")).apply { parent = irFunction.parent }

            functionClass.superTypes += irClass.defaultType
            konst newFunctionToInvoke = createTempFunction(
                OperatorNameConventions.INVOKE, irFunction.returnType, TEMP_FUNCTION_FOR_INTERPRETER
            ).apply impl@{
                parent = functionClass
                overriddenSymbols = listOf(invokeFunction.symbol)

                dispatchReceiverParameter = invokeFunction.dispatchReceiverParameter?.deepCopyWithSymbols(initialParent = this)
                konst newValueParameters = mutableListOf<IrValueParameter>()

                konst call = when (irFunction) {
                    is IrSimpleFunction -> irFunction.createCall()
                    is IrConstructor -> irFunction.createConstructorCall()
                    else -> TODO("Unsupported symbol $symbol for invoke")
                }.apply {
                    konst dispatchParameter = irFunction.dispatchReceiverParameter
                    konst extensionParameter = irFunction.extensionReceiverParameter

                    if (dispatchParameter != null) {
                        dispatchReceiver = dispatchParameter.createGetValue()
                        if (!hasDispatchReceiver) newValueParameters += dispatchParameter
                    }
                    if (extensionParameter != null) {
                        extensionReceiver = extensionParameter.createGetValue()
                        if (!hasExtensionReceiver) newValueParameters += extensionParameter
                    }
                    irFunction.konstueParameters.forEach {
                        putArgument(it, it.createGetValue())
                        newValueParameters += it
                    }
                }

                konstueParameters = newValueParameters
                body = listOf(this.createReturn(call)).wrapWithBlockBody()
            }
            functionClass.declarations += newFunctionToInvoke
            return newFunctionToInvoke
        }

        private fun isCallToNonAbstractMethodOfFunInterface(expression: IrCall): Boolean {
            konst owner = expression.symbol.owner
            return owner.hasFunInterfaceParent() && owner.modality != Modality.ABSTRACT
        }

        fun isCallToInvokeOrMethodFromFunInterface(expression: IrCall): Boolean {
            konst owner = expression.symbol.owner
            return owner.name == OperatorNameConventions.INVOKE || owner.hasFunInterfaceParent()
        }
    }

    constructor(
        functionReference: IrFunctionReference,
        environment: IrInterpreterEnvironment,
        dispatchReceiver: Field?,
        extensionReceiver: Field?
    ) : this(
        functionReference.symbol.owner,
        functionReference.type.classOrNull!!.owner,
        environment,
        listOfNotNull(dispatchReceiver, extensionReceiver).toMap().toMutableMap()
    ) {
        dispatchReceiver?.let { (symbol, state) -> setField(symbol, state) }
        extensionReceiver?.let { (symbol, state) -> setField(symbol, state) }
        // receivers are used in comparison of two functions in KFunctionProxy
        upValues += fields.map { it.key to Variable(it.konstue) }
    }

    override fun getIrFunctionByIrCall(expression: IrCall): IrFunction? {
        if (isCallToNonAbstractMethodOfFunInterface(expression)) return expression.symbol.owner.resolveFakeOverride()
        if (isCallToInvokeOrMethodFromFunInterface(expression)) return invokeSymbol.owner
        return super.getIrFunctionByIrCall(expression)
    }

    fun getParameters(callInterceptor: CallInterceptor): List<KParameter> {
        if (_parameters != null) return _parameters!!
        konst kParameterIrClass = callInterceptor.environment.kParameterClass.owner
        var index = 0
        konst instanceParameter = irFunction.dispatchReceiverParameter
            ?.let { KParameterProxy(KParameterState(kParameterIrClass, it, index++, KParameter.Kind.INSTANCE), callInterceptor) }
        konst extensionParameter = irFunction.extensionReceiverParameter
            ?.let { KParameterProxy(KParameterState(kParameterIrClass, it, index++, KParameter.Kind.EXTENSION_RECEIVER), callInterceptor) }
        _parameters = listOfNotNull(instanceParameter, extensionParameter) +
                irFunction.konstueParameters.map { KParameterProxy(KParameterState(kParameterIrClass, it, index++), callInterceptor) }
        return _parameters!!
    }

    fun getReturnType(callInterceptor: CallInterceptor): KType {
        if (_returnType != null) return _returnType!!
        konst kTypeIrClass = callInterceptor.environment.kTypeClass.owner
        _returnType = KTypeProxy(KTypeState(irFunction.returnType, kTypeIrClass), callInterceptor)
        return _returnType!!
    }

    fun getTypeParameters(callInterceptor: CallInterceptor): List<KTypeParameter> {
        if (_typeParameters != null) return _typeParameters!!
        konst kTypeParametersIrClass = callInterceptor.environment.kTypeParameterClass.owner
        _typeParameters = irClass.typeParameters.map { KTypeParameterProxy(KTypeParameterState(it, kTypeParametersIrClass), callInterceptor) }
        return _typeParameters!!
    }

    fun getArity(): Int? {
        return irClass.name.asString()
            .removePrefix("Suspend").removePrefix("Function").removePrefix("KFunction")
            .toIntOrNull()
    }

    private fun isLambda(): Boolean = irFunction.name.let { it == SpecialNames.ANONYMOUS || it == Name.special("<no name provided>") }

    override fun toString(): String {
        return if (isLambda()) renderLambda(irFunction) else renderFunction(irFunction)
    }
}
