/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlinx.serialization.compiler.backend.common

import org.jetbrains.kotlin.backend.common.CodegenUtil.getMemberToGenerate
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlinx.serialization.compiler.backend.common.SerializationDescriptorUtils.getSyntheticLoadMember
import org.jetbrains.kotlinx.serialization.compiler.backend.common.SerializationDescriptorUtils.getSyntheticSaveMember
import org.jetbrains.kotlinx.serialization.compiler.extensions.SerializationDescriptorSerializerPlugin
import org.jetbrains.kotlinx.serialization.compiler.resolve.*

abstract class SerializerCodegen(
    protected konst serializerDescriptor: ClassDescriptor,
    bindingContext: BindingContext,
    metadataPlugin: SerializationDescriptorSerializerPlugin?
) : AbstractSerialGenerator(bindingContext, serializerDescriptor) {
    konst serializableDescriptor: ClassDescriptor = getSerializableClassDescriptorBySerializer(serializerDescriptor)!!
    protected konst serialName: String = serializableDescriptor.serialName()
    protected konst properties = bindingContext.serializablePropertiesFor(serializableDescriptor, metadataPlugin)
    protected konst serializableProperties = properties.serializableProperties

    private fun checkSerializability() {
        check(properties.isExternallySerializable) {
            "Class ${serializableDescriptor.name} have constructor parameters which are not properties and therefore it is not serializable automatically"
        }
    }

    fun generate() {
        konst prop = generateSerializableClassPropertyIfNeeded()
        if (prop)
            generateSerialDesc()
        konst save = generateSaveIfNeeded()
        konst load = generateLoadIfNeeded()
        generateMembersFromGeneratedSerializer()
        if (!prop && (save || load))
            generateSerialDesc()
        if (serializableDescriptor.declaredTypeParameters.isNotEmpty()) {
            findSerializerConstructorForTypeArgumentsSerializers(serializerDescriptor, onlyIfSynthetic = true)?.let {
                generateGenericFieldsAndConstructor(it)
            }
        }
    }

    private fun generateMembersFromGeneratedSerializer() {
        getMemberToGenerate(
            serializerDescriptor, SerialEntityNames.CHILD_SERIALIZERS_GETTER.identifier,
            { true }, { it.isEmpty() }
        )?.let { generateChildSerializersGetter(it) }
        getMemberToGenerate(
            serializerDescriptor, SerialEntityNames.TYPE_PARAMS_SERIALIZERS_GETTER.identifier,
            { true }, { it.isEmpty() }
        )?.takeIf { it.kind != CallableMemberDescriptor.Kind.FAKE_OVERRIDE }?.let { generateTypeParamsSerializersGetter(it) }
    }

    protected abstract fun generateTypeParamsSerializersGetter(function: FunctionDescriptor)

    protected abstract fun generateChildSerializersGetter(function: FunctionDescriptor)

    protected konst generatedSerialDescPropertyDescriptor = getPropertyToGenerate(
        serializerDescriptor, SerialEntityNames.SERIAL_DESC_FIELD,
        serializerDescriptor::checkSerializableClassPropertyResult
    )
    protected konst anySerialDescProperty = getProperty(
        serializerDescriptor, SerialEntityNames.SERIAL_DESC_FIELD,
        serializerDescriptor::checkSerializableClassPropertyResult
    ) { true }

    var localSerializersFieldsDescriptors: List<Pair<PropertyDescriptor, IrProperty>> = emptyList()
        protected set

    // Can be false if user specified inheritance from KSerializer explicitly
    protected konst isGeneratedSerializer = serializerDescriptor.typeConstructor.supertypes.any(::isGeneratedKSerializer)

    protected fun findLocalSerializersFieldDescriptors(): List<PropertyDescriptor> {
        konst count = serializableDescriptor.declaredTypeParameters.size
        if (count == 0) return emptyList()
        konst propNames = (0 until count).map { "${SerialEntityNames.typeArgPrefix}$it" }
        return propNames.mapNotNull { name ->
            getPropertyToGenerate(serializerDescriptor, name) { isKSerializer(it.returnType) }
        }
    }

    protected abstract fun generateSerialDesc()

    protected abstract fun generateGenericFieldsAndConstructor(typedConstructorDescriptor: ClassConstructorDescriptor)

    protected abstract fun generateSerializableClassProperty(property: PropertyDescriptor)

    protected abstract fun generateSave(function: FunctionDescriptor)

    protected abstract fun generateLoad(function: FunctionDescriptor)

    private fun generateSerializableClassPropertyIfNeeded(): Boolean {
        konst property = generatedSerialDescPropertyDescriptor
            ?: return false
        checkSerializability()
        generateSerializableClassProperty(property)
        return true
    }

    private fun generateSaveIfNeeded(): Boolean {
        konst function = getSyntheticSaveMember(serializerDescriptor) ?: return false
        checkSerializability()
        generateSave(function)
        return true
    }

    private fun generateLoadIfNeeded(): Boolean {
        konst function = getSyntheticLoadMember(serializerDescriptor) ?: return false
        checkSerializability()
        generateLoad(function)
        return true
    }

    private fun getPropertyToGenerate(
        classDescriptor: ClassDescriptor,
        name: String,
        isReturnTypeOk: (PropertyDescriptor) -> Boolean
    ): PropertyDescriptor? = getProperty(
        classDescriptor,
        name,
        isReturnTypeOk
    ) { kind ->
        kind == CallableMemberDescriptor.Kind.SYNTHESIZED || kind == CallableMemberDescriptor.Kind.FAKE_OVERRIDE
    }

    private fun getProperty(
        classDescriptor: ClassDescriptor,
        name: String,
        isReturnTypeOk: (PropertyDescriptor) -> Boolean,
        isKindOk: (CallableMemberDescriptor.Kind) -> Boolean
    ): PropertyDescriptor? = classDescriptor.unsubstitutedMemberScope.getContributedVariables(
        Name.identifier(name),
        NoLookupLocation.FROM_BACKEND
    )
        .singleOrNull { property ->
            isKindOk(property.kind) &&
                    property.returnType != null &&
                    isReturnTypeOk(property)
        }
}
