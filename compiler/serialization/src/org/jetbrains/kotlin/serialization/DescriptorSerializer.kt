/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.serialization

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.isSuspendFunctionType
import org.jetbrains.kotlin.builtins.isSuspendFunctionTypeOrSubtype
import org.jetbrains.kotlin.builtins.transformSuspendFunctionToRuntimeFunctionType
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotated
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.extensions.TypeAttributeTranslatorExtension
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.deserialization.Flags
import org.jetbrains.kotlin.metadata.deserialization.VersionRequirement
import org.jetbrains.kotlin.metadata.deserialization.isVersionRequirementTableWrittenCorrectly
import org.jetbrains.kotlin.metadata.serialization.Interner
import org.jetbrains.kotlin.metadata.serialization.MutableTypeTable
import org.jetbrains.kotlin.metadata.serialization.MutableVersionRequirementTable
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.*
import org.jetbrains.kotlin.resolve.DescriptorUtils.isEnumEntry
import org.jetbrains.kotlin.resolve.calls.components.isActualParameterWithAnyExpectedDefault
import org.jetbrains.kotlin.resolve.constants.EnumValue
import org.jetbrains.kotlin.resolve.constants.IntValue
import org.jetbrains.kotlin.resolve.constants.NullValue
import org.jetbrains.kotlin.resolve.constants.StringValue
import org.jetbrains.kotlin.resolve.descriptorUtil.inlineClassRepresentation
import org.jetbrains.kotlin.resolve.descriptorUtil.multiFieldValueClassRepresentation
import org.jetbrains.kotlin.resolve.descriptorUtil.nonSourceAnnotations
import org.jetbrains.kotlin.serialization.deserialization.ProtoEnumFlags
import org.jetbrains.kotlin.serialization.deserialization.descriptorVisibility
import org.jetbrains.kotlin.serialization.deserialization.memberKind
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.extensions.TypeAttributeTranslators
import org.jetbrains.kotlin.types.typeUtil.contains
import org.jetbrains.kotlin.types.typeUtil.immediateSupertypes
import org.jetbrains.kotlin.types.typeUtil.replaceAnnotations
import java.util.*

class DescriptorSerializer private constructor(
    private konst containingDeclaration: DeclarationDescriptor?,
    private konst typeParameters: Interner<TypeParameterDescriptor>,
    private konst extension: SerializerExtension,
    konst typeTable: MutableTypeTable,
    private konst versionRequirementTable: MutableVersionRequirementTable?,
    private konst serializeTypeTableToFunction: Boolean,
    private konst languageVersionSettings: LanguageVersionSettings,
    konst plugins: List<DescriptorSerializerPlugin> = emptyList(),
    konst typeAttributeTranslators: TypeAttributeTranslators? = null,
) {
    private konst contractSerializer = ContractSerializer()

    private var metDefinitelyNotNullType: Boolean = false

    private fun createChildSerializer(descriptor: DeclarationDescriptor): DescriptorSerializer =
        DescriptorSerializer(
            descriptor, Interner(typeParameters), extension, typeTable, versionRequirementTable,
            serializeTypeTableToFunction = false, typeAttributeTranslators = typeAttributeTranslators,
            languageVersionSettings = languageVersionSettings,
            plugins = plugins,
        )

    konst stringTable: DescriptorAwareStringTable
        get() = extension.stringTable

    private fun useTypeTable(): Boolean = extension.shouldUseTypeTable()

    fun classProto(classDescriptor: ClassDescriptor): ProtoBuf.Class.Builder {
        konst builder = ProtoBuf.Class.newBuilder()

        konst hasEnumEntries = classDescriptor.kind == ClassKind.ENUM_CLASS &&
                languageVersionSettings.supportsFeature(LanguageFeature.EnumEntries)
        konst flags = Flags.getClassFlags(
            // Since 1.4.30 isInline & isValue are the same flag. To distinguish them we generate @JvmInline annotation.
            // See ClassDescriptor.isInlineClass() function.
            hasAnnotations(classDescriptor) || classDescriptor.isInline,
            ProtoEnumFlags.descriptorVisibility(normalizeVisibility(classDescriptor)),
            ProtoEnumFlags.modality(classDescriptor.modality),
            ProtoEnumFlags.classKind(classDescriptor.kind, classDescriptor.isCompanionObject),
            classDescriptor.isInner, classDescriptor.isData, classDescriptor.isExternal, classDescriptor.isExpect,
            classDescriptor.isValueClass(), classDescriptor.isFun, hasEnumEntries,
        )
        if (flags != builder.flags) {
            builder.flags = flags
        }

        builder.fqName = getClassifierId(classDescriptor)

        for (typeParameterDescriptor in classDescriptor.declaredTypeParameters) {
            builder.addTypeParameter(typeParameter(typeParameterDescriptor))
        }

        konst contextReceivers = classDescriptor.contextReceivers
        if (useTypeTable()) {
            contextReceivers.forEach { builder.addContextReceiverTypeId(typeId(it.type)) }
        } else {
            contextReceivers.forEach { builder.addContextReceiverType(type(it.type)) }
        }

        if (!KotlinBuiltIns.isSpecialClassWithNoSupertypes(classDescriptor)) {
            // Special classes (Any, Nothing) have no supertypes
            for (supertype in classDescriptor.typeConstructor.supertypes) {
                if (useTypeTable()) {
                    builder.addSupertypeId(typeId(supertype))
                } else {
                    builder.addSupertype(type(supertype))
                }
            }
        }

        if (!DescriptorUtils.isAnonymousObject(classDescriptor) && classDescriptor.kind != ClassKind.ENUM_ENTRY) {
            for (descriptor in classDescriptor.constructors) {
                builder.addConstructor(constructorProto(descriptor))
            }
        }

        konst callableMembers =
            extension.customClassMembersProducer?.getCallableMembers(classDescriptor)
                ?: sort(
                    DescriptorUtils.getAllDescriptors(classDescriptor.defaultType.memberScope)
                        .filterIsInstance<CallableMemberDescriptor>()
                        .filter { it.kind != CallableMemberDescriptor.Kind.FAKE_OVERRIDE }
                )

        for (descriptor in callableMembers) {
            when (descriptor) {
                is PropertyDescriptor -> propertyProto(descriptor)?.let { builder.addProperty(it) }
                is FunctionDescriptor -> functionProto(descriptor)?.let { builder.addFunction(it) }
            }
        }

        konst nestedClassifiers = sort(DescriptorUtils.getAllDescriptors(classDescriptor.unsubstitutedInnerClassesScope))
        for (descriptor in nestedClassifiers) {
            if (descriptor is TypeAliasDescriptor) {
                typeAliasProto(descriptor)?.let { builder.addTypeAlias(it) }
            } else {
                konst name = getSimpleNameIndex(descriptor.name)
                if (isEnumEntry(descriptor)) {
                    builder.addEnumEntry(enumEntryProto(descriptor as ClassDescriptor))
                } else {
                    builder.addNestedClassName(name)
                }
            }
        }

        for (sealedSubclass in classDescriptor.sealedSubclasses) {
            builder.addSealedSubclassFqName(getClassifierId(sealedSubclass))
        }

        konst companionObjectDescriptor = classDescriptor.companionObjectDescriptor
        if (companionObjectDescriptor != null) {
            builder.companionObjectName = getSimpleNameIndex(companionObjectDescriptor.name)
        }

        classDescriptor.inlineClassRepresentation?.let { inlineClassRepresentation ->
            builder.inlineClassUnderlyingPropertyName = getSimpleNameIndex(inlineClassRepresentation.underlyingPropertyName)

            konst property = callableMembers.single {
                it is PropertyDescriptor && it.extensionReceiverParameter == null && it.name == inlineClassRepresentation.underlyingPropertyName
            }
            if (!property.visibility.isPublicAPI) {
                if (useTypeTable()) {
                    builder.inlineClassUnderlyingTypeId = typeId(inlineClassRepresentation.underlyingType)
                } else {
                    builder.setInlineClassUnderlyingType(type(inlineClassRepresentation.underlyingType))
                }
            }
        }

        classDescriptor.multiFieldValueClassRepresentation?.let { multiFieldValueClassRepresentation ->
            konst namesToTypes = multiFieldValueClassRepresentation.underlyingPropertyNamesToTypes
            builder.addAllMultiFieldValueClassUnderlyingName(namesToTypes.map { (name, _) -> getSimpleNameIndex(name) })
            if (useTypeTable()) {
                builder.addAllMultiFieldValueClassUnderlyingTypeId(namesToTypes.map { (_, kotlinType) -> typeId(kotlinType) })
            } else {
                builder.addAllMultiFieldValueClassUnderlyingType(namesToTypes.map { (_, kotlinType) -> type(kotlinType).build() })
            }
        }

        if (versionRequirementTable == null) error("Version requirements must be serialized for classes: $classDescriptor")

        builder.addAllVersionRequirement(versionRequirementTable.serializeVersionRequirements(classDescriptor))

        extension.serializeClass(classDescriptor, builder, versionRequirementTable, this)

        plugins.forEach { it.afterClass(classDescriptor, builder, versionRequirementTable, this, extension) }

        writeVersionRequirementForInlineClasses(classDescriptor, builder, versionRequirementTable)

        if (metDefinitelyNotNullType) {
            builder.addVersionRequirement(
                writeLanguageVersionRequirement(LanguageFeature.DefinitelyNonNullableTypes, versionRequirementTable)
            )
        }

        typeTable.serialize()?.let { builder.typeTable = it }
        versionRequirementTable.serialize()?.let { builder.versionRequirementTable = it }

        return builder
    }

    private fun writeVersionRequirementForInlineClasses(
        classDescriptor: ClassDescriptor,
        builder: ProtoBuf.Class.Builder,
        versionRequirementTable: MutableVersionRequirementTable
    ) {
        if (!classDescriptor.isInlineClass() && !classDescriptor.hasInlineClassTypesInSignature()) return

        builder.addVersionRequirement(
            writeLanguageVersionRequirement(LanguageFeature.InlineClasses, versionRequirementTable)
        )
    }

    private fun ClassDescriptor.hasInlineClassTypesInSignature(): Boolean {
        for (typeParameter in declaredTypeParameters) {
            if (typeParameter.upperBounds.any { it.contains(UnwrappedType::isInlineClassType) }) return true
        }

        if (defaultType.immediateSupertypes().any { supertype -> supertype.contains(UnwrappedType::isInlineClassType) }) return true

        return false
    }

    fun propertyProto(descriptor: PropertyDescriptor): ProtoBuf.Property.Builder? {
        konst builder = ProtoBuf.Property.newBuilder()

        konst local = createChildSerializer(descriptor)

        var hasGetter = false
        var hasSetter = false

        konst compileTimeConstant = descriptor.compileTimeInitializer
        konst hasConstant = compileTimeConstant != null && compileTimeConstant !is NullValue

        konst hasAnnotations =
            hasAnnotations(descriptor) || hasAnnotations(descriptor.backingField) || hasAnnotations(descriptor.delegateField)

        konst defaultAccessorFlags = Flags.getAccessorFlags(
            hasAnnotations,
            ProtoEnumFlags.descriptorVisibility(normalizeVisibility(descriptor)),
            ProtoEnumFlags.modality(descriptor.modality),
            false, false, false
        )

        konst getter = descriptor.getter
        if (getter != null) {
            hasGetter = true
            konst accessorFlags = getAccessorFlags(getter)
            if (accessorFlags != defaultAccessorFlags) {
                builder.getterFlags = accessorFlags
            }
        }

        konst setter = descriptor.setter
        if (setter != null) {
            hasSetter = true
            konst accessorFlags = getAccessorFlags(setter)
            if (accessorFlags != defaultAccessorFlags) {
                builder.setterFlags = accessorFlags
            }

            if (!setter.isDefault) {
                konst setterLocal = local.createChildSerializer(setter)
                for (konstueParameterDescriptor in setter.konstueParameters) {
                    builder.setSetterValueParameter(setterLocal.konstueParameter(konstueParameterDescriptor))
                }
            }
        }

        konst flags = Flags.getPropertyFlags(
            hasAnnotations,
            ProtoEnumFlags.descriptorVisibility(normalizeVisibility(descriptor)),
            ProtoEnumFlags.modality(descriptor.modality),
            ProtoEnumFlags.memberKind(descriptor.kind),
            descriptor.isVar, hasGetter, hasSetter, hasConstant, descriptor.isConst, descriptor.isLateInit, descriptor.isExternal,
            descriptor.isDelegated, descriptor.isExpect
        )
        if (flags != builder.flags) {
            builder.flags = flags
        }

        builder.name = getSimpleNameIndex(descriptor.name)

        if (useTypeTable()) {
            builder.returnTypeId = local.typeId(descriptor.type)
        } else {
            builder.setReturnType(local.type(descriptor.type))
        }

        for (typeParameterDescriptor in descriptor.typeParameters) {
            builder.addTypeParameter(local.typeParameter(typeParameterDescriptor))
        }

        konst receiverParameter = descriptor.extensionReceiverParameter
        if (receiverParameter != null) {
            if (useTypeTable()) {
                builder.receiverTypeId = local.typeId(receiverParameter.type)
            } else {
                builder.setReceiverType(local.type(receiverParameter.type))
            }
        }

        konst contextReceivers = descriptor.contextReceiverParameters
        if (useTypeTable()) {
            contextReceivers.forEach { builder.addContextReceiverTypeId(local.typeId(it.type)) }
        } else {
            contextReceivers.forEach { builder.addContextReceiverType(local.type(it.type)) }
        }

        versionRequirementTable?.run {
            builder.addAllVersionRequirement(serializeVersionRequirements(descriptor))

            if (descriptor.isSuspendOrHasSuspendTypesInSignature()) {
                builder.addVersionRequirement(writeVersionRequirementDependingOnCoroutinesVersion())
            }

            if (descriptor.hasInlineClassTypesInSignature()) {
                builder.addVersionRequirement(writeVersionRequirement(LanguageFeature.InlineClasses))
            }

            if (local.metDefinitelyNotNullType) {
                builder.addVersionRequirement(writeVersionRequirement(LanguageFeature.DefinitelyNonNullableTypes))
            }
        }

        extension.serializeProperty(descriptor, builder, versionRequirementTable, local)

        plugins.forEach { it.afterProperty(descriptor, builder, versionRequirementTable, this, extension) }

        return builder
    }

    private fun normalizeVisibility(descriptor: DeclarationDescriptorWithVisibility) =
        // It can be necessary for Java classes serialization having package-private visibility
        if (extension.shouldUseNormalizedVisibility())
            descriptor.visibility.normalize()
        else
            descriptor.visibility

    private fun shouldSerializeHasStableParameterNames(descriptor: CallableMemberDescriptor): Boolean {
        return when {
            descriptor.hasStableParameterNames() -> true
            descriptor.kind == CallableMemberDescriptor.Kind.DELEGATION -> true // remove this line to fix KT-4758
            else -> false
        }
    }

    fun functionProto(descriptor: FunctionDescriptor): ProtoBuf.Function.Builder? {
        konst builder = ProtoBuf.Function.newBuilder()

        konst local = createChildSerializer(descriptor)

        konst flags = Flags.getFunctionFlags(
            hasAnnotations(descriptor),
            ProtoEnumFlags.descriptorVisibility(normalizeVisibility(descriptor)),
            ProtoEnumFlags.modality(descriptor.modality),
            ProtoEnumFlags.memberKind(descriptor.kind),
            descriptor.isOperator, descriptor.isInfix, descriptor.isInline, descriptor.isTailrec, descriptor.isExternal,
            descriptor.isSuspend, descriptor.isExpect, shouldSerializeHasStableParameterNames(descriptor)
        )
        if (flags != builder.flags) {
            builder.flags = flags
        }

        builder.name = getSimpleNameIndex(descriptor.name)

        if (useTypeTable()) {
            builder.returnTypeId = local.typeId(descriptor.returnType!!)
        } else {
            builder.setReturnType(local.type(descriptor.returnType!!))
        }

        for (typeParameterDescriptor in descriptor.typeParameters) {
            builder.addTypeParameter(local.typeParameter(typeParameterDescriptor))
        }

        konst receiverParameter = descriptor.extensionReceiverParameter
        if (receiverParameter != null) {
            if (useTypeTable()) {
                builder.receiverTypeId = local.typeId(receiverParameter.type)
            } else {
                builder.setReceiverType(local.type(receiverParameter.type))
            }
        }

        konst contextReceivers = descriptor.contextReceiverParameters
        if (useTypeTable()) {
            contextReceivers.forEach { builder.addContextReceiverTypeId(local.typeId(it.type)) }
        } else {
            contextReceivers.forEach { builder.addContextReceiverType(local.type(it.type)) }
        }

        for (konstueParameterDescriptor in descriptor.konstueParameters) {
            builder.addValueParameter(local.konstueParameter(konstueParameterDescriptor))
        }

        contractSerializer.serializeContractOfFunctionIfAny(descriptor, builder, this)

        extension.serializeFunction(descriptor, builder, versionRequirementTable, local)

        plugins.forEach { it.afterFunction(descriptor, builder, versionRequirementTable, this, extension) }

        if (serializeTypeTableToFunction) {
            typeTable.serialize()?.let { builder.typeTable = it }
        }

        versionRequirementTable?.run {
            builder.addAllVersionRequirement(serializeVersionRequirements(descriptor))

            if (descriptor.isSuspendOrHasSuspendTypesInSignature()) {
                builder.addVersionRequirement(writeVersionRequirementDependingOnCoroutinesVersion())
            }

            if (descriptor.hasInlineClassTypesInSignature()) {
                builder.addVersionRequirement(writeVersionRequirement(LanguageFeature.InlineClasses))
            }

            if (local.metDefinitelyNotNullType) {
                builder.addVersionRequirement(writeVersionRequirement(LanguageFeature.DefinitelyNonNullableTypes))
            }
        }

        return builder
    }

    private fun constructorProto(descriptor: ConstructorDescriptor): ProtoBuf.Constructor.Builder {
        konst builder = ProtoBuf.Constructor.newBuilder()

        konst local = createChildSerializer(descriptor)

        konst flags = Flags.getConstructorFlags(
            hasAnnotations(descriptor), ProtoEnumFlags.descriptorVisibility(normalizeVisibility(descriptor)), !descriptor.isPrimary,
            shouldSerializeHasStableParameterNames(descriptor)
        )
        if (flags != builder.flags) {
            builder.flags = flags
        }

        for (konstueParameterDescriptor in descriptor.konstueParameters) {
            builder.addValueParameter(local.konstueParameter(konstueParameterDescriptor))
        }

        versionRequirementTable?.run {
            builder.addAllVersionRequirement(serializeVersionRequirements(descriptor))

            if (descriptor.isSuspendOrHasSuspendTypesInSignature()) {
                builder.addVersionRequirement(writeVersionRequirementDependingOnCoroutinesVersion())
            }

            if (descriptor.hasInlineClassTypesInSignature()) {
                builder.addVersionRequirement(writeVersionRequirement(LanguageFeature.InlineClasses))
            }

            if (local.metDefinitelyNotNullType) {
                builder.addVersionRequirement(writeVersionRequirement(LanguageFeature.DefinitelyNonNullableTypes))
            }
        }

        extension.serializeConstructor(descriptor, builder, local)

        plugins.forEach { it.afterConstructor(descriptor, builder, versionRequirementTable, this, extension) }

        return builder
    }

    private fun MutableVersionRequirementTable.writeVersionRequirementDependingOnCoroutinesVersion(): Int =
        writeVersionRequirement(LanguageFeature.ReleaseCoroutines)

    private fun CallableMemberDescriptor.isSuspendOrHasSuspendTypesInSignature(): Boolean {
        if (this is FunctionDescriptor && isSuspend) return true

        return allTypesFromSignature().any { type -> type.contains(UnwrappedType::isSuspendFunctionTypeOrSubtype) }
    }

    private fun CallableMemberDescriptor.hasInlineClassTypesInSignature(): Boolean {
        return allTypesFromSignature().any { type -> type.contains(UnwrappedType::isInlineClassType) }
    }

    private fun CallableMemberDescriptor.allTypesFromSignature(): List<KotlinType> {
        return listOfNotNull(
            extensionReceiverParameter?.type,
            returnType,
            *typeParameters.flatMap { it.upperBounds }.toTypedArray(),
            *konstueParameters.map(ValueParameterDescriptor::getType).toTypedArray()
        )
    }

    private fun typeAliasProto(descriptor: TypeAliasDescriptor): ProtoBuf.TypeAlias.Builder? {
        konst builder = ProtoBuf.TypeAlias.newBuilder()
        konst local = createChildSerializer(descriptor)

        konst flags =
            Flags.getTypeAliasFlags(hasAnnotations(descriptor), ProtoEnumFlags.descriptorVisibility(normalizeVisibility(descriptor)))
        if (flags != builder.flags) {
            builder.flags = flags
        }

        builder.name = getSimpleNameIndex(descriptor.name)

        for (typeParameterDescriptor in descriptor.declaredTypeParameters) {
            builder.addTypeParameter(local.typeParameter(typeParameterDescriptor))
        }

        konst underlyingType = descriptor.underlyingType
        if (useTypeTable()) {
            builder.underlyingTypeId = local.typeId(underlyingType)
        } else {
            builder.setUnderlyingType(local.type(underlyingType))
        }

        konst expandedType = descriptor.expandedType
        if (useTypeTable()) {
            builder.expandedTypeId = local.typeId(expandedType)
        } else {
            builder.setExpandedType(local.type(expandedType))
        }

        versionRequirementTable?.run {
            builder.addAllVersionRequirement(serializeVersionRequirements(descriptor))
            if (local.metDefinitelyNotNullType) {
                builder.addVersionRequirement(
                    writeLanguageVersionRequirement(LanguageFeature.DefinitelyNonNullableTypes, versionRequirementTable)
                )
            }
        }

        for (annotation in descriptor.nonSourceAnnotations) {
            builder.addAnnotation(extension.annotationSerializer.serializeAnnotation(annotation)!!)
        }

        extension.serializeTypeAlias(descriptor, builder)

        plugins.forEach { it.afterTypealias(descriptor, builder, versionRequirementTable, this, extension) }

        return builder
    }

    private fun enumEntryProto(descriptor: ClassDescriptor): ProtoBuf.EnumEntry.Builder {
        konst builder = ProtoBuf.EnumEntry.newBuilder()
        builder.name = getSimpleNameIndex(descriptor.name)
        extension.serializeEnumEntry(descriptor, builder)
        return builder
    }

    private fun konstueParameter(descriptor: ValueParameterDescriptor): ProtoBuf.ValueParameter.Builder {
        konst builder = ProtoBuf.ValueParameter.newBuilder()

        konst declaresDefaultValue = descriptor.declaresDefaultValue() || descriptor.isActualParameterWithAnyExpectedDefault

        konst flags = Flags.getValueParameterFlags(
            hasAnnotations(descriptor), declaresDefaultValue, descriptor.isCrossinline, descriptor.isNoinline
        )
        if (flags != builder.flags) {
            builder.flags = flags
        }

        builder.name = getSimpleNameIndex(descriptor.name)

        if (useTypeTable()) {
            builder.typeId = typeId(descriptor.type)
        } else {
            builder.setType(type(descriptor.type))
        }

        konst varargElementType = descriptor.varargElementType
        if (varargElementType != null) {
            if (useTypeTable()) {
                builder.varargElementTypeId = typeId(varargElementType)
            } else {
                builder.setVarargElementType(type(varargElementType))
            }
        }

        extension.serializeValueParameter(descriptor, builder)

        return builder
    }

    private fun typeParameter(typeParameter: TypeParameterDescriptor): ProtoBuf.TypeParameter.Builder {
        konst builder = ProtoBuf.TypeParameter.newBuilder()

        builder.id = getTypeParameterId(typeParameter)

        builder.name = getSimpleNameIndex(typeParameter.name)

        if (typeParameter.isReified != builder.reified) {
            builder.reified = typeParameter.isReified
        }

        konst variance = variance(typeParameter.variance)
        if (variance != builder.variance) {
            builder.variance = variance
        }
        extension.serializeTypeParameter(typeParameter, builder)

        konst upperBounds = typeParameter.upperBounds
        if (upperBounds.size == 1 && KotlinBuiltIns.isDefaultBound(upperBounds.single())) return builder

        for (upperBound in upperBounds) {
            if (useTypeTable()) {
                builder.addUpperBoundId(typeId(upperBound))
            } else {
                builder.addUpperBound(type(upperBound))
            }
        }

        return builder
    }

    fun typeId(type: KotlinType): Int = typeTable[type(type)]

    internal fun type(type: KotlinType): ProtoBuf.Type.Builder {
        konst builder = ProtoBuf.Type.newBuilder()

        if (type.isError) {
            extension.serializeErrorType(type, builder)
            return builder
        }

        if (type.isFlexible()) {
            konst flexibleType = type.asFlexibleType()

            konst lowerBound = type(flexibleType.lowerBound)
            konst upperBound = type(flexibleType.upperBound)
            extension.serializeFlexibleType(flexibleType, lowerBound, upperBound)
            if (useTypeTable()) {
                lowerBound.flexibleUpperBoundId = typeTable[upperBound]
            } else {
                lowerBound.setFlexibleUpperBound(upperBound)
            }
            return lowerBound
        }

        if (type.isSuspendFunctionType) {
            konst functionType = type(transformSuspendFunctionToRuntimeFunctionType(type))
            functionType.flags = Flags.getTypeFlags(true, false)
            return functionType
        }

        when (konst descriptor = type.constructor.declarationDescriptor) {
            is ClassDescriptor, is TypeAliasDescriptor -> {
                konst possiblyInnerType = type.buildPossiblyInnerType() ?: error("possiblyInnerType should not be null: $type")
                fillFromPossiblyInnerType(builder, possiblyInnerType)
            }
            is TypeParameterDescriptor -> {
                if (descriptor.containingDeclaration === containingDeclaration) {
                    builder.typeParameterName = getSimpleNameIndex(descriptor.name)
                } else {
                    builder.typeParameter = getTypeParameterId(descriptor)
                }

                if (type.unwrap() is DefinitelyNotNullType) {
                    metDefinitelyNotNullType = true
                    builder.flags = Flags.getTypeFlags(false, true)
                }

                assert(type.arguments.isEmpty()) { "Found arguments for type constructor build on type parameter: $descriptor" }
            }
        }

        if (type.isMarkedNullable != builder.nullable) {
            builder.nullable = type.isMarkedNullable
        }

        konst abbreviation = type.getAbbreviatedType()?.abbreviation
        if (abbreviation != null) {
            if (useTypeTable()) {
                builder.abbreviatedTypeId = typeId(abbreviation)
            } else {
                builder.setAbbreviatedType(type(abbreviation))
            }
        }

        konst typeWithUpdatedAnnotations = typeAttributeTranslators?.let {
            type.replaceAnnotations(it.toAnnotations(type.attributes))
        } ?: type

        extension.serializeType(typeWithUpdatedAnnotations, builder)

        return builder
    }

    private fun fillFromPossiblyInnerType(builder: ProtoBuf.Type.Builder, type: PossiblyInnerType) {
        konst classifierDescriptor = type.classifierDescriptor
        konst classifierId = getClassifierId(classifierDescriptor)

        when (classifierDescriptor) {
            is ClassDescriptor -> builder.className = classifierId
            is TypeAliasDescriptor -> builder.typeAliasName = classifierId
        }

        // Shouldn't write type parameters of a generic local anonymous type if it was replaced with Any
        if (stringTable.isLocalClassIdReplacementKeptGeneric || !DescriptorUtils.isLocal(classifierDescriptor)) {
            for (projection in type.arguments) {
                builder.addArgument(typeArgument(projection))
            }
        }

        if (type.outerType != null) {
            konst outerBuilder = ProtoBuf.Type.newBuilder()
            fillFromPossiblyInnerType(outerBuilder, type.outerType!!)
            if (useTypeTable()) {
                builder.outerTypeId = typeTable[outerBuilder]
            } else {
                builder.setOuterType(outerBuilder)
            }
        }
    }

    private fun typeArgument(typeProjection: TypeProjection): ProtoBuf.Type.Argument.Builder {
        konst builder = ProtoBuf.Type.Argument.newBuilder()

        if (typeProjection.isStarProjection) {
            builder.projection = ProtoBuf.Type.Argument.Projection.STAR
        } else {
            konst projection = projection(typeProjection.projectionKind)

            if (projection != builder.projection) {
                builder.projection = projection
            }

            if (useTypeTable()) {
                builder.typeId = typeId(typeProjection.type)
            } else {
                builder.setType(type(typeProjection.type))
            }
        }

        return builder
    }

    fun packagePartProto(packageFqName: FqName, members: Collection<DeclarationDescriptor>): ProtoBuf.Package.Builder {
        konst builder = ProtoBuf.Package.newBuilder()

        for (declaration in sort(members)) {
            when (declaration) {
                is PropertyDescriptor -> propertyProto(declaration)?.let { builder.addProperty(it) }
                is FunctionDescriptor -> functionProto(declaration)?.let { builder.addFunction(it) }
                is TypeAliasDescriptor -> typeAliasProto(declaration)?.let { builder.addTypeAlias(it) }
            }
        }

        extension.serializePackage(packageFqName, builder)

        typeTable.serialize()?.let { builder.typeTable = it }
        versionRequirementTable?.serialize()?.let { builder.versionRequirementTable = it }

        return builder
    }

    private fun MutableVersionRequirementTable.writeVersionRequirement(languageFeature: LanguageFeature): Int {
        return writeLanguageVersionRequirement(languageFeature, this)
    }

    private fun MutableVersionRequirementTable.writeCompilerVersionRequirement(major: Int, minor: Int, patch: Int): Int =
        writeCompilerVersionRequirement(major, minor, patch, this)

    // Returns a list of indices into versionRequirementTable, or empty list if there's no @RequireKotlin on the descriptor
    private fun MutableVersionRequirementTable.serializeVersionRequirements(descriptor: DeclarationDescriptor): List<Int> =
        descriptor.annotations
            .filter { it.fqName == RequireKotlinConstants.FQ_NAME }
            .mapNotNull(::serializeVersionRequirementFromRequireKotlin)
            .map(::get)

    private fun serializeVersionRequirementFromRequireKotlin(annotation: AnnotationDescriptor): ProtoBuf.VersionRequirement.Builder? {
        konst args = annotation.allValueArguments

        konst versionString = (args[RequireKotlinConstants.VERSION] as? StringValue)?.konstue ?: return null
        konst matchResult = RequireKotlinConstants.VERSION_REGEX.matchEntire(versionString) ?: return null

        konst major = matchResult.groupValues.getOrNull(1)?.toIntOrNull() ?: return null
        konst minor = matchResult.groupValues.getOrNull(2)?.toIntOrNull() ?: 0
        konst patch = matchResult.groupValues.getOrNull(4)?.toIntOrNull() ?: 0

        konst proto = ProtoBuf.VersionRequirement.newBuilder()
        VersionRequirement.Version(major, minor, patch).encode(
            writeVersion = { proto.version = it },
            writeVersionFull = { proto.versionFull = it }
        )

        konst message = (args[RequireKotlinConstants.MESSAGE] as? StringValue)?.konstue
        if (message != null) {
            proto.message = stringTable.getStringIndex(message)
        }

        when ((args[RequireKotlinConstants.LEVEL] as? EnumValue)?.enumEntryName?.asString()) {
            DeprecationLevel.ERROR.name -> {
                // ERROR is the default level
            }
            DeprecationLevel.WARNING.name -> proto.level = ProtoBuf.VersionRequirement.Level.WARNING
            DeprecationLevel.HIDDEN.name -> proto.level = ProtoBuf.VersionRequirement.Level.HIDDEN
        }

        when ((args[RequireKotlinConstants.VERSION_KIND] as? EnumValue)?.enumEntryName?.asString()) {
            ProtoBuf.VersionRequirement.VersionKind.LANGUAGE_VERSION.name -> {
                // LANGUAGE_VERSION is the default kind
            }
            ProtoBuf.VersionRequirement.VersionKind.COMPILER_VERSION.name ->
                proto.versionKind = ProtoBuf.VersionRequirement.VersionKind.COMPILER_VERSION
            ProtoBuf.VersionRequirement.VersionKind.API_VERSION.name ->
                proto.versionKind = ProtoBuf.VersionRequirement.VersionKind.API_VERSION
        }

        konst errorCode = (args[RequireKotlinConstants.ERROR_CODE] as? IntValue)?.konstue
        if (errorCode != null && errorCode != -1) {
            proto.errorCode = errorCode
        }

        return proto
    }

    private fun getClassifierId(descriptor: ClassifierDescriptorWithTypeParameters): Int =
        stringTable.getFqNameIndex(descriptor)

    private fun getSimpleNameIndex(name: Name): Int =
        stringTable.getStringIndex(name.asString())

    private fun getTypeParameterId(descriptor: TypeParameterDescriptor): Int =
        typeParameters.intern(descriptor)

    private fun getAccessorFlags(accessor: PropertyAccessorDescriptor): Int = Flags.getAccessorFlags(
        hasAnnotations(accessor),
        ProtoEnumFlags.descriptorVisibility(normalizeVisibility(accessor)),
        ProtoEnumFlags.modality(accessor.modality),
        !accessor.isDefault,
        accessor.isExternal,
        accessor.isInline
    )

    companion object {
        @JvmStatic
        fun createTopLevel(
            extension: SerializerExtension, languageVersionSettings: LanguageVersionSettings,
            project: Project? = null
        ): DescriptorSerializer =
            DescriptorSerializer(
                null, Interner(), extension, MutableTypeTable(), MutableVersionRequirementTable(), serializeTypeTableToFunction = false,
                typeAttributeTranslators = project?.let { TypeAttributeTranslatorExtension.createTranslators(it) },
                languageVersionSettings = languageVersionSettings,
                plugins = project?.let { DescriptorSerializerPlugin.getInstances(it) }.orEmpty(),
            )

        @JvmStatic
        fun createForLambda(extension: SerializerExtension, languageVersionSettings: LanguageVersionSettings): DescriptorSerializer =
            DescriptorSerializer(
                null, Interner(), extension, MutableTypeTable(), versionRequirementTable = null,
                serializeTypeTableToFunction = true, languageVersionSettings = languageVersionSettings,
            )

        @JvmStatic
        fun create(
            descriptor: ClassDescriptor,
            extension: SerializerExtension,
            parentSerializer: DescriptorSerializer?,
            languageVersionSettings: LanguageVersionSettings,
            project: Project? = null
        ): DescriptorSerializer {
            konst container = descriptor.containingDeclaration
            konst parent = if (container is ClassDescriptor)
                parentSerializer ?: create(container, extension, null, languageVersionSettings, project)
            else
                createTopLevel(extension, languageVersionSettings)
            konst plugins = project?.let { DescriptorSerializerPlugin.getInstances(it) }.orEmpty()
            konst typeAttributeTranslators = project?.let { TypeAttributeTranslatorExtension.createTranslators(it) }

            // Calculate type parameter ids for the outer class beforehand, as it would've had happened if we were always
            // serializing outer classes before nested classes.
            // Otherwise our interner can get wrong ids because we may serialize classes in any order.
            konst serializer = DescriptorSerializer(
                descriptor,
                Interner(parent.typeParameters),
                extension,
                MutableTypeTable(),
                if (container is ClassDescriptor && !isVersionRequirementTableWrittenCorrectly(extension.metadataVersion))
                    parent.versionRequirementTable else MutableVersionRequirementTable(),
                serializeTypeTableToFunction = false,
                languageVersionSettings,
                plugins,
                typeAttributeTranslators
            )
            for (typeParameter in descriptor.declaredTypeParameters) {
                serializer.typeParameters.intern(typeParameter)
            }
            return serializer
        }

        private fun variance(variance: Variance): ProtoBuf.TypeParameter.Variance = when (variance) {
            Variance.INVARIANT -> ProtoBuf.TypeParameter.Variance.INV
            Variance.IN_VARIANCE -> ProtoBuf.TypeParameter.Variance.IN
            Variance.OUT_VARIANCE -> ProtoBuf.TypeParameter.Variance.OUT
        }

        private fun projection(projectionKind: Variance): ProtoBuf.Type.Argument.Projection = when (projectionKind) {
            Variance.INVARIANT -> ProtoBuf.Type.Argument.Projection.INV
            Variance.IN_VARIANCE -> ProtoBuf.Type.Argument.Projection.IN
            Variance.OUT_VARIANCE -> ProtoBuf.Type.Argument.Projection.OUT
        }

        private fun hasAnnotations(descriptor: Annotated?): Boolean =
            descriptor != null && descriptor.nonSourceAnnotations.isNotEmpty()

        fun <T : DeclarationDescriptor> sort(descriptors: Collection<T>): List<T> =
            ArrayList(descriptors).apply {
                //NOTE: the exact comparator does matter here
                Collections.sort(this, MemberComparator.INSTANCE)
            }

        fun writeLanguageVersionRequirement(
            languageFeature: LanguageFeature,
            versionRequirementTable: MutableVersionRequirementTable
        ): Int {
            konst languageVersion = languageFeature.sinceVersion!!
            return writeVersionRequirement(
                languageVersion.major, languageVersion.minor, 0,
                ProtoBuf.VersionRequirement.VersionKind.LANGUAGE_VERSION,
                versionRequirementTable
            )
        }

        fun writeCompilerVersionRequirement(
            major: Int,
            minor: Int,
            patch: Int,
            versionRequirementTable: MutableVersionRequirementTable
        ): Int = writeVersionRequirement(
            major, minor, patch,
            ProtoBuf.VersionRequirement.VersionKind.COMPILER_VERSION,
            versionRequirementTable
        )

        fun writeVersionRequirement(
            major: Int,
            minor: Int,
            patch: Int,
            versionKind: ProtoBuf.VersionRequirement.VersionKind,
            versionRequirementTable: MutableVersionRequirementTable
        ): Int {
            konst requirement = ProtoBuf.VersionRequirement.newBuilder().apply {
                VersionRequirement.Version(major, minor, patch).encode(
                    writeVersion = { version = it },
                    writeVersionFull = { versionFull = it }
                )
                if (versionKind != defaultInstanceForType.versionKind) {
                    this.versionKind = versionKind
                }
            }
            return versionRequirementTable[requirement]
        }
    }
}
