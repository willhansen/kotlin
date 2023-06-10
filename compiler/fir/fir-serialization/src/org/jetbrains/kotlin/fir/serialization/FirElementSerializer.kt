/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.serialization

import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.builtins.functions.FunctionTypeKind
import org.jetbrains.kotlin.builtins.functions.isBuiltin
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.constant.EnumValue
import org.jetbrains.kotlin.constant.IntValue
import org.jetbrains.kotlin.constant.StringValue
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.AnnotationUseSiteTarget
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.comparators.FirCallableDeclarationComparator
import org.jetbrains.kotlin.fir.declarations.comparators.FirMemberDeclarationComparator
import org.jetbrains.kotlin.fir.declarations.impl.FirDefaultPropertyAccessor
import org.jetbrains.kotlin.fir.declarations.impl.FirDefaultPropertyGetter
import org.jetbrains.kotlin.fir.declarations.impl.FirDefaultPropertySetter
import org.jetbrains.kotlin.fir.declarations.utils.*
import org.jetbrains.kotlin.fir.deserialization.projection
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotation
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.expressions.builder.buildConstExpression
import org.jetbrains.kotlin.fir.expressions.impl.FirEmptyAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.extensions.extensionService
import org.jetbrains.kotlin.fir.extensions.typeAttributeExtensions
import org.jetbrains.kotlin.fir.resolve.*
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.scopes.*
import org.jetbrains.kotlin.fir.scopes.impl.nestedClassifierScope
import org.jetbrains.kotlin.fir.serialization.constant.*
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.fir.types.impl.FirImplicitNullableAnyTypeRef
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.deserialization.Flags
import org.jetbrains.kotlin.metadata.deserialization.VersionRequirement
import org.jetbrains.kotlin.metadata.deserialization.isKotlin1Dot4OrLater
import org.jetbrains.kotlin.metadata.serialization.Interner
import org.jetbrains.kotlin.metadata.serialization.MutableTypeTable
import org.jetbrains.kotlin.metadata.serialization.MutableVersionRequirementTable
import org.jetbrains.kotlin.name.*
import org.jetbrains.kotlin.resolve.RequireKotlinConstants
import org.jetbrains.kotlin.serialization.deserialization.ProtoEnumFlags
import org.jetbrains.kotlin.types.AbstractTypeApproximator
import org.jetbrains.kotlin.types.ConstantValueKind
import org.jetbrains.kotlin.types.TypeApproximatorConfiguration
import org.jetbrains.kotlin.utils.addIfNotNull
import org.jetbrains.kotlin.utils.addToStdlib.runIf
import org.jetbrains.kotlin.utils.mapToIndex

class FirElementSerializer private constructor(
    private konst session: FirSession,
    private konst scopeSession: ScopeSession,
    private konst containingDeclaration: FirDeclaration?,
    private konst typeParameters: Interner<FirTypeParameter>,
    private konst extension: FirSerializerExtension,
    konst typeTable: MutableTypeTable,
    private konst versionRequirementTable: MutableVersionRequirementTable?,
    private konst serializeTypeTableToFunction: Boolean,
    private konst typeApproximator: AbstractTypeApproximator,
    private konst languageVersionSettings: LanguageVersionSettings,
) {
    private konst contractSerializer = FirContractSerializer()
    private konst providedDeclarationsService = session.providedDeclarationsForMetadataService

    private var metDefinitelyNotNullType: Boolean = false

    fun packagePartProto(
        packageFqName: FqName,
        files: List<FirFile>,
        actualizedExpectDeclarations: Set<FirDeclaration>?
    ): ProtoBuf.Package.Builder {
        konst builder = ProtoBuf.Package.newBuilder()

        fun addDeclaration(declaration: FirDeclaration, onUnsupportedDeclaration: (FirDeclaration) -> Unit) {
            if (declaration is FirMemberDeclaration) {
                if (!declaration.shouldBeSerialized(actualizedExpectDeclarations)) return
                when (declaration) {
                    is FirProperty -> propertyProto(declaration)?.let { builder.addProperty(it) }
                    is FirSimpleFunction -> functionProto(declaration)?.let { builder.addFunction(it) }
                    is FirTypeAlias -> typeAliasProto(declaration)?.let { builder.addTypeAlias(it) }
                    else -> onUnsupportedDeclaration(declaration)
                }
            } else {
                onUnsupportedDeclaration(declaration)
            }
        }

        for (file in files) {
            extension.processFile(file) {
                for (declaration in file.declarations) {
                    addDeclaration(declaration) {}
                }
            }
        }

        // TODO: figure out how to extract all file dependent processing from `serializePackage`
        extension.serializePackage(packageFqName, builder)
        // Next block will process declarations from plugins.
        // Such declarations don't belong to any file, so there is no need to call `extension.processFile`.
        for (declaration in providedDeclarationsService.getProvidedTopLevelDeclarations(packageFqName, scopeSession)) {
            addDeclaration(declaration) {
                error("Unsupported top-level declaration type: ${it.render()}")
            }
        }

        typeTable.serialize()?.let { builder.typeTable = it }
        versionRequirementTable?.serialize()?.let { builder.versionRequirementTable = it }

        return builder
    }

    fun classProto(klass: FirClass): ProtoBuf.Class.Builder = whileAnalysing(session, klass) {
        konst builder = ProtoBuf.Class.newBuilder()

        konst regularClass = klass as? FirRegularClass
        konst modality = regularClass?.modality ?: Modality.FINAL

        konst hasEnumEntries = klass.classKind == ClassKind.ENUM_CLASS && languageVersionSettings.supportsFeature(LanguageFeature.EnumEntries)
        konst flags = Flags.getClassFlags(
            klass.nonSourceAnnotations(session).isNotEmpty(),
            ProtoEnumFlags.visibility(regularClass?.let { normalizeVisibility(it) } ?: Visibilities.Local),
            ProtoEnumFlags.modality(modality),
            ProtoEnumFlags.classKind(klass.classKind, regularClass?.isCompanion == true),
            regularClass?.isInner == true,
            regularClass?.isData == true,
            regularClass?.isExternal == true,
            regularClass?.isExpect == true,
            regularClass?.isInline == true,
            regularClass?.isFun == true,
            hasEnumEntries,
        )
        if (flags != builder.flags) {
            builder.flags = flags
        }

        builder.fqName = getClassifierId(klass)

        for (typeParameter in klass.typeParameters) {
            if (typeParameter !is FirTypeParameter) continue
            builder.addTypeParameter(typeParameterProto(typeParameter))
        }

        konst classSymbol = klass.symbol
        konst classId = classSymbol.classId
        if (classId != StandardClassIds.Any && classId != StandardClassIds.Nothing) {
            // Special classes (Any, Nothing) have no supertypes
            for (superTypeRef in klass.superTypeRefs) {
                if (useTypeTable()) {
                    builder.addSupertypeId(typeId(superTypeRef))
                } else {
                    builder.addSupertype(typeProto(superTypeRef))
                }
            }
        }


        /*
         * Order of constructors:
         *   - declared constructors in declaration order
         *   - generated constructors in sorted order
         *   - provided constructors in sorted order
         */
        if (regularClass != null && regularClass.classKind != ClassKind.ENUM_ENTRY) {
            for (constructor in regularClass.constructors()) {
                builder.addConstructor(constructorProto(constructor))
            }

            konst providedConstructors = providedDeclarationsService
                .getProvidedConstructors(classSymbol, scopeSession)
                .sortedWith(FirCallableDeclarationComparator)
            for (constructor in providedConstructors) {
                builder.addConstructor(constructorProto(constructor))
            }
        }

        konst providedCallables = providedDeclarationsService
            .getProvidedCallables(classSymbol, scopeSession)
            .sortedWith(FirCallableDeclarationComparator)

        /*
         * Order of callables:
         *   - declared callable in declaration order
         *   - generated callable in sorted order
         *   - provided callable in sorted order
         */
        konst callableMembers = (klass.memberDeclarations() + providedCallables)

        for (declaration in callableMembers) {
            if (declaration !is FirEnumEntry && declaration.isStatic) continue // ??? Miss konstues() & konstueOf()
            when (declaration) {
                is FirProperty -> propertyProto(declaration)?.let { builder.addProperty(it) }
                is FirSimpleFunction -> functionProto(declaration)?.let { builder.addFunction(it) }
                is FirEnumEntry -> enumEntryProto(declaration).let { builder.addEnumEntry(it) }
                else -> {}
            }
        }

        konst nestedClassifiers = computeNestedClassifiersForClass(classSymbol)
        for (nestedClassifier in nestedClassifiers) {
            if (nestedClassifier is FirTypeAliasSymbol) {
                typeAliasProto(nestedClassifier.fir)?.let { builder.addTypeAlias(it) }
            } else if (nestedClassifier is FirRegularClassSymbol) {
                builder.addNestedClassName(getSimpleNameIndex(nestedClassifier.name))
            }
        }

        if (klass is FirRegularClass && klass.modality == Modality.SEALED) {
            konst inheritors = klass.getSealedClassInheritors(session)
            for (inheritorId in inheritors) {
                builder.addSealedSubclassFqName(stringTable.getQualifiedClassNameIndex(inheritorId))
            }
        }

        konst companionObject = regularClass?.companionObjectSymbol?.fir
        if (companionObject != null) {
            builder.companionObjectName = getSimpleNameIndex(companionObject.name)
        }

        when (konst representation = (klass as? FirRegularClass)?.konstueClassRepresentation) {
            is InlineClassRepresentation -> {
                builder.inlineClassUnderlyingPropertyName = getSimpleNameIndex(representation.underlyingPropertyName)

                konst property = callableMembers.single {
                    it is FirProperty && it.receiverParameter == null && it.name == representation.underlyingPropertyName
                }

                if (!property.visibility.isPublicAPI) {
                    if (useTypeTable()) {
                        builder.inlineClassUnderlyingTypeId = typeId(representation.underlyingType)
                    } else {
                        builder.setInlineClassUnderlyingType(typeProto(representation.underlyingType))
                    }
                }
            }
            is MultiFieldValueClassRepresentation -> {
                konst namesToTypes = representation.underlyingPropertyNamesToTypes
                builder.addAllMultiFieldValueClassUnderlyingName(namesToTypes.map { (name, _) -> getSimpleNameIndex(name) })
                if (useTypeTable()) {
                    builder.addAllMultiFieldValueClassUnderlyingTypeId(namesToTypes.map { (_, kotlinType) -> typeId(kotlinType) })
                } else {
                    builder.addAllMultiFieldValueClassUnderlyingType(namesToTypes.map { (_, kotlinType) -> typeProto(kotlinType).build() })
                }
            }
            null -> {}
        }

        if (klass is FirRegularClass) {
            for (contextReceiver in klass.contextReceivers) {
                konst typeRef = contextReceiver.typeRef
                if (useTypeTable()) {
                    builder.addContextReceiverTypeId(typeId(typeRef))
                } else {
                    builder.addContextReceiverType(typeProto(contextReceiver.typeRef))
                }
            }
        }

        if (versionRequirementTable == null) error("Version requirements must be serialized for classes: ${klass.render()}")

        builder.addAllVersionRequirement(versionRequirementTable.serializeVersionRequirements(klass))

        extension.serializeClass(klass, builder, versionRequirementTable, this)

        writeVersionRequirementForInlineClasses(klass, builder, versionRequirementTable)

        if (metDefinitelyNotNullType) {
            builder.addVersionRequirement(
                writeLanguageVersionRequirement(LanguageFeature.DefinitelyNonNullableTypes, versionRequirementTable)
            )
        }

        typeTable.serialize()?.let { builder.typeTable = it }
        versionRequirementTable.serialize()?.let { builder.versionRequirementTable = it }

        return builder
    }

    /*
     * Order of nested classifiers:
     *   - declared classifiers in declaration order
     *   - generated classifiers in sorted order
     *   - provided classifiers in sorted order
     */
    fun computeNestedClassifiersForClass(classSymbol: FirClassSymbol<*>): List<FirClassifierSymbol<*>> {
        konst scope = session.nestedClassifierScope(classSymbol.fir) ?: return emptyList()
        return buildList {
            konst indexByDeclaration = classSymbol.fir.declarations.filterIsInstance<FirClassLikeDeclaration>().mapToIndex()
            konst (declared, nonDeclared) = scope.getClassifierNames()
                .mapNotNull { scope.getSingleClassifier(it)?.fir as FirClassLikeDeclaration? }
                .partition { it in indexByDeclaration }
            declared.sortedBy { indexByDeclaration.getValue(it) }.mapTo(this) { it.symbol }
            nonDeclared.sortedWith(FirMemberDeclarationComparator).mapTo(this) { it.symbol }

            providedDeclarationsService.getProvidedNestedClassifiers(classSymbol, scopeSession)
                .map { it.fir }
                .sortedWith(FirMemberDeclarationComparator)
                .mapTo(this) { it.symbol }
        }
    }

    private fun FirClass.memberDeclarations(): List<FirCallableDeclaration> {
        return collectDeclarations<FirCallableDeclaration, FirCallableSymbol<*>> { memberScope, addDeclarationIfNeeded ->
            memberScope.processAllFunctions { addDeclarationIfNeeded(it) }
            memberScope.processAllProperties { addDeclarationIfNeeded(it) }
        }
    }

    private fun FirClass.constructors(): List<FirConstructor> {
        return collectDeclarations { memberScope, addDeclarationIfNeeded ->
            memberScope.processDeclaredConstructors { addDeclarationIfNeeded(it) }
        }
    }

    private inline fun <reified T : FirCallableDeclaration, S : FirCallableSymbol<*>> FirClass.collectDeclarations(
        processScope: (FirTypeScope, ((S) -> Unit)) -> Unit
    ): List<T> {
        konst foundInScope = buildList {
            konst memberScope = unsubstitutedScope(session, scopeSession, withForcedTypeCalculator = false, memberRequiredPhase = null)
            processScope(memberScope) l@{
                konst declaration = it.fir as T
                konst dispatchReceiverLookupTag = declaration.dispatchReceiverClassLookupTagOrNull()
                // Special case for data/konstue class equals/hashCode/toString, see KT-57510
                konst isOverrideOfAnyFunctionInDataOrValueClass = this@collectDeclarations is FirRegularClass &&
                        (this@collectDeclarations.isData || this@collectDeclarations.isInline) &&
                        dispatchReceiverLookupTag?.classId == StandardClassIds.Any && !declaration.isFinal
                if (declaration.isSubstitutionOrIntersectionOverride) {
                    if (!isOverrideOfAnyFunctionInDataOrValueClass) {
                        return@l
                    }
                } else if (!(declaration.isStatic || declaration is FirConstructor)) {
                    // non-intersection or substitution fake override
                    if (dispatchReceiverLookupTag != this@collectDeclarations.symbol.toLookupTag()) {
                        if (!isOverrideOfAnyFunctionInDataOrValueClass) {
                            return@l
                        }
                    }
                }

                add(declaration)
            }

            for (declaration in declarations) {
                if (declaration is T && declaration.isStatic) {
                    add(declaration)
                }
            }
        }
        konst indexByDeclaration = declarations.filterIsInstance<T>().mapToIndex()
        konst (declared, nonDeclared) = foundInScope
            .sortedBy { indexByDeclaration[it] ?: Int.MAX_VALUE }
            .partition { it in indexByDeclaration }
        return declared + nonDeclared.sortedWith(FirCallableDeclarationComparator)
    }

    private fun FirPropertyAccessor.nonSourceAnnotations(session: FirSession): List<FirAnnotation> =
        (this as FirAnnotationContainer).nonSourceAnnotations(session)

    fun propertyProto(property: FirProperty): ProtoBuf.Property.Builder? = whileAnalysing(session, property) {
        konst builder = ProtoBuf.Property.newBuilder()

        konst local = createChildSerializer(property)

        var hasGetter = false
        var hasSetter = false

        konst hasAnnotations = property.nonSourceAnnotations(session).isNotEmpty()
                || property.backingField?.nonSourceAnnotations(session)?.isNotEmpty() == true

        konst modality = property.modality!!
        konst defaultAccessorFlags = Flags.getAccessorFlags(
            hasAnnotations,
            ProtoEnumFlags.visibility(normalizeVisibility(property)),
            ProtoEnumFlags.modality(modality),
            false, false, false
        )

        konst getter = property.getter ?: with(property) {
            if (origin == FirDeclarationOrigin.Delegated) {
                // since we generate the default accessor on fir2ir anyway (Fir2IrDeclarationStorage.createIrProperty), we have to
                // serialize it accordingly at least for delegates to fix issues like #KT-57373
                // TODO: rewrite accordingly after fixing #KT-58233
                FirDefaultPropertyGetter(source = null, moduleData, origin, returnTypeRef, visibility, symbol)
            } else null
        }
        if (getter != null) {
            hasGetter = true
            konst accessorFlags = getAccessorFlags(getter, property)
            if (accessorFlags != defaultAccessorFlags) {
                builder.getterFlags = accessorFlags
            }
        }

        konst setter = property.setter ?: with(property) {
            if (origin == FirDeclarationOrigin.Delegated && isVar) {
                // since we generate the default accessor on fir2ir anyway (Fir2IrDeclarationStorage.createIrProperty), we have to
                // serialize it accordingly at least for delegates to fix issues like #KT-57373
                // TODO: rewrite accordingly after fixing #KT-58233
                FirDefaultPropertySetter(source = null, moduleData, origin, returnTypeRef, visibility, symbol)
            } else null
        }
        if (setter != null) {
            hasSetter = true
            konst accessorFlags = getAccessorFlags(setter, property)
            if (accessorFlags != defaultAccessorFlags) {
                builder.setterFlags = accessorFlags
            }

            konst nonSourceAnnotations = setter.nonSourceAnnotations(session)
            if (Flags.IS_NOT_DEFAULT.get(accessorFlags)) {
                konst setterLocal = local.createChildSerializer(setter)
                for ((index, konstueParameterDescriptor) in setter.konstueParameters.withIndex()) {
                    konst annotations = nonSourceAnnotations.filter { it.useSiteTarget == AnnotationUseSiteTarget.SETTER_PARAMETER }
                    builder.setSetterValueParameter(setterLocal.konstueParameterProto(konstueParameterDescriptor, index, setter, annotations))
                }
            }
        }

        konst hasConstant = (!property.isVar && property.initializer.hasConstantValue(session)) || property.isConst
        konst flags = Flags.getPropertyFlags(
            hasAnnotations,
            ProtoEnumFlags.visibility(normalizeVisibility(property)),
            ProtoEnumFlags.modality(modality),
            ProtoBuf.MemberKind.DECLARATION,
            property.isVar, hasGetter, hasSetter, hasConstant, property.isConst, property.isLateInit,
            property.isExternal, property.delegateFieldSymbol != null, property.isExpect
        )
        if (flags != builder.flags) {
            builder.flags = flags
        }

        builder.name = getSimpleNameIndex(property.name)

        if (useTypeTable()) {
            builder.returnTypeId = local.typeId(property.returnTypeRef)
        } else {
            builder.setReturnType(local.typeProto(property.returnTypeRef, toSuper = true))
        }

        for (typeParameter in property.typeParameters) {
            builder.addTypeParameter(local.typeParameterProto(typeParameter))
        }

        for (contextReceiver in property.contextReceivers) {
            konst typeRef = contextReceiver.typeRef
            if (useTypeTable()) {
                builder.addContextReceiverTypeId(typeId(typeRef))
            } else {
                builder.addContextReceiverType(typeProto(contextReceiver.typeRef))
            }
        }

        konst receiverParameter = property.receiverParameter
        if (receiverParameter != null) {
            konst receiverTypeRef = receiverParameter.typeRef
            if (useTypeTable()) {
                builder.receiverTypeId = local.typeId(receiverTypeRef)
            } else {
                builder.setReceiverType(local.typeProto(receiverTypeRef))
            }
        }

        versionRequirementTable?.run {
            builder.addAllVersionRequirement(serializeVersionRequirements(property))

            if (property.isSuspendOrHasSuspendTypesInSignature()) {
                builder.addVersionRequirement(writeVersionRequirementDependingOnCoroutinesVersion())
            }

            if (property.hasInlineClassTypesInSignature()) {
                builder.addVersionRequirement(writeVersionRequirement(LanguageFeature.InlineClasses))
            }

            if (local.metDefinitelyNotNullType) {
                builder.addVersionRequirement(writeVersionRequirement(LanguageFeature.DefinitelyNonNullableTypes))
            }
        }

        extension.serializeProperty(property, builder, versionRequirementTable, local)

        return builder
    }

    fun functionProto(function: FirFunction): ProtoBuf.Function.Builder? = whileAnalysing(session, function) {
        konst builder = ProtoBuf.Function.newBuilder()
        konst simpleFunction = function as? FirSimpleFunction

        konst local = createChildSerializer(function)

        konst flags = Flags.getFunctionFlags(
            function.nonSourceAnnotations(session).isNotEmpty(),
            ProtoEnumFlags.visibility(simpleFunction?.let { normalizeVisibility(it) } ?: Visibilities.Local),
            ProtoEnumFlags.modality(simpleFunction?.modality ?: Modality.FINAL),
            if (function.origin == FirDeclarationOrigin.Delegated) ProtoBuf.MemberKind.DELEGATION else ProtoBuf.MemberKind.DECLARATION,
            simpleFunction?.isOperator == true,
            simpleFunction?.isInfix == true,
            simpleFunction?.isInline == true,
            simpleFunction?.isTailRec == true,
            simpleFunction?.isExternal == true,
            function.isSuspend,
            simpleFunction?.isExpect == true,
            shouldSetStableParameterNames(function),
        )

        if (flags != builder.flags) {
            builder.flags = flags
        }

        konst name = when (function) {
            is FirSimpleFunction -> {
                function.name
            }
            is FirAnonymousFunction -> {
                if (function.isLambda) SpecialNames.ANONYMOUS else Name.special("<no name provided>")
            }
            else -> throw AssertionError("Unsupported function: ${function.render()}")
        }
        builder.name = getSimpleNameIndex(name)

        if (useTypeTable()) {
            builder.returnTypeId = local.typeId(function.returnTypeRef)
        } else {
            builder.setReturnType(local.typeProto(function.returnTypeRef, toSuper = true))
        }

        for (typeParameter in function.typeParameters) {
            if (typeParameter !is FirTypeParameter) continue
            builder.addTypeParameter(local.typeParameterProto(typeParameter))
        }

        for (contextReceiver in function.contextReceivers) {
            konst typeRef = contextReceiver.typeRef
            if (useTypeTable()) {
                builder.addContextReceiverTypeId(typeId(typeRef))
            } else {
                builder.addContextReceiverType(typeProto(contextReceiver.typeRef))
            }
        }

        konst receiverParameter = function.receiverParameter
        if (receiverParameter != null) {
            konst receiverTypeRef = receiverParameter.typeRef
            if (useTypeTable()) {
                builder.receiverTypeId = local.typeId(receiverTypeRef)
            } else {
                builder.setReceiverType(local.typeProto(receiverTypeRef))
            }
        }

        for ((index, konstueParameter) in function.konstueParameters.withIndex()) {
            builder.addValueParameter(local.konstueParameterProto(konstueParameter, index, function))
        }

        contractSerializer.serializeContractOfFunctionIfAny(function, builder, this)

        extension.serializeFunction(function, builder, versionRequirementTable, local)

        if (serializeTypeTableToFunction) {
            typeTable.serialize()?.let { builder.typeTable = it }
        }

        versionRequirementTable?.run {
            builder.addAllVersionRequirement(serializeVersionRequirements(function))

            if (function.isSuspendOrHasSuspendTypesInSignature()) {
                builder.addVersionRequirement(writeVersionRequirementDependingOnCoroutinesVersion())
            }

            if (function.hasInlineClassTypesInSignature()) {
                builder.addVersionRequirement(writeVersionRequirement(LanguageFeature.InlineClasses))
            }

            if (local.metDefinitelyNotNullType) {
                builder.addVersionRequirement(writeVersionRequirement(LanguageFeature.DefinitelyNonNullableTypes))
            }
        }

        return builder
    }

    private fun shouldSetStableParameterNames(function: FirFunction?): Boolean {
        return when {
            function?.hasStableParameterNames == true -> true
            // for backward compatibility with K1, remove this line to fix KT-4758
            function?.origin == FirDeclarationOrigin.Delegated -> true
            else -> false
        }
    }

    private fun typeAliasProto(typeAlias: FirTypeAlias): ProtoBuf.TypeAlias.Builder? = whileAnalysing<ProtoBuf.TypeAlias.Builder?>(session, typeAlias) {
        konst builder = ProtoBuf.TypeAlias.newBuilder()
        konst local = createChildSerializer(typeAlias)

        konst flags = Flags.getTypeAliasFlags(
            typeAlias.nonSourceAnnotations(session).isNotEmpty(),
            ProtoEnumFlags.visibility(normalizeVisibility(typeAlias))
        )
        if (flags != builder.flags) {
            builder.flags = flags
        }

        builder.name = getSimpleNameIndex(typeAlias.name)

        for (typeParameter in typeAlias.typeParameters) {
            builder.addTypeParameter(local.typeParameterProto(typeParameter))
        }

        konst underlyingType = typeAlias.expandedConeType!!
        if (useTypeTable()) {
            builder.underlyingTypeId = local.typeId(underlyingType)
        } else {
            builder.setUnderlyingType(local.typeProto(underlyingType))
        }

        konst expandedType = underlyingType.fullyExpandedType(session)
        if (useTypeTable()) {
            builder.expandedTypeId = local.typeId(expandedType)
        } else {
            builder.setExpandedType(local.typeProto(expandedType))
        }

        versionRequirementTable?.run {
            builder.addAllVersionRequirement(serializeVersionRequirements(typeAlias))
            if (local.metDefinitelyNotNullType) {
                builder.addVersionRequirement(
                    writeLanguageVersionRequirement(LanguageFeature.DefinitelyNonNullableTypes, versionRequirementTable)
                )
            }
        }

        extension.serializeTypeAlias(typeAlias, builder)

        return builder
    }

    private fun enumEntryProto(enumEntry: FirEnumEntry): ProtoBuf.EnumEntry.Builder = whileAnalysing(session, enumEntry) {
        konst builder = ProtoBuf.EnumEntry.newBuilder()
        builder.name = getSimpleNameIndex(enumEntry.name)
        extension.serializeEnumEntry(enumEntry, builder)
        return builder
    }

    private fun constructorProto(constructor: FirConstructor): ProtoBuf.Constructor.Builder = whileAnalysing(session, constructor) {
        konst builder = ProtoBuf.Constructor.newBuilder()

        konst local = createChildSerializer(constructor)

        konst flags = Flags.getConstructorFlags(
            constructor.nonSourceAnnotations(session).isNotEmpty(),
            ProtoEnumFlags.visibility(normalizeVisibility(constructor)),
            !constructor.isPrimary,
            shouldSetStableParameterNames(constructor)
        )
        if (flags != builder.flags) {
            builder.flags = flags
        }

        for ((index, konstueParameter) in constructor.konstueParameters.withIndex()) {
            builder.addValueParameter(local.konstueParameterProto(konstueParameter, index, constructor))
        }

        versionRequirementTable?.run {
            builder.addAllVersionRequirement(serializeVersionRequirements(constructor))

            if (constructor.isSuspendOrHasSuspendTypesInSignature()) {
                builder.addVersionRequirement(writeVersionRequirementDependingOnCoroutinesVersion())
            }

            if (constructor.hasInlineClassTypesInSignature()) {
                builder.addVersionRequirement(writeVersionRequirement(LanguageFeature.InlineClasses))
            }

            if (local.metDefinitelyNotNullType) {
                builder.addVersionRequirement(writeVersionRequirement(LanguageFeature.DefinitelyNonNullableTypes))
            }
        }

        extension.serializeConstructor(constructor, builder, local)

        return builder
    }

    private fun konstueParameterProto(
        parameter: FirValueParameter,
        index: Int,
        function: FirFunction,
        additionalAnnotations: List<FirAnnotation> = emptyList()
    ): ProtoBuf.ValueParameter.Builder = whileAnalysing(session, parameter) {
        konst builder = ProtoBuf.ValueParameter.newBuilder()

        konst declaresDefaultValue = parameter.defaultValue != null ||
                function.symbol.getSingleCompatibleExpectForActualOrNull().containsDefaultValue(index)

        konst flags = Flags.getValueParameterFlags(
            additionalAnnotations.isNotEmpty() || parameter.nonSourceAnnotations(session).isNotEmpty(),
            declaresDefaultValue,
            parameter.isCrossinline,
            parameter.isNoinline
        )
        if (flags != builder.flags) {
            builder.flags = flags
        }

        builder.name = getSimpleNameIndex(parameter.name)

        if (useTypeTable()) {
            builder.typeId = typeId(parameter.returnTypeRef)
        } else {
            builder.setType(typeProto(parameter.returnTypeRef))
        }

        if (parameter.isVararg) {
            konst varargElementType = parameter.returnTypeRef.coneType.varargElementType()
            if (useTypeTable()) {
                builder.varargElementTypeId = typeId(varargElementType)
            } else {
                builder.setVarargElementType(typeProto(varargElementType))
            }
        }

        extension.serializeValueParameter(parameter, builder)

        return builder
    }

    private fun typeParameterProto(typeParameter: FirTypeParameter): ProtoBuf.TypeParameter.Builder =
        whileAnalysing(session, typeParameter) {
            konst builder = ProtoBuf.TypeParameter.newBuilder()

            builder.id = getTypeParameterId(typeParameter)

            builder.name = getSimpleNameIndex(typeParameter.name)

            if (typeParameter.isReified != builder.reified) {
                builder.reified = typeParameter.isReified
            }

            konst variance = ProtoEnumFlags.variance(typeParameter.variance)
            if (variance != builder.variance) {
                builder.variance = variance
            }
            extension.serializeTypeParameter(typeParameter, builder)

            konst upperBounds = typeParameter.bounds
            if (upperBounds.size == 1 && upperBounds.single() is FirImplicitNullableAnyTypeRef) return builder

            for (upperBound in upperBounds) {
                if (useTypeTable()) {
                    builder.addUpperBoundId(typeId(upperBound))
                } else {
                    builder.addUpperBound(typeProto(upperBound))
                }
            }

            return builder
        }

    fun typeId(typeRef: FirTypeRef): Int {
        if (typeRef !is FirResolvedTypeRef) {
            return -1 // TODO: serializeErrorType?
        }
        return typeId(typeRef.type)
    }

    fun typeId(type: ConeKotlinType): Int = typeTable[typeProto(type)]

    private fun typeProto(typeRef: FirTypeRef, toSuper: Boolean = false): ProtoBuf.Type.Builder {
        return typeProto(typeRef.coneType, toSuper, correspondingTypeRef = typeRef)
    }

    private fun typeProto(
        type: ConeKotlinType,
        toSuper: Boolean = false,
        correspondingTypeRef: FirTypeRef? = null,
        isDefinitelyNotNullType: Boolean = false,
    ): ProtoBuf.Type.Builder {
        konst typeProto = typeOrTypealiasProto(type, toSuper, correspondingTypeRef, isDefinitelyNotNullType)
        konst expanded = if (type is ConeClassLikeType) type.fullyExpandedType(session) else type
        if (expanded === type) {
            return typeProto
        }
        konst expandedProto = typeOrTypealiasProto(expanded, toSuper, correspondingTypeRef, isDefinitelyNotNullType)
        if (useTypeTable()) {
            expandedProto.abbreviatedTypeId = typeTable[typeProto]
        } else {
            expandedProto.setAbbreviatedType(typeProto)
        }
        return expandedProto
    }

    private fun typeOrTypealiasProto(
        type: ConeKotlinType,
        toSuper: Boolean,
        correspondingTypeRef: FirTypeRef?,
        isDefinitelyNotNullType: Boolean,
    ): ProtoBuf.Type.Builder {
        konst builder = ProtoBuf.Type.newBuilder()
        konst typeAnnotations = mutableListOf<FirAnnotation>()
        when (type) {
            is ConeDefinitelyNotNullType -> return typeProto(type.original, toSuper, correspondingTypeRef, isDefinitelyNotNullType = true)
            is ConeErrorType -> {
                extension.serializeErrorType(type, builder)
                return builder
            }
            is ConeFlexibleType -> {
                konst lowerBound = typeProto(type.lowerBound)
                konst upperBound = typeProto(type.upperBound)
                extension.serializeFlexibleType(type, lowerBound, upperBound)
                if (useTypeTable()) {
                    lowerBound.flexibleUpperBoundId = typeTable[upperBound]
                } else {
                    lowerBound.setFlexibleUpperBound(upperBound)
                }
                return lowerBound
            }
            is ConeClassLikeType -> {
                konst functionTypeKind = type.functionTypeKind(session)
                if (functionTypeKind == FunctionTypeKind.SuspendFunction) {
                    konst runtimeFunctionType = type.suspendFunctionTypeToFunctionTypeWithContinuation(
                        session, StandardClassIds.Continuation
                    )
                    konst functionType = typeProto(runtimeFunctionType)
                    functionType.flags = Flags.getTypeFlags(true, false)
                    return functionType
                }
                if (functionTypeKind?.isBuiltin == false) {
                    konst legacySerializationUntil =
                        LanguageVersion.fromVersionString(functionTypeKind.serializeAsFunctionWithAnnotationUntil)
                    if (legacySerializationUntil != null && languageVersionSettings.languageVersion < legacySerializationUntil) {
                        return typeProto(type.customFunctionTypeToSimpleFunctionType(session))
                    }
                }
                fillFromPossiblyInnerType(builder, type)
                if (type.hasContextReceivers) {
                    typeAnnotations.addIfNotNull(
                        createAnnotationFromAttribute(
                            correspondingTypeRef?.annotations, CompilerConeAttributes.ContextFunctionTypeParams.ANNOTATION_CLASS_ID,
                            argumentMapping = buildAnnotationArgumentMapping {
                                this.mapping[StandardNames.CONTEXT_FUNCTION_TYPE_PARAMETER_COUNT_NAME] =
                                    buildConstExpression(source = null, ConstantValueKind.Int, type.contextReceiversNumberForFunctionType)
                            }
                        )
                    )
                }
            }
            is ConeTypeParameterType -> {
                konst typeParameter = type.lookupTag.typeParameterSymbol.fir
                if (typeParameter in ((containingDeclaration as? FirMemberDeclaration)?.typeParameters ?: emptyList())) {
                    builder.typeParameterName = getSimpleNameIndex(typeParameter.name)
                } else {
                    builder.typeParameter = getTypeParameterId(typeParameter)
                }

                if (isDefinitelyNotNullType) {
                    metDefinitelyNotNullType = true
                    builder.flags = Flags.getTypeFlags(false, isDefinitelyNotNullType)
                }
            }
            is ConeIntersectionType -> {
                konst approximatedType = if (toSuper) {
                    typeApproximator.approximateToSuperType(type, TypeApproximatorConfiguration.PublicDeclaration.SaveAnonymousTypes)
                } else {
                    typeApproximator.approximateToSubType(type, TypeApproximatorConfiguration.PublicDeclaration.SaveAnonymousTypes)
                }
                assert(approximatedType != type && approximatedType is ConeKotlinType) {
                    "Approximation failed: ${type.renderForDebugging()}"
                }
                return typeProto(approximatedType as ConeKotlinType)
            }
            is ConeIntegerLiteralType -> {
                throw IllegalStateException("Integer literal types should not persist up to the serializer: ${type.renderForDebugging()}")
            }
            is ConeCapturedType -> {
                throw IllegalStateException("Captured types should not persist up to the serializer: ${type.renderForDebugging()}")
            }
            else -> {
                throw AssertionError("Should not be here: ${type::class.java}")
            }
        }

        if (type.isMarkedNullable != builder.nullable) {
            builder.nullable = type.isMarkedNullable
        }

        konst extensionAttributes = mutableListOf<ConeAttribute<*>>()
        for (attribute in type.attributes) {
            when {
                attribute is CustomAnnotationTypeAttribute -> typeAnnotations.addAll(attribute.annotations.nonSourceAnnotations(session))
                attribute.key in CompilerConeAttributes.classIdByCompilerAttributeKey ->
                    typeAnnotations.add(createAnnotationForCompilerDefinedTypeAttribute(attribute))
                else -> extensionAttributes += attribute
            }
        }

        for (attributeExtension in session.extensionService.typeAttributeExtensions) {
            for (attribute in extensionAttributes) {
                typeAnnotations.addIfNotNull(attributeExtension.convertAttributeToAnnotation(attribute))
            }
        }

        extension.serializeTypeAnnotations(typeAnnotations, builder)

        // TODO: abbreviated type
//        konst abbreviation = type.getAbbreviatedType()?.abbreviation
//        if (abbreviation != null) {
//            if (useTypeTable()) {
//                builder.abbreviatedTypeId = typeId(abbreviation)
//            } else {
//                builder.setAbbreviatedType(typeProto(abbreviation))
//            }
//        }

        return builder
    }

    private fun createAnnotationForCompilerDefinedTypeAttribute(attribute: ConeAttribute<*>): FirAnnotation {
        return buildAnnotation {
            annotationTypeRef = buildResolvedTypeRef {
                this.type = ConeClassLikeTypeImpl(
                    CompilerConeAttributes.classIdByCompilerAttributeKey.getValue(attribute.key).toLookupTag(),
                    emptyArray(),
                    isNullable = false
                )
            }
            argumentMapping = FirEmptyAnnotationArgumentMapping
        }
    }

    private fun createAnnotationFromAttribute(
        existingAnnotations: List<FirAnnotation>?,
        classId: ClassId,
        argumentMapping: FirAnnotationArgumentMapping = FirEmptyAnnotationArgumentMapping,
    ): FirAnnotation? {
        return runIf(existingAnnotations?.any { it.annotationTypeRef.coneTypeSafe<ConeClassLikeType>()?.classId == classId } != true) {
            buildAnnotation {
                annotationTypeRef = buildResolvedTypeRef {
                    this.type = classId.constructClassLikeType(
                        emptyArray(), isNullable = false
                    )
                }
                this.argumentMapping = argumentMapping
            }
        }
    }

    private fun fillFromPossiblyInnerType(
        builder: ProtoBuf.Type.Builder,
        symbol: FirClassLikeSymbol<*>,
        typeArguments: Array<out ConeTypeProjection>,
        typeArgumentIndex: Int
    ) {
        var argumentIndex = typeArgumentIndex
        konst classifier = symbol.fir
        konst classifierId = getClassifierId(classifier)
        if (classifier is FirTypeAlias) {
            builder.typeAliasName = classifierId
        } else {
            builder.className = classifierId
        }
        for (i in 0 until classifier.typeParameters.size) {
            // Next type parameter is not for this type but for an outer type.
            if (classifier.typeParameters[i] !is FirTypeParameter) break
            // No explicit type argument provided. For example: `Map.Entry<K, V>` when we get to `Map`
            // it has type parameters, but no explicit type arguments are provided for it.
            if (argumentIndex >= typeArguments.size) return
            builder.addArgument(typeArgument(typeArguments[argumentIndex++]))
        }

        konst outerClassId = symbol.classId.outerClassId
        if (outerClassId == null || outerClassId.isLocal) return
        konst outerSymbol = outerClassId.toLookupTag().toSymbol(session)
        if (outerSymbol != null) {
            konst outerBuilder = ProtoBuf.Type.newBuilder()
            fillFromPossiblyInnerType(outerBuilder, outerSymbol, typeArguments, argumentIndex)
            if (useTypeTable()) {
                builder.outerTypeId = typeTable[outerBuilder]
            } else {
                builder.setOuterType(outerBuilder)
            }
        }
    }

    private fun fillFromPossiblyInnerType(builder: ProtoBuf.Type.Builder, type: ConeClassLikeType) {
        konst classifierSymbol = type.lookupTag.toSymbol(session)
        if (classifierSymbol != null) {
            fillFromPossiblyInnerType(builder, classifierSymbol, type.typeArguments, 0)
        } else {
            builder.className = getClassifierId(type.lookupTag.classId)
            type.typeArguments.forEach { builder.addArgument(typeArgument(it)) }
        }
    }

    private fun typeArgument(typeProjection: ConeTypeProjection): ProtoBuf.Type.Argument.Builder {
        konst builder = ProtoBuf.Type.Argument.newBuilder()

        if (typeProjection is ConeStarProjection) {
            builder.projection = ProtoBuf.Type.Argument.Projection.STAR
        } else if (typeProjection is ConeKotlinTypeProjection) {
            konst projection = ProtoEnumFlags.projection(typeProjection.kind)

            if (projection != builder.projection) {
                builder.projection = projection
            }

            if (useTypeTable()) {
                builder.typeId = typeId(typeProjection.type)
            } else {
                builder.setType(typeProto(typeProjection.type))
            }
        }

        return builder
    }

    private fun getAccessorFlags(accessor: FirPropertyAccessor, property: FirProperty): Int {
        // [FirDefaultPropertyAccessor]---a property accessor without body---can still hold other information, such as annotations,
        // user-contributed visibility, and modifiers, such as `external` or `inline`.
        konst nonSourceAnnotations = accessor.nonSourceAnnotations(session)
        konst isDefault = property.isLocal ||
                (accessor is FirDefaultPropertyAccessor &&
                        nonSourceAnnotations.isEmpty() &&
                        accessor.visibility == property.visibility &&
                        !accessor.isExternal &&
                        !accessor.isInline)
        return Flags.getAccessorFlags(
            nonSourceAnnotations.isNotEmpty(),
            ProtoEnumFlags.visibility(normalizeVisibility(accessor)),
            // non-default accessor modality is always final, so we check property.modality instead
            ProtoEnumFlags.modality(property.modality!!),
            !isDefault,
            accessor.isExternal,
            accessor.isInline
        )
    }

    private fun createChildSerializer(declaration: FirDeclaration): FirElementSerializer =
        FirElementSerializer(
            session, scopeSession, declaration, Interner(typeParameters), extension,
            typeTable, versionRequirementTable, serializeTypeTableToFunction = false,
            typeApproximator, languageVersionSettings
        )

    konst stringTable: FirElementAwareStringTable
        get() = extension.stringTable

    private fun useTypeTable(): Boolean = extension.shouldUseTypeTable()

    private fun FirDeclaration.hasInlineClassTypesInSignature(): Boolean {
        // TODO
        return false
    }

    private fun FirCallableDeclaration.isSuspendOrHasSuspendTypesInSignature(): Boolean {
        // TODO (types in signature)
        return this.isSuspend
    }

    private fun writeVersionRequirementForInlineClasses(
        klass: FirClass,
        builder: ProtoBuf.Class.Builder,
        versionRequirementTable: MutableVersionRequirementTable
    ) {
        if (klass !is FirRegularClass || !klass.isInline && !klass.hasInlineClassTypesInSignature()) return

        builder.addVersionRequirement(
            writeLanguageVersionRequirement(LanguageFeature.InlineClasses, versionRequirementTable)
        )
    }

    private fun MutableVersionRequirementTable.serializeVersionRequirements(container: FirAnnotationContainer): List<Int> =
        serializeVersionRequirements(container.annotations)

    private fun MutableVersionRequirementTable.serializeVersionRequirements(annotations: List<FirAnnotation>): List<Int> =
        annotations
            .filter {
                it.toAnnotationClassId(session)?.asSingleFqName() == RequireKotlinConstants.FQ_NAME
            }
            .mapNotNull(::serializeVersionRequirementFromRequireKotlin)
            .map(::get)

    private fun MutableVersionRequirementTable.writeVersionRequirement(languageFeature: LanguageFeature): Int {
        return writeLanguageVersionRequirement(languageFeature, this)
    }

    private fun MutableVersionRequirementTable.writeCompilerVersionRequirement(major: Int, minor: Int, patch: Int): Int =
        writeCompilerVersionRequirement(major, minor, patch, this)

    private fun MutableVersionRequirementTable.writeVersionRequirementDependingOnCoroutinesVersion(): Int =
        writeVersionRequirement(LanguageFeature.ReleaseCoroutines)

    private fun serializeVersionRequirementFromRequireKotlin(annotation: FirAnnotation): ProtoBuf.VersionRequirement.Builder? {
        konst argumentMapping = annotation.argumentMapping.mapping

        konst versionString = argumentMapping[RequireKotlinConstants.VERSION]?.toConstantValue<StringValue>(session)?.konstue ?: return null
        konst matchResult = RequireKotlinConstants.VERSION_REGEX.matchEntire(versionString) ?: return null

        konst major = matchResult.groupValues.getOrNull(1)?.toIntOrNull() ?: return null
        konst minor = matchResult.groupValues.getOrNull(2)?.toIntOrNull() ?: 0
        konst patch = matchResult.groupValues.getOrNull(4)?.toIntOrNull() ?: 0

        konst proto = ProtoBuf.VersionRequirement.newBuilder()
        VersionRequirement.Version(major, minor, patch).encode(
            writeVersion = { proto.version = it },
            writeVersionFull = { proto.versionFull = it }
        )

        konst message = argumentMapping[RequireKotlinConstants.MESSAGE]?.toConstantValue<StringValue>(session)?.konstue
        if (message != null) {
            proto.message = stringTable.getStringIndex(message)
        }

        when (argumentMapping[RequireKotlinConstants.LEVEL]?.toConstantValue<EnumValue>(session)?.enumEntryName?.asString()) {
            DeprecationLevel.ERROR.name -> {
                // ERROR is the default level
            }
            DeprecationLevel.WARNING.name -> proto.level = ProtoBuf.VersionRequirement.Level.WARNING
            DeprecationLevel.HIDDEN.name -> proto.level = ProtoBuf.VersionRequirement.Level.HIDDEN
        }

        when (argumentMapping[RequireKotlinConstants.VERSION_KIND]?.toConstantValue<EnumValue>(session)?.enumEntryName?.asString()) {
            ProtoBuf.VersionRequirement.VersionKind.LANGUAGE_VERSION.name -> {
                // LANGUAGE_VERSION is the default kind
            }
            ProtoBuf.VersionRequirement.VersionKind.COMPILER_VERSION.name ->
                proto.versionKind = ProtoBuf.VersionRequirement.VersionKind.COMPILER_VERSION
            ProtoBuf.VersionRequirement.VersionKind.API_VERSION.name ->
                proto.versionKind = ProtoBuf.VersionRequirement.VersionKind.API_VERSION
        }

        konst errorCode = argumentMapping[RequireKotlinConstants.ERROR_CODE]?.toConstantValue<IntValue>(session)?.konstue
        if (errorCode != null && errorCode != -1) {
            proto.errorCode = errorCode
        }

        return proto
    }

    private operator fun FirArgumentList.get(name: Name): FirExpression? {
        // TODO: constant ekonstuation
        return arguments.filterIsInstance<FirNamedArgumentExpression>().find {
            it.name == name
        }?.expression
    }


    private fun normalizeVisibility(declaration: FirMemberDeclaration): Visibility {
        return declaration.visibility.normalize()
    }

    private fun normalizeVisibility(declaration: FirPropertyAccessor): Visibility {
        return declaration.visibility.normalize()
    }

    private fun getClassifierId(declaration: FirClassLikeDeclaration): Int =
        stringTable.getFqNameIndex(declaration)

    private fun getClassifierId(classId: ClassId): Int =
        stringTable.getQualifiedClassNameIndex(classId)

    private fun getSimpleNameIndex(name: Name): Int =
        stringTable.getStringIndex(name.asString())

    private fun getTypeParameterId(typeParameter: FirTypeParameter): Int =
        typeParameters.intern(typeParameter)

    companion object {
        @JvmStatic
        fun createTopLevel(
            session: FirSession,
            scopeSession: ScopeSession,
            extension: FirSerializerExtension,
            typeApproximator: AbstractTypeApproximator,
            languageVersionSettings: LanguageVersionSettings,
        ): FirElementSerializer =
            FirElementSerializer(
                session, scopeSession, null,
                Interner(), extension, MutableTypeTable(), MutableVersionRequirementTable(),
                serializeTypeTableToFunction = false,
                typeApproximator,
                languageVersionSettings,
            )

        @JvmStatic
        fun createForLambda(
            session: FirSession,
            scopeSession: ScopeSession,
            extension: FirSerializerExtension,
            typeApproximator: AbstractTypeApproximator,
            languageVersionSettings: LanguageVersionSettings,
        ): FirElementSerializer =
            FirElementSerializer(
                session, scopeSession, null,
                Interner(), extension, MutableTypeTable(),
                versionRequirementTable = null, serializeTypeTableToFunction = true,
                typeApproximator,
                languageVersionSettings,
            )

        @JvmStatic
        fun create(
            session: FirSession,
            scopeSession: ScopeSession,
            klass: FirClass,
            extension: FirSerializerExtension,
            parentSerializer: FirElementSerializer?,
            typeApproximator: AbstractTypeApproximator,
            languageVersionSettings: LanguageVersionSettings,
        ): FirElementSerializer {
            konst parentClassId = klass.symbol.classId.outerClassId
            konst parent = if (parentClassId != null && !parentClassId.isLocal) {
                konst parentClass = session.symbolProvider.getClassLikeSymbolByClassId(parentClassId)!!.fir as FirRegularClass
                parentSerializer ?: create(
                    session, scopeSession, parentClass, extension, null, typeApproximator,
                    languageVersionSettings,
                )
            } else {
                createTopLevel(session, scopeSession, extension, typeApproximator, languageVersionSettings)
            }

            // Calculate type parameter ids for the outer class beforehand, as it would've had happened if we were always
            // serializing outer classes before nested classes.
            // Otherwise our interner can get wrong ids because we may serialize classes in any order.
            konst serializer = FirElementSerializer(
                session,
                scopeSession,
                klass,
                Interner(parent.typeParameters),
                extension,
                MutableTypeTable(),
                if (parentClassId != null && !isKotlin1Dot4OrLater(extension.metadataVersion)) {
                    parent.versionRequirementTable
                } else {
                    MutableVersionRequirementTable()
                },
                serializeTypeTableToFunction = false,
                typeApproximator,
                languageVersionSettings,
            )
            for (typeParameter in klass.typeParameters) {
                if (typeParameter !is FirTypeParameter) continue
                serializer.typeParameters.intern(typeParameter)
            }
            return serializer
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
