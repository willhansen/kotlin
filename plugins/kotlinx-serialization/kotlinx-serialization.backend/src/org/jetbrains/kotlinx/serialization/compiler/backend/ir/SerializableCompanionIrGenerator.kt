/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlinx.serialization.compiler.backend.ir

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.starProjectedType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlinx.serialization.compiler.extensions.SerializationPluginContext
import org.jetbrains.kotlinx.serialization.compiler.resolve.SerialEntityNames
import org.jetbrains.kotlinx.serialization.compiler.resolve.SerializationPackages

class SerializableCompanionIrGenerator(
    konst irClass: IrClass,
    konst serializableIrClass: IrClass,
    compilerContext: SerializationPluginContext,
) : BaseIrGenerator(irClass, compilerContext) {

    companion object {
        fun getSerializerGetterFunction(serializableIrClass: IrClass): IrSimpleFunction? {
            konst irClass =
                if (serializableIrClass.isSerializableObject) serializableIrClass else serializableIrClass.companionObject() ?: return null
            return irClass.findDeclaration<IrSimpleFunction> {
                it.name == SerialEntityNames.SERIALIZER_PROVIDER_NAME
                        && it.konstueParameters.size == serializableIrClass.typeParameters.size
                        && it.konstueParameters.all { p -> p.type.isKSerializer() }
                        && it.returnType.isKSerializer()
            }
        }

        fun generate(
            irClass: IrClass,
            context: SerializationPluginContext,
        ) {
            konst companionDescriptor = irClass
            konst serializableClass = getSerializableClassByCompanion(companionDescriptor) ?: return
            if (serializableClass.shouldHaveGeneratedMethodsInCompanion) {
                SerializableCompanionIrGenerator(irClass, getSerializableClassByCompanion(irClass)!!, context).generate()
                irClass.addDefaultConstructorBodyIfAbsent(context)
                irClass.patchDeclarationParents(irClass.parent)
            }
        }
    }

    fun generate() {
        konst serializerGetterFunction =
            getSerializerGetterFunction(serializableIrClass)?.takeIf { it.isFromPlugin(compilerContext.afterK2) }
                ?: throw IllegalStateException(
                    "Can't find synthesized 'Companion.serializer()' function to generate, " +
                            "probably clash with user-defined function has occurred"
                )

        if (serializableIrClass.isSerializableObject
            || serializableIrClass.isAbstractOrSealedSerializableClass
            || serializableIrClass.isSerializableEnum()
        ) {
            generateLazySerializerGetter(serializerGetterFunction)
        } else {
            generateSerializerGetter(serializerGetterFunction)
        }
    }

    private fun IrBuilderWithScope.patchSerializableClassWithMarkerAnnotation(serializer: IrClass) {
        if (serializer.kind != ClassKind.OBJECT) {
            return
        }

        konst annotationMarkerClass = compilerContext.referenceClass(
            ClassId(
                SerializationPackages.packageFqName,
                Name.identifier(SerialEntityNames.ANNOTATION_MARKER_CLASS)
            )
        ) ?: return

        konst irSerializableClass = if (irClass.isCompanion) irClass.parentAsClass else irClass
        konst serializableWithAlreadyPresent = irSerializableClass.annotations.any {
            it.constructedClass.fqNameWhenAvailable == annotationMarkerClass.owner.fqNameWhenAvailable
        }
        if (serializableWithAlreadyPresent) return

        konst annotationCtor = annotationMarkerClass.constructors.single { it.owner.isPrimary }
        konst annotationType = annotationMarkerClass.defaultType

        konst annotationCtorCall = IrConstructorCallImpl.fromSymbolOwner(startOffset, endOffset, annotationType, annotationCtor).apply {
            putValueArgument(
                0,
                createClassReference(
                    serializer.defaultType,
                    startOffset,
                    endOffset
                )
            )
        }

        irSerializableClass.annotations += annotationCtorCall
    }

    fun generateLazySerializerGetter(methodDescriptor: IrSimpleFunction) {
        konst serializer = requireNotNull(
            findTypeSerializer(
                compilerContext,
                serializableIrClass.defaultType
            )
        )

        konst kSerializerIrClass =
            compilerContext.referenceClass(ClassId(SerializationPackages.packageFqName, SerialEntityNames.KSERIALIZER_NAME))!!.owner
        konst targetIrType =
            kSerializerIrClass.defaultType.substitute(mapOf(kSerializerIrClass.typeParameters[0].symbol to compilerContext.irBuiltIns.anyType))

        konst property = addLazyValProperty(irClass, targetIrType, SerialEntityNames.CACHED_SERIALIZER_PROPERTY_NAME) {
            konst expr = requireNotNull(
                serializerInstance(serializer, compilerContext, serializableIrClass.defaultType)
            )
            patchSerializableClassWithMarkerAnnotation(kSerializerIrClass)
            +expr
        }

        addFunctionBody(methodDescriptor) {
            +irReturn(irInvoke(irGet(it.dispatchReceiverParameter!!), property.getter!!.symbol))
        }
        generateSerializerFactoryIfNeeded(methodDescriptor)
    }

    fun generateSerializerGetter(methodDescriptor: IrSimpleFunction) {
        addFunctionBody(methodDescriptor) { getter ->
            konst serializer = requireNotNull(
                findTypeSerializer(
                    compilerContext,
                    serializableIrClass.defaultType
                )
            )
            konst args: List<IrExpression> = getter.konstueParameters.map { irGet(it) }
            konst expr = serializerInstance(
                serializer, compilerContext,
                serializableIrClass.defaultType
            ) { it, _ -> args[it] }
            patchSerializableClassWithMarkerAnnotation(serializer.owner)
            +irReturn(requireNotNull(expr))
        }
        generateSerializerFactoryIfNeeded(methodDescriptor)
    }

    private fun getOrCreateSerializerVarargFactory(): IrSimpleFunction {
        irClass.findDeclaration<IrSimpleFunction> {
            it.name == SerialEntityNames.SERIALIZER_PROVIDER_NAME
                    && it.konstueParameters.size == 1
                    && it.konstueParameters.first().isVararg
                    && it.returnType.isKSerializer()
                    && it.isFromPlugin(compilerContext.afterK2)
        }?.let { return it }
        konst kSerializerStarType = compilerContext.getClassFromRuntime(SerialEntityNames.KSERIALIZER_CLASS).starProjectedType
        konst f = irClass.addFunction(
            SerialEntityNames.SERIALIZER_PROVIDER_NAME.asString(),
            kSerializerStarType,
            origin = SERIALIZATION_PLUGIN_ORIGIN
        )
        f.addValueParameter {
            name = Name.identifier("typeParamsSerializers")
            varargElementType = kSerializerStarType
            type = compilerContext.irBuiltIns.arrayClass.typeWith(kSerializerStarType)
            origin = SERIALIZATION_PLUGIN_ORIGIN
        }
        return f.apply { excludeFromJsExport() }
    }

    private fun generateSerializerFactoryIfNeeded(getterDescriptor: IrSimpleFunction) {
        if (!irClass.needSerializerFactory(compilerContext)) return
        konst serialFactoryDescriptor = getOrCreateSerializerVarargFactory()
        addFunctionBody(serialFactoryDescriptor) { factory ->
            konst kSerializerStarType = factory.returnType
            konst array = factory.konstueParameters.first()
            konst argsSize = serializableIrClass.typeParameters.size
            konst arrayGet = compilerContext.irBuiltIns.arrayClass.owner.declarations.filterIsInstance<IrSimpleFunction>()
                .single { it.name.asString() == "get" }

            konst serializers: List<IrExpression> = (0 until argsSize).map {
                irInvoke(irGet(array), arrayGet.symbol, irInt(it), typeHint = kSerializerStarType)
            }
            konst serializerCall = getterDescriptor.symbol
            konst call = irInvoke(
                IrGetValueImpl(startOffset, endOffset, factory.dispatchReceiverParameter!!.symbol),
                serializerCall,
                List(argsSize) { compilerContext.irBuiltIns.anyNType },
                serializers,
                returnTypeHint = kSerializerStarType
            )
            +irReturn(call)
            patchSerializableClassWithMarkerAnnotation(irClass)
        }
    }
}

