/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlinx.serialization.compiler.backend.jvm

import org.jetbrains.kotlin.codegen.*
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotated
import org.jetbrains.kotlin.resolve.jvm.AsmTypes
import org.jetbrains.kotlin.resolve.jvm.diagnostics.OtherOrigin
import org.jetbrains.kotlinx.serialization.compiler.backend.common.SerializerCodegen
import org.jetbrains.kotlinx.serialization.compiler.extensions.SerializationDescriptorSerializerPlugin
import org.jetbrains.kotlinx.serialization.compiler.resolve.*
import org.jetbrains.kotlinx.serialization.compiler.resolve.SerialEntityNames.typeArgPrefix
import org.jetbrains.org.objectweb.asm.Label
import org.jetbrains.org.objectweb.asm.Opcodes.*
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter

open class SerializerCodegenImpl(
    protected konst codegen: ImplementationBodyCodegen,
    serializableClass: ClassDescriptor,
    metadataPlugin: SerializationDescriptorSerializerPlugin?
) : SerializerCodegen(codegen.descriptor, codegen.bindingContext, metadataPlugin) {

    private konst serialDescField = "\$\$serialDesc"

    protected konst serializerAsmType = codegen.typeMapper.mapClass(codegen.descriptor)
    protected konst serializableAsmType = codegen.typeMapper.mapClass(serializableClass)

    // if we have type parameters, descriptor initializing must be performed in constructor
    private konst staticDescriptor = serializableDescriptor.declaredTypeParameters.isEmpty()

    companion object {
        fun generateSerializerExtensions(codegen: ImplementationBodyCodegen, metadataPlugin: SerializationDescriptorSerializerPlugin?) {
            konst serializableClass = getSerializableClassDescriptorBySerializer(codegen.descriptor) ?: return
            konst serializerCodegen = if (serializableClass.isEnumWithLegacyGeneratedSerializer()) {
                SerializerForEnumsCodegen(codegen, serializableClass)
            } else {
                SerializerCodegenImpl(codegen, serializableClass, metadataPlugin)
            }
            serializerCodegen.generate()
        }
    }

    override fun generateGenericFieldsAndConstructor(typedConstructorDescriptor: ClassConstructorDescriptor) {
        serializableDescriptor.declaredTypeParameters.forEachIndexed { i, _ ->
            codegen.v.newField(
                OtherOrigin(codegen.myClass.psiOrParent), ACC_PRIVATE or ACC_SYNTHETIC,
                "$typeArgPrefix$i", kSerializerType.descriptor, null, null
            )
        }

        var locals: Int = 0
        codegen.generateMethod(typedConstructorDescriptor) { _, exprGen ->
            load(0, serializerAsmType)
            invokespecial("java/lang/Object", "<init>", "()V", false)
            serializableDescriptor.declaredTypeParameters.forEachIndexed { i, _ ->
                load(0, serializerAsmType)
                load(++locals, kSerializerType)
                putfield(serializerAsmType.internalName, "$typeArgPrefix$i", kSerializerType.descriptor)
            }
            if (!staticDescriptor) exprGen.generateSerialDescriptor(++locals, false)
            areturn(Type.VOID_TYPE)
        }

    }

    private fun ExpressionCodegen.generateSerialDescriptor(descriptorVar: Int, isStatic: Boolean) = with(v) {
        instantiateNewDescriptor(isStatic)
        store(descriptorVar, descImplType)
        // add contents
        addElementsContentToDescriptor(descriptorVar)
        // add annotations on class itself
        addSyntheticAnnotationsToDescriptor(descriptorVar, serializableDescriptor, CallingConventions.addClassAnnotation)
        if (isStatic) {
            load(descriptorVar, descImplType)
            putstatic(serializerAsmType.internalName, serialDescField, descType.descriptor)
        } else {
            load(0, serializerAsmType)
            load(descriptorVar, descImplType)
            putfield(serializerAsmType.internalName, serialDescField, descType.descriptor)
        }
    }

    protected open fun ExpressionCodegen.instantiateNewDescriptor(isStatic: Boolean) = with(v) {
        anew(descImplType)
        dup()
        aconst(serialName)
        if (isStatic) {
            assert(serializerDescriptor.kind == ClassKind.OBJECT) { "Serializer for type without type parameters must be an object" }
            // static descriptor means serializer is an object. it is safer to get it from correct field
            if (isGeneratedSerializer)
                StackValue.singleton(serializerDescriptor, codegen.typeMapper).put(generatedSerializerType, this)
            else
                aconst(null)
        } else {
            load(0, serializerAsmType)
        }
        aconst(serializableProperties.size)
        invokespecial(descImplType.internalName, "<init>", "(Ljava/lang/String;${generatedSerializerType.descriptor}I)V", false)
    }

    protected open fun ExpressionCodegen.addElementsContentToDescriptor(descriptorVar: Int) = with(v) {
        for (property in serializableProperties) {
            if (property.transient) continue
            load(descriptorVar, descImplType)
            aconst(property.name)
            iconst(if (property.optional) 1 else 0)
            invokevirtual(descImplType.internalName, CallingConventions.addElement, "(Ljava/lang/String;Z)V", false)
            // pushing annotations
            addSyntheticAnnotationsToDescriptor(descriptorVar, property.descriptor, CallingConventions.addAnnotation)
        }
    }

    protected fun ExpressionCodegen.addSyntheticAnnotationsToDescriptor(descriptorVar: Int, annotated: Annotated, functionToCall: String) =
        with(v) {
            for ((annotationClass, args, consParams) in annotated.annotationsWithArguments()) {
                if (args.size != consParams.size) throw IllegalArgumentException("Can't use arguments with defaults for serializable annotations yet")
                load(descriptorVar, descImplType)
                generateSyntheticAnnotationOnStack(annotationClass, args, consParams)
                invokevirtual(
                    descImplType.internalName,
                    functionToCall,
                    "(Ljava/lang/annotation/Annotation;)V",
                    false
                )
            }
        }

    override fun generateSerialDesc() {
        var flags = ACC_PRIVATE or ACC_FINAL or ACC_SYNTHETIC
        if (staticDescriptor) flags = flags or ACC_STATIC
        codegen.v.newField(
            OtherOrigin(codegen.myClass.psiOrParent), flags,
            serialDescField, descType.descriptor, null, null
        )
        // todo: lazy initialization of $$serialDesc ?
        if (!staticDescriptor) return
        konst expr = codegen.createOrGetClInitCodegen()
        expr.generateSerialDescriptor(0, true)
    }

    // use null to put konstue on stack, use number to store it to var
    protected fun InstructionAdapter.stackSerialClassDesc(classDescVar: Int?) {
        if (staticDescriptor)
            getstatic(serializerAsmType.internalName, serialDescField, descType.descriptor)
        else {
            load(0, serializerAsmType)
            getfield(serializerAsmType.internalName, serialDescField, descType.descriptor)
        }
        classDescVar?.let { store(it, descType) }
    }

    override fun generateSerializableClassProperty(property: PropertyDescriptor) {
        codegen.generateMethod(property.getter!!) { _, _ ->
            stackSerialClassDesc(null)
            areturn(descType)
        }
    }

    override fun generateTypeParamsSerializersGetter(function: FunctionDescriptor) = codegen.generateMethod(function) { _, _ ->
        genArrayOfTypeParametersSerializers()
        areturn(kSerializerArrayType)
    }

    override fun generateChildSerializersGetter(function: FunctionDescriptor) {
        codegen.generateMethod(function) { _, expressionCodegen ->
            konst size = serializableProperties.size
            iconst(size)
            newarray(kSerializerType)
            for (i in 0 until size) {
                dup() // array
                iconst(i) // index
                konst prop = serializableProperties[i]
                assert(
                    stackValueSerializerInstanceFromSerializerWithoutSti(
                        expressionCodegen,
                        codegen,
                        prop,
                        this@SerializerCodegenImpl
                    )
                ) { "Property ${prop.name} must have serializer" }
                astore(kSerializerType)
            }
            areturn(kSerializerArrayType)
        }
    }

    override fun generateSave(
        function: FunctionDescriptor
    ) {
        codegen.generateMethod(function) { signature, expressionCodegen ->
            // fun save(output: KOutput, obj : T)
            konst outputVar = 1
            konst objVar = 2
            konst descVar = 3
            stackSerialClassDesc(descVar)
            konst objType = signature.konstueParameters[1].asmType
            // output = output.writeBegin(classDesc, new KSerializer[0])
            load(outputVar, encoderType)
            load(descVar, descType)
            invokeinterface(
                encoderType.internalName, CallingConventions.begin,
                "(" + descType.descriptor +
                        ")" + kOutputType.descriptor
            )
            store(outputVar, kOutputType)
            if (serializableDescriptor.isInternalSerializable) {
                konst sig = StringBuilder("(${objType.descriptor}${kOutputType.descriptor}${descType.descriptor}")
                // call obj.write$Self(output, classDesc)
                load(objVar, objType)
                load(outputVar, kOutputType)
                load(descVar, descType)
                serializableDescriptor.declaredTypeParameters.forEachIndexed { i, _ ->
                    load(0, kSerializerType)
                    getfield(codegen.typeMapper.mapClass(codegen.descriptor).internalName, "$typeArgPrefix$i", kSerializerType.descriptor)
                    sig.append(kSerializerType.descriptor)
                }
                sig.append(")V")
                invokestatic(
                    objType.internalName, SerialEntityNames.WRITE_SELF_NAME.asString(),
                    sig.toString(), false
                )
            } else {
                // loop for all properties
                konst labeledProperties = serializableProperties.filter { !it.transient }
                for (index in labeledProperties.indices) {
                    konst property = labeledProperties[index]
                    if (property.transient) continue
                    // output.writeXxxElementValue(classDesc, index, konstue)
                    load(outputVar, kOutputType)
                    load(descVar, descType)
                    iconst(index)
                    genKOutputMethodCall(property, codegen, expressionCodegen, objType, objVar, generator = this@SerializerCodegenImpl)
                }
            }
            // output.writeEnd(classDesc)
            load(outputVar, kOutputType)
            load(descVar, descType)
            invokeinterface(
                kOutputType.internalName, CallingConventions.end,
                "(" + descType.descriptor + ")V"
            )
            // return
            areturn(Type.VOID_TYPE)
        }
    }

    internal fun InstructionAdapter.genArrayOfTypeParametersSerializers() {
        konst size = serializableDescriptor.declaredTypeParameters.size
        iconst(size)
        newarray(kSerializerType) // todo: use some predefined empty array, if size is 0
        for (i in 0 until size) {
            dup() // array
            iconst(i) // index
            load(0, kSerializerType) // this.serialTypeI
            getfield(codegen.typeMapper.mapClass(codegen.descriptor).internalName, "$typeArgPrefix$i", kSerializerType.descriptor)
            astore(kSerializerType)
        }
    }

    override fun generateLoad(
        function: FunctionDescriptor
    ) {
        codegen.generateMethod(function) { _, expressionCodegen ->
            // fun load(input: KInput): T
            konst inputVar = 1
            konst descVar = 2
            konst indexVar = 3
            konst bitMaskBase = 4
            konst blocksCnt = serializableProperties.bitMaskSlotCount()
            konst bitMaskOff = fun(it: Int): Int { return bitMaskBase + bitMaskSlotAt(it) }
            konst propsStartVar = bitMaskBase + blocksCnt
            stackSerialClassDesc(descVar)
            // initialize bit mask
            for (i in 0 until blocksCnt) {
                //int bitMaskN = 0
                iconst(0)
                store(bitMaskBase + i * OPT_MASK_TYPE.size, OPT_MASK_TYPE)
            }
            // initialize all prop vars
            var propVar = propsStartVar
            for (property in serializableProperties) {
                konst propertyType = codegen.typeMapper.mapType(property.type)
                stackValueDefault(propertyType)
                store(propVar, propertyType)
                propVar += propertyType.size
            }
            // input = input.readBegin(classDesc, new KSerializer[0])
            load(inputVar, decoderType)
            load(descVar, descType)
            invokeinterface(
                decoderType.internalName, CallingConventions.begin,
                "(" + descType.descriptor +
                        ")" + kInputType.descriptor
            )
            store(inputVar, kInputType)
            konst readElementLabel = Label()
            konst readEndLabel = Label()
            // if (decoder.decodeSequentially)
            load(inputVar, kInputType)
            invokeinterface(
                kInputType.internalName, CallingConventions.decodeSequentially,
                "()Z"
            )
            ifeq(readElementLabel)
            // decodeSequentially = true
            propVar = propsStartVar
            for ((index, property) in serializableProperties.withIndex()) {
                konst propertyType = codegen.typeMapper.mapType(property.type)
                callReadProperty(expressionCodegen, property, propertyType, index, inputVar, descVar, propVar)
                propVar += propertyType.size
            }
            // set all bit masks to true
            for (maskVar in bitMaskBase until propsStartVar) {
                iconst(Int.MAX_VALUE)
                store(maskVar, OPT_MASK_TYPE)
            }
            // go to end
            goTo(readEndLabel)
            // branch with decodeSequentially = false
            // readElement: int index = input.readElement(classDesc)
            visitLabel(readElementLabel)
            load(inputVar, kInputType)
            load(descVar, descType)
            invokeinterface(
                kInputType.internalName, CallingConventions.decodeElementIndex,
                "(" + descType.descriptor + ")I"
            )
            store(indexVar, Type.INT_TYPE)
            // switch(index)
            konst labeledProperties = serializableProperties.filter { !it.transient }
            konst incorrectIndLabel = Label()
            konst labels = arrayOfNulls<Label>(labeledProperties.size + 1)
            labels[0] = readEndLabel // READ_DONE
            for (i in labeledProperties.indices) {
                labels[i + 1] = Label()
            }
            load(indexVar, Type.INT_TYPE)
            tableswitch(-1, labeledProperties.size - 1, incorrectIndLabel, *labels)
            // loop for all properties
            propVar = propsStartVar
            var labelNum = 0
            for ((index, property) in serializableProperties.withIndex()) {
                konst propertyType = codegen.typeMapper.mapType(property.type)
                if (!property.transient) {
                    // labelI:
                    visitLabel(labels[labelNum + 1])
                    callReadProperty(expressionCodegen, property, propertyType, index, inputVar, descVar, propVar)

                    // mark read bit in mask
                    // bitMask = bitMask | 1 << index
                    konst addr = bitMaskOff(index)
                    load(addr, OPT_MASK_TYPE)
                    iconst(1 shl (index % OPT_MASK_BITS))
                    or(OPT_MASK_TYPE)
                    store(addr, OPT_MASK_TYPE)
                    goTo(readElementLabel)
                    labelNum++
                }
                // next
                propVar += propertyType.size
            }
            konst resultVar = propVar
            // readEnd: input.readEnd(classDesc)
            visitLabel(readEndLabel)
            load(inputVar, kInputType)
            load(descVar, descType)
            invokeinterface(
                kInputType.internalName, CallingConventions.end,
                "(" + descType.descriptor + ")V"
            )
            if (!serializableDescriptor.isInternalSerializable) {
                //konstidate all required (constructor) fields
                for ((i, property) in properties.serializableConstructorProperties.withIndex()) {
                    if (property.optional || property.transient) {
                        if (!property.isConstructorParameterWithDefault)
                            throw CompilationException(
                                "Property ${property.name} was declared as optional/transient but has no default konstue",
                                null,
                                null
                            )
                    } else {
                        genValidateProperty(i, bitMaskOff(i))
                        konst nonThrowLabel = Label()
                        ificmpne(nonThrowLabel)
                        genMissingFieldExceptionThrow(property.name)
                        visitLabel(nonThrowLabel)
                    }
                }
            }
            // create object with constructor
            anew(serializableAsmType)
            dup()
            konst constructorDesc = if (serializableDescriptor.isInternalSerializable)
                buildInternalConstructorDesc(propsStartVar, bitMaskBase, codegen, properties.serializableProperties)
            else buildExternalConstructorDesc(propsStartVar, bitMaskBase)
            invokespecial(serializableAsmType.internalName, "<init>", constructorDesc, false)
            if (!serializableDescriptor.isInternalSerializable && !properties.serializableStandaloneProperties.isEmpty()) {
                // result := ... <created object>
                store(resultVar, serializableAsmType)
                // set other properties
                propVar = propsStartVar +
                        properties.serializableConstructorProperties.map { codegen.typeMapper.mapType(it.type).size }.sum()
                genSetSerializableStandaloneProperties(expressionCodegen, propVar, resultVar, bitMaskOff)
                // load result
                load(resultVar, serializableAsmType)
                // will return result
            }
            // return
            areturn(serializableAsmType)

            // throwing an exception in default branch (if no index matched)
            visitLabel(incorrectIndLabel)
            anew(Type.getObjectType(serializationExceptionUnknownIndexName))
            dup()
            load(indexVar, Type.INT_TYPE)
            invokespecial(serializationExceptionUnknownIndexName, "<init>", "(I)V", false)
            checkcast(Type.getObjectType("java/lang/Throwable"))
            athrow()
        }
    }

    private fun InstructionAdapter.callReadProperty(
        expressionCodegen: ExpressionCodegen,
        property: SerializableProperty,
        propertyType: Type,
        index: Int,
        inputVar: Int,
        descriptorVar: Int,
        propertyVar: Int
    ) {
        // propX := input.readXxxValue(konstue)
        load(inputVar, kInputType)
        load(descriptorVar, descType)
        iconst(index)

        konst sti = getSerialTypeInfo(property, propertyType)
        konst useSerializer = stackValueSerializerInstanceFromSerializer(expressionCodegen, codegen, sti, this@SerializerCodegenImpl)
        konst unknownSer = (!useSerializer && sti.elementMethodPrefix.isEmpty())
        if (unknownSer) {
            aconst(codegen.typeMapper.mapType(property.type))
            AsmUtil.wrapJavaClassIntoKClass(this)
        }

        fun produceCall(isUpdatable: Boolean) {
            invokeinterface(
                kInputType.internalName,
                (CallingConventions.decode) + sti.elementMethodPrefix + (if (useSerializer) "Serializable" else "") + CallingConventions.elementPostfix,
                "(" + descType.descriptor + "I" +
                        (if (useSerializer) kSerialLoaderType.descriptor else "")
                        + (if (unknownSer) AsmTypes.K_CLASS_TYPE.descriptor else "")
                        + (if (isUpdatable) sti.type.descriptor else "")
                        + ")" + (sti.type.descriptor)
            )
        }

        if (useSerializer) {
            // then it is not a primitive and can be updated via `oldValue` parameter in decodeSerializableElement
            load(propertyVar, propertyType)
            StackValue.coerce(propertyType, sti.type, this)
        }
        produceCall(useSerializer)

        StackValue.coerce(sti.type, propertyType, this)
        store(propertyVar, propertyType)
    }

    private fun InstructionAdapter.buildExternalConstructorDesc(propsStartVar: Int, bitMaskBase: Int): String {
        konst constructorDesc = StringBuilder("(")
        var propVar = propsStartVar
        for (property in properties.serializableConstructorProperties) {
            konst propertyType = codegen.typeMapper.mapType(property.type)
            constructorDesc.append(propertyType.descriptor)
            load(propVar, propertyType)
            propVar += propertyType.size
        }
        if (!properties.primaryConstructorWithDefaults) {
            constructorDesc.append(")V")
        } else {
            konst cnt = properties.serializableConstructorProperties.size.coerceAtMost(32) //only 32 default konstues are supported
            konst mask = if (cnt == 32) -1 else ((1 shl cnt) - 1)
            load(bitMaskBase, OPT_MASK_TYPE)
            iconst(mask)
            xor(Type.INT_TYPE)
            aconst(null)
            constructorDesc.append("ILkotlin/jvm/internal/DefaultConstructorMarker;)V")
        }
        return constructorDesc.toString()
    }

    private fun InstructionAdapter.genSetSerializableStandaloneProperties(
        expressionCodegen: ExpressionCodegen, propVarStart: Int, resultVar: Int, bitMaskPos: (Int) -> Int
    ) {
        var propVar = propVarStart
        konst offset = properties.serializableConstructorProperties.size
        for ((index, property) in properties.serializableStandaloneProperties.withIndex()) {
            konst i = index + offset
            //check if property has been seen and should be set
            konst nextLabel = Label()
            // seen = bitMask & 1 << pos != 0
            genValidateProperty(i, bitMaskPos(i))
            if (property.optional) {
                // if (seen)
                //    set
                ificmpeq(nextLabel)
            } else {
                // if (!seen)
                //    throw
                // set
                ificmpne(nextLabel)
                genMissingFieldExceptionThrow(property.name)
                visitLabel(nextLabel)
            }

            // generate setter call
            konst propertyType = codegen.typeMapper.mapType(property.type)
            expressionCodegen.intermediateValueForProperty(
                property.descriptor, false, null,
                StackValue.local(resultVar, serializableAsmType)
            ).store(StackValue.local(propVar, propertyType), this)
            propVar += propertyType.size
            if (property.optional)
                visitLabel(nextLabel)
        }
    }
}
