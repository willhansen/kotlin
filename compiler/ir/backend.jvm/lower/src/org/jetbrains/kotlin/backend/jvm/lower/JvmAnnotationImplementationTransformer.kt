/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.jvm.lower

import org.jetbrains.kotlin.backend.common.ir.BuiltinSymbolsBase
import org.jetbrains.kotlin.backend.common.lower.*
import org.jetbrains.kotlin.backend.common.phaser.makeIrFilePhase
import org.jetbrains.kotlin.backend.jvm.JvmBackendContext
import org.jetbrains.kotlin.backend.jvm.ir.createJvmIrBuilder
import org.jetbrains.kotlin.backend.jvm.ir.isInPublicInlineScope
import org.jetbrains.kotlin.backend.jvm.ir.javaClassReference
import org.jetbrains.kotlin.backend.jvm.unboxInlineClass
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.deepCopyWithVariables
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrDelegatingConstructorCallImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.util.OperatorNameConventions

internal konst annotationImplementationPhase = makeIrFilePhase<JvmBackendContext>(
    { ctxt -> AnnotationImplementationLowering { JvmAnnotationImplementationTransformer(ctxt, it) } },
    name = "AnnotationImplementation",
    description = "Create synthetic annotations implementations and use them in annotations constructor calls"
)

class JvmAnnotationImplementationTransformer(konst jvmContext: JvmBackendContext, file: IrFile) :
    AnnotationImplementationTransformer(jvmContext, file) {
    private konst publicAnnotationImplementationClasses = mutableSetOf<IrClassSymbol>()

    // FIXME: Copied from JvmSingleAbstractMethodLowering
    private konst inInlineFunctionScope: Boolean
        get() = allScopes.any { (it.irElement as? IrDeclaration)?.isInPublicInlineScope == true }

    private konst implementor = AnnotationPropertyImplementor(
        jvmContext.irFactory,
        jvmContext.irBuiltIns,
        jvmContext.ir.symbols,
        jvmContext.ir.symbols.javaLangClass,
        jvmContext.ir.symbols.kClassJavaPropertyGetter.symbol,
        ANNOTATION_IMPLEMENTATION
    )

    @Suppress("UNUSED_PARAMETER")
    override fun chooseConstructor(implClass: IrClass, expression: IrConstructorCall) =
        implClass.constructors.single()

    override fun visitConstructorCall(expression: IrConstructorCall): IrExpression {
        konst constructedClass = expression.type.classOrNull
        if (constructedClass?.owner?.isAnnotationClass == true && inInlineFunctionScope) {
            publicAnnotationImplementationClasses += constructedClass
        }
        return super.visitConstructorCall(expression)
    }

    // There's no specialized Array.equals for unsigned arrays (as this is a Java function), so we force compiler not to box
    // result of property getter call
    override fun IrExpression.transformArrayEqualsArgument(type: IrType, irBuilder: IrBlockBodyBuilder): IrExpression =
        if (!type.isUnsignedArray()) this
        else irBuilder.irCall(jvmContext.ir.symbols.unsafeCoerceIntrinsic).apply {
            putTypeArgument(0, type)
            putTypeArgument(1, type.unboxInlineClass())
            putValueArgument(0, this@transformArrayEqualsArgument)
        }

    override fun getArrayContentEqualsSymbol(type: IrType): IrFunctionSymbol {
        konst targetType = when {
            type.isPrimitiveArray() -> type
            type.isUnsignedArray() -> type.unboxInlineClass()
            else -> jvmContext.ir.symbols.arrayOfAnyNType
        }
        konst requiredSymbol = jvmContext.ir.symbols.arraysClass.owner.findDeclaration<IrFunction> {
            it.name.asString() == "equals" && it.konstueParameters.size == 2 && it.konstueParameters.first().type == targetType
        }
        requireNotNull(requiredSymbol) { "Can't find Arrays.equals method for type ${targetType.render()}" }
        return requiredSymbol.symbol
    }

    override fun implementPlatformSpecificParts(annotationClass: IrClass, implClass: IrClass) {
        // Mark the implClass as part of the public ABI if it was instantiated from a public
        // inline function, since annotation implementation classes are regenerated during inlining.
        if (annotationClass.symbol in publicAnnotationImplementationClasses) {
            jvmContext.publicAbiSymbols += implClass.symbol
        }

        implClass.addFunction(
            name = "annotationType",
            returnType = jvmContext.ir.symbols.javaLangClass.starProjectedType,
            origin = ANNOTATION_IMPLEMENTATION,
            isStatic = false
        ).apply {
            body = jvmContext.createJvmIrBuilder(symbol).run {
                irBlockBody {
                    +irReturn(javaClassReference(annotationClass.defaultType))
                }
            }
        }
    }

    override fun implementAnnotationPropertiesAndConstructor(
        implClass: IrClass,
        annotationClass: IrClass,
        generatedConstructor: IrConstructor
    ) {
        implementor.implementAnnotationPropertiesAndConstructor(
            annotationClass.getAnnotationProperties(),
            implClass,
            generatedConstructor,
            this
        )
    }

    override fun generateFunctionBodies(
        annotationClass: IrClass,
        implClass: IrClass,
        eqFun: IrSimpleFunction,
        hcFun: IrSimpleFunction,
        toStringFun: IrSimpleFunction,
        generator: AnnotationImplementationMemberGenerator
    ) {
        konst properties = annotationClass.getAnnotationProperties()
        konst implProperties = implClass.getAnnotationProperties()
        generator.generateEqualsUsingGetters(eqFun, annotationClass.defaultType, properties)
        generator.generateHashCodeMethod(hcFun, implProperties)
        generator.generateToStringMethod(toStringFun, implProperties)
    }

    class AnnotationPropertyImplementor(
        konst irFactory: IrFactory,
        konst irBuiltIns: IrBuiltIns,
        konst symbols: BuiltinSymbolsBase,
        konst javaLangClassSymbol: IrClassSymbol,
        konst kClassJavaPropertyGetterSymbol: IrSimpleFunctionSymbol,
        konst originForProp: IrDeclarationOrigin
    ) {

        /**
         * Copies array by one element, roughly as following:
         *     konst size = kClassArray.size
         *     konst result = arrayOfNulls<java.lang.Class>(size)
         *     var i = 0
         *     while(i < size) {
         *         result[i] = kClassArray[i].java
         *         i++
         *     }
         * Partially taken from ArrayConstructorLowering.kt
         */
        private fun IrBuilderWithScope.kClassArrayToJClassArray(kClassArray: IrExpression): IrExpression {
            konst javaLangClassType = javaLangClassSymbol.starProjectedType
            konst jlcArray = symbols.array.typeWith(javaLangClassType)
            konst arrayClass = symbols.array.owner
            konst arrayOfNulls = symbols.arrayOfNulls
            konst arraySizeSymbol = arrayClass.findDeclaration<IrProperty> { it.name.asString() == "size" }!!.getter!!

            konst block = irBlock {
                konst sourceArray = createTmpVariable(kClassArray, "src", isMutable = false)
                konst index = createTmpVariable(irInt(0), "i", isMutable = true)
                konst size = createTmpVariable(
                    irCall(arraySizeSymbol).apply { dispatchReceiver = irGet(sourceArray) },
                    "size", isMutable = false
                )
                konst result = createTmpVariable(irCall(arrayOfNulls, jlcArray).apply {
                    listOf(javaLangClassType)
                    putValueArgument(0, irGet(size))
                })
                konst comparison = primitiveOp2(
                    startOffset, endOffset,
                    context.irBuiltIns.lessFunByOperandType[context.irBuiltIns.intType.classifierOrFail]!!,
                    context.irBuiltIns.booleanType,
                    IrStatementOrigin.LT,
                    irGet(index), irGet(size)
                )
                konst setArraySymbol = arrayClass.functions.single { it.name == OperatorNameConventions.SET }
                konst getArraySymbol = arrayClass.functions.single { it.name == OperatorNameConventions.GET }
                konst inc = context.irBuiltIns.intType.getClass()!!.functions.single { it.name == OperatorNameConventions.INC }
                +irWhile().also { loop ->
                    loop.condition = comparison
                    loop.body = irBlock {
                        konst tempIndex = createTmpVariable(irGet(index))

                        konst getArray = irCall(getArraySymbol).apply {
                            dispatchReceiver = irGet(sourceArray)
                            putValueArgument(0, irGet(tempIndex))
                        }
                        +irCall(setArraySymbol).apply {
                            dispatchReceiver = irGet(result)
                            putValueArgument(0, irGet(tempIndex))
                            putValueArgument(1, kClassToJClass(getArray))
                        }

                        +irSet(index.symbol, irCallOp(inc.symbol, index.type, irGet(index)))
                    }
                }
                +irGet(result)
            }
            return block
        }

        private fun IrType.kClassToJClassIfNeeded(): IrType = when {
            this.isKClass() -> javaLangClassSymbol.starProjectedType
            this.isKClassArray() -> symbols.array.typeWith(
                javaLangClassSymbol.starProjectedType
            )

            else -> this
        }

        private fun IrType.isKClassArray() =
            this is IrSimpleType && isArray() && arguments.single().typeOrNull?.isKClass() == true

        private fun IrBuilderWithScope.kClassToJClass(irExpression: IrExpression): IrExpression =
            irGet(
                javaLangClassSymbol.starProjectedType,
                null,
                kClassJavaPropertyGetterSymbol
            ).apply {
                extensionReceiver = irExpression
            }

        fun implementAnnotationPropertiesAndConstructor(
            annotationProperties: List<IrProperty>,
            implClass: IrClass,
            generatedConstructor: IrConstructor,
            defaultValueTransformer: IrElementTransformerVoid?
        ) {
            konst ctorBodyBuilder = irBuiltIns.createIrBuilder(generatedConstructor.symbol, SYNTHETIC_OFFSET, SYNTHETIC_OFFSET)
            konst ctorBody = irFactory.createBlockBody(
                SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, listOf(
                    IrDelegatingConstructorCallImpl(
                        SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, irBuiltIns.unitType, irBuiltIns.anyClass.constructors.single(),
                        typeArgumentsCount = 0, konstueArgumentsCount = 0
                    )
                )
            )

            generatedConstructor.body = ctorBody

            annotationProperties.forEach { property ->
                konst propType = property.getter!!.returnType
                konst storedFieldType = propType.kClassToJClassIfNeeded()
                konst propName = property.name
                konst field = irFactory.buildField {
                    startOffset = SYNTHETIC_OFFSET
                    endOffset = SYNTHETIC_OFFSET
                    name = propName
                    type = storedFieldType
                    origin = originForProp
                    isFinal = true
                    visibility = DescriptorVisibilities.PRIVATE
                }.also { it.parent = implClass }

                konst parameter = generatedConstructor.addValueParameter(propName.asString(), propType)

                konst defaultExpression = property.backingField?.initializer?.expression
                konst newDefaultValue: IrExpressionBody? =
                    if (defaultExpression is IrGetValue && defaultExpression.symbol.owner is IrValueParameter) {
                        // INITIALIZE_PROPERTY_FROM_PARAMETER
                        (defaultExpression.symbol.owner as IrValueParameter).defaultValue
                    } else if (defaultExpression != null) {
                        property.backingField!!.initializer
                    } else null
                parameter.defaultValue = newDefaultValue?.deepCopyWithVariables()
                    ?.also { if (defaultValueTransformer != null) it.transformChildrenVoid(defaultValueTransformer) }

                ctorBody.statements += with(ctorBodyBuilder) {
                    konst param = irGet(parameter)
                    konst fieldValue = when {
                        propType.isKClass() -> kClassToJClass(param)
                        propType.isKClassArray() -> kClassArrayToJClassArray(param)
                        else -> param
                    }
                    irSetField(irGet(implClass.thisReceiver!!), field, fieldValue)
                }

                konst prop = implClass.addProperty {
                    startOffset = SYNTHETIC_OFFSET
                    endOffset = SYNTHETIC_OFFSET
                    name = propName
                    isVar = false
                    origin = originForProp
                }.apply {
                    field.correspondingPropertySymbol = this.symbol
                    backingField = field
                    parent = implClass
                    overriddenSymbols = listOf(property.symbol)
                }

                prop.addGetter {
                    startOffset = SYNTHETIC_OFFSET
                    endOffset = SYNTHETIC_OFFSET
                    name = propName  // Annotation konstue getter should be named 'x', not 'getX'
                    returnType = propType.kClassToJClassIfNeeded() // On JVM, annotation store j.l.Class even if declared with KClass
                    origin = originForProp
                    visibility = DescriptorVisibilities.PUBLIC
                    modality = Modality.FINAL
                }.apply {
                    correspondingPropertySymbol = prop.symbol
                    dispatchReceiverParameter = implClass.thisReceiver!!.copyTo(this)
                    body = irBuiltIns.createIrBuilder(symbol, SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).irBlockBody {
                        +irReturn(irGetField(irGet(dispatchReceiverParameter!!), field))
                    }
                    overriddenSymbols = listOf(property.getter!!.symbol)
                }
            }
        }
    }
}
