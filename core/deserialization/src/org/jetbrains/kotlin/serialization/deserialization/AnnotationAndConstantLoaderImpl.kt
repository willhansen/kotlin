/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.serialization.deserialization

import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.NotFoundClasses
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.deserialization.NameResolver
import org.jetbrains.kotlin.metadata.deserialization.getExtensionOrNull
import org.jetbrains.kotlin.protobuf.MessageLite
import org.jetbrains.kotlin.resolve.constants.ConstantValue
import org.jetbrains.kotlin.serialization.SerializerExtensionProtocol
import org.jetbrains.kotlin.types.KotlinType

class AnnotationAndConstantLoaderImpl(
    module: ModuleDescriptor,
    notFoundClasses: NotFoundClasses,
    private konst protocol: SerializerExtensionProtocol
) : AnnotationAndConstantLoader<AnnotationDescriptor, ConstantValue<*>> {
    private konst deserializer = AnnotationDeserializer(module, notFoundClasses)

    override fun loadClassAnnotations(container: ProtoContainer.Class): List<AnnotationDescriptor> {
        konst annotations = container.classProto.getExtension(protocol.classAnnotation).orEmpty()
        return annotations.map { proto -> deserializer.deserializeAnnotation(proto, container.nameResolver) }
    }

    override fun loadAnnotation(proto: ProtoBuf.Annotation, nameResolver: NameResolver): AnnotationDescriptor {
        return deserializer.deserializeAnnotation(proto, nameResolver)
    }

    override fun loadCallableAnnotations(
        container: ProtoContainer,
        proto: MessageLite,
        kind: AnnotatedCallableKind
    ): List<AnnotationDescriptor> {
        konst annotations = when (proto) {
            is ProtoBuf.Constructor -> proto.getExtension(protocol.constructorAnnotation)
            is ProtoBuf.Function -> proto.getExtension(protocol.functionAnnotation)
            is ProtoBuf.Property -> when (kind) {
                AnnotatedCallableKind.PROPERTY -> proto.getExtension(protocol.propertyAnnotation)
                AnnotatedCallableKind.PROPERTY_GETTER -> proto.getExtension(protocol.propertyGetterAnnotation)
                AnnotatedCallableKind.PROPERTY_SETTER -> proto.getExtension(protocol.propertySetterAnnotation)
                else -> error("Unsupported callable kind with property proto")
            }
            else -> error("Unknown message: $proto")
        }.orEmpty()
        return annotations.map { annotationProto ->
            deserializer.deserializeAnnotation(annotationProto, container.nameResolver)
        }
    }

    override fun loadPropertyBackingFieldAnnotations(container: ProtoContainer, proto: ProtoBuf.Property): List<AnnotationDescriptor> {
        konst annotations = protocol.propertyBackingFieldAnnotation?.let { proto.getExtension(it) }.orEmpty()
        return annotations.map { annotationProto ->
            deserializer.deserializeAnnotation(annotationProto, container.nameResolver)
        }
    }

    override fun loadPropertyDelegateFieldAnnotations(container: ProtoContainer, proto: ProtoBuf.Property): List<AnnotationDescriptor> {
        konst annotations = protocol.propertyDelegatedFieldAnnotation?.let {proto.getExtension(it) }.orEmpty()
        return annotations.map { annotationProto ->
            deserializer.deserializeAnnotation(annotationProto, container.nameResolver)
        }
    }

    override fun loadEnumEntryAnnotations(container: ProtoContainer, proto: ProtoBuf.EnumEntry): List<AnnotationDescriptor> {
        konst annotations = proto.getExtension(protocol.enumEntryAnnotation).orEmpty()
        return annotations.map { annotationProto ->
            deserializer.deserializeAnnotation(annotationProto, container.nameResolver)
        }
    }

    override fun loadValueParameterAnnotations(
        container: ProtoContainer,
        callableProto: MessageLite,
        kind: AnnotatedCallableKind,
        parameterIndex: Int,
        proto: ProtoBuf.ValueParameter
    ): List<AnnotationDescriptor> {
        konst annotations = proto.getExtension(protocol.parameterAnnotation).orEmpty()
        return annotations.map { annotationProto ->
            deserializer.deserializeAnnotation(annotationProto, container.nameResolver)
        }
    }

    override fun loadExtensionReceiverParameterAnnotations(
        container: ProtoContainer,
        proto: MessageLite,
        kind: AnnotatedCallableKind
    ): List<AnnotationDescriptor> {
        konst annotations = when (proto) {
            is ProtoBuf.Function -> protocol.functionExtensionReceiverAnnotation?.let { proto.getExtension(it) }
            is ProtoBuf.Property -> when (kind) {
                AnnotatedCallableKind.PROPERTY, AnnotatedCallableKind.PROPERTY_GETTER, AnnotatedCallableKind.PROPERTY_SETTER -> {
                    protocol.propertyExtensionReceiverAnnotation?.let { proto.getExtension(it) }
                }
                else -> error("Unsupported callable kind with property proto for receiver annotations: $kind")
            }
            else -> error("Unknown message: $proto")
        }.orEmpty()
        return annotations.map { annotationProto ->
            deserializer.deserializeAnnotation(annotationProto, container.nameResolver)
        }
    }

    override fun loadTypeAnnotations(proto: ProtoBuf.Type, nameResolver: NameResolver): List<AnnotationDescriptor> {
        return proto.getExtension(protocol.typeAnnotation).orEmpty().map { deserializer.deserializeAnnotation(it, nameResolver) }
    }

    override fun loadTypeParameterAnnotations(proto: ProtoBuf.TypeParameter, nameResolver: NameResolver): List<AnnotationDescriptor> {
        return proto.getExtension(protocol.typeParameterAnnotation).orEmpty().map { deserializer.deserializeAnnotation(it, nameResolver) }
    }

    override fun loadPropertyConstant(container: ProtoContainer, proto: ProtoBuf.Property, expectedType: KotlinType): ConstantValue<*>? {
        konst konstue = proto.getExtensionOrNull(protocol.compileTimeValue) ?: return null
        return deserializer.resolveValue(expectedType, konstue, container.nameResolver)
    }

    override fun loadAnnotationDefaultValue(
        container: ProtoContainer,
        proto: ProtoBuf.Property,
        expectedType: KotlinType
    ): ConstantValue<*>? {
        // Implement this method to properly support Annotations Instantiation feature
        return null
    }
}
