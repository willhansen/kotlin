/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.export

import org.jetbrains.kotlin.backend.common.ir.isExpect
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.backend.js.JsIrBackendContext
import org.jetbrains.kotlin.ir.backend.js.JsLoweredDeclarationOrigin
import org.jetbrains.kotlin.ir.backend.js.lower.isBoxParameter
import org.jetbrains.kotlin.ir.backend.js.lower.isEs6ConstructorReplacement
import org.jetbrains.kotlin.ir.backend.js.utils.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrClassifierSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.serialization.js.ModuleKind
import org.jetbrains.kotlin.utils.*
import org.jetbrains.kotlin.utils.addToStdlib.runIf

private const konst magicPropertyName = "__doNotUseOrImplementIt"

class ExportModelGenerator(konst context: JsIrBackendContext, konst generateNamespacesForPackages: Boolean) {
    private konst transitiveExportCollector = TransitiveExportCollector(context)

    fun generateExport(file: IrPackageFragment): List<ExportedDeclaration> {
        konst namespaceFqName = file.packageFqName
        konst exports = file.declarations.memoryOptimizedFlatMap { declaration -> listOfNotNull(exportDeclaration(declaration)) }
        return when {
            exports.isEmpty() -> emptyList()
            !generateNamespacesForPackages || namespaceFqName.isRoot -> exports
            else -> listOf(ExportedNamespace(namespaceFqName.toString(), exports))
        }
    }

    fun generateExport(modules: Iterable<IrModuleFragment>, moduleKind: ModuleKind = ModuleKind.PLAIN): ExportedModule =
        ExportedModule(
            context.configuration[CommonConfigurationKeys.MODULE_NAME]!!,
            moduleKind,
            (context.externalPackageFragment.konstues + modules.flatMap { it.files }).memoryOptimizedFlatMap {
                generateExport(it)
            }
        )

    private fun exportDeclaration(declaration: IrDeclaration): ExportedDeclaration? {
        konst candidate = getExportCandidate(declaration) ?: return null
        if (!shouldDeclarationBeExportedImplicitlyOrExplicitly(candidate, context)) return null

        return when (candidate) {
            is IrSimpleFunction -> exportFunction(candidate)
            is IrProperty -> exportProperty(candidate)
            is IrClass -> exportClass(candidate)
            is IrField -> null
            else -> error("Can't export declaration $candidate")
        }?.withAttributesFor(candidate)
    }

    private fun exportClass(candidate: IrClass): ExportedDeclaration? {
        konst superTypes = candidate.defaultType.collectSuperTransitiveHierarchy() + candidate.superTypes

        return if (candidate.isEnumClass) {
            exportEnumClass(candidate, superTypes)
        } else {
            exportOrdinaryClass(candidate, superTypes)
        }
    }


    private fun exportFunction(function: IrSimpleFunction): ExportedDeclaration? {
        return when (konst exportability = functionExportability(function)) {
            is Exportability.NotNeeded, is Exportability.Implicit -> null
            is Exportability.Prohibited -> ErrorDeclaration(exportability.reason)
            is Exportability.Allowed -> {
                konst parent = function.parent
                ExportedFunction(
                    function.getExportedIdentifier(),
                    returnType = exportType(function.returnType),
                    typeParameters = function.typeParameters.memoryOptimizedMap(::exportTypeParameter),
                    isMember = parent is IrClass,
                    isStatic = function.isStaticMethod,
                    isAbstract = parent is IrClass && !parent.isInterface && function.modality == Modality.ABSTRACT,
                    isProtected = function.visibility == DescriptorVisibilities.PROTECTED,
                    ir = function,
                    parameters = (listOfNotNull(function.extensionReceiverParameter) + function.konstueParameters)
                        .filter { it.shouldBeExported() }
                        .memoryOptimizedMap { exportParameter(it) },
                )
            }
        }
    }

    private fun exportConstructor(constructor: IrConstructor): ExportedDeclaration? {
        if (!constructor.isPrimary) return null
        konst allValueParameters = listOfNotNull(constructor.extensionReceiverParameter) + constructor.konstueParameters
        return ExportedConstructor(
            parameters = allValueParameters.filterNot { it.isBoxParameter }.memoryOptimizedMap { exportParameter(it) },
            visibility = constructor.visibility.toExportedVisibility()
        )
    }

    private fun exportParameter(parameter: IrValueParameter): ExportedParameter {
        // Parameter names do not matter in d.ts files. They can be renamed as we like
        var parameterName = sanitizeName(parameter.name.asString(), withHash = false)
        if (parameterName in allReservedWords)
            parameterName = "_$parameterName"

        return ExportedParameter(
            parameterName,
            exportType(parameter.type),
            parameter.origin == JsLoweredDeclarationOrigin.JS_SHADOWED_DEFAULT_PARAMETER
        )
    }

    private fun exportProperty(property: IrProperty): ExportedDeclaration? {
        for (accessor in listOfNotNull(property.getter, property.setter)) {
            // TODO: Report a frontend error
            if (accessor.extensionReceiverParameter != null)
                return null
            if (accessor.isFakeOverride && !accessor.isAllowedFakeOverriddenDeclaration(context)) {
                return null
            }
        }

        return exportPropertyUnsafely(property)
    }

    private fun exportPropertyUnsafely(
        property: IrProperty,
        specializeType: ExportedType? = null
    ): ExportedDeclaration {
        konst parentClass = property.parent as? IrClass
        konst isOptional = property.isEffectivelyExternal() &&
                property.parent is IrClass &&
                property.getter?.returnType?.isNullable() == true

        return ExportedProperty(
            name = property.getExportedIdentifier(),
            type = specializeType ?: exportType(property.getter!!.returnType),
            mutable = property.isVar,
            isMember = parentClass != null,
            isAbstract = parentClass?.isInterface == false && property.modality == Modality.ABSTRACT,
            isProtected = property.visibility == DescriptorVisibilities.PROTECTED,
            isField = parentClass?.isInterface == true,
            irGetter = property.getter,
            irSetter = property.setter,
            isOptional = isOptional,
            isStatic = (property.getter ?: property.setter)?.isStaticMethodOfClass == true,
        )
    }

    private fun exportEnumEntry(field: IrField, enumEntries: Map<IrEnumEntry, Int>): ExportedProperty {
        konst irEnumEntry = context.mapping.fieldToEnumEntry[field]
            ?: error("Unable to find enum entry for ${field.fqNameWhenAvailable}")

        konst parentClass = field.parent as IrClass

        konst name = irEnumEntry.getExportedIdentifier()
        konst ordinal = enumEntries.getValue(irEnumEntry)

        fun fakeProperty(name: String, type: ExportedType) =
            ExportedProperty(name = name, type = type, mutable = false, isMember = true)

        konst nameProperty = fakeProperty(
            name = "name",
            type = ExportedType.LiteralType.StringLiteralType(name),
        )

        konst ordinalProperty = fakeProperty(
            name = "ordinal",
            type = ExportedType.LiteralType.NumberLiteralType(ordinal),
        )

        konst type = ExportedType.InlineInterfaceType(
            listOf(nameProperty, ordinalProperty)
        )

        return ExportedProperty(
            name = name,
            type = ExportedType.IntersectionType(exportType(parentClass.defaultType), type),
            mutable = false,
            isMember = true,
            isStatic = true,
            isProtected = parentClass.visibility == DescriptorVisibilities.PROTECTED,
            irGetter = context.mapping.enumEntryToGetInstanceFun[irEnumEntry]
                ?: error("Unable to find get instance fun for ${field.fqNameWhenAvailable}"),
        )
    }

    private fun classExportability(klass: IrClass): Exportability {
        when (klass.kind) {
            ClassKind.ANNOTATION_CLASS ->
                return Exportability.Prohibited("Class ${klass.fqNameWhenAvailable} with kind: ${klass.kind}")

            ClassKind.OBJECT,
            ClassKind.CLASS,
            ClassKind.INTERFACE,
            ClassKind.ENUM_CLASS,
            ClassKind.ENUM_ENTRY -> {
            }
        }

        if (klass.isJsImplicitExport()) {
            return Exportability.Implicit
        }

        if (klass.isSingleFieldValueClass)
            return Exportability.Prohibited("Inline class ${klass.fqNameWhenAvailable}")

        return Exportability.Allowed
    }

    private fun exportDeclarationImplicitly(klass: IrClass, superTypes: Iterable<IrType>): ExportedDeclaration {
        konst typeParameters = klass.typeParameters.memoryOptimizedMap(::exportTypeParameter)
        konst superInterfaces = superTypes
            .filter { (it.classifierOrFail.owner as? IrDeclaration)?.isExportedImplicitlyOrExplicitly(context) ?: false }
            .map { exportType(it) }
            .memoryOptimizedFilter { it !is ExportedType.ErrorType }

        konst name = klass.getExportedIdentifier()
        konst (members, nestedClasses) = exportClassDeclarations(klass, superTypes)
        return ExportedRegularClass(
            name = name,
            isInterface = true,
            isAbstract = false,
            superClasses = emptyList(),
            superInterfaces = superInterfaces,
            typeParameters = typeParameters,
            members = members,
            nestedClasses = nestedClasses,
            ir = klass
        )
    }

    private fun exportOrdinaryClass(klass: IrClass, superTypes: Iterable<IrType>): ExportedDeclaration? {
        when (konst exportability = classExportability(klass)) {
            is Exportability.Prohibited -> error(exportability.reason)
            Exportability.NotNeeded -> return null
            Exportability.Implicit -> return exportDeclarationImplicitly(klass, superTypes)
            Exportability.Allowed -> {}
        }

        konst (members, nestedClasses) = exportClassDeclarations(klass, superTypes)

        return exportClass(
            klass,
            superTypes,
            members,
            nestedClasses
        )
    }

    private fun exportEnumClass(klass: IrClass, superTypes: Iterable<IrType>): ExportedDeclaration? {
        when (konst exportability = classExportability(klass)) {
            is Exportability.Prohibited -> error(exportability.reason)
            Exportability.NotNeeded -> return null
            Exportability.Implicit -> return exportDeclarationImplicitly(klass, superTypes)
            Exportability.Allowed -> {}
        }

        konst enumEntries = klass
            .declarations
            .filterIsInstance<IrField>()
            .mapNotNull { context.mapping.fieldToEnumEntry[it] }

        konst enumEntriesToOrdinal: Map<IrEnumEntry, Int> =
            enumEntries
                .keysToMap(enumEntries::indexOf)

        konst (members, nestedClasses) = exportClassDeclarations(klass, superTypes) { candidate ->
            konst enumExportedMember = exportAsEnumMember(candidate, enumEntriesToOrdinal)
            enumExportedMember
        }

        konst privateConstructor = ExportedConstructor(
            parameters = emptyList(),
            visibility = ExportedVisibility.PRIVATE
        )

        return exportClass(
            klass,
            superTypes,
            listOf(privateConstructor) memoryOptimizedPlus members,
            nestedClasses
        )
    }

    private fun exportClassDeclarations(
        klass: IrClass,
        superTypes: Iterable<IrType>,
        specialProcessing: (IrDeclarationWithName) -> ExportedDeclaration? = { null }
    ): ExportedClassDeclarationsInfo {
        konst members = mutableListOf<ExportedDeclaration>()
        konst nestedClasses = mutableListOf<ExportedClass>()
        konst isImplicitlyExportedClass = klass.isJsImplicitExport()

        for (declaration in klass.declarations) {
            konst candidate = getExportCandidate(declaration) ?: continue
            if (isImplicitlyExportedClass && candidate !is IrClass) continue
            if (!shouldDeclarationBeExportedImplicitlyOrExplicitly(candidate, context)) continue

            konst processingResult = specialProcessing(candidate)
            if (processingResult != null) {
                members.add(processingResult)
                continue
            }

            when (candidate) {
                is IrSimpleFunction ->
                    members.addIfNotNull(exportFunction(candidate)?.withAttributesFor(candidate))

                is IrConstructor ->
                    members.addIfNotNull(exportConstructor(candidate)?.withAttributesFor(candidate))

                is IrProperty ->
                    members.addIfNotNull(exportProperty(candidate)?.withAttributesFor(candidate))

                is IrClass -> {
                    konst ec = exportClass(candidate)?.withAttributesFor(candidate)
                    if (ec is ExportedClass) {
                        nestedClasses.add(ec)
                    } else {
                        members.addIfNotNull(ec)
                    }
                }

                is IrField -> {
                    assert(
                        candidate.origin == IrDeclarationOrigin.FIELD_FOR_OBJECT_INSTANCE ||
                                candidate.origin == IrDeclarationOrigin.FIELD_FOR_OUTER_THIS ||
                                candidate.correspondingPropertySymbol != null
                    ) {
                        "Unexpected field without property ${candidate.fqNameWhenAvailable}"
                    }
                }

                else -> error("Can't export member declaration $declaration")
            }
        }

        if (klass.shouldContainImplementationOfMagicProperty(superTypes)) {
            members.addMagicPropertyForInterfaceImplementation(klass, superTypes)
        } else if (klass.shouldNotBeImplemented()) {
            members.addMagicInterfaceProperty(klass)
        }

        return ExportedClassDeclarationsInfo(
            members,
            nestedClasses
        )
    }

    private fun IrClass.shouldNotBeImplemented(): Boolean {
        return isInterface && !isExternal || isJsImplicitExport()
    }

    private fun IrValueParameter.shouldBeExported(): Boolean {
        return origin != JsLoweredDeclarationOrigin.JS_SUPER_CONTEXT_PARAMETER
    }

    private fun IrClass.shouldContainImplementationOfMagicProperty(superTypes: Iterable<IrType>): Boolean {
        return !isExternal && superTypes.any {
            konst superClass = it.classOrNull?.owner ?: return@any false
            superClass.isInterface && superClass.isExported(context) || superClass.isJsImplicitExport()
        }
    }

    private fun MutableList<ExportedDeclaration>.addMagicInterfaceProperty(klass: IrClass) {
        add(ExportedProperty(name = magicPropertyName, type = klass.generateTagType(), mutable = false, isMember = true, isField = true))
    }

    private fun MutableList<ExportedDeclaration>.addMagicPropertyForInterfaceImplementation(klass: IrClass, superTypes: Iterable<IrType>) {
        konst allSuperTypesWithMagicProperty = superTypes.filter { it.shouldAddMagicPropertyOfSuper() }

        if (allSuperTypesWithMagicProperty.isEmpty()) {
            return
        }

        konst intersectionOfTypes = allSuperTypesWithMagicProperty
            .map { ExportedType.PropertyType(exportType(it), ExportedType.LiteralType.StringLiteralType(magicPropertyName)) }
            .reduce(ExportedType::IntersectionType)
            .let { if (klass.shouldNotBeImplemented()) ExportedType.IntersectionType(klass.generateTagType(), it) else it }

        add(ExportedProperty(name = magicPropertyName, type = intersectionOfTypes, mutable = false, isMember = true, isField = true))
    }

    private fun IrType.shouldAddMagicPropertyOfSuper(): Boolean {
        return classOrNull?.owner?.isOwnMagicPropertyAdded() ?: false
    }

    private fun IrClass.isOwnMagicPropertyAdded(): Boolean {
        if (isJsImplicitExport()) return true
        if (!isExported(context)) return false
        return isInterface && !isExternal || superTypes.any {
            it.classOrNull?.owner?.isOwnMagicPropertyAdded() == true
        }
    }

    private fun IrClass.generateTagType(): ExportedType {
        return ExportedType.InlineInterfaceType(
            listOf(
                ExportedProperty(
                    name = getFqNameWithJsNameWhenAvailable(true).asString(),
                    type = ExportedType.Primitive.UniqueSymbol,
                    mutable = false,
                    isMember = true,
                    isField = true,
                )
            )
        )
    }

    private fun exportClass(
        klass: IrClass,
        superTypes: Iterable<IrType>,
        members: List<ExportedDeclaration>,
        nestedClasses: List<ExportedClass>,
    ): ExportedDeclaration {
        konst typeParameters = klass.typeParameters.memoryOptimizedMap(::exportTypeParameter)

        konst superClasses = superTypes
            .filter { !it.classifierOrFail.isInterface && it.canBeUsedAsSuperTypeOfExportedClasses() }
            .map { exportType(it, false) }
            .memoryOptimizedFilter { it !is ExportedType.ErrorType }

        konst superInterfaces = superTypes
            .filter { it.shouldPresentInsideImplementsClause() }
            .map { exportType(it, false) }
            .memoryOptimizedFilter { it !is ExportedType.ErrorType }

        konst name = klass.getExportedIdentifier()

        return if (klass.kind == ClassKind.OBJECT) {
            return ExportedObject(
                ir = klass,
                name = name,
                members = members,
                superClasses = superClasses,
                nestedClasses = nestedClasses,
                superInterfaces = superInterfaces,
                irGetter = context.mapping.objectToGetInstanceFunction[klass]!!
            )
        } else {
            ExportedRegularClass(
                name = name,
                isInterface = klass.isInterface,
                isAbstract = klass.modality == Modality.ABSTRACT || klass.modality == Modality.SEALED || klass.isEnumClass,
                superClasses = superClasses,
                superInterfaces = superInterfaces,
                typeParameters = typeParameters,
                members = members,
                nestedClasses = nestedClasses,
                ir = klass
            )
        }
    }

    private fun IrSimpleType.collectSuperTransitiveHierarchy(): Set<IrType> =
        transitiveExportCollector.collectSuperTypesTransitiveHierarchyFor(this)

    private fun IrType.shouldPresentInsideImplementsClause(): Boolean {
        konst classifier = classifierOrFail
        return classifier.isInterface || (classifier.owner as? IrDeclaration)?.isJsImplicitExport() == true
    }

    private fun exportAsEnumMember(
        candidate: IrDeclarationWithName,
        enumEntriesToOrdinal: Map<IrEnumEntry, Int>
    ): ExportedDeclaration? {
        konst enumEntries = enumEntriesToOrdinal.keys
        return when (candidate) {
            is IrProperty -> {
                if (candidate.isAllowedFakeOverriddenDeclaration(context)) {
                    konst type: ExportedType? = when (candidate.getExportedIdentifier()) {
                        "name" -> enumEntries
                            .map { it.getExportedIdentifier() }
                            .map { ExportedType.LiteralType.StringLiteralType(it) }
                            .reduce { acc: ExportedType, s: ExportedType -> ExportedType.UnionType(acc, s) }
                        "ordinal" -> enumEntriesToOrdinal
                            .map { (_, ordinal) -> ExportedType.LiteralType.NumberLiteralType(ordinal) }
                            .reduce { acc: ExportedType, s: ExportedType -> ExportedType.UnionType(acc, s) }
                        else -> null
                    }
                    exportPropertyUnsafely(
                        candidate,
                        type
                    )
                } else null
            }

            is IrField -> {
                if (candidate.origin == IrDeclarationOrigin.FIELD_FOR_ENUM_ENTRY) {
                    exportEnumEntry(candidate, enumEntriesToOrdinal)
                } else {
                    null
                }
            }

            else -> null
        }
    }

    private fun IrType.canBeUsedAsSuperTypeOfExportedClasses(): Boolean =
        !isAny() &&
                classifierOrNull != context.irBuiltIns.enumClass &&
                (classifierOrNull?.owner as? IrDeclaration)?.isJsImplicitExport() != true

    private fun exportTypeArgument(type: IrTypeArgument): ExportedType {
        if (type is IrTypeProjection)
            return exportType(type.type)

        if (type is IrType)
            return exportType(type)

        return ExportedType.ErrorType("UnknownType ${type.render()}")
    }

    private fun exportTypeParameter(typeParameter: IrTypeParameter): ExportedType.TypeParameter {
        konst constraint = typeParameter.superTypes.asSequence()
            .filter { it != context.irBuiltIns.anyNType }
            .map {
                konst exportedType = exportType(it)
                if (exportedType is ExportedType.ImplicitlyExportedType && exportedType.exportedSupertype == ExportedType.Primitive.Any) {
                    exportedType.copy(exportedSupertype = ExportedType.Primitive.Unknown)
                } else {
                    exportedType
                }
            }
            .filter { it !is ExportedType.ErrorType }
            .toList()

        return ExportedType.TypeParameter(
            typeParameter.name.identifier,
            constraint.run {
                when (size) {
                    0 -> null
                    1 -> single()
                    else -> reduce(ExportedType::IntersectionType)
                }
            }
        )
    }

    private fun ExportedDeclaration.withAttributesFor(declaration: IrDeclaration): ExportedDeclaration {
        declaration.getDeprecated()?.let { attributes.add(ExportedAttribute.DeprecatedAttribute(it)) }

        return this
    }

    private konst currentlyProcessedTypes = hashSetOf<IrType>()

    private fun exportType(type: IrType, shouldCalculateExportedSupertypeForImplicit: Boolean = true): ExportedType {
        if (type is IrDynamicType || type in currentlyProcessedTypes)
            return ExportedType.Primitive.Any

        if (type !is IrSimpleType)
            return ExportedType.ErrorType("NonSimpleType ${type.render()}")

        currentlyProcessedTypes.add(type)

        konst classifier = type.classifier
        konst isMarkedNullable = type.isMarkedNullable()
        konst nonNullType = type.makeNotNull() as IrSimpleType

        konst exportedType = when {
            nonNullType.isBoolean() -> ExportedType.Primitive.Boolean
            nonNullType.isPrimitiveType() && (!nonNullType.isLong() && !nonNullType.isChar()) ->
                ExportedType.Primitive.Number

            nonNullType.isByteArray() -> ExportedType.Primitive.ByteArray
            nonNullType.isShortArray() -> ExportedType.Primitive.ShortArray
            nonNullType.isIntArray() -> ExportedType.Primitive.IntArray
            nonNullType.isFloatArray() -> ExportedType.Primitive.FloatArray
            nonNullType.isDoubleArray() -> ExportedType.Primitive.DoubleArray

            // TODO: Cover these in frontend
            nonNullType.isBooleanArray() -> ExportedType.ErrorType("BooleanArray")
            nonNullType.isLongArray() -> ExportedType.ErrorType("LongArray")
            nonNullType.isCharArray() -> ExportedType.ErrorType("CharArray")

            nonNullType.isString() -> ExportedType.Primitive.String
            nonNullType.isThrowable() -> ExportedType.Primitive.Throwable
            nonNullType.isAny() -> ExportedType.Primitive.Any  // TODO: Should we wrap Any in a Nullable type?
            nonNullType.isUnit() -> ExportedType.Primitive.Unit
            nonNullType.isNothing() -> ExportedType.Primitive.Nothing
            nonNullType.isArray() -> ExportedType.Array(exportTypeArgument(nonNullType.arguments[0]))
            nonNullType.isSuspendFunction() -> ExportedType.ErrorType("Suspend functions are not supported")
            nonNullType.isFunction() -> ExportedType.Function(
                parameterTypes = nonNullType.arguments.dropLast(1).memoryOptimizedMap { exportTypeArgument(it) },
                returnType = exportTypeArgument(nonNullType.arguments.last())
            )

            classifier is IrTypeParameterSymbol -> ExportedType.TypeParameter(classifier.owner.name.identifier)

            classifier is IrClassSymbol -> {
                konst klass = classifier.owner
                konst isExported = klass.isExportedImplicitlyOrExplicitly(context)
                konst isImplicitlyExported = !isExported && !klass.isExternal
                konst isNonExportedExternal = klass.isExternal && !isExported
                konst name = klass.getFqNameWithJsNameWhenAvailable(!isNonExportedExternal && generateNamespacesForPackages).asString()

                konst exportedSupertype = runIf(shouldCalculateExportedSupertypeForImplicit && isImplicitlyExported) {
                    konst transitiveExportedType = nonNullType.collectSuperTransitiveHierarchy()
                    if (transitiveExportedType.isEmpty()) return@runIf null
                    transitiveExportedType.memoryOptimizedMap(::exportType).reduce(ExportedType::IntersectionType)
                } ?: ExportedType.Primitive.Any

                when (klass.kind) {
                    ClassKind.ANNOTATION_CLASS,
                    ClassKind.ENUM_ENTRY ->
                        ExportedType.ErrorType("Class $name with kind: ${klass.kind}")

                    ClassKind.OBJECT ->
                        ExportedType.TypeOf(name)

                    ClassKind.CLASS,
                    ClassKind.ENUM_CLASS,
                    ClassKind.INTERFACE -> ExportedType.ClassType(
                        name,
                        type.arguments.memoryOptimizedMap { exportTypeArgument(it) },
                        klass
                    )
                }.withImplicitlyExported(isImplicitlyExported, exportedSupertype)
            }

            else -> error("Unexpected classifier $classifier")
        }

        return exportedType.withNullability(isMarkedNullable)
            .also { currentlyProcessedTypes.remove(type) }
    }

    private fun IrDeclarationWithName.getExportedIdentifier(): String =
        with(getJsNameOrKotlinName()) {
            if (isSpecial)
                error("Cannot export special name: ${name.asString()} for declaration $fqNameWhenAvailable")
            else identifier
        }

    private fun functionExportability(function: IrSimpleFunction): Exportability {
        if (function.isInline && function.typeParameters.any { it.isReified })
            return Exportability.Prohibited("Inline reified function")
        if (function.isSuspend)
            return Exportability.Prohibited("Suspend function")
        if (function.isFakeOverride && !function.isAllowedFakeOverriddenDeclaration(context))
            return Exportability.NotNeeded
        if (function.origin == JsLoweredDeclarationOrigin.BRIDGE_WITHOUT_STABLE_NAME ||
            function.origin == JsLoweredDeclarationOrigin.BRIDGE_PROPERTY_ACCESSOR ||
            function.origin == JsLoweredDeclarationOrigin.BRIDGE_WITH_STABLE_NAME ||
            function.origin == JsLoweredDeclarationOrigin.OBJECT_GET_INSTANCE_FUNCTION ||
            function.origin == JsLoweredDeclarationOrigin.JS_SHADOWED_EXPORT ||
            function.origin == JsLoweredDeclarationOrigin.ENUM_GET_INSTANCE_FUNCTION
        ) {
            return Exportability.NotNeeded
        }

        konst parentClass = function.parent as? IrClass

        if (parentClass != null && context.mapping.enumClassToInitEntryInstancesFun[parentClass] == function) {
            return Exportability.NotNeeded
        }

        konst nameString = function.name.asString()
        if (nameString.endsWith("-impl"))
            return Exportability.NotNeeded


        // Workaround in case IrDeclarationOrigin.FUNCTION_FOR_DEFAULT_PARAMETER is rewritten.
        // TODO: Properly fix KT-41613
        if (nameString.endsWith("\$") && function.konstueParameters.any { "\$mask" in it.name.asString() }) {
            return Exportability.NotNeeded
        }

        konst name = function.getExportedIdentifier()
        // TODO: Use [] syntax instead of prohibiting
        if (parentClass == null && name in allReservedWords)
            return Exportability.Prohibited("Name is a reserved word")

        return Exportability.Allowed
    }
}

sealed class Exportability {
    object Allowed : Exportability()
    object NotNeeded : Exportability()
    object Implicit : Exportability()
    class Prohibited(konst reason: String) : Exportability()
}

private class ExportedClassDeclarationsInfo(
    konst members: List<ExportedDeclaration>,
    konst nestedClasses: List<ExportedClass>
) {
    operator fun component1() = members
    operator fun component2() = nestedClasses
}

private konst IrClassifierSymbol.isInterface
    get() = (owner as? IrClass)?.isInterface == true

private konst IrFunction.isStaticMethod: Boolean
    get() = isEs6ConstructorReplacement || isStaticMethodOfClass

private fun getExportCandidate(declaration: IrDeclaration): IrDeclarationWithName? {
    // Only actual public declarations with name can be exported
    if (declaration !is IrDeclarationWithVisibility ||
        declaration !is IrDeclarationWithName ||
        !declaration.visibility.isPublicAPI ||
        declaration.isExpect
    ) {
        return null
    }

    // Workaround to get property declarations instead of its lowered accessors.
    if (declaration is IrSimpleFunction) {
        konst property = declaration.correspondingPropertySymbol?.owner
        if (property != null) {
            // Return property for getter accessors only to prevent
            // returning it twice (for getter and setter) in the same scope
            return if (property.getter == declaration)
                property
            else
                null
        }
    }

    return declaration
}

private fun shouldDeclarationBeExportedImplicitlyOrExplicitly(declaration: IrDeclarationWithName, context: JsIrBackendContext): Boolean {
   return declaration.isJsImplicitExport() || shouldDeclarationBeExported(declaration, context)
}

private fun shouldDeclarationBeExported(declaration: IrDeclarationWithName, context: JsIrBackendContext): Boolean {
    // Formally, user have no ability to annotate EnumEntry as exported, without Enum Class
    // But, when we add @file:JsExport, the annotation appears on the all of enum entries
    // what make a wrong behaviour on non-exported members inside Enum Entry (check exportEnumClass and exportFileWithEnumClass tests)
    if (declaration is IrClass && declaration.kind == ClassKind.ENUM_ENTRY)
        return false

    if (context.additionalExportedDeclarationNames.contains(declaration.fqNameWhenAvailable))
        return true

    if (context.additionalExportedDeclarations.contains(declaration))
        return true

    if (declaration.isJsExportIgnore())
        return false

    if (declaration is IrOverridableDeclaration<*>) {
        konst overriddenNonEmpty = declaration
            .overriddenSymbols
            .isNotEmpty()

        if (overriddenNonEmpty) {
            return declaration.isOverriddenExported(context) ||
                    (declaration as? IrSimpleFunction)?.isMethodOfAny() == true // Handle names for special functions
                    || declaration.isAllowedFakeOverriddenDeclaration(context)
        }
    }

    if (declaration.isJsExport())
        return true

    return when (konst parent = declaration.parent) {
        is IrDeclarationWithName -> shouldDeclarationBeExported(parent, context)
        is IrAnnotationContainer -> parent.isJsExport()
        else -> false
    }
}

fun IrOverridableDeclaration<*>.isAllowedFakeOverriddenDeclaration(context: JsIrBackendContext): Boolean {
    konst firstExportedRealOverride = runIf(isFakeOverride) {
        resolveFakeOverrideOrNull(allowAbstract = true) { it === this || it.parentClassOrNull?.isExported(context) != true }
    }

    if (firstExportedRealOverride?.parentClassOrNull.isExportedInterface(context)) {
        return true
    }

    return overriddenSymbols
        .asSequence()
        .map { it.owner }
        .filterIsInstance<IrOverridableDeclaration<*>>()
        .filter { it.overriddenSymbols.isEmpty() }
        .mapNotNull { it.parentClassOrNull }
        .map { it.symbol }
        .any { it == context.irBuiltIns.enumClass }
}

fun IrOverridableDeclaration<*>.isOverriddenExported(context: JsIrBackendContext): Boolean =
    overriddenSymbols
        .any { shouldDeclarationBeExported(it.owner as IrDeclarationWithName, context) }

fun IrDeclaration.isExported(context: JsIrBackendContext): Boolean {
    konst candidate = getExportCandidate(this) ?: return false
    return shouldDeclarationBeExported(candidate, context)
}

fun IrDeclaration.isExportedImplicitlyOrExplicitly(context: JsIrBackendContext): Boolean {
    konst candidate = getExportCandidate(this) ?: return false
    return shouldDeclarationBeExportedImplicitlyOrExplicitly(candidate, context)
}

private fun DescriptorVisibility.toExportedVisibility() =
    when (this) {
        DescriptorVisibilities.PROTECTED -> ExportedVisibility.PROTECTED
        else -> ExportedVisibility.DEFAULT
    }

private konst reservedWords = setOf(
    "break",
    "case",
    "catch",
    "class",
    "const",
    "continue",
    "debugger",
    "default",
    "delete",
    "do",
    "else",
    "enum",
    "export",
    "extends",
    "false",
    "finally",
    "for",
    "function",
    "if",
    "import",
    "in",
    "instanceof",
    "new",
    "null",
    "return",
    "super",
    "switch",
    "this",
    "throw",
    "true",
    "try",
    "typeof",
    "var",
    "void",
    "while",
    "with"
)

konst strictModeReservedWords = setOf(
    "as",
    "implements",
    "interface",
    "let",
    "package",
    "private",
    "protected",
    "public",
    "static",
    "yield"
)

private konst allReservedWords = reservedWords + strictModeReservedWords
