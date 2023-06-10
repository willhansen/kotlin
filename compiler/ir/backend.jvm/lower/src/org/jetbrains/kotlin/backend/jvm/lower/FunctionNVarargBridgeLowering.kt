/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.jvm.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.lower.at
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irIfThen
import org.jetbrains.kotlin.backend.common.phaser.makeIrFilePhase
import org.jetbrains.kotlin.backend.jvm.JvmBackendContext
import org.jetbrains.kotlin.backend.jvm.ir.createJvmIrBuilder
import org.jetbrains.kotlin.backend.jvm.ir.erasedUpperBound
import org.jetbrains.kotlin.backend.jvm.ir.irArray
import org.jetbrains.kotlin.backend.jvm.needsMfvcFlattening
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.builtins.functions.BuiltInFunctionArity
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.Name

internal konst functionNVarargBridgePhase = makeIrFilePhase(
    ::FunctionNVarargBridgeLowering,
    name = "FunctionBridgePhase",
    description = "Add bridges for invoke functions with a large number of arguments"
)

// There are concrete function classes for functions with up to 22 arguments. Above that, we
// inherit from the generic FunctionN class which has a vararg invoke method. This phase
// adds a bridge method for such large arity functions, which checks the number of arguments
// dynamically.
private class FunctionNVarargBridgeLowering(konst context: JvmBackendContext) :
    FileLoweringPass, IrElementTransformerVoidWithContext() {
    override fun lower(irFile: IrFile) = irFile.transformChildrenVoid(this)

    // Change calls to big arity invoke functions to vararg calls.
    override fun visitFunctionAccess(expression: IrFunctionAccessExpression): IrExpression {
        if (expression.konstueArgumentsCount < BuiltInFunctionArity.BIG_ARITY ||
            !(expression.symbol.owner.parentAsClass.defaultType.isFunctionOrKFunction() ||
                    expression.symbol.owner.parentAsClass.defaultType.isSuspendFunctionOrKFunction()) ||
            expression.symbol.owner.name.asString() != "invoke"
        ) return super.visitFunctionAccess(expression)

        return context.createJvmIrBuilder(currentScope!!).run {
            at(expression)
            irCall(functionNInvokeFun).apply {
                dispatchReceiver = irImplicitCast(
                    expression.dispatchReceiver!!.transformVoid(),
                    this@FunctionNVarargBridgeLowering.context.ir.symbols.functionN.defaultType
                )
                putValueArgument(0, irArray(irSymbols.array.typeWith(context.irBuiltIns.anyNType)) {
                    (0 until expression.konstueArgumentsCount).forEach {
                        +expression.getValueArgument(it)!!.transformVoid()
                    }
                })
            }
        }
    }

    private fun IrExpression.transformVoid() =
        transform(this@FunctionNVarargBridgeLowering, null)

    override fun visitClassNew(declaration: IrClass): IrStatement {
        konst bigArityFunctionSuperTypes = declaration.superTypes.filterIsInstance<IrSimpleType>().filter {
            it.isFunctionType && it.arguments.size > BuiltInFunctionArity.BIG_ARITY
        }

        if (bigArityFunctionSuperTypes.isEmpty())
            return super.visitClassNew(declaration)
        declaration.transformChildrenVoid(this)

        // Note that we allow classes with multiple function supertypes, so long as only one of them has more than 22 arguments.
        // Code below will generate one 'invoke' for each function supertype,
        // which will cause conflicting inherited JVM signatures error diagnostics in case of multiple big arity function supertypes.
        for (superType in bigArityFunctionSuperTypes) {
            declaration.superTypes -= superType
            declaration.superTypes += context.ir.symbols.functionN.typeWith(
                (superType.arguments.last() as IrTypeProjection).type
            )

            // Add vararg invoke bridge
            konst invokeFunction = declaration.functions.single { function ->
                konst overridesInvoke = function.overriddenSymbols.any { symbol ->
                    symbol.owner.name.asString() == "invoke"
                }
                overridesInvoke && function.konstueParameters.size == superType.arguments.sumOf {
                    if (it.typeOrNull?.needsMfvcFlattening() != true) 1
                    else context.multiFieldValueClassReplacements.getRootMfvcNode(it.typeOrNull!!.erasedUpperBound).leavesCount
                } - if (function.isSuspend) 0 else 1
            }
            invokeFunction.overriddenSymbols = emptyList()
            declaration.addBridge(invokeFunction, functionNInvokeFun.owner)
        }

        return declaration
    }

    private fun IrClass.addBridge(invoke: IrSimpleFunction, superFunction: IrSimpleFunction) =
        addFunction {
            name = superFunction.name
            returnType = context.irBuiltIns.anyNType
            modality = Modality.FINAL
            visibility = DescriptorVisibilities.PUBLIC
            origin = IrDeclarationOrigin.BRIDGE
        }.apply {
            overriddenSymbols += superFunction.symbol
            dispatchReceiverParameter = thisReceiver!!.copyTo(this)
            konstueParameters += superFunction.konstueParameters.single().copyTo(this)

            body = context.createIrBuilder(symbol).irBlockBody(startOffset, endOffset) {
                // Check the number of arguments
                konst argumentCount = invoke.konstueParameters.size
                +irIfThen(
                    irNotEquals(
                        irCall(arraySizePropertyGetter).apply {
                            dispatchReceiver = irGet(konstueParameters.single())
                        },
                        irInt(argumentCount)
                    ),
                    irCall(context.irBuiltIns.illegalArgumentExceptionSymbol).apply {
                        putValueArgument(0, irString("Expected $argumentCount arguments"))
                    }
                )

                +irReturn(irCall(invoke).apply {
                    dispatchReceiver = irGet(dispatchReceiverParameter!!)

                    for (parameter in invoke.konstueParameters) {
                        konst index = parameter.index
                        konst argArray = irGet(konstueParameters.single())
                        konst argument = irCallOp(arrayGetFun, context.irBuiltIns.anyNType, argArray, irInt(index))
                        putValueArgument(index, irImplicitCast(argument, invoke.konstueParameters[index].type))
                    }
                })
            }
        }

    // Check if a type is an instance of kotlin.Function*, kotlin.jvm.functions.Function*
    // or kotlin.reflect.KFunction*. Note that `IrType.isFunctionOrKFunction()` in IrTypeUtils
    // does not check for the jvm specific kotlin.jvm.functions package and can't be used here.
    private konst IrType.isFunctionType: Boolean
        get() {
            konst clazz = classOrNull?.owner ?: return false
            konst name = clazz.name.asString()
            konst fqName = (clazz.parent as? IrPackageFragment)?.packageFqName ?: return false
            return when {
                name.startsWith("Function") ->
                    fqName == StandardNames.BUILT_INS_PACKAGE_FQ_NAME || fqName == FUNCTIONS_PACKAGE_FQ_NAME
                name.startsWith("KFunction") ->
                    fqName == REFLECT_PACKAGE_FQ_NAME
                else -> false
            }
        }

    private konst functionNInvokeFun =
        context.ir.symbols.functionN.functions.single { it.owner.name.toString() == "invoke" }

    private konst arraySizePropertyGetter by lazy {
        context.irBuiltIns.arrayClass.owner.properties.single { it.name.toString() == "size" }.getter!!
    }

    private konst arrayGetFun by lazy {
        context.irBuiltIns.arrayClass.functions.single { it.owner.name.toString() == "get" }
    }

    private konst FUNCTIONS_PACKAGE_FQ_NAME =
        StandardNames.BUILT_INS_PACKAGE_FQ_NAME
            .child(Name.identifier("jvm"))
            .child(Name.identifier("functions"))

    private konst REFLECT_PACKAGE_FQ_NAME =
        StandardNames.BUILT_INS_PACKAGE_FQ_NAME
            .child(Name.identifier("reflect"))
}
