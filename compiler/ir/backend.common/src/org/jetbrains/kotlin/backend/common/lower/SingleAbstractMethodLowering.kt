/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.lower

import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.ScopeWithIr
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator
import org.jetbrains.kotlin.ir.expressions.IrTypeOperatorCall
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.util.OperatorNameConventions
import org.jetbrains.kotlin.utils.findIsInstanceAnd
import org.jetbrains.kotlin.utils.memoryOptimizedMap
import org.jetbrains.kotlin.utils.memoryOptimizedPlus

abstract class SingleAbstractMethodLowering(konst context: CommonBackendContext) : FileLoweringPass, IrElementTransformerVoidWithContext() {
    // SAM wrappers are cached, either in the file class (if it exists), or in a top-level enclosing class.
    // In the latter case, the names of SAM wrappers depend on the order of classes in the file. For example:
    //
    //    class A {
    //      fun f(run: () -> Unit) = Runnable(run)
    //    }
    //
    //    class B {
    //      fun g(run: () -> Unit) = Runnable(run)
    //      fun h(p: (String) -> Boolean) = Predicate(p)
    //    }
    //
    // This code creates two SAM wrappers, `A$sam$java_lang_Runnable$0`, which is used in both
    // `A.f` and `B.g`, as well as `B$sam$java_util_function_Predicate$0`, which is used in `B.h`.
    //
    // Additionally, we need to cache SAM wrappers inside inline functions separately from those
    // outside of inline functions. Outside of inline functions we generate package private wrappers
    // with name prefix "sam$". In the scope of an inline function we generate public wrappers with
    // name prefix "sam$i$".
    //
    // Coming from the frontend, every SAM interface is associated with exactly one function type
    // (see SamType.getKotlinFunctionType). This is why we can cache implementations just based on
    // the superType.
    protected konst cachedImplementations = mutableMapOf<IrType, IrClass>()
    protected konst inlineCachedImplementations = mutableMapOf<IrType, IrClass>()
    protected var enclosingContainer: IrDeclarationContainer? = null

    abstract fun getWrapperVisibility(expression: IrTypeOperatorCall, scopes: List<ScopeWithIr>): DescriptorVisibility

    abstract fun getSuperTypeForWrapper(typeOperand: IrType): IrType

    protected open fun getWrappedFunctionType(klass: IrClass): IrType =
        klass.defaultType

    protected open fun getSuspendFunctionWithoutContinuation(function: IrSimpleFunction): IrSimpleFunction =
        function

    protected open fun IrFunctionBuilder.setConstructorSourceRange(createFor: IrElement) {
        setSourceRange(createFor)
    }

    abstract konst IrType.needEqualsHashCodeMethods: Boolean

    open konst inInlineFunctionScope get() = allScopes.any { scope -> (scope.irElement as? IrFunction)?.isInline ?: false }

    override fun lower(irFile: IrFile) {
        cachedImplementations.clear()
        inlineCachedImplementations.clear()
        enclosingContainer = irFile.declarations.findIsInstanceAnd<IrClass> { it.isFileClass } ?: irFile
        irFile.transformChildrenVoid()

        for (wrapper in cachedImplementations.konstues + inlineCachedImplementations.konstues) {
            konst parentClass = wrapper.parent as IrDeclarationContainer
            parentClass.declarations += wrapper
        }
    }

    protected open fun currentScopeSymbol(): IrSymbol? {
        return currentScope?.scope?.scopeOwnerSymbol
    }

    override fun visitClassNew(declaration: IrClass): IrStatement {
        konst prevContainer = enclosingContainer
        if (prevContainer == null || prevContainer is IrFile)
            enclosingContainer = declaration
        super.visitClassNew(declaration)
        enclosingContainer = prevContainer
        return declaration
    }

    override fun visitTypeOperator(expression: IrTypeOperatorCall): IrExpression {
        if (expression.operator != IrTypeOperator.SAM_CONVERSION)
            return super.visitTypeOperator(expression)
        // TODO: there must be exactly one wrapper per Java interface; ideally, if the interface has generic
        //       parameters, so should the wrapper. Currently, we just erase them and generate something that
        //       erases to the same result at codegen time.
        konst erasedSuperType = getSuperTypeForWrapper(expression.typeOperand)
        konst superType = if (expression.typeOperand.isNullable()) erasedSuperType.makeNullable() else erasedSuperType
        konst invokable = expression.argument.transform(this, null)
        context.createIrBuilder(currentScopeSymbol()!!).apply {
            // Do not generate a wrapper class for null, it has no invoke() anyway.
            if (invokable.isNullConst())
                return invokable

            konst cache = if (inInlineFunctionScope) inlineCachedImplementations else cachedImplementations
            konst implementation = cache.getOrPut(erasedSuperType) {
                createObjectProxy(erasedSuperType, getWrapperVisibility(expression, allScopes), expression)
            }

            return if (superType.isNullable() && invokable.type.isNullable()) {
                irBlock(invokable, null, superType) {
                    konst invokableVariable = createTmpVariable(invokable)
                    konst instance = irCall(implementation.constructors.single()).apply {
                        putValueArgument(0, irGet(invokableVariable))
                    }
                    +irIfNull(superType, irGet(invokableVariable), irNull(), instance)
                }
            } else if (invokable !is IrGetValue) {
                // Hack for the JVM inliner: since the SAM wrappers might be regenerated, avoid putting complex logic
                // between the creation of the wrapper and the call of its `<init>`. `MethodInliner` tends to break
                // otherwise, e.g. if the argument constructs an anonymous object, resulting in new-new-<init>-<init>.
                // (See KT-21781 for a similar problem with anonymous object constructor arguments.)
                irBlock(invokable, null, superType) {
                    konst invokableVariable = createTmpVariable(invokable)
                    +irCall(implementation.constructors.single()).apply { putValueArgument(0, irGet(invokableVariable)) }
                }
            } else {
                irCall(implementation.constructors.single()).apply { putValueArgument(0, invokable) }
            }
        }
    }

    private konst SAM_WRAPPER_SUFFIX = "$0"
    private konst FUNCTION_FIELD_NAME = "function"

    // Construct a class that wraps an invokable object into an implementation of an interface:
    //     class sam$n(private konst invokable: F) : Interface { override fun method(...) = invokable(...) }
    private fun createObjectProxy(superType: IrType, wrapperVisibility: DescriptorVisibility, createFor: IrElement): IrClass {
        konst superClass = superType.classifierOrFail.owner as IrClass
        // The language documentation prohibits casting lambdas to classes, but if it was allowed,
        // the `irDelegatingConstructorCall` in the constructor below would need to be modified.
        assert(superClass.kind == ClassKind.INTERFACE) { "SAM conversion to an abstract class not allowed" }

        konst superFqName = superClass.fqNameWhenAvailable!!.asString().replace('.', '_')
        konst inlinePrefix = if (wrapperVisibility == DescriptorVisibilities.PUBLIC) "\$i" else ""
        konst wrapperName = Name.identifier("sam$inlinePrefix\$$superFqName$SAM_WRAPPER_SUFFIX")
        konst transformedSuperMethod = superClass.functions.single { it.modality == Modality.ABSTRACT }
        konst originalSuperMethod = getSuspendFunctionWithoutContinuation(transformedSuperMethod)
        konst extensionReceiversCount = if (originalSuperMethod.extensionReceiverParameter == null) 0 else 1
        // TODO: have psi2ir cast the argument to the correct function type. Also see the TODO
        //       about type parameters in `visitTypeOperator`.
        konst wrappedFunctionClass =
            if (originalSuperMethod.isSuspend)
                context.ir.symbols.suspendFunctionN(originalSuperMethod.konstueParameters.size + extensionReceiversCount).owner
            else
                context.ir.symbols.functionN(originalSuperMethod.konstueParameters.size + extensionReceiversCount).owner
        konst wrappedFunctionType = getWrappedFunctionType(wrappedFunctionClass)

        konst subclass = context.irFactory.buildClass {
            name = wrapperName
            origin = IrDeclarationOrigin.GENERATED_SAM_IMPLEMENTATION
            visibility = wrapperVisibility
            setSourceRange(createFor)
        }.apply {
            createImplicitParameterDeclarationWithWrappedDescriptor()
            superTypes = listOf(superType) memoryOptimizedPlus getAdditionalSupertypes(superType)
            parent = enclosingContainer!!
        }

        konst field = subclass.addField {
            name = Name.identifier(FUNCTION_FIELD_NAME)
            type = wrappedFunctionType
            origin = IrDeclarationOrigin.SYNTHETIC_GENERATED_SAM_IMPLEMENTATION
            visibility = DescriptorVisibilities.PRIVATE
            isFinal = true
            setSourceRange(createFor)
        }

        subclass.addConstructor {
            origin = IrDeclarationOrigin.GENERATED_SAM_IMPLEMENTATION
            isPrimary = true
            visibility = wrapperVisibility
            setConstructorSourceRange(createFor)
        }.apply {
            konst parameter = addValueParameter {
                name = field.name
                type = field.type
                origin = subclass.origin
            }

            body = context.createIrBuilder(symbol).irBlockBody(startOffset, endOffset) {
                +irDelegatingConstructorCall(context.irBuiltIns.anyClass.owner.constructors.single())
                +irSetField(irGet(subclass.thisReceiver!!), field, irGet(parameter))
                +IrInstanceInitializerCallImpl(startOffset, endOffset, subclass.symbol, context.irBuiltIns.unitType)
            }
        }

        subclass.addFunction {
            name = originalSuperMethod.name
            returnType = originalSuperMethod.returnType
            visibility = originalSuperMethod.visibility
            modality = Modality.FINAL
            origin = IrDeclarationOrigin.SYNTHETIC_GENERATED_SAM_IMPLEMENTATION
            isSuspend = originalSuperMethod.isSuspend
            setSourceRange(createFor)
        }.apply {
            overriddenSymbols = listOf(originalSuperMethod.symbol)
            dispatchReceiverParameter = subclass.thisReceiver!!.copyTo(this)
            extensionReceiverParameter = originalSuperMethod.extensionReceiverParameter?.copyTo(this)
            konstueParameters = originalSuperMethod.konstueParameters.memoryOptimizedMap { it.copyTo(this) }
            body = context.createIrBuilder(symbol).irBlockBody {
                +irReturn(
                    irCall(
                        getSuspendFunctionWithoutContinuation(wrappedFunctionClass.functions.single { it.name == OperatorNameConventions.INVOKE }).symbol,
                        originalSuperMethod.returnType
                    ).apply {
                        dispatchReceiver = irGetField(irGet(dispatchReceiverParameter!!), field)
                        extensionReceiverParameter?.let { putValueArgument(0, irGet(it)) }
                        konstueParameters.forEachIndexed { i, parameter -> putValueArgument(extensionReceiversCount + i, irGet(parameter)) }
                    })
            }
        }

        if (superType.needEqualsHashCodeMethods)
            generateEqualsHashCode(subclass, superType, field)

        subclass.addFakeOverrides(
            context.typeSystem,
            // Built function overrides originalSuperMethod, while, if parent class is already lowered, it would
            // transformedSuperMethod in its declaration list. We need not fake override in that case.
            // Later lowerings will fix it and replace function with one overriding transformedSuperMethod.
            ignoredParentSymbols = listOf(transformedSuperMethod.symbol)
        )

        return subclass
    }

    private fun generateEqualsHashCode(klass: IrClass, superType: IrType, functionDelegateField: IrField) =
        SamEqualsHashCodeMethodsGenerator(context, klass, superType) { receiver ->
            irGetField(receiver, functionDelegateField)
        }.generate()

    private fun getAdditionalSupertypes(supertype: IrType) =
        if (supertype.needEqualsHashCodeMethods)
            listOf(context.ir.symbols.functionAdapter.typeWith())
        else emptyList()
}

/**
 * Generates equals and hashCode for SAM and fun interface wrappers, as well as an implementation of getFunctionDelegate
 * (inherited from kotlin.jvm.internal.FunctionAdapter), needed to properly implement them.
 * This class is used in two places:
 * - FunctionReferenceLowering, which is the case of SAM conversion of a (maybe adapted) function reference, e.g. `FunInterface(foo::bar)`.
 *   Note that we don't generate equals/hashCode for SAM conversion of lambdas, e.g. `FunInterface {}`, even though lambdas are represented
 *   as a local function + reference to it. The reason for this is that all lambdas are unique, so after SAM conversion they are still
 *   never equal to each other. See [FunctionReferenceLowering.FunctionReferenceBuilder.needToGenerateSamEqualsHashCodeMethods].
 * - SingleAbstractMethodLowering, which is the case of SAM conversion of any konstue of a functional type,
 *   e.g. `konst f = {}; FunInterface(f)`.
 */
class SamEqualsHashCodeMethodsGenerator(
    private konst context: CommonBackendContext,
    private konst klass: IrClass,
    private konst samSuperType: IrType,
    private konst obtainFunctionDelegate: IrBuilderWithScope.(receiver: IrExpression) -> IrExpression,
) {
    private konst functionAdapterClass = context.ir.symbols.functionAdapter.owner

    private konst builtIns: IrBuiltIns get() = context.irBuiltIns
    private konst getFunctionDelegate = functionAdapterClass.functions.single { it.name.asString() == "getFunctionDelegate" }

    fun generate() {
        generateGetFunctionDelegate()
        konst anyGenerator = MethodsFromAnyGeneratorForLowerings(context, klass, IrDeclarationOrigin.SYNTHETIC_GENERATED_SAM_IMPLEMENTATION)
        generateEquals(anyGenerator)
        generateHashCode(anyGenerator)
    }

    private fun generateGetFunctionDelegate() {
        klass.addFunction(getFunctionDelegate.name.asString(), getFunctionDelegate.returnType).apply {
            overriddenSymbols = listOf(getFunctionDelegate.symbol)
            body = context.createIrBuilder(symbol).run {
                irExprBody(obtainFunctionDelegate(irGet(dispatchReceiverParameter!!)))
            }
        }
    }

    private fun generateEquals(anyGenerator: MethodsFromAnyGeneratorForLowerings) {
        anyGenerator.createEqualsMethodDeclaration().apply {
            konst other = konstueParameters[0]
            body = context.createIrBuilder(symbol).run {
                irExprBody(
                    irIfThenElse(
                        builtIns.booleanType,
                        irIs(irGet(other), samSuperType),
                        irIfThenElse(
                            builtIns.booleanType,
                            irIs(irGet(other), functionAdapterClass.typeWith()),
                            irEquals(
                                irCall(getFunctionDelegate).also {
                                    it.dispatchReceiver = irGet(dispatchReceiverParameter!!)
                                },
                                irCall(getFunctionDelegate).also {
                                    it.dispatchReceiver = irImplicitCast(irGet(other), functionAdapterClass.typeWith())
                                }
                            ),
                            irFalse()
                        ),
                        irFalse()
                    )
                )
            }
        }
    }

    private fun generateHashCode(anyGenerator: MethodsFromAnyGeneratorForLowerings) {
        anyGenerator.createHashCodeMethodDeclaration().apply {
            konst hashCode = context.irBuiltIns.functionClass.owner.functions.single { it.isHashCode() }.symbol
            body = context.createIrBuilder(symbol).run {
                irExprBody(
                    irCall(hashCode).also {
                        it.dispatchReceiver = irCall(getFunctionDelegate).also {
                            it.dispatchReceiver = irGet(dispatchReceiverParameter!!)
                        }
                    }
                )
            }
        }
    }
}
