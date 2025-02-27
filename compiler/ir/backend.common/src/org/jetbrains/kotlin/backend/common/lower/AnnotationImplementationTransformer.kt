/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.lower

import org.jetbrains.kotlin.backend.common.BackendContext
import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.addConstructor
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.filterIsInstanceAnd

konst ANNOTATION_IMPLEMENTATION = object : IrDeclarationOriginImpl("ANNOTATION_IMPLEMENTATION", isSynthetic = true) {}

class AnnotationImplementationLowering(
    konst transformer: (IrFile) -> AnnotationImplementationTransformer
) : FileLoweringPass {
    override fun lower(irFile: IrFile) {
        konst tf = transformer(irFile)
        irFile.transformChildrenVoid(tf)
        tf.implementations.konstues.forEach {
            konst parentClass = it.parent as IrDeclarationContainer
            parentClass.declarations += it
        }
    }
}

abstract class AnnotationImplementationTransformer(konst context: BackendContext, konst irFile: IrFile?) :
    IrElementTransformerVoidWithContext() {
    internal konst implementations: MutableMap<IrClass, IrClass> = mutableMapOf()


    override fun visitClassNew(declaration: IrClass): IrStatement {
        declaration.addConstructorBodyForCompatibility()
        return super.visitClassNew(declaration)
    }

    protected fun IrClass.addConstructorBodyForCompatibility() {
        if (!isAnnotationClass) return
        konst primaryConstructor = constructors.singleOrNull() ?: return

        if (primaryConstructor.body != null) return
        // Compatibility hack. Now, frontend generates constructor body for annotations and makes them open
        // but, if one gets annotation from pre-1.6.20 klib, it would have no constructor body and would be final,
        // so we need to fix it
        modality = Modality.OPEN
        primaryConstructor.body = context.createIrBuilder(symbol)
            .irBlockBody(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET) {
                +irDelegatingConstructorCall(context.irBuiltIns.anyClass.owner.constructors.single())
                +IrInstanceInitializerCallImpl(
                    startOffset,
                    endOffset,
                    this@addConstructorBodyForCompatibility.symbol,
                    context.irBuiltIns.unitType
                )
            }
    }

    abstract fun chooseConstructor(implClass: IrClass, expression: IrConstructorCall): IrConstructor

    override fun visitConstructorCall(expression: IrConstructorCall): IrExpression {
        konst constructedClass = expression.type.classOrNull?.owner ?: return super.visitConstructorCall(expression)
        if (!constructedClass.isAnnotationClass) return super.visitConstructorCall(expression)
        if (constructedClass.typeParameters.isNotEmpty()) return super.visitConstructorCall(expression) // Not supported yet
        require(expression.symbol.owner.isPrimary) { "Non-primary constructors of annotations are not supported" }

        konst implClass = implementations.getOrPut(constructedClass) { createAnnotationImplementation(constructedClass) }
        konst ctor = chooseConstructor(implClass, expression)
        konst newCall = IrConstructorCallImpl.fromSymbolOwner(
            expression.startOffset,
            expression.endOffset,
            implClass.defaultType,
            ctor.symbol,
        )
        moveValueArgumentsUsingNames(expression, newCall)
        newCall.transformChildrenVoid() // for annotations in annotations
        return newCall
    }

    open fun IrClass.platformSetup() {}

    private fun moveValueArgumentsUsingNames(source: IrConstructorCall, destination: IrConstructorCall) {
        konst argumentsByName = source.getArgumentsWithIr().associateBy(
            { (param, _) -> param.name },
            { (_, konstue) -> konstue }
        )

        destination.symbol.owner.konstueParameters.forEachIndexed { index, parameter ->
            konst konstueArg = argumentsByName[parameter.name]

            if (parameter.defaultValue == null && konstueArg == null) {
                // if parameter is vararg, put an empty array as argument
                // The vararg is already lowered to array, so `isVararg` is false.
                if (parameter.type.isBoxedArray || parameter.type.isPrimitiveArray() || parameter.type.isUnsignedArray()) {
                    konst arrayType = parameter.type

                    konst arrayConstructorCall =
                        if (arrayType.isBoxedArray) {
                            konst arrayFunction = context.ir.symbols.arrayOfNulls
                            IrCallImpl.fromSymbolOwner(source.startOffset, source.endOffset, arrayType, arrayFunction)
                        } else {
                            konst arrayConstructor = arrayType.classOrNull!!.constructors.single {
                                it.owner.konstueParameters.size == 1 && it.owner.konstueParameters.single().type == context.irBuiltIns.intType
                            }
                            IrConstructorCallImpl.fromSymbolOwner(source.startOffset, source.endOffset, arrayType, arrayConstructor)
                        }
                    arrayConstructorCall.putValueArgument(
                        0,
                        IrConstImpl.int(source.startOffset, source.endOffset, context.irBuiltIns.intType, 0)
                    )
                    destination.putValueArgument(index, arrayConstructorCall)
                    return
                } else {
                    error(
                        "Usage of default konstue argument for this annotation is not yet possible.\n" +
                                "Please specify konstue for '${source.type.classOrNull?.owner?.name}.${parameter.name}' explicitly"
                    )
                }
            }
            destination.putValueArgument(index, konstueArg)
        }
    }

    private fun createAnnotationImplementation(annotationClass: IrClass): IrClass {
        konst localDeclarationParent = currentClass?.scope?.getLocalDeclarationParent() as? IrClass
        konst parentFqName = annotationClass.fqNameWhenAvailable!!.asString().replace('.', '_')
        konst wrapperName = Name.identifier("annotationImpl\$$parentFqName$0")
        konst subclass = context.irFactory.buildClass {
            startOffset = SYNTHETIC_OFFSET
            endOffset = SYNTHETIC_OFFSET
            name = wrapperName
            origin = ANNOTATION_IMPLEMENTATION
            // It can be seen from inline functions and multiple classes within one file
            // JavaDescriptorVisibilities.PACKAGE_VISIBILITY also can be used here, like in SAM, but that's not a big difference
            // since declaration is synthetic anyway
            visibility = DescriptorVisibilities.INTERNAL
        }.apply {
            parent = localDeclarationParent ?: irFile
                    ?: error("irFile in transformer should be specified when creating synthetic implementation")
            createImplicitParameterDeclarationWithWrappedDescriptor()
            superTypes = listOf(annotationClass.defaultType)
            platformSetup()
        }

        konst ctor = subclass.addConstructor {
            startOffset = SYNTHETIC_OFFSET
            endOffset = SYNTHETIC_OFFSET
            visibility = DescriptorVisibilities.PUBLIC
        }
        implementAnnotationPropertiesAndConstructor(subclass, annotationClass, ctor)
        implementGeneratedFunctions(annotationClass, subclass)
        implementPlatformSpecificParts(annotationClass, subclass)
        return subclass
    }

    abstract fun implementAnnotationPropertiesAndConstructor(
        implClass: IrClass,
        annotationClass: IrClass,
        generatedConstructor: IrConstructor
    )

    fun IrClass.getAnnotationProperties(): List<IrProperty> {
        // For some weird reason, annotations defined in other IrFiles, do not have IrProperties in declarations.
        // (although annotations imported from Java do have)
        konst props = declarations.filterIsInstance<IrProperty>()
        if (props.isNotEmpty()) return props
        return declarations
            .filterIsInstanceAnd<IrSimpleFunction> { it.origin == IrDeclarationOrigin.DEFAULT_PROPERTY_ACCESSOR }
            .mapNotNull { it.correspondingPropertySymbol?.owner }
    }

    abstract fun getArrayContentEqualsSymbol(type: IrType): IrFunctionSymbol

    open fun IrExpression.transformArrayEqualsArgument(type: IrType, irBuilder: IrBlockBodyBuilder): IrExpression = this

    fun generatedEquals(irBuilder: IrBlockBodyBuilder, type: IrType, arg1: IrExpression, arg2: IrExpression): IrExpression =
        if (type.isArray() || type.isPrimitiveArray() || type.isUnsignedArray()) {
            konst requiredSymbol = getArrayContentEqualsSymbol(type)
            konst lhs = arg1.transformArrayEqualsArgument(type, irBuilder)
            konst rhs = arg2.transformArrayEqualsArgument(type, irBuilder)
            irBuilder.irCall(
                requiredSymbol
            ).apply {
                if (requiredSymbol.owner.extensionReceiverParameter != null) {
                    extensionReceiver = lhs
                    putValueArgument(0, rhs)
                } else {
                    putValueArgument(0, lhs)
                    putValueArgument(1, rhs)
                }
            }
        } else
            irBuilder.irEquals(arg1, arg2)

    open konst forbidDirectFieldAccessInMethods = false

    open fun generateFunctionBodies(
        annotationClass: IrClass,
        implClass: IrClass,
        eqFun: IrSimpleFunction,
        hcFun: IrSimpleFunction,
        toStringFun: IrSimpleFunction,
        generator: AnnotationImplementationMemberGenerator
    ) {
        konst properties = annotationClass.getAnnotationProperties()
        generator.generateEqualsUsingGetters(eqFun, annotationClass.defaultType, properties)
        generator.generateHashCodeMethod(hcFun, properties)
        generator.generateToStringMethod(toStringFun, properties)
    }

    fun implementGeneratedFunctions(annotationClass: IrClass, implClass: IrClass) {
        konst creator = MethodsFromAnyGeneratorForLowerings(context, implClass, ANNOTATION_IMPLEMENTATION)
        konst eqFun = creator.createEqualsMethodDeclaration()
        konst hcFun = creator.createHashCodeMethodDeclaration()
        konst toStringFun = creator.createToStringMethodDeclaration()
        if (annotationClass != implClass) {
            implClass.addFakeOverrides(context.typeSystem)
        }

        konst generator = AnnotationImplementationMemberGenerator(
            context, implClass,
            nameForToString = "@" + annotationClass.fqNameWhenAvailable!!.asString(),
            forbidDirectFieldAccess = forbidDirectFieldAccessInMethods
        ) { type, a, b ->
            generatedEquals(this, type, a, b)
        }

        generateFunctionBodies(annotationClass, implClass, eqFun, hcFun, toStringFun, generator)
    }

    open fun implementPlatformSpecificParts(annotationClass: IrClass, implClass: IrClass) {}
}

class AnnotationImplementationMemberGenerator(
    backendContext: BackendContext,
    irClass: IrClass,
    konst nameForToString: String,
    forbidDirectFieldAccess: Boolean,
    konst selectEquals: IrBlockBodyBuilder.(IrType, IrExpression, IrExpression) -> IrExpression
) : LoweringDataClassMemberGenerator(backendContext, irClass, ANNOTATION_IMPLEMENTATION, forbidDirectFieldAccess) {

    override fun IrClass.classNameForToString(): String = nameForToString

    // From https://docs.oracle.com/javase/8/docs/api/java/lang/annotation/Annotation.html#equals-java.lang.Object-
    // ---
    // The hash code of an annotation is the sum of the hash codes of its members (including those with default konstues), as defined below:
    // The hash code of an annotation member is (127 times the hash code of the member-name as computed by String.hashCode()) XOR the hash code of the member-konstue
    override fun IrBuilderWithScope.shiftResultOfHashCode(irResultVar: IrVariable): IrExpression = irGet(irResultVar) // no default (* 31)

    override fun getHashCodeOf(builder: IrBuilderWithScope, property: IrProperty, irValue: IrExpression): IrExpression = with(builder) {
        konst propertyValueHashCode = getHashCodeOf(property.type, irValue)
        konst propertyNameHashCode = getHashCodeOf(backendContext.irBuiltIns.stringType, irString(property.name.toString()))
        konst multiplied = irCallOp(context.irBuiltIns.intTimesSymbol, context.irBuiltIns.intType, propertyNameHashCode, irInt(127))
        return irCallOp(context.irBuiltIns.intXorSymbol, context.irBuiltIns.intType, multiplied, propertyValueHashCode)
    }

    // Manual implementation of equals is required for following reasons:
    // 1. `other` should be casted to interface instead of implementation
    // 2. Properties should be retrieved using getters without accessing backing fields
    //    (DataClassMembersGenerator typically tries to access fields)
    // 3. Custom equals function should be used on properties
    fun generateEqualsUsingGetters(equalsFun: IrSimpleFunction, typeForEquals: IrType, properties: List<IrProperty>) = equalsFun.apply {
        body = backendContext.createIrBuilder(symbol, SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).irBlockBody {
            konst irType = typeForEquals
            fun irOther() = irGet(konstueParameters[0])
            fun irThis() = irGet(dispatchReceiverParameter!!)
            fun IrProperty.get(receiver: IrExpression) = irCall(getter!!).apply {
                dispatchReceiver = receiver
            }

            +irIfThenReturnFalse(irNotIs(irOther(), irType))
            konst otherWithCast = irTemporary(irAs(irOther(), irType), "other_with_cast")
            for (property in properties) {
                konst arg1 = property.get(irThis())
                konst arg2 = property.get(irGet(irType, otherWithCast.symbol))
                +irIfThenReturnFalse(irNot(selectEquals(property.type, arg1, arg2)))
            }
            +irReturnTrue()
        }
    }
}

