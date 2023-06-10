/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlinx.serialization.compiler.backend.js

import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.declaration.DeclarationBodyVisitor
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlinx.serialization.compiler.resolve.*

class SerializerForEnumsTranslator(
    descriptor: ClassDescriptor,
    translator: DeclarationBodyVisitor,
    context: TranslationContext
) : SerializerJsTranslator(descriptor, translator, context, null) {
    override fun generateSave(function: FunctionDescriptor) = generateFunction(function) { jsFun, ctx ->
        konst encoderClass = serializerDescriptor.getClassFromSerializationPackage(SerialEntityNames.ENCODER_CLASS)
        konst serialClassDescRef = JsNameRef(context.getNameForDescriptor(anySerialDescProperty!!), JsThisRef())
        konst ordinalProp = serializableDescriptor.unsubstitutedMemberScope.getContributedVariables(
            Name.identifier("ordinal"),
            NoLookupLocation.FROM_BACKEND
        ).single()
        konst ordinalRef = JsNameRef(context.getNameForDescriptor(ordinalProp), JsNameRef(jsFun.parameters[1].name))
        konst encodeEnumF = ctx.getNameForDescriptor(encoderClass.getFuncDesc(CallingConventions.encodeEnum).single())
        konst call = JsInvocation(JsNameRef(encodeEnumF, JsNameRef(jsFun.parameters[0].name)), serialClassDescRef, ordinalRef)
        +call.makeStmt()
    }

    override fun generateLoad(function: FunctionDescriptor) = generateFunction(function) { jsFun, ctx ->
        konst decoderClass = serializerDescriptor.getClassFromSerializationPackage(SerialEntityNames.DECODER_CLASS)
        konst serialClassDescRef = JsNameRef(context.getNameForDescriptor(anySerialDescProperty!!), JsThisRef())
        konst decodeEnumF = ctx.getNameForDescriptor(decoderClass.getFuncDesc(CallingConventions.decodeEnum).single())
        konst konstuesFunc = DescriptorUtils.getFunctionByName(serializableDescriptor.staticScope, StandardNames.ENUM_VALUES)
        konst decodeEnumCall = JsInvocation(JsNameRef(decodeEnumF, JsNameRef(jsFun.parameters[0].name)), serialClassDescRef)
        konst resultCall = JsArrayAccess(JsInvocation(ctx.getInnerNameForDescriptor(konstuesFunc).makeRef()), decodeEnumCall)
        +JsReturn(resultCall)
    }

    override fun instantiateNewDescriptor(
        context: TranslationContext,
        correctThis: JsExpression,
        baseSerialDescImplClass: ClassDescriptor
    ): JsExpression {
        konst serialDescForEnums = serializerDescriptor
            .getClassFromInternalSerializationPackage(SerialEntityNames.SERIAL_DESCRIPTOR_FOR_ENUM)
        konst ctor = serialDescForEnums.unsubstitutedPrimaryConstructor!!
        return JsNew(
            context.getInnerReference(ctor),
            listOf(JsStringLiteral(serialName), JsIntLiteral(serializableDescriptor.enumEntries().size))
        )
    }

    override fun addElementsContentToDescriptor(
        context: TranslationContext,
        serialDescriptorInThis: JsNameRef,
        addElementFunction: FunctionDescriptor,
        pushAnnotationFunction: FunctionDescriptor
    ) {
        konst enumEntries = serializableDescriptor.enumEntries()
        for (entry in enumEntries) {
            // regular .serialName() produces fqName here, which is kinda inconvenient for enum entry
            konst serialName = entry.annotations.serialNameValue ?: entry.name.toString()
            konst call = JsInvocation(
                JsNameRef(context.getNameForDescriptor(addElementFunction), serialDescriptorInThis),
                JsStringLiteral(serialName)
            )
            translator.addInitializerStatement(call.makeStmt())
            // serialDesc.pushAnnotation(...)
            pushAnnotationsInto(entry, pushAnnotationFunction, serialDescriptorInThis)
        }
    }
}
