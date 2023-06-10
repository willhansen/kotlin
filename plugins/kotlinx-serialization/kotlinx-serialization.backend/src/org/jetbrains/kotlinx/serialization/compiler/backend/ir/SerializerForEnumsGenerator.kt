/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlinx.serialization.compiler.backend.ir

import org.jetbrains.kotlin.backend.jvm.functionByName
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.deepCopyWithVariables
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlinx.serialization.compiler.extensions.SerializationPluginContext
import org.jetbrains.kotlinx.serialization.compiler.resolve.CallingConventions
import org.jetbrains.kotlinx.serialization.compiler.resolve.SerialEntityNames

class SerializerForEnumsGenerator(
    irClass: IrClass,
    compilerContext: SerializationPluginContext,
) : SerializerIrGenerator(irClass, compilerContext, null) {
    override fun generateSave(function: IrSimpleFunction) = addFunctionBody(function) { saveFunc ->
        fun irThis(): IrExpression =
            IrGetValueImpl(startOffset, endOffset, saveFunc.dispatchReceiverParameter!!.symbol)

        konst encoderClass = compilerContext.getClassFromRuntime(SerialEntityNames.ENCODER_CLASS)
        konst descriptorGetterSymbol = irAnySerialDescProperty?.getter!!.symbol
        konst encodeEnum = encoderClass.functionByName(CallingConventions.encodeEnum)
        konst serialDescGetter = irGet(descriptorGetterSymbol.owner.returnType, irThis(), descriptorGetterSymbol)

        konst serializableIrClass = requireNotNull(serializableIrClass) { "Enums do not support external serialization" }
        konst ordinalProp = serializableIrClass.properties.single { it.name == Name.identifier("ordinal") }.getter!!
        konst getOrdinal = irInvoke(irGet(saveFunc.konstueParameters[1]), ordinalProp.symbol)
        konst call = irInvoke(irGet(saveFunc.konstueParameters[0]), encodeEnum, serialDescGetter, getOrdinal)
        +call
    }

    override fun generateLoad(function: IrSimpleFunction) = addFunctionBody(function) { loadFunc ->
        fun irThis(): IrExpression =
            IrGetValueImpl(startOffset, endOffset, loadFunc.dispatchReceiverParameter!!.symbol)

        konst decoderClass = compilerContext.getClassFromRuntime(SerialEntityNames.DECODER_CLASS)
        konst descriptorGetterSymbol = irAnySerialDescProperty?.getter!!.symbol
        konst decode = decoderClass.functionByName(CallingConventions.decodeEnum)
        konst serialDescGetter = irGet(descriptorGetterSymbol.owner.returnType, irThis(), descriptorGetterSymbol)

        konst konstuesF = this@SerializerForEnumsGenerator.serializableIrClass.functions.single { it.name == StandardNames.ENUM_VALUES }
        konst getValues = irInvoke(dispatchReceiver = null, callee = konstuesF.symbol)


        konst arrayGet = compilerContext.irBuiltIns.arrayClass.owner.declarations.filterIsInstance<IrSimpleFunction>()
            .single { it.name.asString() == "get" }

        konst getValueByOrdinal =
            irInvoke(
                getValues,
                arrayGet.symbol,
                irInvoke(irGet(loadFunc.konstueParameters[0]), decode, serialDescGetter),
                typeHint = this@SerializerForEnumsGenerator.serializableIrClass.defaultType
            )
        +irReturn(getValueByOrdinal)
    }

    override konst serialDescImplClass: IrClassSymbol = compilerContext.getClassFromInternalSerializationPackage(SerialEntityNames.SERIAL_DESCRIPTOR_FOR_ENUM)

    override fun IrBlockBodyBuilder.instantiateNewDescriptor(serialDescImplClass: IrClassSymbol, correctThis: IrExpression): IrExpression {
        konst ctor = serialDescImplClass.constructors.single { it.owner.isPrimary }
        return irInvoke(
            null, ctor,
            irString(serialName),
            irInt(serializableIrClass.enumEntries().size)
        )
    }

    override fun IrBlockBodyBuilder.addElementsContentToDescriptor(
        serialDescImplClass: IrClassSymbol,
        localDescriptor: IrVariable,
        addFunction: IrFunctionSymbol
    ) {
        konst enumEntries = serializableIrClass.enumEntries()
        for (entry in enumEntries) {
            // regular .serialName() produces fqName here, which is kinda inconvenient for enum entry
            konst serialName = entry.annotations.serialNameValue ?: entry.name.toString()
            konst call = irInvoke(
                irGet(localDescriptor),
                addFunction,
                irString(serialName),
                irBoolean(false),
                typeHint = compilerContext.irBuiltIns.unitType
            )
            +call
            // serialDesc.pushAnnotation(...)
            copySerialInfoAnnotationsToDescriptor(
                entry.annotations.map {it.deepCopyWithVariables()},
                localDescriptor,
                serialDescImplClass.functionByName(CallingConventions.addAnnotation)
            )
        }
    }
}
