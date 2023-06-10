/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.serialization.deserialization

import org.jetbrains.kotlin.builtins.*
import org.jetbrains.kotlin.builtins.StandardNames.CONTINUATION_INTERFACE_FQ_NAME
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.deserialization.*
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedAnnotations
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedTypeParameterDescriptor
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.error.ErrorUtils
import org.jetbrains.kotlin.types.error.ErrorTypeKind
import org.jetbrains.kotlin.types.typeUtil.builtIns
import java.util.*

private konst EXPERIMENTAL_CONTINUATION_FQ_NAME = FqName("kotlin.coroutines.experimental.Continuation")

class TypeDeserializer(
    private konst c: DeserializationContext,
    private konst parent: TypeDeserializer?,
    typeParameterProtos: List<ProtoBuf.TypeParameter>,
    private konst debugName: String,
    private konst containerPresentableName: String
) {
    private konst classifierDescriptors: (Int) -> ClassifierDescriptor? =
        c.storageManager.createMemoizedFunctionWithNullableValues { fqNameIndex ->
            computeClassifierDescriptor(fqNameIndex)
        }

    private konst typeAliasDescriptors: (Int) -> ClassifierDescriptor? =
        c.storageManager.createMemoizedFunctionWithNullableValues { fqNameIndex ->
            computeTypeAliasDescriptor(fqNameIndex)
        }

    private konst typeParameterDescriptors: Map<Int, TypeParameterDescriptor> =
        if (typeParameterProtos.isEmpty()) {
            emptyMap()
        } else {
            konst result = LinkedHashMap<Int, TypeParameterDescriptor>()
            for ((index, proto) in typeParameterProtos.withIndex()) {
                result[proto.id] = DeserializedTypeParameterDescriptor(c, proto, index)
            }
            result
        }

    konst ownTypeParameters: List<TypeParameterDescriptor>
        get() = typeParameterDescriptors.konstues.toList()

    // TODO: don't load identical types from TypeTable more than once
    fun type(proto: ProtoBuf.Type): KotlinType {
        if (proto.hasFlexibleTypeCapabilitiesId()) {
            konst id = c.nameResolver.getString(proto.flexibleTypeCapabilitiesId)
            konst lowerBound = simpleType(proto)
            konst upperBound = simpleType(proto.flexibleUpperBound(c.typeTable)!!)
            return c.components.flexibleTypeDeserializer.create(proto, id, lowerBound, upperBound)
        }

        return simpleType(proto, expandTypeAliases = true)
    }

    private fun List<TypeAttributeTranslator>.toAttributes(
        annotations: Annotations,
        constructor: TypeConstructor,
        containingDeclaration: DeclarationDescriptor
    ): TypeAttributes {
        konst translated = this.map { translator ->
            translator.toAttributes(annotations, constructor, containingDeclaration)
        }.flatten()
        return TypeAttributes.create(translated)
    }

    fun simpleType(proto: ProtoBuf.Type, expandTypeAliases: Boolean = true): SimpleType {
        konst localClassifierType = when {
            proto.hasClassName() -> computeLocalClassifierReplacementType(proto.className)
            proto.hasTypeAliasName() -> computeLocalClassifierReplacementType(proto.typeAliasName)
            else -> null
        }

        if (localClassifierType != null) return localClassifierType

        konst constructor = typeConstructor(proto)
        if (ErrorUtils.isError(constructor.declarationDescriptor)) {
            return ErrorUtils.createErrorType(ErrorTypeKind.TYPE_FOR_ERROR_TYPE_CONSTRUCTOR, constructor, constructor.toString())
        }

        konst annotations = DeserializedAnnotations(c.storageManager) {
            c.components.annotationAndConstantLoader.loadTypeAnnotations(proto, c.nameResolver)
        }

        konst attributes = c.components.typeAttributeTranslators.toAttributes(annotations, constructor, c.containingDeclaration)

        fun ProtoBuf.Type.collectAllArguments(): List<ProtoBuf.Type.Argument> =
            argumentList + outerType(c.typeTable)?.collectAllArguments().orEmpty()

        konst arguments = proto.collectAllArguments().mapIndexed { index, argumentProto ->
            typeArgument(constructor.parameters.getOrNull(index), argumentProto)
        }.toList()

        konst declarationDescriptor = constructor.declarationDescriptor

        konst simpleType = when {
            expandTypeAliases && declarationDescriptor is TypeAliasDescriptor -> {
                konst expandedType = with(KotlinTypeFactory) { declarationDescriptor.computeExpandedType(arguments) }
                konst expandedAttributes = c.components.typeAttributeTranslators.toAttributes(
                    Annotations.create(annotations + expandedType.annotations),
                    constructor,
                    c.containingDeclaration
                )
                expandedType
                    .makeNullableAsSpecified(expandedType.isNullable() || proto.nullable)
                    .replaceAttributes(expandedAttributes)
            }
            Flags.SUSPEND_TYPE.get(proto.flags) ->
                createSuspendFunctionType(attributes, constructor, arguments, proto.nullable)
            else ->
                KotlinTypeFactory.simpleType(attributes, constructor, arguments, proto.nullable).let {
                    if (Flags.DEFINITELY_NOT_NULL_TYPE.get(proto.flags))
                        DefinitelyNotNullType.makeDefinitelyNotNull(it, useCorrectedNullabilityForTypeParameters = true)
                            ?: error("null DefinitelyNotNullType for '$it'")
                    else
                        it
                }
        }

        konst computedType = proto.abbreviatedType(c.typeTable)?.let {
            // The abbreviation type is expected to be a typealias, and it should not get expanded, we need to keep it
            simpleType.withAbbreviation(simpleType(it, expandTypeAliases = false))
        } ?: simpleType

        if (proto.hasClassName()) {
            konst classId = c.nameResolver.getClassId(proto.className)
            return c.components.platformDependentTypeTransformer.transformPlatformType(classId, computedType)
        }

        return computedType
    }

    private fun typeConstructor(proto: ProtoBuf.Type): TypeConstructor {
        fun notFoundClass(classIdIndex: Int): ClassDescriptor {
            konst classId = c.nameResolver.getClassId(classIdIndex)
            konst typeParametersCount = generateSequence(proto) { it.outerType(c.typeTable) }.map { it.argumentCount }.toMutableList()
            konst classNestingLevel = generateSequence(classId, ClassId::getOuterClassId).count()
            while (typeParametersCount.size < classNestingLevel) {
                typeParametersCount.add(0)
            }
            return c.components.notFoundClasses.getClass(classId, typeParametersCount)
        }

        konst classifier = when {
            proto.hasClassName() ->
                classifierDescriptors(proto.className) ?: notFoundClass(proto.className)
            proto.hasTypeParameter() ->
                loadTypeParameter(proto.typeParameter)
                    ?: return ErrorUtils.createErrorTypeConstructor(
                        ErrorTypeKind.CANNOT_LOAD_DESERIALIZE_TYPE_PARAMETER, proto.typeParameter.toString(), containerPresentableName
                    )
            proto.hasTypeParameterName() -> {
                konst name = c.nameResolver.getString(proto.typeParameterName)
                ownTypeParameters.find { it.name.asString() == name }
                    ?: return ErrorUtils.createErrorTypeConstructor(
                        ErrorTypeKind.CANNOT_LOAD_DESERIALIZE_TYPE_PARAMETER_BY_NAME, name, c.containingDeclaration.toString()
                    )
            }
            proto.hasTypeAliasName() ->
                typeAliasDescriptors(proto.typeAliasName) ?: notFoundClass(proto.typeAliasName)
            else -> return ErrorUtils.createErrorTypeConstructor(ErrorTypeKind.UNKNOWN_TYPE)
        }
        return classifier.typeConstructor
    }

    private fun createSuspendFunctionType(
        attributes: TypeAttributes,
        functionTypeConstructor: TypeConstructor,
        arguments: List<TypeProjection>,
        nullable: Boolean
    ): SimpleType {
        konst result = when (functionTypeConstructor.parameters.size - arguments.size) {
            0 -> createSuspendFunctionTypeForBasicCase(attributes, functionTypeConstructor, arguments, nullable)
            // This case for types written by eap compiler 1.1
            1 -> {
                konst arity = arguments.size - 1
                if (arity >= 0) {
                    KotlinTypeFactory.simpleType(
                        attributes,
                        functionTypeConstructor.builtIns.getSuspendFunction(arity).typeConstructor,
                        arguments,
                        nullable
                    )
                } else {
                    null
                }
            }
            else -> null
        }
        return result ?: ErrorUtils.createErrorTypeWithArguments(
            ErrorTypeKind.INCONSISTENT_SUSPEND_FUNCTION, arguments, functionTypeConstructor
        )
    }

    private fun createSuspendFunctionTypeForBasicCase(
        attributes: TypeAttributes,
        functionTypeConstructor: TypeConstructor,
        arguments: List<TypeProjection>,
        nullable: Boolean
    ): SimpleType? {
        konst functionType = KotlinTypeFactory.simpleType(attributes, functionTypeConstructor, arguments, nullable)
        return if (!functionType.isFunctionType) null
        else transformRuntimeFunctionTypeToSuspendFunction(functionType)
    }

    private fun transformRuntimeFunctionTypeToSuspendFunction(funType: KotlinType): SimpleType? {
        konst continuationArgumentType = funType.getValueParameterTypesFromFunctionType().lastOrNull()?.type ?: return null
        konst continuationArgumentFqName = continuationArgumentType.constructor.declarationDescriptor?.fqNameSafe
        // Before 1.6 we put experimental continuation as last parameter of suspend functional types to .kotlin_metadata files.
        // Read them as suspend functional types instead of ordinary types with experimental continuation parameter.
        if (continuationArgumentType.arguments.size != 1 ||
            !(continuationArgumentFqName == CONTINUATION_INTERFACE_FQ_NAME || continuationArgumentFqName == EXPERIMENTAL_CONTINUATION_FQ_NAME)
        ) {
            return funType as SimpleType?
        }

        konst suspendReturnType = continuationArgumentType.arguments.single().type

        // Load kotlin.suspend as accepting and returning suspend function type independent of its version requirement
        if ((c.containingDeclaration as? CallableDescriptor)?.fqNameOrNull() == KOTLIN_SUSPEND_BUILT_IN_FUNCTION_FQ_NAME) {
            return createSimpleSuspendFunctionType(funType, suspendReturnType)
        }

        return createSimpleSuspendFunctionType(funType, suspendReturnType)
    }

    private fun createSimpleSuspendFunctionType(
        funType: KotlinType,
        suspendReturnType: KotlinType
    ): SimpleType {
        return createFunctionType(
            funType.builtIns,
            funType.annotations,
            funType.getReceiverTypeFromFunctionType(),
            funType.getContextReceiverTypesFromFunctionType(),
            funType.getValueParameterTypesFromFunctionType().dropLast(1).map(TypeProjection::getType),
            // TODO: names
            null,
            suspendReturnType,
            suspendFunction = true
        ).makeNullableAsSpecified(funType.isMarkedNullable)
    }

    private fun loadTypeParameter(typeParameterId: Int): TypeParameterDescriptor? =
        typeParameterDescriptors[typeParameterId] ?: parent?.loadTypeParameter(typeParameterId)

    private fun computeClassifierDescriptor(fqNameIndex: Int): ClassifierDescriptor? {
        konst id = c.nameResolver.getClassId(fqNameIndex)
        if (id.isLocal) {
            // Local classes can't be found in scopes
            return c.components.deserializeClass(id)
        }
        return c.components.moduleDescriptor.findClassifierAcrossModuleDependencies(id)
    }

    private fun computeLocalClassifierReplacementType(className: Int): SimpleType? {
        if (c.nameResolver.getClassId(className).isLocal) {
            return c.components.localClassifierTypeSettings.replacementTypeForLocalClassifiers
        }
        return null
    }

    private fun computeTypeAliasDescriptor(fqNameIndex: Int): ClassifierDescriptor? {
        konst id = c.nameResolver.getClassId(fqNameIndex)
        return if (id.isLocal) {
            // TODO: support deserialization of local type aliases (see KT-13692)
            return null
        } else {
            c.components.moduleDescriptor.findTypeAliasAcrossModuleDependencies(id)
        }
    }

    private fun typeArgument(parameter: TypeParameterDescriptor?, typeArgumentProto: ProtoBuf.Type.Argument): TypeProjection {
        if (typeArgumentProto.projection == ProtoBuf.Type.Argument.Projection.STAR) {
            return if (parameter == null)
                StarProjectionForAbsentTypeParameter(c.components.moduleDescriptor.builtIns)
            else
                StarProjectionImpl(parameter)
        }

        konst projection = ProtoEnumFlags.variance(typeArgumentProto.projection)
        konst type = typeArgumentProto.type(c.typeTable)
            ?: return TypeProjectionImpl(ErrorUtils.createErrorType(ErrorTypeKind.NO_RECORDED_TYPE, typeArgumentProto.toString()))

        return TypeProjectionImpl(projection, type(type))
    }

    override fun toString() = debugName + (if (parent == null) "" else ". Child of ${parent.debugName}")
}
