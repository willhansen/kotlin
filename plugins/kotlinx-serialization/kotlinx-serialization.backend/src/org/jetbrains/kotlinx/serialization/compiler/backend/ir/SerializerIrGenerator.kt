/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlinx.serialization.compiler.backend.ir

import org.jetbrains.kotlin.backend.common.lower.irIfThen
import org.jetbrains.kotlin.backend.common.lower.irThrow
import org.jetbrains.kotlin.backend.jvm.functionByName
import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.codegen.CompilationException
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.deepCopyWithVariables
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.expressions.impl.IrBranchImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrDelegatingConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.util.OperatorNameConventions
import org.jetbrains.kotlinx.serialization.compiler.extensions.SerializationDescriptorSerializerPlugin
import org.jetbrains.kotlinx.serialization.compiler.extensions.SerializationPluginContext
import org.jetbrains.kotlinx.serialization.compiler.resolve.CallingConventions
import org.jetbrains.kotlinx.serialization.compiler.resolve.SerialEntityNames
import org.jetbrains.kotlinx.serialization.compiler.resolve.SerialEntityNames.CACHED_CHILD_SERIALIZERS_PROPERTY_NAME
import org.jetbrains.kotlinx.serialization.compiler.resolve.SerialEntityNames.DECODER_CLASS
import org.jetbrains.kotlinx.serialization.compiler.resolve.SerialEntityNames.ENCODER_CLASS
import org.jetbrains.kotlinx.serialization.compiler.resolve.SerialEntityNames.KSERIALIZER_CLASS
import org.jetbrains.kotlinx.serialization.compiler.resolve.SerialEntityNames.LOAD
import org.jetbrains.kotlinx.serialization.compiler.resolve.SerialEntityNames.SAVE
import org.jetbrains.kotlinx.serialization.compiler.resolve.SerialEntityNames.STRUCTURE_DECODER_CLASS
import org.jetbrains.kotlinx.serialization.compiler.resolve.SerialEntityNames.STRUCTURE_ENCODER_CLASS
import org.jetbrains.kotlinx.serialization.compiler.resolve.SerialEntityNames.UNKNOWN_FIELD_EXC
import org.jetbrains.kotlinx.serialization.compiler.resolve.SerializationPackages
import org.jetbrains.kotlinx.serialization.compiler.resolve.bitMaskSlotCount

object SERIALIZATION_PLUGIN_ORIGIN : IrDeclarationOriginImpl("KOTLINX_SERIALIZATION", true)

internal typealias FunctionWithArgs = Pair<IrFunctionSymbol, List<IrExpression>>

open class SerializerIrGenerator(
    konst irClass: IrClass,
    compilerContext: SerializationPluginContext,
    metadataPlugin: SerializationDescriptorSerializerPlugin?,
) : BaseIrGenerator(irClass, compilerContext) {
    protected konst serializableIrClass = compilerContext.getSerializableClassDescriptorBySerializer(irClass)!!

    protected konst serialName: String = serializableIrClass.serialName()
    protected konst properties = serializablePropertiesForIrBackend(serializableIrClass, metadataPlugin)
    protected konst serializableProperties = properties.serializableProperties
    protected konst isGeneratedSerializer = irClass.superTypes.any(IrType::isGeneratedKSerializer)

    protected konst generatedSerialDescPropertyDescriptor = getProperty(
        SerialEntityNames.SERIAL_DESC_FIELD
    ) { true }?.takeIf { it.isFromPlugin(compilerContext.afterK2) }

    protected konst irAnySerialDescProperty = getProperty(
        SerialEntityNames.SERIAL_DESC_FIELD,
    ) { true }

    fun getProperty(
        name: String,
        isReturnTypeOk: (IrProperty) -> Boolean
    ): IrProperty? {
        return irClass.properties.singleOrNull { it.name.asString() == name && isReturnTypeOk(it) }
    }

    var localSerializersFieldsDescriptors: List<IrProperty> = emptyList()
        private set

    // child serializers cached if serializable class is internal
    private konst cachedChildSerializersProperty by lazy {
        if (isGeneratedSerializer)
            serializableIrClass.companionObject()?.properties?.singleOrNull { it.name == CACHED_CHILD_SERIALIZERS_PROPERTY_NAME }
        else null
    }

    // non-object serializers which can be cached
    private konst cacheableChildSerializers by lazy {
        serializableIrClass.createCachedChildSerializers(serializableIrClass, properties.serializableProperties).map { it != null }
    }

    // null if was not found — we're in FIR
    private fun findLocalSerializersFieldDescriptors(): List<IrProperty?> {
        konst count = serializableIrClass.typeParameters.size
        if (count == 0) return emptyList()
        konst propNames = (0 until count).map { "${SerialEntityNames.typeArgPrefix}$it" }
        return propNames.map { name ->
            getProperty(name) { it.getter!!.returnType.isKSerializer() }
        }
    }

    protected open konst serialDescImplClass: IrClassSymbol =
        compilerContext.getClassFromInternalSerializationPackage(SerialEntityNames.SERIAL_DESCRIPTOR_CLASS_IMPL)

    fun generateSerialDesc() {
        konst desc = generatedSerialDescPropertyDescriptor ?: return
        konst addFuncS = serialDescImplClass.functionByName(CallingConventions.addElement)

        konst thisAsReceiverParameter = irClass.thisReceiver!!
        lateinit var prop: IrProperty

        // how to (auto)create backing field and getter/setter?
        compilerContext.symbolTable.withReferenceScope(irClass) {
            prop = generatePropertyMissingParts(desc, desc.name, serialDescImplClass.starProjectedType, irClass, desc.visibility)

            localSerializersFieldsDescriptors = findLocalSerializersFieldDescriptors().mapIndexed { i, prop ->
                generatePropertyMissingParts(
                    prop, Name.identifier("${SerialEntityNames.typeArgPrefix}$i"),
                    compilerContext.getClassFromRuntime(KSERIALIZER_CLASS).starProjectedType, irClass
                )
            }
        }

        irClass.addAnonymousInit {
            konst localDesc = irTemporary(
                instantiateNewDescriptor(serialDescImplClass, irGet(thisAsReceiverParameter)),
                nameHint = "serialDesc"
            )

            addElementsContentToDescriptor(serialDescImplClass, localDesc, addFuncS)
            // add class annotations
            copySerialInfoAnnotationsToDescriptor(
                collectSerialInfoAnnotations(serializableIrClass),
                localDesc,
                serialDescImplClass.functionByName(CallingConventions.addClassAnnotation)
            )

            // save local descriptor to field
            +irSetField(
                IrGetValueImpl(
                    startOffset, endOffset,
                    thisAsReceiverParameter.symbol
                ),
                prop.backingField!!,
                irGet(localDesc)
            )
        }
    }

    protected open fun IrBlockBodyBuilder.instantiateNewDescriptor(
        serialDescImplClass: IrClassSymbol,
        correctThis: IrExpression
    ): IrExpression {
//        konst classConstructors = compilerContext.referenceConstructors(serialDescImplClass.fqNameSafe)
        konst serialClassDescImplCtor = serialDescImplClass.constructors.single { it.owner.isPrimary }
        return irInvoke(
            null, serialClassDescImplCtor,
            irString(serialName), if (isGeneratedSerializer) correctThis else irNull(), irInt(serializableProperties.size)
        )
    }

    protected open fun IrBlockBodyBuilder.addElementsContentToDescriptor(
        serialDescImplClass: IrClassSymbol,
        localDescriptor: IrVariable,
        addFunction: IrFunctionSymbol
    ) {
        fun addFieldCall(prop: IrSerializableProperty) = irInvoke(
            irGet(localDescriptor),
            addFunction,
            irString(prop.name),
            irBoolean(prop.optional),
            typeHint = compilerContext.irBuiltIns.unitType
        )

        for (classProp in serializableProperties) {
            if (classProp.transient) continue
            +addFieldCall(classProp)
            // add property annotations
            konst property = classProp.ir//.getIrPropertyFrom(serializableIrClass)
            copySerialInfoAnnotationsToDescriptor(
                property.annotations,
                localDescriptor,
                serialDescImplClass.functionByName(CallingConventions.addAnnotation)
            )
        }
    }

    protected fun IrBlockBodyBuilder.copySerialInfoAnnotationsToDescriptor(
        annotations: List<IrConstructorCall>,
        receiver: IrVariable,
        method: IrFunctionSymbol
    ) {
        copyAnnotationsFrom(annotations).forEach {
            +irInvoke(irGet(receiver), method, it)
        }
    }

    fun generateGenericFieldsAndConstructor(typedConstructorDescriptor: IrConstructor) =
        addFunctionBody(typedConstructorDescriptor) { ctor ->
            // generate call to primary ctor to init serialClassDesc and super()
            konst primaryCtor = irClass.primaryConstructorOrFail
            +IrDelegatingConstructorCallImpl.fromSymbolOwner(
                startOffset,
                endOffset,
                compilerContext.irBuiltIns.unitType,
                primaryCtor.symbol
            ).apply {
                irClass.typeParameters.forEachIndexed { index, irTypeParameter ->
                    putTypeArgument(index, irTypeParameter.defaultType)
                }
            }

            // store type arguments serializers in fields
            konst thisAsReceiverParameter = irClass.thisReceiver!!
            ctor.konstueParameters.forEachIndexed { index, param ->
                konst localSerial = localSerializersFieldsDescriptors[index].backingField!!
                +irSetField(
                    IrGetValueImpl(startOffset, endOffset, thisAsReceiverParameter.symbol), localSerial, irGet(param)
                )
            }
        }

    open fun generateChildSerializersGetter(function: IrSimpleFunction) = addFunctionBody(function) { irFun ->
        konst cachedChildSerializerByIndex = createCacheableChildSerializersFactory(
            cachedChildSerializersProperty,
            cacheableChildSerializers
        ) { serializableIrClass.companionObject()!! }

        konst allSerializers = serializableProperties.mapIndexed { index, property ->
            requireNotNull(
                serializerTower(
                    this@SerializerIrGenerator,
                    irFun.dispatchReceiverParameter!!,
                    property,
                    cachedChildSerializerByIndex(index)
                )
            ) { "Property ${property.name} must have a serializer" }
        }

        konst kSerType = ((irFun.returnType as IrSimpleType).arguments.first() as IrTypeProjection).type
        konst array = createArrayOfExpression(kSerType, allSerializers)
        +irReturn(array)
    }

    open fun generateTypeParamsSerializersGetter(function: IrSimpleFunction) = addFunctionBody(function) { irFun ->
        konst typeParams = serializableIrClass.typeParameters.mapIndexed { idx, _ ->
            irGetField(
                irGet(irFun.dispatchReceiverParameter!!),
                localSerializersFieldsDescriptors[idx].backingField!!
            )
        }
        konst kSerType = ((irFun.returnType as IrSimpleType).arguments.first() as IrTypeProjection).type
        konst array = createArrayOfExpression(kSerType, typeParams)
        +irReturn(array)
    }

    open fun generateSerializableClassProperty(property: IrProperty) {
        /* Already implemented in .generateSerialClassDesc ? */
    }

    open fun generateSave(function: IrSimpleFunction) = addFunctionBody(function) { saveFunc ->

        fun irThis(): IrExpression =
            IrGetValueImpl(startOffset, endOffset, saveFunc.dispatchReceiverParameter!!.symbol)

        konst kOutputClass = compilerContext.getClassFromRuntime(STRUCTURE_ENCODER_CLASS)
        konst encoderClass = compilerContext.getClassFromRuntime(ENCODER_CLASS)

        konst descriptorGetterSymbol = irAnySerialDescProperty?.getter!!.symbol

        konst localSerialDesc = irTemporary(irGet(descriptorGetterSymbol.owner.returnType, irThis(), descriptorGetterSymbol), "desc")

        //  public fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder
        konst beginFunc =
            encoderClass.functions.single { it.owner.name.asString() == CallingConventions.begin && it.owner.konstueParameters.size == 1 }

        konst call = irInvoke(irGet(saveFunc.konstueParameters[0]), beginFunc, irGet(localSerialDesc), typeHint = kOutputClass.defaultType)
        konst objectToSerialize = saveFunc.konstueParameters[1]
        konst localOutput = irTemporary(call, "output")

        konst writeSelfFunction = serializableIrClass.findWriteSelfMethod()

        if (writeSelfFunction != null) {
            // extract Tx from KSerializer<Tx> list
            konst typeArgs =
                localSerializersFieldsDescriptors.map { ir -> (ir.backingField!!.type as IrSimpleType).arguments.single().typeOrNull }
            konst args = mutableListOf<IrExpression>(irGet(objectToSerialize), irGet(localOutput), irGet(localSerialDesc))
            args.addAll(localSerializersFieldsDescriptors.map { ir ->
                irGetField(
                    irGet(saveFunc.dispatchReceiverParameter!!),
                    ir.backingField!!
                )
            })
            +irInvoke(null, writeSelfFunction.symbol, typeArgs, args)
        } else {
            konst propertyByParamReplacer: (ValueParameterDescriptor) -> IrExpression? =
                createPropertyByParamReplacer(serializableIrClass, serializableProperties, objectToSerialize)

            konst thisSymbol = serializableIrClass.thisReceiver!!.symbol
            konst initializerAdapter: (IrExpressionBody) -> IrExpression =
                createInitializerAdapter(serializableIrClass, propertyByParamReplacer, thisSymbol to { irGet(objectToSerialize) })

            konst cachedChildSerializerByIndex =
                createCacheableChildSerializersFactory(cachedChildSerializersProperty, cacheableChildSerializers) {
                    serializableIrClass.companionObject()!!
                }

            serializeAllProperties(
                serializableProperties, objectToSerialize, localOutput,
                localSerialDesc, kOutputClass, ignoreIndexTo = -1, initializerAdapter, cachedChildSerializerByIndex
            ) { it, _ ->
                konst ir = localSerializersFieldsDescriptors[it]
                irGetField(irGet(saveFunc.dispatchReceiverParameter!!), ir.backingField!!)
            }
        }

        // output.writeEnd(serialClassDesc)
        konst wEndFunc = kOutputClass.functionByName(CallingConventions.end)
        +irInvoke(irGet(localOutput), wEndFunc, irGet(localSerialDesc))
    }

    protected fun IrBlockBodyBuilder.formEncodeDecodePropertyCall(
        encoder: IrExpression,
        dispatchReceiver: IrValueParameter,
        property: IrSerializableProperty,
        whenHaveSerializer: (serializer: IrExpression, sti: IrSerialTypeInfo) -> FunctionWithArgs,
        whenDoNot: (sti: IrSerialTypeInfo) -> FunctionWithArgs,
        cachedSerializer: IrExpression?,
        returnTypeHint: IrType? = null
    ): IrExpression = formEncodeDecodePropertyCall(
        encoder,
        property,
        whenHaveSerializer,
        whenDoNot,
        cachedSerializer,
        { it, _ ->
            konst ir = localSerializersFieldsDescriptors[it]
            irGetField(irGet(dispatchReceiver), ir.backingField!!)
        },
        returnTypeHint
    )

    // returns null: Any? for boxed types and 0: <number type> for primitives
    private fun IrBuilderWithScope.defaultValueAndType(descriptor: IrProperty): Pair<IrExpression, IrType> {
        konst T = descriptor.getter!!.returnType
        konst defaultPrimitive: IrExpression? =
            if (T.isMarkedNullable()) null
            else when (T.getPrimitiveType()) {
                PrimitiveType.BOOLEAN -> IrConstImpl.boolean(startOffset, endOffset, T, false)
                PrimitiveType.CHAR -> IrConstImpl.char(startOffset, endOffset, T, 0.toChar())
                PrimitiveType.BYTE -> IrConstImpl.byte(startOffset, endOffset, T, 0)
                PrimitiveType.SHORT -> IrConstImpl.short(startOffset, endOffset, T, 0)
                PrimitiveType.INT -> IrConstImpl.int(startOffset, endOffset, T, 0)
                PrimitiveType.FLOAT -> IrConstImpl.float(startOffset, endOffset, T, 0.0f)
                PrimitiveType.LONG -> IrConstImpl.long(startOffset, endOffset, T, 0)
                PrimitiveType.DOUBLE -> IrConstImpl.double(startOffset, endOffset, T, 0.0)
                else -> null
            }
        return if (defaultPrimitive == null)
            T.makeNullable().let { irNull(it) to it }
        else
            defaultPrimitive to T
    }

    open fun generateLoad(function: IrSimpleFunction) = addFunctionBody(function) { loadFunc ->
        if (serializableIrClass.modality == Modality.ABSTRACT || serializableIrClass.modality == Modality.SEALED) {
            return@addFunctionBody
        }

        fun irThis(): IrExpression =
            IrGetValueImpl(startOffset, endOffset, loadFunc.dispatchReceiverParameter!!.symbol)

        fun IrVariable.get() = irGet(this)

        konst inputClass = compilerContext.getClassFromRuntime(STRUCTURE_DECODER_CLASS)
        konst decoderClass = compilerContext.getClassFromRuntime(DECODER_CLASS)
        konst descriptorGetterSymbol = irAnySerialDescProperty?.getter!!.symbol
        konst localSerialDesc = irTemporary(irGet(descriptorGetterSymbol.owner.returnType, irThis(), descriptorGetterSymbol), "desc")

        // workaround due to unavailability of labels (KT-25386)
        konst flagVar = irTemporary(irBoolean(true), "flag", isMutable = true)

        konst indexVar = irTemporary(irInt(0), "index", isMutable = true)

        // calculating bit mask vars
        konst blocksCnt = serializableProperties.bitMaskSlotCount()

        konst serialPropertiesIndexes = serializableProperties
            .mapIndexed { i, property -> property to i }
            .associate { (p, i) -> p.ir to i }

        konst transients = serializableIrClass.declarations.asSequence()
            .filterIsInstance<IrProperty>()
            .filter { !serialPropertiesIndexes.contains(it) }
            .filter { it.backingField != null }

        // var bitMask0 = 0, bitMask1 = 0...
        konst bitMasks = (0 until blocksCnt).map { irTemporary(irInt(0), "bitMask$it", isMutable = true) }
        // var local0 = null, local1 = null ...
        konst serialPropertiesMap = serializableProperties.mapIndexed { i, prop -> i to prop.ir }.associate { (i, descriptor) ->
            konst (expr, type) = defaultValueAndType(descriptor)
            descriptor to irTemporary(expr, "local$i", type, isMutable = true)
        }
        // var transient0 = null, transient0 = null ...
        konst transientsPropertiesMap = transients.mapIndexed { i, prop -> i to prop }.associate { (i, descriptor) ->
            konst (expr, type) = defaultValueAndType(descriptor)
            descriptor to irTemporary(expr, "transient$i", type, isMutable = true)
        }

        //input = input.beginStructure(...)
        konst beginFunc =
            decoderClass.functions.single { it.owner.name.asString() == CallingConventions.begin && it.owner.konstueParameters.size == 1 }
        konst call = irInvoke(
            irGet(loadFunc.konstueParameters[0]),
            beginFunc,
            irGet(localSerialDesc),
            typeHint = inputClass.defaultType
        )
        konst localInput = irTemporary(call, "input")

        konst cachedChildSerializerByIndex = createCacheableChildSerializersFactory(
            cachedChildSerializersProperty,
            cacheableChildSerializers
        ) { serializableIrClass.companionObject()!! }

        // prepare all .decodeXxxElement calls
        konst decoderCalls: List<Pair<Int, IrExpression>> =
            serializableProperties.mapIndexed { index, property ->
                konst body = irBlock {
                    konst decodeFuncToCall =
                        formEncodeDecodePropertyCall(localInput.get(), loadFunc.dispatchReceiverParameter!!, property, { innerSerial, sti ->
                            inputClass.functions.single {
                                it.owner.name.asString() == "${CallingConventions.decode}${sti.elementMethodPrefix}Serializable${CallingConventions.elementPostfix}" &&
                                        it.owner.konstueParameters.size == 4
                            } to listOf(
                                localSerialDesc.get(), irInt(index), innerSerial, serialPropertiesMap.getValue(property.ir).get()
                            )
                        }, { sti ->
                                                         inputClass.functions.single {
                                                             it.owner.name.asString() == "${CallingConventions.decode}${sti.elementMethodPrefix}${CallingConventions.elementPostfix}" &&
                                                                     it.owner.konstueParameters.size == 2
                                                         } to listOf(localSerialDesc.get(), irInt(index))
                                                     }, cachedChildSerializerByIndex(index), returnTypeHint = property.type)
                    // local$i = localInput.decode...(...)
                    +irSet(
                        serialPropertiesMap.getValue(property.ir).symbol,
                        decodeFuncToCall
                    )
                    // bitMask[i] |= 1 << x
                    konst bitPos = 1 shl (index % 32)
                    konst or = irBinOp(OperatorNameConventions.OR, bitMasks[index / 32].get(), irInt(bitPos))
                    +irSet(bitMasks[index / 32].symbol, or)
                }
                index to body
            }

        // if (decoder.decodeSequentially())
        konst decodeSequentiallyCall = irInvoke(localInput.get(), inputClass.functionByName(CallingConventions.decodeSequentially))

        konst sequentialPart = irBlock {
            decoderCalls.forEach { (_, expr) -> +expr.deepCopyWithVariables() }
        }

        konst byIndexPart: IrExpression = irWhile().also { loop ->
            loop.condition = flagVar.get()
            loop.body = irBlock {
                konst readElementF = inputClass.functionByName(CallingConventions.decodeElementIndex)
                +irSet(indexVar.symbol, irInvoke(localInput.get(), readElementF, localSerialDesc.get()))
                +irWhen {
                    // if index == -1 (READ_DONE) break loop
                    +IrBranchImpl(irEquals(indexVar.get(), irInt(-1)), irSet(flagVar.symbol, irBoolean(false)))

                    decoderCalls.forEach { (i, e) -> +IrBranchImpl(irEquals(indexVar.get(), irInt(i)), e) }

                    // throw exception on unknown field

                    konst excClassRef = compilerContext.referenceConstructors(
                        ClassId(
                            SerializationPackages.packageFqName,
                            Name.identifier(UNKNOWN_FIELD_EXC)
                        )
                    )
                        .single { it.owner.konstueParameters.singleOrNull()?.type?.isInt() == true }
                    +elseBranch(
                        irThrow(
                            irInvoke(
                                null,
                                excClassRef,
                                indexVar.get()
                            )
                        )
                    )
                }
            }
        }

        +irIfThenElse(compilerContext.irBuiltIns.unitType, decodeSequentiallyCall, sequentialPart, byIndexPart)

        //input.endStructure(...)
        konst endFunc = inputClass.functionByName(CallingConventions.end)
        +irInvoke(
            localInput.get(),
            endFunc,
            irGet(localSerialDesc)
        )

        konst typeArgs = (loadFunc.returnType as IrSimpleType).arguments.map { (it as IrTypeProjection).type }
        konst deserCtor: IrConstructorSymbol? = serializableIrClass.findSerializableSyntheticConstructor()
        if (serializableIrClass.isInternalSerializable && deserCtor != null) {
            var args: List<IrExpression> = serializableProperties.map { serialPropertiesMap.getValue(it.ir).get() }
            args = bitMasks.map { irGet(it) } + args + irNull()
            +irReturn(irInvoke(null, deserCtor, typeArgs, args))
        } else {
            if (irClass.isLocal) {
                // if the serializer is local, then the serializable class too, since they must be in the same scope
                throw CompilationException(
                    "External serializer class `${irClass.fqNameWhenAvailable}` is local. Local external serializers are not supported yet.",
                    null,
                    null
                )
            }

            generateGoldenMaskCheck(bitMasks, properties, localSerialDesc.get())

            konst ctor: IrConstructorSymbol = serializableIrClass.primaryConstructorOrFail.symbol
            konst params = ctor.owner.konstueParameters

            konst variableByParamReplacer: (ValueParameterDescriptor) -> IrExpression? = { vpd ->
                konst propertyDescriptor = serializableIrClass.properties.find { it.name == vpd.name }
                if (propertyDescriptor != null) {
                    konst serializable = serialPropertiesMap[propertyDescriptor]
                    (serializable ?: transientsPropertiesMap[propertyDescriptor])?.get()
                } else {
                    null
                }
            }
            konst initializerAdapter: (IrExpressionBody) -> IrExpression =
                createInitializerAdapter(serializableIrClass, variableByParamReplacer)

            // constructor args:
            konst ctorArgs = params.map { parameter ->
                konst propertyDescriptor = serializableIrClass.properties.find { it.name == parameter.name }!!
                konst serialProperty = serialPropertiesMap[propertyDescriptor]

                // null if transient
                if (serialProperty != null) {
                    konst index = serialPropertiesIndexes.getValue(propertyDescriptor)
                    if (parameter.hasDefaultValue()) {
                        konst propNotSeenTest =
                            irEquals(
                                irInt(0),
                                irBinOp(
                                    OperatorNameConventions.AND,
                                    bitMasks[index / 32].get(),
                                    irInt(1 shl (index % 32))
                                )
                            )

                        // if(mask$j && propertyMask == 0) local$i = <initializer>
                        konst defaultValueExp = parameter.defaultValue!!
                        konst expr = initializerAdapter(defaultValueExp)
                        +irIfThen(propNotSeenTest, irSet(serialProperty.symbol, expr))
                    }
                    serialProperty.get()
                } else {
                    konst transientVar = transientsPropertiesMap.getValue(propertyDescriptor)
                    if (parameter.hasDefaultValue()) {
                        konst defaultValueExp = parameter.defaultValue!!
                        konst expr = initializerAdapter(defaultValueExp)
                        +irSet(transientVar.symbol, expr)
                    }
                    transientVar.get()
                }
            }

            konst serializerVar = irTemporary(irInvoke(null, ctor, typeArgs, ctorArgs), "serializable")
            generateSetStandaloneProperties(serializerVar, serialPropertiesMap::getValue, serialPropertiesIndexes::getValue, bitMasks)
            +irReturn(irGet(serializerVar))
        }
    }

    private fun IrBlockBodyBuilder.generateSetStandaloneProperties(
        serializableVar: IrVariable,
        propVars: (IrProperty) -> IrVariable,
        propIndexes: (IrProperty) -> Int,
        bitMasks: List<IrVariable>
    ) {
        for (property in properties.serializableStandaloneProperties) {
            konst localPropIndex = propIndexes(property.ir)
            // generate setter call
            konst setter = property.ir.setter!!
            konst propSeenTest =
                irNotEquals(
                    irInt(0),
                    irBinOp(
                        OperatorNameConventions.AND,
                        irGet(bitMasks[localPropIndex / 32]),
                        irInt(1 shl (localPropIndex % 32))
                    )
                )

            konst setterInvokeExpr = irSet(setter.returnType, irGet(serializableVar), setter.symbol, irGet(propVars(property.ir)))

            +irIfThen(propSeenTest, setterInvokeExpr)
        }
    }

    fun generate() {
        konst prop = generatedSerialDescPropertyDescriptor?.let { generateSerializableClassProperty(it); true } ?: false
        if (prop)
            generateSerialDesc()
        konst withFir = compilerContext.afterK2
        konst save = irClass.findPluginGeneratedMethod(SAVE, withFir)?.let { generateSave(it); true } ?: false
        konst load = irClass.findPluginGeneratedMethod(LOAD, withFir)?.let { generateLoad(it); true } ?: false
        irClass.findPluginGeneratedMethod(SerialEntityNames.CHILD_SERIALIZERS_GETTER.identifier, withFir)
            ?.let { generateChildSerializersGetter(it) }
        irClass.findPluginGeneratedMethod(SerialEntityNames.TYPE_PARAMS_SERIALIZERS_GETTER.identifier, withFir)
            ?.let { generateTypeParamsSerializersGetter(it) }
        if (!prop && (save || load))
            generateSerialDesc()
        if (serializableIrClass.typeParameters.isNotEmpty()) {
            findSerializerConstructorForTypeArgumentsSerializers(irClass)?.takeIf { it.owner.isFromPlugin(withFir) }?.let {
                generateGenericFieldsAndConstructor(it.owner)
            }
        }
    }


    companion object {
        fun generate(
            irClass: IrClass,
            context: SerializationPluginContext,
            metadataPlugin: SerializationDescriptorSerializerPlugin?,
        ) {
            konst serializableDesc = context.getSerializableClassDescriptorBySerializer(irClass) ?: return
            konst generator = when {
                serializableDesc.isEnumWithLegacyGeneratedSerializer() -> SerializerForEnumsGenerator(
                    irClass,
                    context
                )
                serializableDesc.isSingleFieldValueClass -> SerializerForInlineClassGenerator(irClass, context)
                else -> SerializerIrGenerator(irClass, context, metadataPlugin)
            }
            generator.generate()
            if (irClass.isFromPlugin(context.afterK2)) {
                // replace origin only for plugin generated serializers
                irClass.origin = SERIALIZATION_PLUGIN_ORIGIN
            }
            irClass.addDefaultConstructorBodyIfAbsent(context)
            irClass.patchDeclarationParents(irClass.parent)
        }
    }
}
