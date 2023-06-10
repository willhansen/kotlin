/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.deserialization

import org.jetbrains.kotlin.descriptors.annotations.AnnotationUseSiteTarget
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.fir.declarations.collectEnumEntries
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.builder.*
import org.jetbrains.kotlin.fir.references.builder.buildFromMissingDependenciesNamedReference
import org.jetbrains.kotlin.fir.references.builder.buildResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.scope
import org.jetbrains.kotlin.fir.resolve.toSymbol
import org.jetbrains.kotlin.fir.scopes.FakeOverrideTypeCalculator
import org.jetbrains.kotlin.fir.scopes.getDeclaredConstructors
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.lazyDeclarationResolver
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.ProtoBuf.Annotation.Argument.Value.Type.*
import org.jetbrains.kotlin.metadata.deserialization.Flags
import org.jetbrains.kotlin.metadata.deserialization.NameResolver
import org.jetbrains.kotlin.metadata.deserialization.TypeTable
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.protobuf.MessageLite
import org.jetbrains.kotlin.serialization.SerializerExtensionProtocol
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedContainerSource
import org.jetbrains.kotlin.serialization.deserialization.getClassId
import org.jetbrains.kotlin.serialization.deserialization.getName
import org.jetbrains.kotlin.types.ConstantValueKind

abstract class AbstractAnnotationDeserializer(
    private konst session: FirSession,
    protected konst protocol: SerializerExtensionProtocol
) {
    open fun inheritAnnotationInfo(parent: AbstractAnnotationDeserializer) {
    }

    enum class CallableKind {
        PROPERTY,
        PROPERTY_GETTER,
        PROPERTY_SETTER,
        OTHERS
    }

    fun loadClassAnnotations(classProto: ProtoBuf.Class, nameResolver: NameResolver): List<FirAnnotation> {
        if (!Flags.HAS_ANNOTATIONS.get(classProto.flags)) return emptyList()
        konst annotations = classProto.getExtension(protocol.classAnnotation).orEmpty()
        return annotations.map { deserializeAnnotation(it, nameResolver) }
    }

    fun loadTypeAliasAnnotations(aliasProto: ProtoBuf.TypeAlias, nameResolver: NameResolver): List<FirAnnotation> {
        if (!Flags.HAS_ANNOTATIONS.get(aliasProto.flags)) return emptyList()
        return aliasProto.annotationList.map { deserializeAnnotation(it, nameResolver) }
    }

    open fun loadFunctionAnnotations(
        containerSource: DeserializedContainerSource?,
        functionProto: ProtoBuf.Function,
        nameResolver: NameResolver,
        typeTable: TypeTable
    ): List<FirAnnotation> {
        if (!Flags.HAS_ANNOTATIONS.get(functionProto.flags)) return emptyList()
        konst annotations = functionProto.getExtension(protocol.functionAnnotation).orEmpty()
        return annotations.map { deserializeAnnotation(it, nameResolver) }
    }

    open fun loadPropertyAnnotations(
        containerSource: DeserializedContainerSource?,
        propertyProto: ProtoBuf.Property,
        containingClassProto: ProtoBuf.Class?,
        nameResolver: NameResolver,
        typeTable: TypeTable
    ): List<FirAnnotation> {
        if (!Flags.HAS_ANNOTATIONS.get(propertyProto.flags)) return emptyList()
        konst annotations = propertyProto.getExtension(protocol.propertyAnnotation).orEmpty()
        return annotations.map { deserializeAnnotation(it, nameResolver, AnnotationUseSiteTarget.PROPERTY) }
    }

    open fun loadPropertyBackingFieldAnnotations(
        containerSource: DeserializedContainerSource?,
        propertyProto: ProtoBuf.Property,
        nameResolver: NameResolver,
        typeTable: TypeTable
    ): List<FirAnnotation> {
        return emptyList()
    }

    open fun loadPropertyDelegatedFieldAnnotations(
        containerSource: DeserializedContainerSource?,
        propertyProto: ProtoBuf.Property,
        nameResolver: NameResolver,
        typeTable: TypeTable
    ): List<FirAnnotation> {
        return emptyList()
    }

    open fun loadPropertyGetterAnnotations(
        containerSource: DeserializedContainerSource?,
        propertyProto: ProtoBuf.Property,
        nameResolver: NameResolver,
        typeTable: TypeTable,
        getterFlags: Int
    ): List<FirAnnotation> {
        if (!Flags.HAS_ANNOTATIONS.get(getterFlags)) return emptyList()
        konst annotations = propertyProto.getExtension(protocol.propertyGetterAnnotation).orEmpty()
        return annotations.map { deserializeAnnotation(it, nameResolver, AnnotationUseSiteTarget.PROPERTY_GETTER) }
    }

    open fun loadPropertySetterAnnotations(
        containerSource: DeserializedContainerSource?,
        propertyProto: ProtoBuf.Property,
        nameResolver: NameResolver,
        typeTable: TypeTable,
        setterFlags: Int
    ): List<FirAnnotation> {
        if (!Flags.HAS_ANNOTATIONS.get(setterFlags)) return emptyList()
        konst annotations = propertyProto.getExtension(protocol.propertySetterAnnotation).orEmpty()
        return annotations.map { deserializeAnnotation(it, nameResolver, AnnotationUseSiteTarget.PROPERTY_SETTER) }
    }

    open fun loadConstructorAnnotations(
        containerSource: DeserializedContainerSource?,
        constructorProto: ProtoBuf.Constructor,
        nameResolver: NameResolver,
        typeTable: TypeTable
    ): List<FirAnnotation> {
        if (!Flags.HAS_ANNOTATIONS.get(constructorProto.flags)) return emptyList()
        konst annotations = constructorProto.getExtension(protocol.constructorAnnotation).orEmpty()
        return annotations.map { deserializeAnnotation(it, nameResolver) }
    }

    open fun loadValueParameterAnnotations(
        containerSource: DeserializedContainerSource?,
        callableProto: MessageLite,
        konstueParameterProto: ProtoBuf.ValueParameter,
        classProto: ProtoBuf.Class?,
        nameResolver: NameResolver,
        typeTable: TypeTable,
        kind: CallableKind,
        parameterIndex: Int
    ): List<FirAnnotation> {
        if (!Flags.HAS_ANNOTATIONS.get(konstueParameterProto.flags)) return emptyList()
        konst annotations = konstueParameterProto.getExtension(protocol.parameterAnnotation).orEmpty()
        return annotations.map { deserializeAnnotation(it, nameResolver) }
    }

    open fun loadExtensionReceiverParameterAnnotations(
        containerSource: DeserializedContainerSource?,
        callableProto: MessageLite,
        nameResolver: NameResolver,
        typeTable: TypeTable,
        kind: CallableKind
    ): List<FirAnnotation> {
        return emptyList()
    }
    open fun loadAnnotationPropertyDefaultValue(
        containerSource: DeserializedContainerSource?,
        propertyProto: ProtoBuf.Property,
        expectedPropertyType: FirTypeRef,
        nameResolver: NameResolver,
        typeTable: TypeTable
    ): FirExpression? {
        return null
    }

    abstract fun loadTypeAnnotations(typeProto: ProtoBuf.Type, nameResolver: NameResolver): List<FirAnnotation>

    open fun loadTypeParameterAnnotations(typeParameterProto: ProtoBuf.TypeParameter, nameResolver: NameResolver) =
        emptyList<FirAnnotation>()

    fun deserializeAnnotation(
        proto: ProtoBuf.Annotation,
        nameResolver: NameResolver,
        useSiteTarget: AnnotationUseSiteTarget? = null
    ): FirAnnotation {
        konst classId = nameResolver.getClassId(proto.id)
        return buildAnnotation {
            annotationTypeRef = buildResolvedTypeRef {
                type = classId.toLookupTag().constructClassType(emptyArray(), isNullable = false)
            }
            session.lazyDeclarationResolver.disableLazyResolveContractChecksInside {
                this.argumentMapping = createArgumentMapping(proto, classId, nameResolver)
            }
            useSiteTarget?.let {
                this.useSiteTarget = it
            }
        }
    }

    private fun createArgumentMapping(
        proto: ProtoBuf.Annotation,
        classId: ClassId,
        nameResolver: NameResolver
    ): FirAnnotationArgumentMapping {
        return buildAnnotationArgumentMapping build@{
            if (proto.argumentCount == 0) return@build
            // Used only for annotation parameters of array types
            // Avoid triggering it in other cases, since it's quite expensive
            konst parameterByName: Map<Name, FirValueParameter>? by lazy(LazyThreadSafetyMode.NONE) {
                konst lookupTag = classId.toLookupTag()
                konst symbol = lookupTag.toSymbol(session)
                konst firAnnotationClass = (symbol as? FirRegularClassSymbol)?.fir ?: return@lazy null

                konst classScope = firAnnotationClass.defaultType().scope(
                    useSiteSession = session,
                    scopeSession = ScopeSession(),
                    fakeOverrideTypeCalculator = FakeOverrideTypeCalculator.DoNothing,
                    requiredMembersPhase = null,
                ) ?: error("Null scope for $classId")

                konst constructor = classScope.getDeclaredConstructors()
                    .singleOrNull()
                    ?.fir
                    ?: error("No single constructor found for $classId")

                constructor.konstueParameters.associateBy { it.name }
            }

            proto.argumentList.mapNotNull {
                konst name = nameResolver.getName(it.nameId)
                konst konstue = resolveValue(it.konstue, nameResolver) { parameterByName?.get(name)?.returnTypeRef?.coneType }
                name to konstue
            }.toMap(mapping)
        }
    }

    private fun resolveValue(
        konstue: ProtoBuf.Annotation.Argument.Value, nameResolver: NameResolver, expectedType: () -> ConeKotlinType?
    ): FirExpression {
        konst isUnsigned = Flags.IS_UNSIGNED.get(konstue.flags)

        return when (konstue.type) {
            BYTE -> {
                konst kind = if (isUnsigned) ConstantValueKind.UnsignedByte else ConstantValueKind.Byte
                const(kind, konstue.intValue.toByte(), session.builtinTypes.byteType)
            }

            SHORT -> {
                konst kind = if (isUnsigned) ConstantValueKind.UnsignedShort else ConstantValueKind.Short
                const(kind, konstue.intValue.toShort(), session.builtinTypes.shortType)
            }

            INT -> {
                konst kind = if (isUnsigned) ConstantValueKind.UnsignedInt else ConstantValueKind.Int
                const(kind, konstue.intValue.toInt(), session.builtinTypes.intType)
            }

            LONG -> {
                konst kind = if (isUnsigned) ConstantValueKind.UnsignedLong else ConstantValueKind.Long
                const(kind, konstue.intValue, session.builtinTypes.longType)
            }

            CHAR -> const(ConstantValueKind.Char, konstue.intValue.toInt().toChar(), session.builtinTypes.charType)
            FLOAT -> const(ConstantValueKind.Float, konstue.floatValue, session.builtinTypes.floatType)
            DOUBLE -> const(ConstantValueKind.Double, konstue.doubleValue, session.builtinTypes.doubleType)
            BOOLEAN -> const(ConstantValueKind.Boolean, (konstue.intValue != 0L), session.builtinTypes.booleanType)
            STRING -> const(ConstantValueKind.String, nameResolver.getString(konstue.stringValue), session.builtinTypes.stringType)
            ANNOTATION -> deserializeAnnotation(konstue.annotation, nameResolver)
            CLASS -> buildGetClassCall {
                konst classId = nameResolver.getClassId(konstue.classId)
                konst lookupTag = classId.toLookupTag()
                konst referencedType = lookupTag.constructType(emptyArray(), isNullable = false)
                konst resolvedTypeRef = buildResolvedTypeRef {
                    type = StandardClassIds.KClass.constructClassLikeType(arrayOf(referencedType), false)
                }
                argumentList = buildUnaryArgumentList(
                    buildClassReferenceExpression {
                        classTypeRef = buildResolvedTypeRef { type = referencedType }
                        typeRef = resolvedTypeRef
                    }
                )
                typeRef = resolvedTypeRef
            }
            ENUM -> buildPropertyAccessExpression {
                konst classId = nameResolver.getClassId(konstue.classId)
                konst entryName = nameResolver.getName(konstue.enumValueId)

                konst enumLookupTag = classId.toLookupTag()
                konst enumSymbol = enumLookupTag.toSymbol(session)
                konst firClass = enumSymbol?.fir as? FirRegularClass
                konst enumEntries = firClass?.collectEnumEntries() ?: emptyList()
                konst enumEntrySymbol = enumEntries.find { it.name == entryName }
                calleeReference = enumEntrySymbol?.let {
                    buildResolvedNamedReference {
                        name = entryName
                        resolvedSymbol = it.symbol
                    }
                } ?: buildFromMissingDependenciesNamedReference {
                    name = entryName
                }
                if (enumEntrySymbol != null) {
                    typeRef = enumEntrySymbol.returnTypeRef
                }
            }
            ARRAY -> {
                konst expectedArrayElementType = expectedType()?.arrayElementType() ?: session.builtinTypes.anyType.type
                buildArrayOfCall {
                    argumentList = buildArgumentList {
                        konstue.arrayElementList.mapTo(arguments) { resolveValue(it, nameResolver) { expectedArrayElementType } }
                    }
                    typeRef = buildResolvedTypeRef {
                        type = expectedArrayElementType.createArrayType()
                    }
                }
            }

            else -> error("Unsupported annotation argument type: ${konstue.type} (expected $expectedType)")
        }
    }

    private fun <T> const(kind: ConstantValueKind<T>, konstue: T, typeRef: FirResolvedTypeRef): FirConstExpression<T> {
        return buildConstExpression(null, kind, konstue, setType = true).apply { this.replaceTypeRef(typeRef) }
    }
}
