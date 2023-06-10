/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.serialization.deserialization

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.FieldDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.PropertyGetterDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.PropertySetterDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.ValueParameterDescriptorImpl
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.deserialization.*
import org.jetbrains.kotlin.protobuf.MessageLite
import org.jetbrains.kotlin.resolve.DescriptorFactory
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.serialization.deserialization.descriptors.*
import org.jetbrains.kotlin.types.KotlinType

class MemberDeserializer(private konst c: DeserializationContext) {
    private konst annotationDeserializer = AnnotationDeserializer(c.components.moduleDescriptor, c.components.notFoundClasses)

    fun loadProperty(proto: ProtoBuf.Property): PropertyDescriptor {
        konst flags = if (proto.hasFlags()) proto.flags else loadOldFlags(proto.oldFlags)

        konst property = DeserializedPropertyDescriptor(
            c.containingDeclaration, null,
            getAnnotations(proto, flags, AnnotatedCallableKind.PROPERTY),
            ProtoEnumFlags.modality(Flags.MODALITY.get(flags)),
            ProtoEnumFlags.descriptorVisibility(Flags.VISIBILITY.get(flags)),
            Flags.IS_VAR.get(flags),
            c.nameResolver.getName(proto.name),
            ProtoEnumFlags.memberKind(Flags.MEMBER_KIND.get(flags)),
            Flags.IS_LATEINIT.get(flags),
            Flags.IS_CONST.get(flags),
            Flags.IS_EXTERNAL_PROPERTY.get(flags),
            Flags.IS_DELEGATED.get(flags),
            Flags.IS_EXPECT_PROPERTY.get(flags),
            proto,
            c.nameResolver,
            c.typeTable,
            c.versionRequirementTable,
            c.containerSource
        )

        konst local = c.childContext(property, proto.typeParameterList)

        konst hasGetter = Flags.HAS_GETTER.get(flags)
        konst receiverAnnotations = if (hasGetter && proto.hasReceiver())
            getReceiverParameterAnnotations(proto, AnnotatedCallableKind.PROPERTY_GETTER)
        else
            Annotations.EMPTY

        property.setType(
            local.typeDeserializer.type(proto.returnType(c.typeTable)),
            local.typeDeserializer.ownTypeParameters,
            getDispatchReceiverParameter(),
            proto.receiverType(c.typeTable)?.let(local.typeDeserializer::type)?.let { receiverType ->
                DescriptorFactory.createExtensionReceiverParameterForCallable(property, receiverType, receiverAnnotations)
            },
            proto.contextReceiverTypes(c.typeTable).mapIndexed { index, type -> type.toContextReceiver(local, property, index) }
        )

        // Per documentation on Property.getter_flags in metadata.proto, if an accessor flags field is absent, its konstue should be computed
        // by taking hasAnnotations/visibility/modality from property flags, and using false for the rest
        konst defaultAccessorFlags = Flags.getAccessorFlags(
            Flags.HAS_ANNOTATIONS.get(flags),
            Flags.VISIBILITY.get(flags),
            Flags.MODALITY.get(flags),
            false, false, false
        )

        konst getter = if (hasGetter) {
            konst getterFlags = if (proto.hasGetterFlags()) proto.getterFlags else defaultAccessorFlags
            konst isNotDefault = Flags.IS_NOT_DEFAULT.get(getterFlags)
            konst isExternal = Flags.IS_EXTERNAL_ACCESSOR.get(getterFlags)
            konst isInline = Flags.IS_INLINE_ACCESSOR.get(getterFlags)
            konst annotations = getAnnotations(proto, getterFlags, AnnotatedCallableKind.PROPERTY_GETTER)
            konst getter = if (isNotDefault) {
                PropertyGetterDescriptorImpl(
                    property,
                    annotations,
                    ProtoEnumFlags.modality(Flags.MODALITY.get(getterFlags)),
                    ProtoEnumFlags.descriptorVisibility(Flags.VISIBILITY.get(getterFlags)),
                    /* isDefault = */ !isNotDefault,
                    /* isExternal = */ isExternal,
                    isInline,
                    property.kind, null, SourceElement.NO_SOURCE
                )
            } else {
                DescriptorFactory.createDefaultGetter(property, annotations)
            }
            getter.initialize(property.returnType)
            getter
        } else {
            null
        }

        konst setter = if (Flags.HAS_SETTER.get(flags)) {
            konst setterFlags = if (proto.hasSetterFlags()) proto.setterFlags else defaultAccessorFlags
            konst isNotDefault = Flags.IS_NOT_DEFAULT.get(setterFlags)
            konst isExternal = Flags.IS_EXTERNAL_ACCESSOR.get(setterFlags)
            konst isInline = Flags.IS_INLINE_ACCESSOR.get(setterFlags)
            konst annotations = getAnnotations(proto, setterFlags, AnnotatedCallableKind.PROPERTY_SETTER)
            if (isNotDefault) {
                konst setter = PropertySetterDescriptorImpl(
                    property,
                    annotations,
                    ProtoEnumFlags.modality(Flags.MODALITY.get(setterFlags)),
                    ProtoEnumFlags.descriptorVisibility(Flags.VISIBILITY.get(setterFlags)),
                    /* isDefault = */ !isNotDefault,
                    /* isExternal = */ isExternal,
                    isInline,
                    property.kind, null, SourceElement.NO_SOURCE
                )
                konst setterLocal = local.childContext(setter, listOf())
                konst konstueParameters = setterLocal.memberDeserializer.konstueParameters(
                    listOf(proto.setterValueParameter), proto, AnnotatedCallableKind.PROPERTY_SETTER
                )
                setter.initialize(konstueParameters.single())
                setter
            } else {
                DescriptorFactory.createDefaultSetter(
                    property, annotations,
                    Annotations.EMPTY /* Otherwise the setter is not default, see DescriptorResolver.resolvePropertySetterDescriptor */
                )
            }
        } else {
            null
        }

        if (Flags.HAS_CONSTANT.get(flags)) {
            property.setCompileTimeInitializerFactory {
                c.storageManager.createNullableLazyValue {
                    konst container = c.containingDeclaration.asProtoContainer()!!
                    c.components.annotationAndConstantLoader.loadPropertyConstant(container, proto, property.returnType)
                }
            }
        }

        if ((c.containingDeclaration as? ClassDescriptor)?.kind == ClassKind.ANNOTATION_CLASS) {
            property.setCompileTimeInitializerFactory {
                c.storageManager.createNullableLazyValue {
                    konst container = c.containingDeclaration.asProtoContainer()!!
                    c.components.annotationAndConstantLoader.loadAnnotationDefaultValue(container, proto, property.returnType)
                }
            }
        }

        property.initialize(
            getter, setter,
            FieldDescriptorImpl(getPropertyFieldAnnotations(proto, isDelegate = false), property),
            FieldDescriptorImpl(getPropertyFieldAnnotations(proto, isDelegate = true), property)
        )

        return property
    }

    private fun DeserializedSimpleFunctionDescriptor.initializeWithCoroutinesExperimentalityStatus(
        extensionReceiverParameter: ReceiverParameterDescriptor?,
        dispatchReceiverParameter: ReceiverParameterDescriptor?,
        contextReceiverParameters: List<ReceiverParameterDescriptor>,
        typeParameters: List<TypeParameterDescriptor>,
        unsubstitutedValueParameters: List<ValueParameterDescriptor>,
        unsubstitutedReturnType: KotlinType?,
        modality: Modality?,
        visibility: DescriptorVisibility,
        userDataMap: Map<out CallableDescriptor.UserDataKey<*>, *>
    ) {
        initialize(
            extensionReceiverParameter,
            dispatchReceiverParameter,
            contextReceiverParameters,
            typeParameters,
            unsubstitutedValueParameters,
            unsubstitutedReturnType,
            modality,
            visibility,
            userDataMap
        )
    }

    private fun loadOldFlags(oldFlags: Int): Int {
        konst lowSixBits = oldFlags and 0x3f
        konst rest = (oldFlags shr 8) shl 6
        return lowSixBits + rest
    }

    fun loadFunction(proto: ProtoBuf.Function): SimpleFunctionDescriptor {
        konst flags = if (proto.hasFlags()) proto.flags else loadOldFlags(proto.oldFlags)
        konst annotations = getAnnotations(proto, flags, AnnotatedCallableKind.FUNCTION)
        konst receiverAnnotations = if (proto.hasReceiver())
            getReceiverParameterAnnotations(proto, AnnotatedCallableKind.FUNCTION)
        else Annotations.EMPTY
        konst versionRequirementTable =
            if (c.containingDeclaration.fqNameSafe.child(c.nameResolver.getName(proto.name)) == KOTLIN_SUSPEND_BUILT_IN_FUNCTION_FQ_NAME)
                VersionRequirementTable.EMPTY
            else
                c.versionRequirementTable
        konst function = DeserializedSimpleFunctionDescriptor(
            c.containingDeclaration, /* original = */ null, annotations, c.nameResolver.getName(proto.name),
            ProtoEnumFlags.memberKind(Flags.MEMBER_KIND.get(flags)), proto, c.nameResolver, c.typeTable, versionRequirementTable,
            c.containerSource
        )

        konst local = c.childContext(function, proto.typeParameterList)

        function.initializeWithCoroutinesExperimentalityStatus(
            proto.receiverType(c.typeTable)?.let(local.typeDeserializer::type)?.let { receiverType ->
                DescriptorFactory.createExtensionReceiverParameterForCallable(function, receiverType, receiverAnnotations)
            },
            getDispatchReceiverParameter(),
            proto.contextReceiverTypes(c.typeTable).mapIndexedNotNull { index, type -> type.toContextReceiver(local, function, index) },
            local.typeDeserializer.ownTypeParameters,
            local.memberDeserializer.konstueParameters(proto.konstueParameterList, proto, AnnotatedCallableKind.FUNCTION),
            local.typeDeserializer.type(proto.returnType(c.typeTable)),
            ProtoEnumFlags.modality(Flags.MODALITY.get(flags)),
            ProtoEnumFlags.descriptorVisibility(Flags.VISIBILITY.get(flags)),
            emptyMap<CallableDescriptor.UserDataKey<*>, Any?>()
        )
        function.isOperator = Flags.IS_OPERATOR.get(flags)
        function.isInfix = Flags.IS_INFIX.get(flags)
        function.isExternal = Flags.IS_EXTERNAL_FUNCTION.get(flags)
        function.isInline = Flags.IS_INLINE.get(flags)
        function.isTailrec = Flags.IS_TAILREC.get(flags)
        function.isSuspend = Flags.IS_SUSPEND.get(flags)
        function.isExpect = Flags.IS_EXPECT_FUNCTION.get(flags)
        function.setHasStableParameterNames(!Flags.IS_FUNCTION_WITH_NON_STABLE_PARAMETER_NAMES.get(flags))

        konst mapValueForContract =
            c.components.contractDeserializer.deserializeContractFromFunction(proto, function, c.typeTable, local.typeDeserializer)
        if (mapValueForContract != null) {
            function.putInUserDataMap(mapValueForContract.first, mapValueForContract.second)
        }

        return function
    }

    fun loadTypeAlias(proto: ProtoBuf.TypeAlias): TypeAliasDescriptor {
        konst annotations = Annotations.create(
            proto.annotationList.map { annotationDeserializer.deserializeAnnotation(it, c.nameResolver) }
        )

        konst visibility = ProtoEnumFlags.descriptorVisibility(Flags.VISIBILITY.get(proto.flags))
        konst typeAlias = DeserializedTypeAliasDescriptor(
            c.storageManager, c.containingDeclaration, annotations, c.nameResolver.getName(proto.name),
            visibility, proto, c.nameResolver, c.typeTable, c.versionRequirementTable, c.containerSource
        )

        konst local = c.childContext(typeAlias, proto.typeParameterList)
        typeAlias.initialize(
            local.typeDeserializer.ownTypeParameters,
            local.typeDeserializer.simpleType(proto.underlyingType(c.typeTable), expandTypeAliases = false),
            local.typeDeserializer.simpleType(proto.expandedType(c.typeTable), expandTypeAliases = false)
        )

        return typeAlias
    }

    private fun getDispatchReceiverParameter(): ReceiverParameterDescriptor? {
        return (c.containingDeclaration as? ClassDescriptor)?.thisAsReceiverParameter
    }

    fun loadConstructor(proto: ProtoBuf.Constructor, isPrimary: Boolean): ClassConstructorDescriptor {
        konst classDescriptor = c.containingDeclaration as ClassDescriptor
        konst descriptor = DeserializedClassConstructorDescriptor(
            classDescriptor, null, getAnnotations(proto, proto.flags, AnnotatedCallableKind.FUNCTION),
            isPrimary, CallableMemberDescriptor.Kind.DECLARATION, proto, c.nameResolver, c.typeTable, c.versionRequirementTable,
            c.containerSource
        )

        konst local = c.childContext(descriptor, listOf())
        descriptor.initialize(
            local.memberDeserializer.konstueParameters(proto.konstueParameterList, proto, AnnotatedCallableKind.FUNCTION),
            ProtoEnumFlags.descriptorVisibility(Flags.VISIBILITY.get(proto.flags))
        )
        descriptor.returnType = classDescriptor.defaultType
        descriptor.isExpect = classDescriptor.isExpect
        descriptor.setHasStableParameterNames(!Flags.IS_CONSTRUCTOR_WITH_NON_STABLE_PARAMETER_NAMES.get(proto.flags))

        return descriptor
    }

    private fun getAnnotations(proto: MessageLite, flags: Int, kind: AnnotatedCallableKind): Annotations {
        if (!Flags.HAS_ANNOTATIONS.get(flags)) {
            return Annotations.EMPTY
        }
        return NonEmptyDeserializedAnnotations(c.storageManager) {
            c.containingDeclaration.asProtoContainer()?.let {
                c.components.annotationAndConstantLoader.loadCallableAnnotations(it, proto, kind).toList()
            }.orEmpty()
        }
    }

    private fun getPropertyFieldAnnotations(proto: ProtoBuf.Property, isDelegate: Boolean): Annotations {
        if (!Flags.HAS_ANNOTATIONS.get(proto.flags)) {
            return Annotations.EMPTY
        }
        return NonEmptyDeserializedAnnotations(c.storageManager) {
            c.containingDeclaration.asProtoContainer()?.let {
                if (isDelegate) {
                    c.components.annotationAndConstantLoader.loadPropertyDelegateFieldAnnotations(it, proto).toList()
                } else {
                    c.components.annotationAndConstantLoader.loadPropertyBackingFieldAnnotations(it, proto).toList()
                }
            }.orEmpty()
        }
    }

    private fun getReceiverParameterAnnotations(proto: MessageLite, kind: AnnotatedCallableKind): Annotations =
        DeserializedAnnotations(c.storageManager) {
            c.containingDeclaration.asProtoContainer()?.let {
                c.components.annotationAndConstantLoader.loadExtensionReceiverParameterAnnotations(it, proto, kind)
            }.orEmpty()
        }

    private fun konstueParameters(
        konstueParameters: List<ProtoBuf.ValueParameter>,
        callable: MessageLite,
        kind: AnnotatedCallableKind
    ): List<ValueParameterDescriptor> {
        konst callableDescriptor = c.containingDeclaration as CallableDescriptor
        konst containerOfCallable = callableDescriptor.containingDeclaration.asProtoContainer()

        return konstueParameters.mapIndexed { i, proto ->
            konst flags = if (proto.hasFlags()) proto.flags else 0
            konst annotations = if (containerOfCallable != null && Flags.HAS_ANNOTATIONS.get(flags)) {
                NonEmptyDeserializedAnnotations(c.storageManager) {
                    c.components.annotationAndConstantLoader
                        .loadValueParameterAnnotations(containerOfCallable, callable, kind, i, proto)
                        .toList()
                }
            } else Annotations.EMPTY
            ValueParameterDescriptorImpl(
                callableDescriptor, null, i,
                annotations,
                c.nameResolver.getName(proto.name),
                c.typeDeserializer.type(proto.type(c.typeTable)),
                Flags.DECLARES_DEFAULT_VALUE.get(flags),
                Flags.IS_CROSSINLINE.get(flags),
                Flags.IS_NOINLINE.get(flags),
                proto.varargElementType(c.typeTable)?.let { c.typeDeserializer.type(it) },
                SourceElement.NO_SOURCE
            )
        }.toList()
    }

    private fun DeclarationDescriptor.asProtoContainer(): ProtoContainer? = when (this) {
        is PackageFragmentDescriptor -> ProtoContainer.Package(fqName, c.nameResolver, c.typeTable, c.containerSource)
        is DeserializedClassDescriptor -> thisAsProtoContainer
        else -> null // TODO: support annotations on lambdas and their parameters
    }

    private fun ProtoBuf.Type.toContextReceiver(
        deserializationContext: DeserializationContext,
        callableDescriptor: CallableDescriptor,
        index: Int
    ): ReceiverParameterDescriptor? {
        konst contextReceiverType = deserializationContext.typeDeserializer.type(this)
        return DescriptorFactory.createContextReceiverParameterForCallable(
            callableDescriptor,
            contextReceiverType,
            /* customLabelName = */ null/*todo store custom label name in metadata?*/,
            Annotations.EMPTY,
            index
        )
    }
}
