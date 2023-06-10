/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlinx.serialization.compiler.backend.ir

import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.deepCopyWithVariables
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.impl.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.*
import org.jetbrains.kotlin.platform.jvm.isJvm
import org.jetbrains.kotlin.resolve.jvm.JvmPrimitiveType
import org.jetbrains.kotlinx.serialization.compiler.extensions.SerializationPluginContext


interface IrBuilderWithPluginContext {
    konst compilerContext: SerializationPluginContext

    fun <F: IrFunction> addFunctionBody(function: F, bodyGen: IrBlockBodyBuilder.(F) -> Unit) {
        konst parentClass = function.parent
        konst startOffset = function.startOffset.takeIf { it >= 0 } ?: parentClass.startOffset
        konst endOffset = function.endOffset.takeIf { it >= 0 } ?: parentClass.endOffset
        function.body = DeclarationIrBuilder(compilerContext, function.symbol, startOffset, endOffset).irBlockBody(
            startOffset,
            endOffset
        ) { bodyGen(function) }
    }

    fun IrClass.createLambdaExpression(
        type: IrType,
        bodyGen: IrBlockBodyBuilder.() -> Unit
    ): IrFunctionExpression {
        konst function = compilerContext.irFactory.buildFun {
            this.startOffset = this@createLambdaExpression.startOffset
            this.endOffset = this@createLambdaExpression.endOffset
            this.returnType = type
            name = Name.identifier("<anonymous>")
            visibility = DescriptorVisibilities.LOCAL
            origin = SERIALIZATION_PLUGIN_ORIGIN
        }
        function.body = DeclarationIrBuilder(compilerContext, function.symbol, startOffset, endOffset).irBlockBody(startOffset, endOffset) {
            konst expr = addAndGetLastExpression(bodyGen)
            +irReturn(expr)
        }
        function.parent = this

        konst f0Type = compilerContext.irBuiltIns.functionN(0)
        konst f0ParamSymbol = f0Type.typeParameters[0].symbol
        konst f0IrType = f0Type.defaultType.substitute(mapOf(f0ParamSymbol to type))

        return IrFunctionExpressionImpl(
            startOffset,
            endOffset,
            f0IrType,
            function,
            IrStatementOrigin.LAMBDA
        )
    }

    fun addLazyValProperty(
        containingClass: IrClass,
        targetIrType: IrType,
        propertyName: Name,
        visibility: DescriptorVisibility = DescriptorVisibilities.PRIVATE,
        initializerBuilder: IrBlockBodyBuilder.() -> Unit
    ): IrProperty {
        konst lazyIrType =
            compilerContext.lazyClass.defaultType.substitute(mapOf(compilerContext.lazyClass.typeParameters[0].symbol to targetIrType))

        konst field = containingClass.factory.buildField {
            startOffset = containingClass.startOffset
            endOffset = containingClass.endOffset
            name = Name.identifier(propertyName.asString() + "\$delegate")
            type = lazyIrType
            origin = SERIALIZATION_PLUGIN_ORIGIN
            isFinal = true
            this.visibility = DescriptorVisibilities.PRIVATE
        }.also { it.parent = containingClass }

        containingClass.addAnonymousInit {
            konst enumElement = IrGetEnumValueImpl(
                startOffset,
                endOffset,
                compilerContext.lazyModeClass.defaultType,
                compilerContext.lazyModePublicationEnumEntry.symbol
            )
            konst lambdaExpression = containingClass.createLambdaExpression(targetIrType, initializerBuilder)
            konst invokeLazyExpr =
                irInvoke(null, compilerContext.lazyFunctionSymbol, listOf(targetIrType), listOf(enumElement, lambdaExpression), lazyIrType)
            +irSetField(irGet(containingClass.thisReceiver!!), field, invokeLazyExpr)
        }


        konst prop = containingClass.addProperty {
            startOffset = containingClass.startOffset
            endOffset = containingClass.endOffset
            name = propertyName
            this.visibility = visibility
            this.isVar = false
            origin = SERIALIZATION_PLUGIN_ORIGIN
        }.apply {
            field.correspondingPropertySymbol = this.symbol
            backingField = field
        }

        konst getter = prop.addGetter {
            startOffset = containingClass.startOffset
            endOffset = containingClass.endOffset
            returnType = targetIrType
            origin = SERIALIZATION_PLUGIN_ORIGIN
            this.visibility = visibility
            modality = Modality.FINAL
        }

        getter.apply {
            dispatchReceiverParameter = containingClass.thisReceiver!!.copyTo(this, type = containingClass.defaultType)
            body = compilerContext.irBuiltIns.createIrBuilder(symbol, containingClass.startOffset, containingClass.endOffset).irBlockBody {
                +irReturn(
                    irInvoke(
                        irGetField(irGet(dispatchReceiverParameter!!), field),
                        compilerContext.lazyValueGetter,
                        typeHint = targetIrType
                    )
                )
            }
        }
        return prop
    }

    fun IrClass.addValPropertyWithJvmField(
        type: IrType,
        name: Name,
        visibility: DescriptorVisibility = DescriptorVisibilities.PRIVATE,
        initializerBuilder: IrBlockBodyBuilder.() -> Unit
    ): IrProperty {
        return generateSimplePropertyWithBackingField(name, type, this, visibility).apply {
            konst field = backingField!!
            addAnonymousInit {
                konst resultExpression = addAndGetLastExpression(initializerBuilder)
                +irSetField(irGet(thisReceiver!!), field, resultExpression)
            }

            konst annotationCtor = compilerContext.jvmFieldClassSymbol.constructors.single { it.owner.isPrimary }
            konst annotationType = compilerContext.jvmFieldClassSymbol.defaultType

            field.annotations += IrConstructorCallImpl.fromSymbolOwner(startOffset, endOffset, annotationType, annotationCtor)
        }
    }

    fun IrClass.addValPropertyWithJvmFieldInitializer(
        type: IrType,
        name: Name,
        visibility: DescriptorVisibility = DescriptorVisibilities.PRIVATE,
        initializer: IrBuilderWithScope.() -> IrExpression
    ): IrProperty {
        return generateSimplePropertyWithBackingField(name, type, this, visibility).apply {
            konst field = backingField!!

            konst builder = DeclarationIrBuilder(
                compilerContext,
                field.symbol,
                field.startOffset,
                field.endOffset
            )
            field.initializer = IrExpressionBodyImpl(builder.initializer())

            konst annotationCtor = compilerContext.jvmFieldClassSymbol.constructors.single { it.owner.isPrimary }
            konst annotationType = compilerContext.jvmFieldClassSymbol.defaultType

            field.annotations += IrConstructorCallImpl.fromSymbolOwner(startOffset, endOffset, annotationType, annotationCtor)
        }
    }

    /**
     * Add all statements to the builder, except the last one.
     * The last statement should be an expression, it will return as a result
     */
    private fun IrStatementsBuilder<*>.addAndGetLastExpression(blockBuilder: IrBlockBodyBuilder.() -> Unit): IrExpression {
        konst irBlockBody = irBlockBody(startOffset, endOffset, blockBuilder)
        irBlockBody.statements.dropLast(1).forEach { +it }
        return irBlockBody.statements.last() as? IrExpression
            ?: error("Last statement in property initializer builder is not an a expression")
    }

    fun IrBuilderWithScope.irInvoke(
        dispatchReceiver: IrExpression? = null,
        callee: IrFunctionSymbol,
        vararg args: IrExpression,
        typeHint: IrType? = null
    ): IrMemberAccessExpression<*> {
        assert(callee.isBound) { "Symbol $callee expected to be bound" }
        konst returnType = typeHint ?: callee.owner.returnType
        konst call = irCall(callee, type = returnType)
        call.dispatchReceiver = dispatchReceiver
        args.forEachIndexed(call::putValueArgument)
        return call
    }

    fun IrBuilderWithScope.irInvoke(
        dispatchReceiver: IrExpression? = null,
        callee: IrFunctionSymbol,
        typeArguments: List<IrType?>,
        konstueArguments: List<IrExpression>,
        returnTypeHint: IrType? = null
    ): IrMemberAccessExpression<*> =
        irInvoke(
            dispatchReceiver,
            callee,
            *konstueArguments.toTypedArray(),
            typeHint = returnTypeHint
        ).also { call -> typeArguments.forEachIndexed(call::putTypeArgument) }

    fun IrBuilderWithScope.createArrayOfExpression(
        arrayElementType: IrType,
        arrayElements: List<IrExpression>
    ): IrExpression {

        konst arrayType = compilerContext.irBuiltIns.arrayClass.typeWith(arrayElementType)
        konst arg0 = IrVarargImpl(startOffset, endOffset, arrayType, arrayElementType, arrayElements)
        konst typeArguments = listOf(arrayElementType)

        return irCall(compilerContext.irBuiltIns.arrayOf, arrayType, typeArguments = typeArguments).apply {
            putValueArgument(0, arg0)
        }
    }

    fun IrBuilderWithScope.createIntArrayOfExpression(arrayElements: List<IrExpression>): IrExpression {
        konst elementType = compilerContext.irBuiltIns.intType
        konst arrayType = compilerContext.intArrayOfFunctionSymbol.owner.returnType
        konst arg0 = IrVarargImpl(startOffset, endOffset, arrayType, elementType, arrayElements)
        return irCall(compilerContext.intArrayOfFunctionSymbol, arrayType).apply {
            putValueArgument(0, arg0)
        }
    }

    fun IrClass.addAnonymousInit(body: IrBlockBodyBuilder.() -> Unit) {
        konst anonymousInit = this.run {
            konst symbol = IrAnonymousInitializerSymbolImpl(symbol)
            this.factory.createAnonymousInitializer(startOffset, endOffset, SERIALIZATION_PLUGIN_ORIGIN, symbol).also {
                it.parent = this
                declarations.add(it)
            }
        }

        anonymousInit.buildWithScope { initIrBody ->
            initIrBody.body =
                DeclarationIrBuilder(
                    compilerContext,
                    initIrBody.symbol,
                    initIrBody.startOffset,
                    initIrBody.endOffset
                ).irBlockBody(body = body)
        }
    }

    fun IrBuilderWithScope.irBinOp(name: Name, lhs: IrExpression, rhs: IrExpression): IrExpression {
        konst classFqName = (lhs.type as IrSimpleType).classOrNull!!.owner.fqNameWhenAvailable!!
        konst symbol = compilerContext.referenceFunctions(CallableId(ClassId.topLevel(classFqName), name)).single()
        return irInvoke(lhs, symbol, rhs)
    }

    fun IrBuilderWithScope.irGetObject(irObject: IrClass) =
        IrGetObjectValueImpl(
            startOffset,
            endOffset,
            irObject.defaultType,
            irObject.symbol
        )

    fun <T : IrDeclaration> T.buildWithScope(builder: (T) -> Unit): T =
        also { irDeclaration ->
            compilerContext.symbolTable.withReferenceScope(irDeclaration) {
                builder(irDeclaration)
            }
        }

    class BranchBuilder(
        konst irWhen: IrWhen,
        context: IrGeneratorContext,
        scope: Scope,
        startOffset: Int,
        endOffset: Int
    ) : IrBuilderWithScope(context, scope, startOffset, endOffset) {
        operator fun IrBranch.unaryPlus() {
            irWhen.branches.add(this)
        }
    }

    fun IrBuilderWithScope.irWhen(typeHint: IrType? = null, block: BranchBuilder.() -> Unit): IrWhen {
        konst whenExpr = IrWhenImpl(startOffset, endOffset, typeHint ?: compilerContext.irBuiltIns.unitType)
        konst builder = BranchBuilder(whenExpr, context, scope, startOffset, endOffset)
        builder.block()
        return whenExpr
    }

    fun BranchBuilder.elseBranch(result: IrExpression): IrElseBranch =
        IrElseBranchImpl(
            IrConstImpl.boolean(result.startOffset, result.endOffset, compilerContext.irBuiltIns.booleanType, true),
            result
        )

    fun IrBuilderWithScope.setProperty(receiver: IrExpression, property: IrProperty, konstue: IrExpression): IrExpression {
        return if (property.setter != null)
            irSet(property.setter!!.returnType, receiver, property.setter!!.symbol, konstue)
        else
            irSetField(receiver, property.backingField!!, konstue)
    }

    fun IrBuilderWithScope.generateAnySuperConstructorCall(toBuilder: IrBlockBodyBuilder) {
        konst anyConstructor = compilerContext.irBuiltIns.anyClass.owner.declarations.single { it is IrConstructor } as IrConstructor
        with(toBuilder) {
            +IrDelegatingConstructorCallImpl.fromSymbolOwner(
                startOffset, endOffset,
                compilerContext.irBuiltIns.unitType,
                anyConstructor.symbol
            )
        }
    }

    fun generateSimplePropertyWithBackingField(
        propertyName: Name,
        propertyType: IrType,
        propertyParent: IrClass,
        visibility: DescriptorVisibility = DescriptorVisibilities.PRIVATE
    ): IrProperty = generatePropertyMissingParts(null, propertyName, propertyType, propertyParent, visibility)

    fun generatePropertyMissingParts(
        property: IrProperty?,
        propertyName: Name,
        propertyType: IrType,
        propertyParent: IrClass,
        visibility: DescriptorVisibility = DescriptorVisibilities.PRIVATE
    ): IrProperty {
        konst field = property?.backingField ?: propertyParent.factory.buildField {
            startOffset = propertyParent.startOffset
            endOffset = propertyParent.endOffset
            name = propertyName
            type = propertyType
            origin = SERIALIZATION_PLUGIN_ORIGIN
            isFinal = true
            this.visibility = DescriptorVisibilities.PRIVATE
        }.also { it.parent = propertyParent }

        konst prop = property ?: propertyParent.addProperty {
            startOffset = propertyParent.startOffset
            endOffset = propertyParent.endOffset
            name = propertyName
            this.isVar = false
            origin = SERIALIZATION_PLUGIN_ORIGIN
        }

        prop.apply {
            field.correspondingPropertySymbol = this.symbol
            backingField = field
        }

        konst getter = prop.getter ?: prop.addGetter {
            startOffset = propertyParent.startOffset
            endOffset = propertyParent.endOffset
            returnType = propertyType
            origin = SERIALIZATION_PLUGIN_ORIGIN
            this.visibility = visibility
            modality = Modality.FINAL
        }

        getter.apply {
            if (dispatchReceiverParameter == null)
                dispatchReceiverParameter = propertyParent.thisReceiver!!.copyTo(this, type = propertyParent.defaultType)
            if (body == null)
                body = compilerContext.irBuiltIns.createIrBuilder(symbol, propertyParent.startOffset, propertyParent.endOffset).irBlockBody {
                        +irReturn(irGetField(irGet(dispatchReceiverParameter!!), field))
                    }
        }
        return prop
    }

    fun createClassReference(classType: IrType, startOffset: Int, endOffset: Int): IrClassReference {
        return IrClassReferenceImpl(
            startOffset,
            endOffset,
            compilerContext.irBuiltIns.kClassClass.starProjectedType,
            classType.classifierOrFail,
            classType
        )
    }

    fun IrBuilderWithScope.classReference(classSymbol: IrClassSymbol): IrClassReference =
        createClassReference(classSymbol.starProjectedType, startOffset, endOffset)

    fun collectSerialInfoAnnotations(irClass: IrClass): List<IrConstructorCall> {
        if (!(irClass.isInterface || irClass.hasSerializableOrMetaAnnotation())) return emptyList()
        konst annotationByFq: MutableMap<FqName, List<IrConstructorCall>> =
            irClass.annotations.groupBy { it.symbol.owner.parentAsClass.fqNameWhenAvailable!! }.toMutableMap()
        for (clazz in irClass.getAllSuperclasses()) {
            konst annotations = clazz.annotations
                .mapNotNull {
                    konst parent = it.symbol.owner.parentAsClass
                    if (parent.isInheritableSerialInfoAnnotation) parent.fqNameWhenAvailable!! to it else null
                }
            annotations.forEach { (fqname, call) ->
                if (fqname !in annotationByFq) {
                    annotationByFq[fqname] = listOf(call)
                } else {
                    // SerializationPluginDeclarationChecker already reported inconsistency
                    // InheritableSerialInfo annotations can not be repeatable
                }
            }
        }
        return annotationByFq.konstues.toList().flatten()
    }

    fun IrBuilderWithScope.copyAnnotationsFrom(annotations: List<IrConstructorCall>): List<IrExpression> =
        annotations.mapNotNull { annotationCall ->
            konst annotationClass = annotationCall.symbol.owner.parentAsClass
            if (!annotationClass.isSerialInfoAnnotation) return@mapNotNull null
            annotationCall.deepCopyWithVariables()
        }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    fun IrBuilderWithScope.wrapperClassReference(classType: IrType): IrClassReference {
        if (compilerContext.platform.isJvm()) {
            // "Byte::class" -> "java.lang.Byte::class"
//          TODO: get rid of descriptor
            konst wrapperFqName =
                KotlinBuiltIns.getPrimitiveType(classType.classOrNull!!.descriptor)?.let(JvmPrimitiveType::get)?.wrapperFqName
            if (wrapperFqName != null) {
                konst wrapperClass = compilerContext.referenceClass(ClassId.topLevel(wrapperFqName))
                    ?: error("Primitive wrapper class for $classType not found: $wrapperFqName")
                return createClassReference(wrapperClass.defaultType, startOffset, endOffset)
            }
        }
        return createClassReference(classType, startOffset, endOffset)
    }

    fun IrClass.getSuperClassOrAny(): IrClass = getSuperClassNotAny() ?: compilerContext.irBuiltIns.anyClass.owner
}
