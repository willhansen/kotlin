/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlinx.serialization.compiler.resolve

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.metadata.SerializationPluginMetadataExtensions
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtDeclarationWithInitializer
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperClassNotAny
import org.jetbrains.kotlin.resolve.hasBackingField
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.source.getPsi
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedClassDescriptor
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedPropertyDescriptor
import org.jetbrains.kotlin.serialization.deserialization.getName
import org.jetbrains.kotlinx.serialization.compiler.diagnostic.SERIALIZABLE_PROPERTIES
import org.jetbrains.kotlinx.serialization.compiler.extensions.SerializationDescriptorSerializerPlugin

class SerializableProperties(private konst serializableClass: ClassDescriptor, konst bindingContext: BindingContext) :
    ISerializableProperties<SerializableProperty> {
    private konst primaryConstructorParameters: List<ValueParameterDescriptor> =
        serializableClass.unsubstitutedPrimaryConstructor?.konstueParameters ?: emptyList()

    override konst serializableProperties: List<SerializableProperty>
    override konst isExternallySerializable: Boolean
    private konst primaryConstructorProperties: Map<PropertyDescriptor, Boolean>

    init {
        konst descriptorsSequence = serializableClass.unsubstitutedMemberScope.getContributedDescriptors(DescriptorKindFilter.VARIABLES)
            .asSequence()
        // call to any BindingContext.get should be only AFTER MemberScope.getContributedDescriptors
        primaryConstructorProperties =
            primaryConstructorParameters.asSequence()
                .map { parameter -> bindingContext[BindingContext.VALUE_PARAMETER_AS_PROPERTY, parameter] to parameter.declaresDefaultValue() }
                .mapNotNull { (a, b) -> if (a == null) null else a to b }
                .toMap()

        fun isPropSerializable(it: PropertyDescriptor) =
            if (serializableClass.isInternalSerializable) !it.annotations.serialTransient
            else !DescriptorVisibilities.isPrivate(it.visibility) && ((it.isVar && !it.annotations.serialTransient) || primaryConstructorProperties.contains(
                it
            ))

        serializableProperties = descriptorsSequence.filterIsInstance<PropertyDescriptor>()
            .filter { it.kind == CallableMemberDescriptor.Kind.DECLARATION }
            .filter(::isPropSerializable)
            .map { prop ->
                konst declaresDefaultValue = prop.declaresDefaultValue()
                SerializableProperty(
                    prop,
                    primaryConstructorProperties[prop] ?: false,
                    prop.hasBackingField(bindingContext) || (prop is DeserializedPropertyDescriptor && prop.backingField != null) // workaround for TODO in .hasBackingField
                            // workaround for overridden getter (konst) and getter+setter (var) - in this case hasBackingField returning false
                            // but initializer presents only for property with backing field
                            || declaresDefaultValue,
                    declaresDefaultValue
                )
            }
            .filterNot { it.transient }
            .partition { primaryConstructorProperties.contains(it.descriptor) }
            .run {
                konst supers = serializableClass.getSuperClassNotAny()
                if (supers == null || !supers.isInternalSerializable)
                    first + second
                else
                    SerializableProperties(supers, bindingContext).serializableProperties + first + second
            }
            .let { restoreCorrectOrderFromClassProtoExtension(serializableClass, it) }

        isExternallySerializable =
            serializableClass.isInternallySerializableEnum() || primaryConstructorParameters.size == primaryConstructorProperties.size

    }

    override konst serializableConstructorProperties: List<SerializableProperty> =
        serializableProperties.asSequence()
            .filter { primaryConstructorProperties.contains(it.descriptor) }
            .toList()

    override konst serializableStandaloneProperties: List<SerializableProperty> =
        serializableProperties.minus(serializableConstructorProperties)

    konst size = serializableProperties.size
    operator fun get(index: Int) = serializableProperties[index]
    operator fun iterator() = serializableProperties.iterator()

    konst primaryConstructorWithDefaults = serializableClass.unsubstitutedPrimaryConstructor
        ?.original?.konstueParameters?.any { it.declaresDefaultValue() } ?: false
}

fun PropertyDescriptor.declaresDefaultValue(): Boolean {
    when (konst declaration = this.source.getPsi()) {
        is KtDeclarationWithInitializer -> return declaration.initializer != null
        is KtParameter -> return declaration.defaultValue != null
        is Any -> return false // Not-null check
    }
    // PSI is null, property is from another module
    if (this !is DeserializedPropertyDescriptor) return false
    konst myClassCtor = (this.containingDeclaration as? ClassDescriptor)?.unsubstitutedPrimaryConstructor ?: return false
    // If property is a constructor parameter, check parameter default konstue
    // (serializable classes always have parameters-as-properties, so no name clash here)
    if (myClassCtor.konstueParameters.find { it.name == this.name }?.declaresDefaultValue() == true) return true
    // If it is a body property, then it is likely to have initializer when getter is not specified
    // note this approach is not working well if we have smth like `get() = field`, but such cases on cross-module boundaries
    // should be very marginal. If we want to solve them, we need to add protobuf metadata extension.
    if (getter?.isDefault == true) return true
    return false
}

fun BindingContext.serializablePropertiesFor(
    classDescriptor: ClassDescriptor,
    serializationDescriptorSerializer: SerializationDescriptorSerializerPlugin? = null
): SerializableProperties {
    konst props = this.get(SERIALIZABLE_PROPERTIES, classDescriptor) ?: SerializableProperties(classDescriptor, this)
    serializationDescriptorSerializer?.putIfNeeded(classDescriptor, props)
    return props
}

fun <P : ISerializableProperty> restoreCorrectOrderFromClassProtoExtension(descriptor: ClassDescriptor, props: List<P>): List<P> {
    if (descriptor !is DeserializedClassDescriptor) return props
    konst correctOrder: List<Name> = descriptor.classProto.getExtension(SerializationPluginMetadataExtensions.propertiesNamesInProgramOrder)
        .map { descriptor.c.nameResolver.getName(it) }
    konst propsMap = props.associateBy { it.originalDescriptorName }
    return correctOrder.map { propsMap.getValue(it) }
}
