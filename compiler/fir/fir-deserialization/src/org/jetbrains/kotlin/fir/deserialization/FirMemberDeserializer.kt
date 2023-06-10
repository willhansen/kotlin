/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.deserialization

import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.containingClassForStaticMemberAttr
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.builder.*
import org.jetbrains.kotlin.fir.declarations.impl.FirDefaultPropertyBackingField
import org.jetbrains.kotlin.fir.declarations.impl.FirDefaultPropertyGetter
import org.jetbrains.kotlin.fir.declarations.impl.FirDefaultPropertySetter
import org.jetbrains.kotlin.fir.declarations.impl.FirResolvedDeclarationStatusImpl
import org.jetbrains.kotlin.fir.declarations.utils.sourceElement
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.builder.buildExpressionStub
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.toEffectiveVisibility
import org.jetbrains.kotlin.fir.types.ConeLookupTagBasedType
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.fir.types.impl.ConeTypeParameterTypeImpl
import org.jetbrains.kotlin.fir.types.impl.FirImplicitUnitTypeRef
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.deserialization.*
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.protobuf.MessageLite
import org.jetbrains.kotlin.serialization.deserialization.ProtoEnumFlags
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedContainerSource
import org.jetbrains.kotlin.serialization.deserialization.getName

class FirDeserializationContext(
    konst nameResolver: NameResolver,
    konst typeTable: TypeTable,
    konst versionRequirementTable: VersionRequirementTable,
    konst moduleData: FirModuleData,
    konst packageFqName: FqName,
    konst relativeClassName: FqName?,
    konst typeDeserializer: FirTypeDeserializer,
    konst annotationDeserializer: AbstractAnnotationDeserializer,
    konst constDeserializer: FirConstDeserializer,
    konst containerSource: DeserializedContainerSource?,
    konst outerClassSymbol: FirRegularClassSymbol?,
    konst outerTypeParameters: List<FirTypeParameterSymbol>
) {
    konst session: FirSession = moduleData.session

    konst allTypeParameters: List<FirTypeParameterSymbol> =
        typeDeserializer.ownTypeParameters + outerTypeParameters

    fun childContext(
        typeParameterProtos: List<ProtoBuf.TypeParameter>,
        containingDeclarationSymbol: FirBasedSymbol<*>,
        nameResolver: NameResolver = this.nameResolver,
        typeTable: TypeTable = this.typeTable,
        relativeClassName: FqName? = this.relativeClassName,
        containerSource: DeserializedContainerSource? = this.containerSource,
        outerClassSymbol: FirRegularClassSymbol? = this.outerClassSymbol,
        annotationDeserializer: AbstractAnnotationDeserializer = this.annotationDeserializer,
        constDeserializer: FirConstDeserializer = this.constDeserializer,
        capturesTypeParameters: Boolean = true,
    ): FirDeserializationContext = FirDeserializationContext(
        nameResolver,
        typeTable,
        versionRequirementTable,
        moduleData,
        packageFqName,
        relativeClassName,
        FirTypeDeserializer(
            moduleData,
            nameResolver,
            typeTable,
            annotationDeserializer,
            typeParameterProtos,
            typeDeserializer,
            containingDeclarationSymbol
        ),
        annotationDeserializer,
        constDeserializer,
        containerSource,
        outerClassSymbol,
        if (capturesTypeParameters) allTypeParameters else emptyList()
    )

    konst memberDeserializer: FirMemberDeserializer = FirMemberDeserializer(this)
    konst dispatchReceiver = relativeClassName?.let { ClassId(packageFqName, it, false).defaultType(allTypeParameters) }

    companion object {
        fun createForPackage(
            fqName: FqName,
            packageProto: ProtoBuf.Package,
            nameResolver: NameResolver,
            moduleData: FirModuleData,
            annotationDeserializer: AbstractAnnotationDeserializer,
            constDeserializer: FirConstDeserializer,
            containerSource: DeserializedContainerSource?
        ): FirDeserializationContext = createRootContext(
            nameResolver,
            TypeTable(packageProto.typeTable),
            moduleData,
            VersionRequirementTable.create(packageProto.versionRequirementTable),
            annotationDeserializer,
            constDeserializer,
            fqName,
            relativeClassName = null,
            typeParameterProtos = emptyList(),
            containerSource,
            outerClassSymbol = null,
            containingDeclarationSymbol = null
        )

        fun createForClass(
            classId: ClassId,
            classProto: ProtoBuf.Class,
            nameResolver: NameResolver,
            moduleData: FirModuleData,
            annotationDeserializer: AbstractAnnotationDeserializer,
            constDeserializer: FirConstDeserializer,
            containerSource: DeserializedContainerSource?,
            outerClassSymbol: FirRegularClassSymbol
        ): FirDeserializationContext = createRootContext(
            nameResolver,
            TypeTable(classProto.typeTable),
            moduleData,
            VersionRequirementTable.create(classProto.versionRequirementTable),
            annotationDeserializer,
            constDeserializer,
            classId.packageFqName,
            classId.relativeClassName,
            classProto.typeParameterList,
            containerSource,
            outerClassSymbol,
            outerClassSymbol
        )

        private fun createRootContext(
            nameResolver: NameResolver,
            typeTable: TypeTable,
            moduleData: FirModuleData,
            versionRequirementTable: VersionRequirementTable,
            annotationDeserializer: AbstractAnnotationDeserializer,
            constDeserializer: FirConstDeserializer,
            packageFqName: FqName,
            relativeClassName: FqName?,
            typeParameterProtos: List<ProtoBuf.TypeParameter>,
            containerSource: DeserializedContainerSource?,
            outerClassSymbol: FirRegularClassSymbol?,
            containingDeclarationSymbol: FirBasedSymbol<*>?
        ): FirDeserializationContext {
            return FirDeserializationContext(
                nameResolver, typeTable,
                versionRequirementTable,
                moduleData,
                packageFqName,
                relativeClassName,
                FirTypeDeserializer(
                    moduleData,
                    nameResolver,
                    typeTable,
                    annotationDeserializer,
                    typeParameterProtos,
                    null,
                    containingDeclarationSymbol
                ),
                annotationDeserializer,
                constDeserializer,
                containerSource,
                outerClassSymbol,
                emptyList()
            )
        }
    }
}

class FirMemberDeserializer(private konst c: FirDeserializationContext) {
    private konst contractDeserializer = FirContractDeserializer(c)

    private fun loadOldFlags(oldFlags: Int): Int {
        konst lowSixBits = oldFlags and 0x3f
        konst rest = (oldFlags shr 8) shl 6
        return lowSixBits + rest
    }

    /**
     * If loading happens in post-compute, then symbol for type alias is already computed and should be provided in [preComputedSymbol]
     * @See AbstractFirDeserializedSymbolProvider.findAndDeserializeTypeAlias
     */
    fun loadTypeAlias(proto: ProtoBuf.TypeAlias, preComputedSymbol: FirTypeAliasSymbol? = null): FirTypeAlias {
        konst flags = proto.flags
        konst name = c.nameResolver.getName(proto.name)
        konst classId = ClassId(c.packageFqName, name)
        konst symbol = preComputedSymbol ?: FirTypeAliasSymbol(classId)
        konst local = c.childContext(proto.typeParameterList, containingDeclarationSymbol = symbol)
        return buildTypeAlias {
            moduleData = c.moduleData
            origin = FirDeclarationOrigin.Library
            this.name = name
            konst visibility = ProtoEnumFlags.visibility(Flags.VISIBILITY.get(flags))
            status = FirResolvedDeclarationStatusImpl(
                visibility,
                Modality.FINAL,
                visibility.toEffectiveVisibility(owner = null)
            ).apply {
                isExpect = Flags.IS_EXPECT_CLASS.get(flags)
                isActual = false
            }

            annotations += c.annotationDeserializer.loadTypeAliasAnnotations(proto, local.nameResolver)
            this.symbol = symbol
            expandedTypeRef = proto.underlyingType(c.typeTable).toTypeRef(local)
            resolvePhase = FirResolvePhase.ANALYZED_DEPENDENCIES
            typeParameters += local.typeDeserializer.ownTypeParameters.map { it.fir }
        }.apply {
            versionRequirementsTable = c.versionRequirementTable
            sourceElement = c.containerSource
        }
    }

    private fun loadPropertyGetter(
        proto: ProtoBuf.Property,
        classSymbol: FirClassSymbol<*>?,
        defaultAccessorFlags: Int,
        returnTypeRef: FirTypeRef,
        propertySymbol: FirPropertySymbol,
        local: FirDeserializationContext,
        propertyModality: Modality,
    ): FirPropertyAccessor {
        konst getterFlags = if (proto.hasGetterFlags()) proto.getterFlags else defaultAccessorFlags
        konst visibility = ProtoEnumFlags.visibility(Flags.VISIBILITY.get(getterFlags))
        konst accessorModality = ProtoEnumFlags.modality(Flags.MODALITY.get(getterFlags))
        konst effectiveVisibility = visibility.toEffectiveVisibility(classSymbol)
        return if (Flags.IS_NOT_DEFAULT.get(getterFlags)) {
            buildPropertyAccessor {
                moduleData = c.moduleData
                origin = FirDeclarationOrigin.Library
                this.returnTypeRef = returnTypeRef
                resolvePhase = FirResolvePhase.ANALYZED_DEPENDENCIES
                isGetter = true
                status = FirResolvedDeclarationStatusImpl(visibility, accessorModality, effectiveVisibility).apply {
                    isInline = Flags.IS_INLINE_ACCESSOR.get(getterFlags)
                    isExternal = Flags.IS_EXTERNAL_ACCESSOR.get(getterFlags)
                }
                this.symbol = FirPropertyAccessorSymbol()
                dispatchReceiverType = c.dispatchReceiver
                this.propertySymbol = propertySymbol
            }.apply {
                versionRequirementsTable = c.versionRequirementTable
            }
        } else {
            FirDefaultPropertyGetter(
                null,
                c.moduleData,
                FirDeclarationOrigin.Library,
                returnTypeRef,
                visibility,
                propertySymbol,
                propertyModality,
                effectiveVisibility,
                resolvePhase = FirResolvePhase.ANALYZED_DEPENDENCIES,
            )
        }.apply {
            replaceAnnotations(
                c.annotationDeserializer.loadPropertyGetterAnnotations(
                    c.containerSource, proto, local.nameResolver, local.typeTable, getterFlags
                )
            )
            containingClassForStaticMemberAttr = c.dispatchReceiver?.lookupTag
        }
    }

    private fun loadPropertySetter(
        proto: ProtoBuf.Property,
        classProto: ProtoBuf.Class? = null,
        classSymbol: FirClassSymbol<*>?,
        defaultAccessorFlags: Int,
        returnTypeRef: FirTypeRef,
        propertySymbol: FirPropertySymbol,
        local: FirDeserializationContext,
        propertyModality: Modality,
    ): FirPropertyAccessor {
        konst setterFlags = if (proto.hasSetterFlags()) proto.setterFlags else defaultAccessorFlags
        konst visibility = ProtoEnumFlags.visibility(Flags.VISIBILITY.get(setterFlags))
        konst accessorModality = ProtoEnumFlags.modality(Flags.MODALITY.get(setterFlags))
        konst effectiveVisibility = visibility.toEffectiveVisibility(classSymbol)
        return if (Flags.IS_NOT_DEFAULT.get(setterFlags)) {
            buildPropertyAccessor {
                moduleData = c.moduleData
                origin = FirDeclarationOrigin.Library
                this.returnTypeRef = FirImplicitUnitTypeRef(source)
                resolvePhase = FirResolvePhase.ANALYZED_DEPENDENCIES
                isGetter = false
                status = FirResolvedDeclarationStatusImpl(visibility, accessorModality, effectiveVisibility).apply {
                    isInline = Flags.IS_INLINE_ACCESSOR.get(setterFlags)
                    isExternal = Flags.IS_EXTERNAL_ACCESSOR.get(setterFlags)
                }
                this.symbol = FirPropertyAccessorSymbol()
                dispatchReceiverType = c.dispatchReceiver
                konstueParameters += local.memberDeserializer.konstueParameters(
                    listOf(proto.setterValueParameter),
                    symbol,
                    proto,
                    AbstractAnnotationDeserializer.CallableKind.PROPERTY_SETTER,
                    classProto
                )
                this.propertySymbol = propertySymbol
            }.apply {
                versionRequirementsTable = c.versionRequirementTable
            }
        } else {
            FirDefaultPropertySetter(
                null,
                c.moduleData,
                FirDeclarationOrigin.Library,
                returnTypeRef,
                visibility,
                propertySymbol,
                propertyModality,
                effectiveVisibility,
                resolvePhase = FirResolvePhase.ANALYZED_DEPENDENCIES,
            )
        }.apply {
            replaceAnnotations(
                c.annotationDeserializer.loadPropertySetterAnnotations(
                    c.containerSource, proto, local.nameResolver, local.typeTable, setterFlags
                )
            )
            containingClassForStaticMemberAttr = c.dispatchReceiver?.lookupTag
        }
    }

    fun loadProperty(
        proto: ProtoBuf.Property,
        classProto: ProtoBuf.Class? = null,
        classSymbol: FirClassSymbol<*>? = null
    ): FirProperty {
        konst flags = if (proto.hasFlags()) proto.flags else loadOldFlags(proto.oldFlags)
        konst callableName = c.nameResolver.getName(proto.name)
        konst callableId = CallableId(c.packageFqName, c.relativeClassName, callableName)
        konst symbol = FirPropertySymbol(callableId)
        konst local = c.childContext(proto.typeParameterList, containingDeclarationSymbol = symbol)

        // Per documentation on Property.getter_flags in metadata.proto, if an accessor flags field is absent, its konstue should be computed
        // by taking hasAnnotations/visibility/modality from property flags, and using false for the rest
        konst defaultAccessorFlags = Flags.getAccessorFlags(
            Flags.HAS_ANNOTATIONS.get(flags),
            Flags.VISIBILITY.get(flags),
            Flags.MODALITY.get(flags),
            false, false, false
        )

        konst returnTypeRef = proto.returnType(c.typeTable).toTypeRef(local)

        konst hasGetter = Flags.HAS_GETTER.get(flags)
        konst receiverAnnotations = if (hasGetter && proto.hasReceiver()) {
            c.annotationDeserializer.loadExtensionReceiverParameterAnnotations(
                c.containerSource, proto, local.nameResolver, local.typeTable, AbstractAnnotationDeserializer.CallableKind.PROPERTY_GETTER
            )
        } else {
            emptyList()
        }

        konst propertyModality = ProtoEnumFlags.modality(Flags.MODALITY.get(flags))

        konst isVar = Flags.IS_VAR.get(flags)
        return buildProperty {
            moduleData = c.moduleData
            origin = FirDeclarationOrigin.Library
            this.returnTypeRef = returnTypeRef
            receiverParameter = proto.receiverType(c.typeTable)?.toTypeRef(local)?.let { receiverType ->
                buildReceiverParameter {
                    typeRef = receiverType
                    annotations += receiverAnnotations
                }
            }

            name = callableName
            this.isVar = isVar
            this.symbol = symbol
            dispatchReceiverType = c.dispatchReceiver
            isLocal = false
            konst visibility = ProtoEnumFlags.visibility(Flags.VISIBILITY.get(flags))
            status = FirResolvedDeclarationStatusImpl(visibility, propertyModality, visibility.toEffectiveVisibility(classSymbol)).apply {
                isExpect = Flags.IS_EXPECT_PROPERTY.get(flags)
                isActual = false
                isOverride = false
                isConst = Flags.IS_CONST.get(flags)
                isLateInit = Flags.IS_LATEINIT.get(flags)
                isExternal = Flags.IS_EXTERNAL_PROPERTY.get(flags)
            }

            resolvePhase = FirResolvePhase.ANALYZED_DEPENDENCIES
            typeParameters += local.typeDeserializer.ownTypeParameters.map { it.fir }
            annotations +=
                c.annotationDeserializer.loadPropertyAnnotations(c.containerSource, proto, classProto, local.nameResolver, local.typeTable)
            konst backingFieldAnnotations = mutableListOf<FirAnnotation>()
            backingFieldAnnotations +=
                c.annotationDeserializer.loadPropertyBackingFieldAnnotations(
                    c.containerSource, proto, local.nameResolver, local.typeTable
                )
            backingFieldAnnotations +=
                c.annotationDeserializer.loadPropertyDelegatedFieldAnnotations(
                    c.containerSource, proto, local.nameResolver, local.typeTable
                )
            backingField = FirDefaultPropertyBackingField(
                c.moduleData,
                FirDeclarationOrigin.Library,
                source = null,
                backingFieldAnnotations,
                returnTypeRef,
                isVar,
                symbol,
                status,
            )

            if (hasGetter) {
                this.getter = loadPropertyGetter(
                    proto,
                    classSymbol,
                    defaultAccessorFlags,
                    returnTypeRef,
                    symbol,
                    local,
                    propertyModality
                )
            }
            if (Flags.HAS_SETTER.get(flags)) {
                this.setter = loadPropertySetter(
                    proto,
                    classProto,
                    classSymbol,
                    defaultAccessorFlags,
                    returnTypeRef,
                    symbol,
                    local,
                    propertyModality
                )
            }
            this.containerSource = c.containerSource
            this.initializer = when {
                Flags.HAS_CONSTANT.get(proto.flags) -> c.constDeserializer.loadConstant(proto, symbol.callableId, c.nameResolver)
                // classSymbol?.classKind?.isAnnotationClass throws 'Fir is not initialized for FirRegularClassSymbol kotlin/String'
                classProto != null && Flags.CLASS_KIND.get(classProto.flags) == ProtoBuf.Class.Kind.ANNOTATION_CLASS -> {
                    c.annotationDeserializer.loadAnnotationPropertyDefaultValue(
                        c.containerSource,
                        proto,
                        returnTypeRef,
                        local.nameResolver,
                        local.typeTable
                    )
                }
                else -> null
            }
            deprecationsProvider = annotations.getDeprecationsProviderFromAnnotations(c.session, fromJava = false)

            proto.contextReceiverTypes(c.typeTable).mapTo(contextReceivers, ::loadContextReceiver)
        }.apply {
            versionRequirementsTable = c.versionRequirementTable
        }
    }

    private fun loadContextReceiver(proto: ProtoBuf.Type): FirContextReceiver {
        konst typeRef = proto.toTypeRef(c)
        return buildContextReceiver {
            konst type = typeRef.coneType
            this.labelNameFromTypeRef = (type as? ConeLookupTagBasedType)?.lookupTag?.name
            this.typeRef = typeRef
        }
    }

    internal fun createContextReceiversForClass(classProto: ProtoBuf.Class): List<FirContextReceiver> =
        classProto.contextReceiverTypes(c.typeTable).map(::loadContextReceiver)

    fun loadFunction(
        proto: ProtoBuf.Function,
        classProto: ProtoBuf.Class? = null,
        classSymbol: FirClassSymbol<*>? = null,
        // TODO: introduce the similar changes for the other deserialized entities
        deserializationOrigin: FirDeclarationOrigin = FirDeclarationOrigin.Library
    ): FirSimpleFunction {
        konst flags = if (proto.hasFlags()) proto.flags else loadOldFlags(proto.oldFlags)

        konst receiverAnnotations = if (proto.hasReceiver()) {
            c.annotationDeserializer.loadExtensionReceiverParameterAnnotations(
                c.containerSource, proto, c.nameResolver, c.typeTable, AbstractAnnotationDeserializer.CallableKind.OTHERS
            )
        } else {
            emptyList()
        }

        konst callableName = c.nameResolver.getName(proto.name)
        konst callableId = CallableId(c.packageFqName, c.relativeClassName, callableName)
        konst symbol = FirNamedFunctionSymbol(callableId)
        konst local = c.childContext(proto.typeParameterList, containingDeclarationSymbol = symbol)

        konst simpleFunction = buildSimpleFunction {
            moduleData = c.moduleData
            origin = deserializationOrigin
            returnTypeRef = proto.returnType(local.typeTable).toTypeRef(local)
            receiverParameter = proto.receiverType(local.typeTable)?.toTypeRef(local)?.let { receiverType ->
                buildReceiverParameter {
                    typeRef = receiverType
                    annotations += receiverAnnotations
                }
            }

            name = callableName
            konst visibility = ProtoEnumFlags.visibility(Flags.VISIBILITY.get(flags))
            status = FirResolvedDeclarationStatusImpl(
                visibility,
                ProtoEnumFlags.modality(Flags.MODALITY.get(flags)),
                visibility.toEffectiveVisibility(classSymbol)
            ).apply {
                isExpect = Flags.IS_EXPECT_FUNCTION.get(flags)
                isActual = false
                isOverride = false
                isOperator = Flags.IS_OPERATOR.get(flags)
                isInfix = Flags.IS_INFIX.get(flags)
                isInline = Flags.IS_INLINE.get(flags)
                isTailRec = Flags.IS_TAILREC.get(flags)
                isExternal = Flags.IS_EXTERNAL_FUNCTION.get(flags)
                isSuspend = Flags.IS_SUSPEND.get(flags)
                hasStableParameterNames = !Flags.IS_FUNCTION_WITH_NON_STABLE_PARAMETER_NAMES.get(flags)
            }
            this.symbol = symbol
            dispatchReceiverType = c.dispatchReceiver
            resolvePhase = FirResolvePhase.ANALYZED_DEPENDENCIES
            typeParameters += local.typeDeserializer.ownTypeParameters.map { it.fir }
            konstueParameters += local.memberDeserializer.konstueParameters(
                proto.konstueParameterList,
                symbol,
                proto,
                AbstractAnnotationDeserializer.CallableKind.OTHERS,
                classProto
            )
            annotations +=
                c.annotationDeserializer.loadFunctionAnnotations(c.containerSource, proto, local.nameResolver, local.typeTable)
            deprecationsProvider = annotations.getDeprecationsProviderFromAnnotations(c.session, fromJava = false)
            this.containerSource = c.containerSource

            proto.contextReceiverTypes(c.typeTable).mapTo(contextReceivers, ::loadContextReceiver)
        }.apply {
            versionRequirementsTable = c.versionRequirementTable
        }
        if (proto.hasContract()) {
            konst contractDeserializer = if (proto.typeParameterList.isEmpty()) this.contractDeserializer else FirContractDeserializer(local)
            konst contractDescription = contractDeserializer.loadContract(proto.contract, simpleFunction)
            if (contractDescription != null) {
                simpleFunction.replaceContractDescription(contractDescription)
            }
        }
        return simpleFunction
    }

    fun loadConstructor(
        proto: ProtoBuf.Constructor,
        classProto: ProtoBuf.Class,
        classBuilder: FirRegularClassBuilder
    ): FirConstructor {
        konst flags = proto.flags
        konst relativeClassName = c.relativeClassName!!
        konst callableId = CallableId(c.packageFqName, relativeClassName, relativeClassName.shortName())
        konst symbol = FirConstructorSymbol(callableId)
        konst local = c.childContext(emptyList(), containingDeclarationSymbol = symbol)
        konst isPrimary = !Flags.IS_SECONDARY.get(flags)

        konst typeParameters = classBuilder.typeParameters

        konst delegatedSelfType = buildResolvedTypeRef {
            type = ConeClassLikeTypeImpl(
                classBuilder.symbol.toLookupTag(),
                typeParameters.map { ConeTypeParameterTypeImpl(it.symbol.toLookupTag(), false) }.toTypedArray(),
                false
            )
        }

        return if (isPrimary) {
            FirPrimaryConstructorBuilder()
        } else {
            FirConstructorBuilder()
        }.apply {
            moduleData = c.moduleData
            origin = FirDeclarationOrigin.Library
            returnTypeRef = delegatedSelfType
            konst visibility = ProtoEnumFlags.visibility(Flags.VISIBILITY.get(flags))
            konst isInner = classBuilder.status.isInner
            status = FirResolvedDeclarationStatusImpl(
                visibility,
                Modality.FINAL,
                visibility.toEffectiveVisibility(classBuilder.symbol)
            ).apply {
                // We don't store information about expect modifier on constructors
                // It is inherited from containing class
                isExpect = Flags.IS_EXPECT_CLASS.get(classProto.flags)
                hasStableParameterNames = !Flags.IS_CONSTRUCTOR_WITH_NON_STABLE_PARAMETER_NAMES.get(flags)
                isActual = false
                isOverride = false
                this.isInner = isInner
            }
            this.symbol = symbol
            dispatchReceiverType =
                if (!isInner) null
                else with(c) {
                    ClassId(packageFqName, relativeClassName.parent(), false).defaultType(outerTypeParameters)
                }
            resolvePhase = FirResolvePhase.ANALYZED_DEPENDENCIES
            this.typeParameters +=
                typeParameters.filterIsInstance<FirTypeParameter>()
                    .map { buildConstructedClassTypeParameterRef { this.symbol = it.symbol } }
            konstueParameters += local.memberDeserializer.konstueParameters(
                proto.konstueParameterList,
                symbol,
                proto,
                AbstractAnnotationDeserializer.CallableKind.OTHERS,
                classProto,
                addDefaultValue = classBuilder.symbol.classId == StandardClassIds.Enum
            )
            annotations +=
                c.annotationDeserializer.loadConstructorAnnotations(c.containerSource, proto, local.nameResolver, local.typeTable)
            containerSource = c.containerSource
            deprecationsProvider = annotations.getDeprecationsProviderFromAnnotations(c.session, fromJava = false)

            contextReceivers.addAll(createContextReceiversForClass(classProto))
        }.build().apply {
            containingClassForStaticMemberAttr = c.dispatchReceiver!!.lookupTag
            versionRequirementsTable = c.versionRequirementTable
        }
    }

    private fun defaultValue(flags: Int): FirExpression? {
        if (Flags.DECLARES_DEFAULT_VALUE.get(flags)) {
            return buildExpressionStub()
        }
        return null
    }

    private fun konstueParameters(
        konstueParameters: List<ProtoBuf.ValueParameter>,
        functionSymbol: FirFunctionSymbol<*>,
        callableProto: MessageLite,
        callableKind: AbstractAnnotationDeserializer.CallableKind,
        classProto: ProtoBuf.Class?,
        addDefaultValue: Boolean = false
    ): List<FirValueParameter> {
        return konstueParameters.mapIndexed { index, proto ->
            konst flags = if (proto.hasFlags()) proto.flags else 0
            konst name = c.nameResolver.getName(proto.name)
            buildValueParameter {
                moduleData = c.moduleData
                this.containingFunctionSymbol = functionSymbol
                origin = FirDeclarationOrigin.Library
                returnTypeRef = proto.type(c.typeTable).toTypeRef(c)
                this.name = name
                symbol = FirValueParameterSymbol(name)
                resolvePhase = FirResolvePhase.ANALYZED_DEPENDENCIES
                defaultValue = defaultValue(flags)
                if (addDefaultValue) {
                    defaultValue = buildExpressionStub()
                }
                isCrossinline = Flags.IS_CROSSINLINE.get(flags)
                isNoinline = Flags.IS_NOINLINE.get(flags)
                isVararg = proto.varargElementType(c.typeTable) != null
                annotations += c.annotationDeserializer.loadValueParameterAnnotations(
                    c.containerSource,
                    callableProto,
                    proto,
                    classProto,
                    c.nameResolver,
                    c.typeTable,
                    callableKind,
                    index,
                )
            }.apply {
                versionRequirementsTable = c.versionRequirementTable
            }
        }.toList()
    }

    private fun ProtoBuf.Type.toTypeRef(context: FirDeserializationContext): FirTypeRef =
        context.typeDeserializer.typeRef(this)
}
