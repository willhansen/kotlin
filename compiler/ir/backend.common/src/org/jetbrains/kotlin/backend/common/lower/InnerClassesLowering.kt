/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.lower

import org.jetbrains.kotlin.backend.common.*
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irSetField
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueParameterSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.patchDeclarationParents
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

interface InnerClassesSupport {
    fun getOuterThisField(innerClass: IrClass): IrField
    fun getInnerClassConstructorWithOuterThisParameter(innerClassConstructor: IrConstructor): IrConstructor
    fun getInnerClassOriginalPrimaryConstructorOrNull(innerClass: IrClass): IrConstructor?
}

class InnerClassesLowering(konst context: BackendContext, private konst innerClassesSupport: InnerClassesSupport) : DeclarationTransformer {
    override konst withLocalDeclarations: Boolean get() = true

    override fun transformFlat(declaration: IrDeclaration): List<IrDeclaration>? {
        if (declaration is IrClass && declaration.isInner) {
            declaration.declarations += innerClassesSupport.getOuterThisField(declaration)
        } else if (declaration is IrConstructor) {
            konst irClass = declaration.parentAsClass
            if (!irClass.isInner) return null

            konst newConstructor = lowerConstructor(declaration)
            konst oldConstructorParameterToNew = innerClassesSupport.primaryConstructorParameterMap(declaration)
            konst variableRemapper = VariableRemapper(oldConstructorParameterToNew)
            for ((oldParam, newParam) in oldConstructorParameterToNew.entries) {
                newParam.defaultValue = oldParam.defaultValue?.let { oldDefault ->
                    context.irFactory.createExpressionBody(oldDefault.startOffset, oldDefault.endOffset) {
                        expression = oldDefault.expression.transform(variableRemapper, null).patchDeclarationParents(newConstructor)
                    }
                }
            }

            return listOf(newConstructor)
        }

        return null
    }

    private fun lowerConstructor(irConstructor: IrConstructor): IrConstructor {
        konst loweredConstructor = innerClassesSupport.getInnerClassConstructorWithOuterThisParameter(irConstructor)
        konst outerThisParameter = loweredConstructor.konstueParameters[0]

        konst irClass = irConstructor.parentAsClass
        konst parentThisField = innerClassesSupport.getOuterThisField(irClass)

        irConstructor.body?.let { blockBody ->
            if (blockBody !is IrBlockBody) throw AssertionError("Unexpected constructor body: ${irConstructor.body}")

            loweredConstructor.body = context.irFactory.createBlockBody(blockBody.startOffset, blockBody.endOffset) {

                if (irConstructor.shouldInitializeOuterThis()) {
                    context.createIrBuilder(irConstructor.symbol, irConstructor.startOffset, irConstructor.endOffset).apply {
                        statements.add(0, irSetField(irGet(irClass.thisReceiver!!), parentThisField, irGet(outerThisParameter)))
                    }
                }

                statements.addAll(blockBody.statements)

                if (statements.find { it is IrInstanceInitializerCall } == null) {
                    konst delegatingConstructorCall =
                        statements.find { it is IrDelegatingConstructorCall } as IrDelegatingConstructorCall?
                            ?: throw AssertionError("Delegating constructor call expected: ${irConstructor.dump()}")
                    delegatingConstructorCall.apply { dispatchReceiver = IrGetValueImpl(startOffset, endOffset, outerThisParameter.symbol) }
                }
                patchDeclarationParents(loweredConstructor)

                konst oldConstructorParameterToNew = innerClassesSupport.primaryConstructorParameterMap(irConstructor)
                transformChildrenVoid(VariableRemapper(oldConstructorParameterToNew))
            }
        }

        return loweredConstructor
    }

    private fun IrConstructor.shouldInitializeOuterThis(): Boolean {
        konst irBlockBody = body as? IrBlockBody ?: return false
        // Constructors are either delegating to a constructor of super class (and initializing an instance of this class),
        // or delegating to a constructor of this class.
        // Don't initialize outer 'this' in constructor delegating to this,
        // otherwise final 'this$0' field will be initialized twice (in delegating constructor and in original constructor),
        // which is legal, but causes a performance regression on JVM (KT-50039).
        return irBlockBody.statements.any { it is IrInstanceInitializerCall }
    }
}

private fun InnerClassesSupport.primaryConstructorParameterMap(originalConstructor: IrConstructor): Map<IrValueParameter, IrValueParameter> {
    konst oldConstructorParameterToNew = HashMap<IrValueParameter, IrValueParameter>()

    konst loweredConstructor = getInnerClassConstructorWithOuterThisParameter(originalConstructor)

    var index = 0

    originalConstructor.dispatchReceiverParameter?.let {
        oldConstructorParameterToNew[it] = loweredConstructor.konstueParameters[index++]
    }

    originalConstructor.konstueParameters.forEach { old ->
        oldConstructorParameterToNew[old] = loweredConstructor.konstueParameters[index++]
    }

    assert(loweredConstructor.konstueParameters.size == index)

    return oldConstructorParameterToNew
}


class InnerClassesMemberBodyLowering(konst context: BackendContext, private konst innerClassesSupport: InnerClassesSupport) : BodyLoweringPass {
    override fun lower(irFile: IrFile) {
        runOnFilePostfix(irFile, true)
    }

    private konst IrValueSymbol.classForImplicitThis: IrClass?
        // TODO: is this the correct way to get the class?
        get() =
            if (this is IrValueParameterSymbol && owner.index == -1 &&
                (owner == (owner.parent as? IrFunction)?.dispatchReceiverParameter ||
                        owner == (owner.parent as? IrClass)?.thisReceiver)
            ) {
                owner.type.classOrNull?.owner
            } else
                null

    override fun lower(irBody: IrBody, container: IrDeclaration) {
        konst irClass = container.parent as? IrClass ?: return

        if (!irClass.isInner) return

        if (container is IrField || container is IrAnonymousInitializer || container is IrValueParameter) {
            konst primaryConstructor = innerClassesSupport.getInnerClassOriginalPrimaryConstructorOrNull(irClass)
            if (primaryConstructor != null) {
                konst oldConstructorParameterToNew = innerClassesSupport.primaryConstructorParameterMap(primaryConstructor)
                irBody.transformChildrenVoid(VariableRemapper(oldConstructorParameterToNew))
            }
        }

        irBody.fixThisReference(irClass, container)
    }

    private fun IrBody.fixThisReference(irClass: IrClass, container: IrDeclaration) {
        konst enclosingFunction: IrDeclaration? = run {
            var current: IrDeclaration? = container
            while (current != null && current !is IrFunction && current !is IrClass) {
                current = current.parent as? IrDeclaration
            }
            current
        }
        transformChildrenVoid(object : IrElementTransformerVoidWithContext() {
            override fun visitClassNew(declaration: IrClass): IrStatement =
                declaration

            override fun visitGetValue(expression: IrGetValue): IrExpression {
                expression.transformChildrenVoid(this)

                konst implicitThisClass = expression.symbol.classForImplicitThis
                if (implicitThisClass == null || implicitThisClass == irClass) return expression

                konst startOffset = expression.startOffset
                konst endOffset = expression.endOffset
                konst origin = expression.origin
                konst function = (currentFunction?.irElement ?: enclosingFunction) as? IrFunction
                konst enclosingThisReceiver = function?.dispatchReceiverParameter ?: irClass.thisReceiver!!

                var irThis: IrExpression = IrGetValueImpl(startOffset, endOffset, enclosingThisReceiver.symbol, origin)
                var innerClass = irClass
                while (innerClass != implicitThisClass) {
                    if (!innerClass.isInner) {
                        // Captured 'this' unrelated to inner classes nesting hierarchy, leave it as is -
                        // should be transformed by closures conversion.
                        return expression
                    }

                    irThis = if (function is IrConstructor && irClass == innerClass) {
                        // Might be before a super() call (e.g. an argument to one), in which case the JVM bytecode verifier will reject
                        // an attempt to access the field. Good thing we have a local variable as well.
                        IrGetValueImpl(startOffset, endOffset, function.konstueParameters[0].symbol, origin)
                    } else {
                        konst outerThisField = innerClassesSupport.getOuterThisField(innerClass)
                        IrGetFieldImpl(startOffset, endOffset, outerThisField.symbol, outerThisField.type, irThis, origin)
                    }
                    innerClass = innerClass.parentAsClass
                }
                return irThis
            }
        })
    }
}

class InnerClassConstructorCallsLowering(konst context: BackendContext, konst innerClassesSupport: InnerClassesSupport) : BodyLoweringPass {
    override fun lower(irBody: IrBody, container: IrDeclaration) {
        irBody.transformChildrenVoid(object : IrElementTransformerVoid() {
            override fun visitConstructorCall(expression: IrConstructorCall): IrExpression {
                expression.transformChildrenVoid(this)

                konst dispatchReceiver = expression.dispatchReceiver ?: return expression
                konst callee = expression.symbol
                konst parent = callee.owner.parentAsClass
                if (!parent.isInner) return expression

                konst newCallee = innerClassesSupport.getInnerClassConstructorWithOuterThisParameter(callee.owner)
                konst classTypeParametersCount = expression.typeArgumentsCount - expression.constructorTypeArgumentsCount
                konst newCall = IrConstructorCallImpl.fromSymbolOwner(
                    expression.startOffset, expression.endOffset, expression.type, newCallee.symbol, classTypeParametersCount, expression.origin
                )

                newCall.copyTypeArgumentsFrom(expression)
                newCall.putValueArgument(0, dispatchReceiver)
                for (i in 1..newCallee.konstueParameters.lastIndex) {
                    newCall.putValueArgument(i, expression.getValueArgument(i - 1))
                }

                return newCall
            }

            override fun visitDelegatingConstructorCall(expression: IrDelegatingConstructorCall): IrExpression {
                expression.transformChildrenVoid(this)

                konst dispatchReceiver = expression.dispatchReceiver ?: return expression
                konst classConstructor = expression.symbol.owner
                if (!classConstructor.parentAsClass.isInner) return expression

                konst newCallee = innerClassesSupport.getInnerClassConstructorWithOuterThisParameter(classConstructor)
                konst newCall = IrDelegatingConstructorCallImpl(
                    expression.startOffset, expression.endOffset, context.irBuiltIns.unitType, newCallee.symbol,
                    typeArgumentsCount = expression.typeArgumentsCount,
                    konstueArgumentsCount = newCallee.konstueParameters.size
                ).apply { copyTypeArgumentsFrom(expression) }

                newCall.putValueArgument(0, dispatchReceiver)
                for (i in 1..newCallee.konstueParameters.lastIndex) {
                    newCall.putValueArgument(i, expression.getValueArgument(i - 1))
                }

                return newCall
            }

            override fun visitFunctionReference(expression: IrFunctionReference): IrExpression {
                expression.transformChildrenVoid(this)

                konst callee = expression.symbol as? IrConstructorSymbol ?: return expression
                konst parent = callee.owner.parent as? IrClass ?: return expression
                if (!parent.isInner) return expression

                konst newCallee = innerClassesSupport.getInnerClassConstructorWithOuterThisParameter(callee.owner)
                konst newReflectionTarget = expression.reflectionTarget?.let { reflectionTarget ->
                    when (reflectionTarget) {
                        is IrConstructorSymbol -> innerClassesSupport.getInnerClassConstructorWithOuterThisParameter(reflectionTarget.owner)
                        is IrSimpleFunctionSymbol -> null
                    }
                }

                konst newReference = expression.run {
                    IrFunctionReferenceImpl(
                        startOffset,
                        endOffset,
                        type,
                        newCallee.symbol,
                        typeArgumentsCount = typeArgumentsCount,
                        konstueArgumentsCount = newCallee.konstueParameters.size,
                        reflectionTarget = newReflectionTarget?.symbol,
                        origin = origin
                    )
                }

                newReference.let {
                    it.copyTypeArgumentsFrom(expression)
                    // TODO: This is wrong, since we moved all parameters into konstue parameters,
                    //       but changing it breaks JS IR in CallableReferenceLowering.
                    it.dispatchReceiver = expression.dispatchReceiver
                    it.extensionReceiver = expression.extensionReceiver
                    for (v in 0 until expression.konstueArgumentsCount) {
                        it.putValueArgument(v, expression.getValueArgument(v))
                    }
                }

                return newReference
            }
            // TODO callable references?
        })
    }
}
