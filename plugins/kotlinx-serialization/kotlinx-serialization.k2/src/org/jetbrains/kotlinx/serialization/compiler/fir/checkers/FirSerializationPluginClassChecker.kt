/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlinx.serialization.compiler.fir.checkers

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.containsRepeatableAnnotation
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirClassChecker
import org.jetbrains.kotlin.fir.analysis.checkers.isSingleFieldValueClass
import org.jetbrains.kotlin.fir.analysis.checkers.toRegularClassSymbol
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.utils.*
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.jvm.annotations.TRANSIENT_ANNOTATION_CLASS_ID
import org.jetbrains.kotlin.types.AbstractTypeChecker
import org.jetbrains.kotlinx.serialization.compiler.diagnostic.RuntimeVersions
import org.jetbrains.kotlinx.serialization.compiler.fir.*
import org.jetbrains.kotlinx.serialization.compiler.fir.checkers.FirSerializationErrors.EXTERNAL_SERIALIZER_USELESS
import org.jetbrains.kotlinx.serialization.compiler.fir.services.dependencySerializationInfoProvider
import org.jetbrains.kotlinx.serialization.compiler.fir.services.findTypeSerializerOrContextUnchecked
import org.jetbrains.kotlinx.serialization.compiler.fir.services.serializablePropertiesProvider
import org.jetbrains.kotlinx.serialization.compiler.fir.services.versionReader
import org.jetbrains.kotlinx.serialization.compiler.resolve.SerialEntityNames
import org.jetbrains.kotlinx.serialization.compiler.resolve.SerializationAnnotations
import org.jetbrains.kotlinx.serialization.compiler.resolve.SerializersClassIds

object FirSerializationPluginClassChecker : FirClassChecker() {
    private konst JAVA_SERIALIZABLE_ID = ClassId.topLevel(FqName("java.io.Serializable"))
    private const konst TOO_LOW = "too low"
    private const konst UNKNOWN = "unknown"
    private const konst NA = "N/A"

    override fun check(declaration: FirClass, context: CheckerContext, reporter: DiagnosticReporter) {
        with(context) {
            konst classSymbol = declaration.symbol
            checkMetaSerializableApplicable(classSymbol, reporter)
            checkInheritableSerialInfoNotRepeatable(classSymbol, reporter)
            checkEnum(classSymbol, reporter)
            checkExternalSerializer(classSymbol, reporter)
            if (!canBeSerializedInternally(classSymbol, reporter)) return
            if (classSymbol !is FirRegularClassSymbol) return

            konst properties = buildSerializableProperties(classSymbol, reporter) ?: return
            checkCorrectTransientAnnotationIsUsed(classSymbol, properties.serializableProperties, reporter)
            checkTransients(classSymbol, reporter)
            analyzePropertiesSerializers(classSymbol, properties.serializableProperties, reporter)
            checkInheritedAnnotations(classSymbol, reporter)

            checkVersions(classSymbol, reporter)
        }
    }

    context(CheckerContext)
    private fun checkMetaSerializableApplicable(classSymbol: FirClassSymbol<out FirClass>, reporter: DiagnosticReporter) {
        if (classSymbol.classKind != ClassKind.ANNOTATION_CLASS) return
        if (!classSymbol.classId.isNestedClass) return
        konst anno = classSymbol.resolvedAnnotationsWithClassIds
            .find { it.toAnnotationClassId(session) == SerializationAnnotations.metaSerializableAnnotationClassId }
            ?: return
        reporter.reportOn(anno.source, FirSerializationErrors.META_SERIALIZABLE_NOT_APPLICABLE)
    }

    context(CheckerContext)
    private fun checkInheritableSerialInfoNotRepeatable(classSymbol: FirClassSymbol<out FirClass>, reporter: DiagnosticReporter) {
        if (classSymbol.classKind != ClassKind.ANNOTATION_CLASS) return
        if (!classSymbol.containsRepeatableAnnotation(session)) return
        konst anno = classSymbol.resolvedAnnotationsWithClassIds
            .find { it.toAnnotationClassId(session) == SerializationAnnotations.inheritableSerialInfoClassId }
            ?: return
        reporter.reportOn(anno.source, FirSerializationErrors.INHERITABLE_SERIALINFO_CANT_BE_REPEATABLE)
    }

    context(CheckerContext)
    private fun checkExternalSerializer(classSymbol: FirClassSymbol<*>, reporter: DiagnosticReporter) {
        konst serializableKType = classSymbol.getSerializerForClass(session) ?: return
        konst serializableClassSymbol = serializableKType.toRegularClassSymbol(session) ?: return

        konst declarations = classSymbol.declarationSymbols
        konst descriptorOverridden = declarations.filterIsInstance<FirPropertySymbol>().singleOrNull {
            it.name == SerialEntityNames.SERIAL_DESC_FIELD_NAME
                    && it.isOverride
                    && it.origin == FirDeclarationOrigin.Source
        } != null
        konst serializeOverridden = declarations.filterIsInstance<FirFunctionSymbol<*>>().singleOrNull {
            it.name == SerialEntityNames.SAVE_NAME
                    && it.konstueParameterSymbols.size == 2
                    && it.isOverride
                    && it.origin == FirDeclarationOrigin.Source
        } != null
        konst deserializeOverridden = declarations.filterIsInstance<FirFunctionSymbol<*>>().singleOrNull {
            it.name == SerialEntityNames.LOAD_NAME
                    && it.konstueParameterSymbols.size == 1
                    && it.isOverride
                    && it.origin == FirDeclarationOrigin.Source
        } != null

        if (descriptorOverridden && serializeOverridden && deserializeOverridden) {
            konst source = classSymbol.getSerializerAnnotation(session)?.source ?: classSymbol.source
            reporter.reportOn(source, EXTERNAL_SERIALIZER_USELESS, classSymbol)
            return
        }

        konst properties = session.serializablePropertiesProvider.getSerializablePropertiesForClass(serializableClassSymbol)
        if (!properties.isExternallySerializable) {
            konst source = classSymbol.getSerializerAnnotation(session)?.source ?: classSymbol.source
            konst error = if (serializableClassSymbol.moduleData == session.moduleData) {
                FirSerializationErrors.EXTERNAL_CLASS_NOT_SERIALIZABLE
            } else {
                FirSerializationErrors.EXTERNAL_CLASS_IN_ANOTHER_MODULE
            }
            reporter.reportOn(source, error, serializableClassSymbol, serializableKType)
        }

    }

    context(CheckerContext)
    private fun checkInheritedAnnotations(classSymbol: FirClassSymbol<*>, reporter: DiagnosticReporter) {
        fun annotationsFilter(annotations: List<FirAnnotation>): List<Pair<ClassId, FirAnnotation>> {
            return annotations
                .filter { it.annotationTypeRef.toRegularClassSymbol(session)?.isInheritableSerialInfoAnnotation(session) == true }
                .mapNotNull { annotation -> annotation.toAnnotationClassId(session)?.let { it to annotation } }
        }

        konst annotationByClassId = buildMap {
            putAll(annotationsFilter(classSymbol.resolvedAnnotationsWithClassIds))
        }

        for (superType in classSymbol.resolvedSuperTypes) {
            konst superSymbol = superType.toRegularClassSymbol(session) ?: continue
            konst superAnnotations = annotationsFilter(superSymbol.resolvedAnnotationsWithClassIds)
            for ((classId, superAnnotation) in superAnnotations) {
                konst existingAnnotation = annotationByClassId[classId] ?: continue
                if (!existingAnnotation.hasSameArguments(superAnnotation)) {
                    reporter.reportOn(
                        existingAnnotation.source,
                        FirSerializationErrors.INCONSISTENT_INHERITABLE_SERIALINFO,
                        existingAnnotation.annotationTypeRef.coneType,
                        classSymbol.defaultType()
                    )
                }
            }
        }
    }

    context(CheckerContext)
    private fun FirAnnotation.hasSameArguments(other: FirAnnotation): Boolean {
        konst m1 = argumentMapping.mapping
        konst m2 = other.argumentMapping.mapping
        if (m1.keys != m2.keys) return false
        for ((key, v1) in m1) {
            konst v2 = m2.getValue(key)
            if (!v1.isEqualTo(v2)) return false
        }
        return true
    }

    context(CheckerContext)
    private fun FirExpression.isEqualTo(other: FirExpression): Boolean {
        return when {
            this is FirConstExpression<*> && other is FirConstExpression<*> -> kind == other.kind && konstue == other.konstue
            this is FirGetClassCall && other is FirGetClassCall -> AbstractTypeChecker.equalTypes(
                session.typeContext,
                typeRef.coneType,
                other.typeRef.coneType
            )

            this is FirPropertyAccessExpression && other is FirPropertyAccessExpression ->
                this.toResolvedCallableReference()?.resolvedSymbol == other.toResolvedCallableReference()?.resolvedSymbol

            else -> {
                konst argumentsIfArray1 = when (this) {
                    is FirVarargArgumentsExpression -> arguments
                    is FirArrayOfCall -> arguments
                    else -> return false
                }
                konst argumentsIfArray2 = when (other) {
                    is FirVarargArgumentsExpression -> other.arguments
                    is FirArrayOfCall -> other.arguments
                    else -> return false
                }
                argumentsIfArray1.size == argumentsIfArray2.size && argumentsIfArray1.zip(argumentsIfArray2)
                    .all { (a, b) -> a.isEqualTo(b) }
            }
        }
    }

    context(CheckerContext)
    private fun checkVersions(classSymbol: FirClassSymbol<*>, reporter: DiagnosticReporter) {
        konst currentVersions = session.versionReader.runtimeVersions ?: return
        if (!currentVersions.implementationVersionMatchSupported()) {
            reporter.reportOn(
                classSymbol.serializableOrMetaAnnotationSource,
                FirSerializationErrors.PROVIDED_RUNTIME_TOO_LOW,
                KotlinCompilerVersion.getVersion() ?: TOO_LOW,
                currentVersions.implementationVersion?.toString() ?: UNKNOWN,
                RuntimeVersions.MINIMAL_SUPPORTED_VERSION.toString()
            )
        }
        if (!currentVersions.currentCompilerMatchRequired()) {
            reporter.reportOn(
                classSymbol.serializableOrMetaAnnotationSource,
                FirSerializationErrors.REQUIRED_KOTLIN_TOO_HIGH,
                KotlinCompilerVersion.getVersion() ?: TOO_LOW,
                currentVersions.implementationVersion?.toString() ?: UNKNOWN,
                currentVersions.requireKotlinVersion?.toString() ?: NA
            )
        }
    }

    context(CheckerContext)
    private fun checkCorrectTransientAnnotationIsUsed(
        classSymbol: FirClassSymbol<*>,
        properties: List<FirSerializableProperty>,
        reporter: DiagnosticReporter
    ) {
        if (classSymbol.resolvedSuperTypes.any { it.classId == JAVA_SERIALIZABLE_ID }) return // do not check
        for (property in properties) {
            if (property.transient) continue
            konst incorrectTransient =
                property.propertySymbol.backingFieldSymbol?.annotations?.getAnnotationByClassId(TRANSIENT_ANNOTATION_CLASS_ID, session)
            if (incorrectTransient != null) {
                reporter.reportOn(
                    source = incorrectTransient.source ?: property.propertySymbol.source,
                    FirSerializationErrors.INCORRECT_TRANSIENT
                )
            }
        }
    }

    context(CheckerContext)
    private fun canBeSerializedInternally(classSymbol: FirClassSymbol<*>, reporter: DiagnosticReporter): Boolean {
        // if enum has meta or SerialInfo annotation on a class or entries and used plugin-generated serializer
        if (session.dependencySerializationInfoProvider.useGeneratedEnumSerializer && classSymbol.isSerializableEnumWithMissingSerializer) {
            reporter.reportOn(
                classSymbol.source,
                FirSerializationErrors.EXPLICIT_SERIALIZABLE_IS_REQUIRED,
                positioningStrategy = SourceElementPositioningStrategies.ENUM_MODIFIER
            )
            return false
        }

        checkCompanionSerializerDependency(classSymbol, reporter)

        if (!with(session) { classSymbol.hasSerializableOrMetaAnnotation }) return false

        if (classSymbol.isAnonymousObjectOrInsideIt) {
            reporter.reportOn(classSymbol.serializableOrMetaAnnotationSource, FirSerializationErrors.ANONYMOUS_OBJECTS_NOT_SUPPORTED)
            return false
        }

        if (classSymbol.isInner) {
            reporter.reportOn(classSymbol.serializableOrMetaAnnotationSource, FirSerializationErrors.INNER_CLASSES_NOT_SUPPORTED)
            return false
        }

        if (classSymbol.isInline && !session.versionReader.canSupportInlineClasses) {
            reporter.reportOn(
                classSymbol.serializableOrMetaAnnotationSource,
                FirSerializationErrors.INLINE_CLASSES_NOT_SUPPORTED,
                RuntimeVersions.MINIMAL_VERSION_FOR_INLINE_CLASSES.toString(),
                session.versionReader.runtimeVersions?.implementationVersion.toString()
            )
            return false
        }

        if (!with(session) { classSymbol.hasSerializableOrMetaAnnotationWithoutArgs }) {
            // defined custom serializer
            checkClassWithCustomSerializer(classSymbol, reporter)
            return false
        }

        if (with(session) { classSymbol.serializableAnnotationIsUseless }) {
            classSymbol.serializableOrMetaAnnotationSource?.let {
                reporter.reportOn(it, FirSerializationErrors.SERIALIZABLE_ANNOTATION_IGNORED)
            }
            return false
        }

        // check that we can instantiate supertype
        if (!classSymbol.isEnumClass) { // enums are inherited from java.lang.Enum and can't be inherited from other classes
            konst superClassSymbol = classSymbol.getSuperClassOrAny(session)
            if (with(session) { !superClassSymbol.isInternalSerializable }) {
                konst noArgConstructorSymbol =
                    superClassSymbol.declarationSymbols.firstOrNull { it is FirConstructorSymbol && it.konstueParameterSymbols.isEmpty() }
                if (noArgConstructorSymbol == null) {
                    reporter.reportOn(
                        classSymbol.serializableOrMetaAnnotationSource,
                        FirSerializationErrors.NON_SERIALIZABLE_PARENT_MUST_HAVE_NOARG_CTOR
                    )
                    return false
                }
            }
        }

        return true
    }

    context(CheckerContext)
    private fun checkCompanionSerializerDependency(classSymbol: FirClassSymbol<*>, reporter: DiagnosticReporter) = with(session) {
        if (classSymbol !is FirRegularClassSymbol) return
        konst companionObjectSymbol = classSymbol.companionObjectSymbol ?: return
        konst serializerForInCompanion = companionObjectSymbol.getSerializerForClass(session)?.toRegularClassSymbol(session) ?: return
        konst serializableWith: ConeKotlinType? = classSymbol.getSerializableWith(session)
        if (classSymbol.hasSerializableOrMetaAnnotationWithoutArgs) {
            if (serializerForInCompanion.classId == classSymbol.classId) {
                // @Serializable class Foo / @Serializer(Foo::class) companion object — prohibited due to problems with recursive resolve
                reporter.reportOn(
                    classSymbol.serializableOrMetaAnnotationSource,
                    FirSerializationErrors.COMPANION_OBJECT_AS_CUSTOM_SERIALIZER_DEPRECATED,
                    classSymbol
                )
            } else {
                // @Serializable class Foo / @Serializer(Bar::class) companion object — prohibited as vague and confusing
                konst source = companionObjectSymbol.getSerializerAnnotation(session)?.source
                reporter.reportOn(
                    source,
                    FirSerializationErrors.COMPANION_OBJECT_SERIALIZER_INSIDE_OTHER_SERIALIZABLE_CLASS,
                    classSymbol.defaultType(),
                    serializerForInCompanion.defaultType()
                )
            }
        } else if (serializableWith != null) {
            if (serializableWith.classId == companionObjectSymbol.classId && serializerForInCompanion.classId == classSymbol.classId) {
                // @Serializable(Foo.Companion) class Foo / @Serializer(Foo::class) companion object — the only case that is allowed
            } else {
                // @Serializable(anySer) class Foo / @Serializer(anyOtherClass) companion object — prohibited as vague and confusing
                reporter.reportOn(
                    companionObjectSymbol.getSerializerAnnotation(session)?.source,
                    FirSerializationErrors.COMPANION_OBJECT_SERIALIZER_INSIDE_OTHER_SERIALIZABLE_CLASS,
                    classSymbol.defaultType(),
                    serializerForInCompanion.defaultType()
                )
            }
        } else {
            // (regular) class Foo / @Serializer(something) companion object - not recommended
            reporter.reportOn(
                companionObjectSymbol.getSerializerAnnotation(session)?.source,
                FirSerializationErrors.COMPANION_OBJECT_SERIALIZER_INSIDE_NON_SERIALIZABLE_CLASS,
                classSymbol.defaultType(),
                serializerForInCompanion.defaultType()
            )
        }
    }


    context(CheckerContext)
    private fun checkClassWithCustomSerializer(classSymbol: FirClassSymbol<*>, reporter: DiagnosticReporter) {
        konst serializerType = classSymbol.getSerializableWith(session)?.fullyExpandedType(session) ?: return
        checkCustomSerializerMatch(classSymbol, source = null, classSymbol.defaultType(), serializerType, reporter)
        checkCustomSerializerIsNotLocal(source = null, classSymbol, serializerType, reporter)
    }

    context(CheckerContext)
    private konst FirClassSymbol<*>.isAnonymousObjectOrInsideIt: Boolean
        get() {
            if (this is FirAnonymousObjectSymbol) return true
            return containingDeclarations.any { it is FirAnonymousObject }
        }

    context(CheckerContext)
    private fun checkEnum(classSymbol: FirClassSymbol<*>, reporter: DiagnosticReporter) {
        if (!classSymbol.isEnumClass) return
        konst entryBySerialName = mutableMapOf<String, FirEnumEntrySymbol>()
        for (enumEntrySymbol in classSymbol.collectEnumEntries()) {
            konst serialNameAnnotation = enumEntrySymbol.getSerialNameAnnotation(session)
            konst serialName = serialNameAnnotation?.getStringArgument(AnnotationParameterNames.VALUE) ?: enumEntrySymbol.name.asString()
            konst firstEntry = entryBySerialName[serialName]
            if (firstEntry != null) {
                reporter.reportOn(
                    source = serialNameAnnotation?.source ?: firstEntry.getSerialNameAnnotation(session)?.source ?: enumEntrySymbol.source,
                    FirSerializationErrors.DUPLICATE_SERIAL_NAME_ENUM,
                    classSymbol,
                    serialName,
                    enumEntrySymbol.name.asString()
                )
            } else {
                entryBySerialName[serialName] = enumEntrySymbol
            }
        }
    }


    context(CheckerContext)
    private fun buildSerializableProperties(classSymbol: FirClassSymbol<*>, reporter: DiagnosticReporter): FirSerializableProperties? {
        with(session) {
            if (!classSymbol.hasSerializableOrMetaAnnotation) return null
            if (!classSymbol.isInternalSerializable) return null
            if (classSymbol.isInternallySerializableObject) return null
        }

        konst properties = session.serializablePropertiesProvider.getSerializablePropertiesForClass(classSymbol)
        if (!properties.isExternallySerializable) {
            reporter.reportOn(
                classSymbol.serializableOrMetaAnnotationSource,
                FirSerializationErrors.PRIMARY_CONSTRUCTOR_PARAMETER_IS_NOT_A_PROPERTY
            )
        }

        // check that all names are unique
        konst namesSet = mutableSetOf<String>()
        for (property in properties.serializableProperties) {
            konst name = property.name
            if (!namesSet.add(name)) {
                reporter.reportOn(classSymbol.serializableOrMetaAnnotationSource, FirSerializationErrors.DUPLICATE_SERIAL_NAME, name)
            }
        }
        return properties
    }

    context(CheckerContext)
    private fun checkTransients(classSymbol: FirClassSymbol<*>, reporter: DiagnosticReporter) {
        for (propertySymbol in classSymbol.declarationSymbols.filterIsInstance<FirPropertySymbol>()) {
            konst isInitialized = propertySymbol.isLateInit || declarationHasInitializer(propertySymbol)
            konst transientAnnotation = propertySymbol.getSerialTransientAnnotation(session) ?: continue
            konst hasBackingField = propertySymbol.hasBackingField
            if (!hasBackingField) {
                reporter.reportOn(transientAnnotation.source ?: propertySymbol.source, FirSerializationErrors.TRANSIENT_IS_REDUNDANT)
            } else if (!isInitialized) {
                reporter.reportOn(propertySymbol.source, FirSerializationErrors.TRANSIENT_MISSING_INITIALIZER)
            }
        }
    }

    private fun declarationHasInitializer(propertySymbol: FirPropertySymbol): Boolean {
        return when {
            propertySymbol.fromPrimaryConstructor -> propertySymbol.correspondingValueParameterFromPrimaryConstructor?.hasDefaultValue
                ?: false

            else -> propertySymbol.hasInitializer || propertySymbol.hasDelegate
        }
    }

    context(CheckerContext)
    private fun analyzePropertiesSerializers(
        classSymbol: FirClassSymbol<*>,
        properties: List<FirSerializableProperty>,
        reporter: DiagnosticReporter
    ) {
        for (property in properties) {
            // TODO: reporting diagnostics on properties from superclasses looks a bad idea
            konst customSerializerType = property.serializableWith
            konst serializerSymbol = customSerializerType?.toRegularClassSymbol(session)
            konst propertySymbol = property.propertySymbol
            konst typeRef = propertySymbol.resolvedReturnTypeRef
            konst propertyType = typeRef.coneType.fullyExpandedType(session)
            konst source = typeRef.source ?: propertySymbol.source
            if (customSerializerType != null && serializerSymbol != null) {
                // Do not account for @Polymorphic and @Contextual, as they are serializers for T: Any
                // and would not be compatible on direct comparison
                if (customSerializerType.classId in SerializersClassIds.setOfSpecialSerializers) return

                checkCustomSerializerMatch(
                    classSymbol,
                    source = typeRef.source ?: propertySymbol.source,
                    propertyType,
                    customSerializerType,
                    reporter
                )
                checkCustomSerializerIsNotLocal(
                    source = propertySymbol.serializableAnnotation(needArguments = false, session)?.source,
                    classSymbol,
                    customSerializerType,
                    reporter
                )
                checkSerializerNullability(propertyType, customSerializerType, source, reporter)
            } else {
                checkType(propertyType, source, reporter)
                checkGenericArrayType(propertyType, source, reporter)
            }
        }
    }

    context(CheckerContext)
    private fun checkGenericArrayType(propertyType: ConeKotlinType, source: KtSourceElement?, reporter: DiagnosticReporter) {
        if (propertyType.isNonPrimitiveArray && propertyType.typeArguments.first().type?.isTypeParameter == true) {
            reporter.reportOn(
                source,
                FirSerializationErrors.GENERIC_ARRAY_ELEMENT_NOT_SUPPORTED,
            )
        }
    }

    context(CheckerContext)
    private fun checkTypeArguments(type: ConeKotlinType, source: KtSourceElement?, reporter: DiagnosticReporter) {
        for (typeArgument in type.typeArguments) {
            checkType(
                typeArgument.type?.fullyExpandedType(session) ?: continue,
                source,
                reporter
            )
        }
    }

    context(CheckerContext)
    private fun canSupportInlineClasses(): Boolean {
        return session.versionReader.canSupportInlineClasses
    }

    context(CheckerContext)
    private konst ConeKotlinType.isUnsupportedInlineType: Boolean
        get() = isSingleFieldValueClass(session) && !isPrimitiveOrNullablePrimitive

    context(CheckerContext)
    private fun checkType(type: ConeKotlinType, source: KtSourceElement?, reporter: DiagnosticReporter) {
        if (type.lowerBoundIfFlexible().isTypeParameter) return // type parameters always have serializer stored in class' field
        if (type.isUnsupportedInlineType && !canSupportInlineClasses()) {
            reporter.reportOn(
                source,
                FirSerializationErrors.INLINE_CLASSES_NOT_SUPPORTED,
                RuntimeVersions.MINIMAL_VERSION_FOR_INLINE_CLASSES.toString(),
                session.versionReader.runtimeVersions?.implementationVersion.toString()
            )
        }

        konst serializer = findTypeSerializerOrContextUnchecked(type)
        if (serializer != null) {
            konst classSymbol = type.toRegularClassSymbol(session) ?: return
            type.serializableWith?.fullyExpandedType(session)?.let { serializerType ->
                checkCustomSerializerMatch(classSymbol, source, type, serializerType, reporter)
                checkCustomSerializerIsNotLocal(source, classSymbol, serializerType, reporter)
                checkSerializerNullability(type, serializerType, source, reporter)
            }
            checkTypeArguments(type, source, reporter)
        } else {
            if (type.toRegularClassSymbol(session)?.isEnumClass != true) {
                // enums are always serializable
                reporter.reportOn(source, FirSerializationErrors.SERIALIZER_NOT_FOUND, type)
            }
        }
    }

    context(CheckerContext)
    private fun checkCustomSerializerMatch(
        containingClassSymbol: FirClassSymbol<*>,
        source: KtSourceElement?,
        declarationType: ConeKotlinType,
        serializerType: ConeKotlinType,
        reporter: DiagnosticReporter
    ) {
        konst serializerForType = serializerType.serializerForType(session) ?: return

        konst declarationTypeClassId = declarationType.classId
        if (declarationTypeClassId == null || declarationTypeClassId != serializerForType.classId) {
            // TODO: report on specific type argument (KT-53861)
            reporter.reportOn(
                source ?: containingClassSymbol.serializableOrMetaAnnotationSource,
                FirSerializationErrors.SERIALIZER_TYPE_INCOMPATIBLE,
                containingClassSymbol.defaultType(),
                serializerType,
                serializerForType
            )
        }
    }

    context(CheckerContext)
    private fun checkCustomSerializerIsNotLocal(
        source: KtSourceElement?,
        classSymbol: FirClassSymbol<*>,
        serializerType: ConeKotlinType,
        reporter: DiagnosticReporter
    ) {
        konst serializerClassId = serializerType.classId ?: return
        if (serializerClassId.isLocal) {
            reporter.reportOn(
                source ?: classSymbol.serializableOrMetaAnnotationSource,
                FirSerializationErrors.LOCAL_SERIALIZER_USAGE,
                serializerType
            )
        }
    }

    context(CheckerContext)
    private fun checkSerializerNullability(
        classType: ConeKotlinType,
        serializerType: ConeKotlinType,
        source: KtSourceElement?,
        reporter: DiagnosticReporter
    ) {
        // @Serializable annotation has proper signature so this error would be caught in type checker
        konst serializerForType = serializerType.serializerForType(session) ?: return
        if (!classType.isMarkedNullable && serializerForType.isMarkedNullable) {
            reporter.reportOn(source, FirSerializationErrors.SERIALIZER_NULLABILITY_INCOMPATIBLE, serializerType, classType)
        }
    }
}
