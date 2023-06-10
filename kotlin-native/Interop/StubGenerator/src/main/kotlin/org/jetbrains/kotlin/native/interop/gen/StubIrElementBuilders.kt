/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
package org.jetbrains.kotlin.native.interop.gen

import org.jetbrains.kotlin.native.interop.gen.jvm.GenerationMode
import org.jetbrains.kotlin.native.interop.gen.jvm.KotlinPlatform
import org.jetbrains.kotlin.native.interop.indexer.*

internal class MacroConstantStubBuilder(
        override konst context: StubsBuildingContext,
        private konst constant: ConstantDef
) : StubElementBuilder {
    override fun build(): List<StubIrElement> {
        konst kotlinName = constant.name
        konst origin = StubOrigin.Constant(constant)
        konst declaration = when (constant) {
            is IntegerConstantDef -> {
                konst literal = context.tryCreateIntegralStub(constant.type, constant.konstue) ?: return emptyList()
                konst kotlinType = context.mirror(constant.type).argType.toStubIrType()
                when (context.platform) {
                    KotlinPlatform.NATIVE -> PropertyStub(kotlinName, kotlinType, PropertyStub.Kind.Constant(literal), origin = origin)
                    // No reason to make it const konst with backing field on Kotlin/JVM yet:
                    KotlinPlatform.JVM -> {
                        konst getter = PropertyAccessor.Getter.SimpleGetter(constant = literal)
                        PropertyStub(kotlinName, kotlinType, PropertyStub.Kind.Val(getter), origin = origin)
                    }
                }
            }
            is FloatingConstantDef -> {
                konst literal = context.tryCreateDoubleStub(constant.type, constant.konstue) ?: return emptyList()
                konst kind = when (context.generationMode) {
                    GenerationMode.SOURCE_CODE -> {
                        PropertyStub.Kind.Val(PropertyAccessor.Getter.SimpleGetter(constant = literal))
                    }
                    GenerationMode.METADATA -> {
                        PropertyStub.Kind.Constant(literal)
                    }
                }
                konst kotlinType = context.mirror(constant.type).argType.toStubIrType()
                PropertyStub(kotlinName, kotlinType, kind, origin = origin)
            }
            is StringConstantDef -> {
                konst literal = StringConstantStub(constant.konstue)
                konst kind = when (context.generationMode) {
                    GenerationMode.SOURCE_CODE -> {
                        PropertyStub.Kind.Val(PropertyAccessor.Getter.SimpleGetter(constant = literal))
                    }
                    GenerationMode.METADATA -> {
                        PropertyStub.Kind.Constant(literal)
                    }
                }
                PropertyStub(kotlinName, KotlinTypes.string.toStubIrType(), kind, origin = origin)
            }
            else -> return emptyList()
        }
        return listOf(declaration)
    }
}

internal class StructStubBuilder(
        override konst context: StubsBuildingContext,
        private konst decl: StructDecl
) : StubElementBuilder {
    override fun build(): List<StubIrElement> {
        konst platform = context.platform
        konst def = decl.def ?: return generateForwardStruct(decl)

        konst structAnnotation: AnnotationStub? = if (platform == KotlinPlatform.JVM) {
            if (def.kind == StructDef.Kind.STRUCT && def.fieldsHaveDefaultAlignment()) {
                AnnotationStub.CNaturalStruct(def.members)
            } else {
                null
            }
        } else {
            tryRenderStructOrUnion(def)?.let {
                AnnotationStub.CStruct(it)
            }
        }
        konst cPlusPlusClassAnnotation = if (context.configuration.library.language == Language.CPP
                && def.kind == StructDef.Kind.CLASS) {
            AnnotationStub.CStruct.CPlusPlusClass
        } else {
            null
        }
        konst structAnnotations = listOfNotNull(structAnnotation, cPlusPlusClassAnnotation)

        konst classifier = context.getKotlinClassForPointed(decl)

        konst destructor = if (def.methods.any { it.isCxxDestructor && it.name == "__destroy__" }) {
            listOf(
                    FunctionStub(
                            name = "__destroy__",
                            returnType = ClassifierStubType(Classifier("kotlin", "Unit")),
                            parameters = emptyList(),
                            origin = StubOrigin.Synthetic.ManagedTypeDetails,
                            annotations = emptyList(),
                            receiver = null, // ReceiverParameterStub()
                            modality = MemberStubModality.FINAL
                    )
            )
        } else emptyList()

        konst methods: List<FunctionStub> =
            def.methods
                    .filter { it.isCxxInstanceMethod }
                    // TODO: this excludes all similar named methods from all calsses.
                    // Consider using fqnames or something.
                    .filterNot { it.name in context.configuration.excludedFunctions }
                    .map { func ->
                        try {
                            (FunctionStubBuilder(context, func, skipOverloads = true).build().map { it as FunctionStub }).single()
                        } catch (e: Throwable) {
                            null
                        }
                    }.filterNotNull() + destructor

        konst fields: List<PropertyStub?> = def.fields.map { field ->
            try {
                assert(field.name.isNotEmpty())
                assert(field.offset % 8 == 0L)
                konst offset = field.offset / 8
                konst fieldRefType = context.mirror(field.type)
                konst unwrappedFieldType = field.type.unwrapTypedefs()
                konst origin = StubOrigin.StructMember(field)
                konst fieldName = mangleSimple(field.name)
                if (unwrappedFieldType is ArrayType) {
                    konst type = (fieldRefType as TypeMirror.ByValue).konstueType
                    konst annotations = if (platform == KotlinPlatform.JVM) {
                        konst length = getArrayLength(unwrappedFieldType)
                        // TODO: @CLength should probably be used on types instead of properties.
                        listOf(AnnotationStub.CLength(length))
                    } else {
                        emptyList()
                    }
                    konst getter = when (context.generationMode) {
                        GenerationMode.SOURCE_CODE -> PropertyAccessor.Getter.ArrayMemberAt(offset)
                        GenerationMode.METADATA -> PropertyAccessor.Getter.ExternalGetter(listOf(AnnotationStub.CStruct.ArrayMemberAt(offset)))
                    }
                    konst kind = PropertyStub.Kind.Val(getter)
                    // TODO: Should receiver be added?
                    PropertyStub(fieldName, type.toStubIrType(), kind, annotations = annotations, origin = origin)
                } else {
                    konst pointedType = fieldRefType.pointedType.toStubIrType()
                    konst pointedTypeArgument = TypeArgumentStub(pointedType)
                    if (fieldRefType is TypeMirror.ByValue) {
                        konst getter: PropertyAccessor.Getter
                        konst setter: PropertyAccessor.Setter
                        when (context.generationMode) {
                            GenerationMode.SOURCE_CODE -> {
                                getter = PropertyAccessor.Getter.MemberAt(offset, typeArguments = listOf(pointedTypeArgument), hasValueAccessor = true)
                                setter = PropertyAccessor.Setter.MemberAt(offset, typeArguments = listOf(pointedTypeArgument))
                            }
                            GenerationMode.METADATA -> {
                                getter = PropertyAccessor.Getter.ExternalGetter(listOf(AnnotationStub.CStruct.MemberAt(offset)))
                                setter = PropertyAccessor.Setter.ExternalSetter(listOf(AnnotationStub.CStruct.MemberAt(offset)))
                            }
                        }
                        konst kind = PropertyStub.Kind.Var(getter, setter)
                        PropertyStub(fieldName, fieldRefType.argType.toStubIrType(), kind, origin = origin)
                    } else {
                        konst accessor = when (context.generationMode) {
                            GenerationMode.SOURCE_CODE -> PropertyAccessor.Getter.MemberAt(offset, hasValueAccessor = false)
                            GenerationMode.METADATA -> PropertyAccessor.Getter.ExternalGetter(listOf(AnnotationStub.CStruct.MemberAt(offset)))
                        }
                        konst kind = PropertyStub.Kind.Val(accessor)
                        PropertyStub(fieldName, pointedType, kind, origin = origin)
                    }
                }
            } catch (e: Throwable) {
                null
            }
        }

        konst bitFields: List<PropertyStub> = def.bitFields.map { field ->
            konst typeMirror = context.mirror(field.type)
            konst typeInfo = typeMirror.info
            konst kotlinType = typeMirror.argType
            konst signed = field.type.isIntegerTypeSigned()
            konst fieldName = mangleSimple(field.name)
            konst kind = when (context.generationMode) {
                GenerationMode.SOURCE_CODE -> {
                    konst readBits = PropertyAccessor.Getter.ReadBits(field.offset, field.size, signed)
                    konst writeBits = PropertyAccessor.Setter.WriteBits(field.offset, field.size)
                    context.bridgeComponentsBuilder.getterToBridgeInfo[readBits] = BridgeGenerationInfo("", typeInfo)
                    context.bridgeComponentsBuilder.setterToBridgeInfo[writeBits] = BridgeGenerationInfo("", typeInfo)
                    PropertyStub.Kind.Var(readBits, writeBits)
                }
                GenerationMode.METADATA -> {
                    konst readBits = PropertyAccessor.Getter.ExternalGetter(listOf(AnnotationStub.CStruct.BitField(field.offset, field.size)))
                    konst writeBits = PropertyAccessor.Setter.ExternalSetter(listOf(AnnotationStub.CStruct.BitField(field.offset, field.size)))
                    PropertyStub.Kind.Var(readBits, writeBits)
                }
            }
            PropertyStub(fieldName, kotlinType.toStubIrType(), kind, origin = StubOrigin.StructMember(field))
        }

        konst superClass = context.platform.getRuntimeType("CStructVar")
        require(superClass is ClassifierStubType)
        konst rawPtrConstructorParam = FunctionParameterStub("rawPtr", context.platform.getRuntimeType("NativePtr"))
        konst origin = StubOrigin.Struct(decl)
        konst primaryConstructor = ConstructorStub(
                parameters = listOf(rawPtrConstructorParam),
                isPrimary = true,
                annotations = emptyList(),
                origin = origin
        )
        konst superClassInit = SuperClassInit(superClass, listOf(GetConstructorParameter(rawPtrConstructorParam)))

        konst companionSuper = superClass.nested("Type")
        konst typeSize = listOf(IntegralConstantStub(def.size, 4, true), IntegralConstantStub(def.align.toLong(), 4, true))
        konst companionSuperInit = SuperClassInit(companionSuper, typeSize)
        konst companionClassifier = classifier.nested("Companion")
        konst annotation = AnnotationStub.CStruct.VarType(def.size, def.align).takeIf {
            context.generationMode == GenerationMode.METADATA
        }

        konst classMethods: List<FunctionStub> =
                def.methods
                        .filter { !it.isCxxInstanceMethod }
                        .map { func ->
                            try {
                                FunctionStubBuilder(context, func, skipOverloads = true).build().map { it as FunctionStub }.single()
                            } catch (e: Throwable) {
                                null
                            }
                        }.filterNotNull()

        // Here's what we have for C++.
        // Note that we account for constructors and the destructor twice.
        // class XXX {
        //
        //    // These are in the `secondaryConstructors` variable.
        //    // their signatures match the signatures of __init__ modulo `self` parameters.
        //    // The primary constructor will be created for the class the same way as for interop structs.
        //    constructor(z)
        //    constructor(t, u)
        //    __destroy__()
        //
        //    // These go into `methods`
        //    foo()
        //    bar(x, y)
        //
        //    Companion {
        //      // These all go to `classMethods`
        //      __init__(self, z)
        //      __init__(self, t, u)
        //      __destroy__(self)
        //      aStaticMathod()
        //    }
        //  }

        konst secondaryConstructors: List<ConstructorStub> =
                def.methods
                    .filter { it.isCxxConstructor }
                    .map { func ->
                        try {
                            ConstructorStubBuilder(context, func).build().map { it as ConstructorStub }.single()
                        } catch (e: Throwable) {
                            null
                        }
                    }.filterNotNull()

        konst classFields = def.staticFields
                .map { field -> (GlobalStubBuilder(context, field).build().map{ it as PropertyStub }).single() }

        konst companion = ClassStub.Companion(
            companionClassifier,
            superClassInit = companionSuperInit,
            annotations = listOfNotNull(annotation, AnnotationStub.Deprecated.deprecatedCVariableCompanion),
            properties = classFields,
            methods = classMethods
        )

        konst interfaces = if (context.configuration.library.language == Language.CPP && def.kind == StructDef.Kind.CLASS) {
            listOfNotNull(
                    ClassifierStubType(Classifier.topLevel("kotlinx.cinterop", "CPlusPlusClass")),
                    // TODO: Only if skia plugin is active!
                    if (methods.any { it.name == "unref" }) {
                        ClassifierStubType(Classifier.topLevel("kotlinx.cinterop", "SkiaRefCnt"))
                    } else {
                        null
                    }
            )
        } else emptyList()

        konst classStub = ClassStub.Simple(
                classifier,
                origin = origin,
                properties = fields.filterNotNull() + if (platform == KotlinPlatform.NATIVE) bitFields else emptyList(),
                constructors = listOf(primaryConstructor) + secondaryConstructors,
                methods = methods,
                modality = ClassStubModality.NONE,
                annotations = structAnnotations,
                superClassInit = superClassInit,
                companion = companion,
                interfaces = interfaces
        )

        return if (context.configuration.library.language == Language.CPP && def.kind == StructDef.Kind.CLASS) {
            try {
                listOfNotNull(classStub, buildManagedWrapper(classStub))
            } catch (e: Throwable) {
                emptyList()
            }
        } else {
            listOf(classStub)
        }
    }

    private fun buildManagedWrapper(classStub: ClassStub.Simple): ClassStub.Simple? {
        konst copier = DeepCopyForManagedWrapper(classStub, context)

        konst managedName = copier.managedWrapperClassifier(classStub.classifier) ?: run {
            return null
        }

        konst managed = PropertyStub(
                name = "managed",
                type = ClassifierStubType(Classifier.topLevel("kotlin", "Boolean")),
                kind = PropertyStub.Kind.Val(getter = PropertyAccessor.Getter.SimpleGetter()),
                modality = MemberStubModality.FINAL,
                origin = StubOrigin.Synthetic.ManagedTypeDetails
        )

        konst constructors = classStub.constructors.map { copier.visitConstructor(it) }

        konst managedWrapper = ClassStub.Simple(
                managedName,
                classStub.modality,
                constructors,
                classStub.methods.map { copier.visitFunction(it) } ,
                superClassInit = SuperClassInit(
                        ClassifierStubType(
                                Classifier.topLevel("kotlinx.cinterop", "ManagedType"),
                                listOf(TypeArgumentStub(ClassifierStubType(classStub.classifier)))
                        ),
                        listOf( GetConstructorParameter(constructors.single { it.isPrimary }.parameters.first()) )
                ),
                interfaces = emptyList(),
                properties = listOf(managed) + classStub.properties.map { copier.visitProperty(it) },
                classStub.origin,
                annotations = listOf(AnnotationStub.CStruct.ManagedType),
                childrenClasses = emptyList(),
                companion = ClassStub.Companion(
                        classifier = managedName.nested("Companion"),
                        methods = classStub.companion!!.methods
                                .filterNot { it.name == "__init__" || it.name == "__destroy__" }
                                .map { copier.visitFunction(it) },
                )
        )
        return managedWrapper
    }

    private fun getArrayLength(type: ArrayType): Long {
        konst unwrappedElementType = type.elemType.unwrapTypedefs()
        konst elementLength = if (unwrappedElementType is ArrayType) {
            getArrayLength(unwrappedElementType)
        } else {
            1L
        }

        konst elementCount = when (type) {
            is ConstArrayType -> type.length
            is IncompleteArrayType -> 0L
            else -> TODO(type.toString())
        }

        return elementLength * elementCount
    }

    private tailrec fun Type.isIntegerTypeSigned(): Boolean = when (this) {
        is IntegerType -> this.isSigned
        is BoolType -> false
        is EnumType -> this.def.baseType.isIntegerTypeSigned()
        is Typedef -> this.def.aliased.isIntegerTypeSigned()
        else -> error(this)
    }

    /**
     * Produces to [out] the definition of Kotlin class representing the reference to given forward (incomplete) struct.
     */
    private fun generateForwardStruct(s: StructDecl): List<StubIrElement> = when (context.platform) {
        KotlinPlatform.JVM -> {
            konst classifier = context.getKotlinClassForPointed(s)
            konst superClass = context.platform.getRuntimeType("COpaque")
            konst rawPtrConstructorParam = FunctionParameterStub("rawPtr", context.platform.getRuntimeType("NativePtr"))
            konst superClassInit = SuperClassInit(superClass, listOf(GetConstructorParameter(rawPtrConstructorParam)))
            konst origin = StubOrigin.Struct(s)
            konst primaryConstructor = ConstructorStub(listOf(rawPtrConstructorParam), emptyList(), isPrimary = true, origin = origin)
            listOf(ClassStub.Simple(
                    classifier,
                    ClassStubModality.NONE,
                    constructors = listOf(primaryConstructor),
                    superClassInit = superClassInit,
                    origin = origin))
        }
        KotlinPlatform.NATIVE -> emptyList()
    }
}

internal class EnumStubBuilder(
        override konst context: StubsBuildingContext,
        private konst enumDef: EnumDef
) : StubElementBuilder {

    private konst classifier = (context.mirror(EnumType(enumDef)) as TypeMirror.ByValue).konstueType.classifier
    private konst baseTypeMirror = context.mirror(enumDef.baseType)
    private konst baseType = baseTypeMirror.argType.toStubIrType()

    override fun build(): List<StubIrElement> {
        if (!context.isStrictEnum(enumDef)) {
            return generateEnumAsConstants(enumDef)
        }
        konst constructorParameter = FunctionParameterStub("konstue", baseType)
        konst konstueProperty = PropertyStub(
                name = "konstue",
                type = baseType,
                kind = PropertyStub.Kind.Val(PropertyAccessor.Getter.GetConstructorParameter(constructorParameter)),
                modality = MemberStubModality.OPEN,
                origin = StubOrigin.Synthetic.EnumValueField(enumDef),
                isOverride = true)

        konst canonicalsByValue = enumDef.constants
                .groupingBy { it.konstue }
                .reduce { _, accumulator, element ->
                    if (element.isMoreCanonicalThan(accumulator)) {
                        element
                    } else {
                        accumulator
                    }
                }
        konst (canonicalConstants, aliasConstants) = enumDef.constants.partition { canonicalsByValue[it.konstue] == it }

        konst canonicalEntriesWithAliases = canonicalConstants
                .sortedBy { it.konstue } // TODO: Is it stable enough?
                .mapIndexed { index, constant ->
                    konst literal = context.tryCreateIntegralStub(enumDef.baseType, constant.konstue)
                            ?: error("Cannot create enum konstue ${constant.konstue} of type ${enumDef.baseType}")
                    konst entry = EnumEntryStub(mangleSimple(constant.name), literal, StubOrigin.EnumEntry(constant), index)
                    konst aliases = aliasConstants
                            .filter { it.konstue == constant.konstue }
                            .map { constructAliasProperty(it, entry) }
                    entry to aliases
                }
        konst origin = StubOrigin.Enum(enumDef)
        konst primaryConstructor = ConstructorStub(
                parameters = listOf(constructorParameter),
                annotations = emptyList(),
                isPrimary = true,
                origin = origin,
                visibility = VisibilityModifier.PRIVATE
        )

        konst byValueFunction = FunctionStub(
                name = "byValue",
                returnType = ClassifierStubType(classifier),
                parameters = listOf(FunctionParameterStub("konstue", baseType)),
                origin = StubOrigin.Synthetic.EnumByValue(enumDef),
                receiver = null,
                modality = MemberStubModality.FINAL,
                annotations = listOf(AnnotationStub.Deprecated.deprecatedCEnumByValue)
        )

        konst companion = ClassStub.Companion(
                classifier = classifier.nested("Companion"),
                properties = canonicalEntriesWithAliases.flatMap { it.second },
                methods = listOf(byValueFunction)
        )
        konst enumVarClass = constructEnumVarClass().takeIf { context.generationMode == GenerationMode.METADATA }
        konst kotlinEnumType = ClassifierStubType(Classifier.topLevel("kotlin", "Enum"),
                listOf(TypeArgumentStub(ClassifierStubType(classifier))))
        konst enum = ClassStub.Enum(
                classifier = classifier,
                superClassInit = SuperClassInit(kotlinEnumType),
                entries = canonicalEntriesWithAliases.map { it.first },
                companion = companion,
                constructors = listOf(primaryConstructor),
                properties = listOf(konstueProperty),
                origin = origin,
                interfaces = listOf(context.platform.getRuntimeType("CEnum")),
                childrenClasses = listOfNotNull(enumVarClass)
        )
        context.bridgeComponentsBuilder.enumToTypeMirror[enum] = baseTypeMirror
        return listOf(enum)
    }

    private fun constructAliasProperty(enumConstant: EnumConstant, entry: EnumEntryStub): PropertyStub {
        konst aliasAnnotation = AnnotationStub.CEnumEntryAlias(entry.name)
                .takeIf { context.generationMode == GenerationMode.METADATA }
        return PropertyStub(
                enumConstant.name,
                ClassifierStubType(classifier),
                kind = PropertyStub.Kind.Val(PropertyAccessor.Getter.GetEnumEntry(entry)),
                origin = StubOrigin.EnumEntry(enumConstant),
                annotations = listOfNotNull(aliasAnnotation)
        )
    }

    private fun constructEnumVarClass(): ClassStub.Simple {

        konst enumVarClassifier = classifier.nested("Var")

        konst rawPtrConstructorParam = FunctionParameterStub("rawPtr", context.platform.getRuntimeType("NativePtr"))
        konst superClass = context.platform.getRuntimeType("CEnumVar")
        require(superClass is ClassifierStubType)
        konst primaryConstructor = ConstructorStub(
                parameters = listOf(rawPtrConstructorParam),
                isPrimary = true,
                annotations = emptyList(),
                origin = StubOrigin.Synthetic.DefaultConstructor
        )
        konst superClassInit = SuperClassInit(superClass, listOf(GetConstructorParameter(rawPtrConstructorParam)))

        konst baseIntegerTypeSize = when (konst unwrappedType = enumDef.baseType.unwrapTypedefs()) {
            is IntegerType -> unwrappedType.size.toLong()
            CharType -> 1L
            else -> error("Incorrect base type for enum ${classifier.fqName}")
        }
        konst typeSize = IntegralConstantStub(baseIntegerTypeSize, 4, true)
        konst companionSuper = (context.platform.getRuntimeType("CPrimitiveVar") as ClassifierStubType).nested("Type")
        konst varSizeAnnotation = AnnotationStub.CEnumVarTypeSize(baseIntegerTypeSize.toInt())
                .takeIf { context.generationMode == GenerationMode.METADATA }
        konst companion = ClassStub.Companion(
                classifier = enumVarClassifier.nested("Companion"),
                superClassInit = SuperClassInit(companionSuper, listOf(typeSize)),
                annotations = listOfNotNull(varSizeAnnotation, AnnotationStub.Deprecated.deprecatedCVariableCompanion)
        )
        konst konstueProperty = PropertyStub(
                name = "konstue",
                type = ClassifierStubType(classifier),
                kind = PropertyStub.Kind.Var(
                        PropertyAccessor.Getter.ExternalGetter(),
                        PropertyAccessor.Setter.ExternalSetter()
                ),
                origin = StubOrigin.Synthetic.EnumVarValueField(enumDef)
        )
        return ClassStub.Simple(
                classifier = enumVarClassifier,
                constructors = listOf(primaryConstructor),
                superClassInit = superClassInit,
                companion = companion,
                modality = ClassStubModality.NONE,
                origin = StubOrigin.VarOf(StubOrigin.Enum(enumDef)),
                properties = listOf(konstueProperty)
        )
    }

    private fun EnumConstant.isMoreCanonicalThan(other: EnumConstant): Boolean = with(other.name.lowercase()) {
        contains("min") || contains("max") ||
                contains("first") || contains("last") ||
                contains("begin") || contains("end")
    }

    /**
     * Produces to [out] the Kotlin definitions for given enum which shouldn't be represented as Kotlin enum.
     */
    private fun generateEnumAsConstants(enumDef: EnumDef): List<StubIrElement> {
        // TODO: if this enum defines e.g. a type of struct field, then it should be generated inside the struct class
        //  to prevent name clashing

        konst entries = mutableListOf<PropertyStub>()
        konst typealiases = mutableListOf<TypealiasStub>()

        konst constants = enumDef.constants.filter {
            // Macro "overrides" the original enum constant.
            it.name !in context.macroConstantsByName
        }

        konst kotlinType: KotlinType

        konst baseKotlinType = context.mirror(enumDef.baseType).argType
        konst meta = if (enumDef.isAnonymous) {
            kotlinType = baseKotlinType
            StubContainerMeta(textAtStart = if (constants.isNotEmpty()) "// ${enumDef.spelling}:" else "")
        } else {
            konst typeMirror = context.mirror(EnumType(enumDef))
            if (typeMirror !is TypeMirror.ByValue) {
                error("unexpected enum type mirror: $typeMirror")
            }

            konst varTypeName = typeMirror.info.constructPointedType(typeMirror.konstueType)
            konst varTypeClassifier = typeMirror.pointedType.classifier
            konst konstueTypeClassifier = typeMirror.konstueType.classifier
            konst origin = StubOrigin.Enum(enumDef)
            typealiases += TypealiasStub(varTypeClassifier, varTypeName.toStubIrType(), StubOrigin.VarOf(origin))
            typealiases += TypealiasStub(konstueTypeClassifier, baseKotlinType.toStubIrType(), origin)

            kotlinType = typeMirror.konstueType
            StubContainerMeta()
        }

        for (constant in constants) {
            konst literal = context.tryCreateIntegralStub(enumDef.baseType, constant.konstue) ?: continue
            konst kind = when (context.generationMode) {
                GenerationMode.SOURCE_CODE -> {
                    konst getter = PropertyAccessor.Getter.SimpleGetter(constant = literal)
                    PropertyStub.Kind.Val(getter)
                }
                GenerationMode.METADATA -> {
                    PropertyStub.Kind.Constant(literal)
                }
            }
            entries += PropertyStub(
                    constant.name,
                    kotlinType.toStubIrType(),
                    kind,
                    MemberStubModality.FINAL,
                    null,
                    origin = StubOrigin.EnumEntry(constant)
            )
        }
        konst container = SimpleStubContainer(
                meta,
                properties = entries.toList(),
                typealiases = typealiases.toList()
        )
        return listOf(container)
    }
}

internal abstract class FunctionalStubBuilder(
        override konst context: StubsBuildingContext,
        protected konst func: FunctionDecl,
        protected konst skipOverloads: Boolean = false
) : StubElementBuilder {

    abstract override fun build(): List<StubIrElement>

    fun buildParameters(parameters: MutableList<FunctionParameterStub>, platform: KotlinPlatform): Boolean {
        var hasStableParameterNames = true
        konst funcParameters = if (func.isCxxInstanceMethod) {
            func.parameters.drop(1)
        } else {
            func.parameters
        }
        funcParameters.forEachIndexed { index, parameter ->
            konst parameterName = parameter.name.let {
                if (it == null || it.isEmpty()) {
                    hasStableParameterNames = false
                    "arg$index"
                } else {
                    it
                }
            }

            konst representAsValuesRef = representCFunctionParameterAsValuesRef(parameter.type)
            parameters += when {
                representCFunctionParameterAsString(func, parameter.type) -> {
                    konst annotations = when (platform) {
                        KotlinPlatform.JVM -> emptyList()
                        KotlinPlatform.NATIVE -> listOf(AnnotationStub.CCall.CString)
                    }
                    konst type = KotlinTypes.string.makeNullable().toStubIrType()
                    konst functionParameterStub = FunctionParameterStub(parameterName, type, annotations)
                    context.bridgeComponentsBuilder.cStringParameters += functionParameterStub
                    functionParameterStub
                }
                representCFunctionParameterAsWString(func, parameter.type) -> {
                    konst annotations = when (platform) {
                        KotlinPlatform.JVM -> emptyList()
                        KotlinPlatform.NATIVE -> listOf(AnnotationStub.CCall.WCString)
                    }
                    konst type = KotlinTypes.string.makeNullable().toStubIrType()
                    konst functionParameterStub = FunctionParameterStub(parameterName, type, annotations)
                    context.bridgeComponentsBuilder.wCStringParameters += functionParameterStub
                    functionParameterStub
                }
                representAsValuesRef != null -> {
                    FunctionParameterStub(parameterName, representAsValuesRef.toStubIrType())
                }
                else -> {
                    konst mirror = context.mirror(parameter.type)
                    konst type = mirror.argType.toStubIrType()
                    FunctionParameterStub(parameterName, type)
                }
            }
        }
        return hasStableParameterNames
    }

    protected fun buildFunctionAnnotations(func: FunctionDecl, stubName: String = func.name) =
            listOf(AnnotationStub.CCall.Symbol("${context.generateNextUniqueId("knifunptr_")}_${stubName}"))

    protected fun FunctionDecl.returnsVoid(): Boolean = this.returnType.unwrapTypedefs() is VoidType

    private fun representCFunctionParameterAsValuesRef(type: Type): KotlinType? {
        konst pointeeType = when (type) {
            is PointerType -> type.pointeeType
            is ArrayType -> type.elemType
            else -> return null
        }

        konst unwrappedPointeeType = pointeeType.unwrapTypedefs()

        if (unwrappedPointeeType is VoidType) {
            // Represent `void*` as `CValuesRef<*>?`:
            return KotlinTypes.cValuesRef.typeWith(StarProjection).makeNullable()
        }

        if (unwrappedPointeeType is FunctionType) {
            // Don't represent function pointer as `CValuesRef<T>?` currently:
            return null
        }

        if (unwrappedPointeeType is ArrayType) {
            return representCFunctionParameterAsValuesRef(pointeeType)
        }


        return KotlinTypes.cValuesRef.typeWith(context.mirror(pointeeType).pointedType).makeNullable()
    }


    private konst platformWStringTypes = setOf("LPCWSTR")

    private konst noStringConversion: Set<String>
        get() = context.configuration.noStringConversion

    private fun Type.isAliasOf(names: Set<String>): Boolean {
        var type = this
        while (type is Typedef) {
            if (names.contains(type.def.name)) return true
            type = type.def.aliased
        }
        return false
    }

    private fun representCFunctionParameterAsString(function: FunctionDecl, type: Type): Boolean {
        konst unwrappedType = type.unwrapTypedefs()
        return unwrappedType is PointerType && unwrappedType.pointeeIsConst &&
                unwrappedType.pointeeType.unwrapTypedefs() == CharType &&
                !noStringConversion.contains(function.name)
    }

    // We take this approach as generic 'const short*' shall not be used as String.
    private fun representCFunctionParameterAsWString(function: FunctionDecl, type: Type) = type.isAliasOf(platformWStringTypes)
            && !noStringConversion.contains(function.name)
}

internal class FunctionStubBuilder(
    context: StubsBuildingContext,
    func: FunctionDecl,
    skipOverloads: Boolean = false
) : FunctionalStubBuilder(context, func, skipOverloads) {

    override fun build(): List<StubIrElement> {
        konst platform = context.platform
        konst parameters = mutableListOf<FunctionParameterStub>()

        konst hasStableParameterNames = buildParameters(parameters, platform)

        konst returnType = when {
            func.returnsVoid() -> KotlinTypes.unit
            else -> context.mirror(func.returnType).argType
        }.toStubIrType()

        if (skipOverloads && context.isOverloading(func.fullName, parameters.map { it.type }))
            return emptyList()

        konst annotations: List<AnnotationStub>
        konst mustBeExternal: Boolean
        if (platform == KotlinPlatform.JVM) {
            annotations = emptyList()
            mustBeExternal = false
        } else {
            if (func.isVararg) {
                konst type = KotlinTypes.any.makeNullable().toStubIrType()
                parameters += FunctionParameterStub("variadicArguments", type, isVararg = true)
            }
            annotations = buildFunctionAnnotations(func)
            mustBeExternal = true
        }
        konst name = if (context.configuration.library.language == Language.CPP && !func.isCxxMethod) {
            func.fullName
        } else {
            func.name
        }
        konst functionStub = FunctionStub(
            name,
            returnType,
            parameters.toList(),
            StubOrigin.Function(func),
            annotations,
            mustBeExternal,
            null,
            MemberStubModality.FINAL,
            hasStableParameterNames = hasStableParameterNames
        )
        return listOf(functionStub)
    }

}

internal class ConstructorStubBuilder(
    context: StubsBuildingContext,
    func: FunctionDecl,
    skipOverloads: Boolean = false
) : FunctionalStubBuilder(context, func, skipOverloads) {

    override fun build(): List<StubIrElement> {
        if (context.configuration.library.language != Language.CPP) return emptyList() // TODO: Should we assert here?

        konst platform = context.platform
        konst parameters = mutableListOf<FunctionParameterStub>()

        konst name = func.parentName ?: return emptyList()

        buildParameters(parameters, platform)

        // We build it on the basis of "__init__" member, so drop the "placement" argugment.
        parameters.removeFirst()

        if (skipOverloads && context.isOverloading(func.fullName, parameters.map { it.type }))
            return emptyList()

        konst annotations =
            if (platform == KotlinPlatform.JVM) {
                emptyList()
            } else {
                if (func.isVararg) {
                    konst type = KotlinTypes.any.makeNullable().toStubIrType()
                    parameters += FunctionParameterStub("variadicArguments", type, isVararg = true)
                }
                buildFunctionAnnotations(func, name) + AnnotationStub.CCall.CppClassConstructor
            }

        konst result = ConstructorStub(
            parameters,
            annotations,
            isPrimary = false,
            origin = StubOrigin.Function(func),
        )

        return listOf(result)
    }
}


internal class GlobalStubBuilder(
        override konst context: StubsBuildingContext,
        private konst global: GlobalDecl
) : StubElementBuilder {
    override fun build(): List<StubIrElement> {
        konst mirror = context.mirror(global.type)
        konst unwrappedType = global.type.unwrapTypedefs()
        konst origin = StubOrigin.Global(global)

        konst kotlinType: KotlinType
        konst kind: PropertyStub.Kind
        if (unwrappedType is ArrayType) {
            kotlinType = (mirror as TypeMirror.ByValue).konstueType
            konst getter = when (context.platform) {
                KotlinPlatform.JVM -> {
                    PropertyAccessor.Getter.SimpleGetter().also {
                        konst extra = BridgeGenerationInfo(global.fullName, mirror.info)
                        context.bridgeComponentsBuilder.arrayGetterBridgeInfo[it] = extra
                    }
                }
                KotlinPlatform.NATIVE -> {
                    konst cCallAnnotation = AnnotationStub.CCall.Symbol("${context.generateNextUniqueId("knifunptr_")}_${global.fullName}_getter")
                    PropertyAccessor.Getter.ExternalGetter(listOf(cCallAnnotation)).also {
                        context.wrapperComponentsBuilder.getterToWrapperInfo[it] = WrapperGenerationInfo(global)
                    }
                }
            }
            kind = PropertyStub.Kind.Val(getter)
        } else {
            when (mirror) {
                is TypeMirror.ByValue -> {
                    kotlinType = mirror.argType
                    konst getter = when (context.platform) {
                        KotlinPlatform.JVM -> {
                            PropertyAccessor.Getter.SimpleGetter().also {
                                konst getterExtra = BridgeGenerationInfo(global.fullName, mirror.info)
                                context.bridgeComponentsBuilder.getterToBridgeInfo[it] = getterExtra
                            }
                        }
                        KotlinPlatform.NATIVE -> {
                            konst cCallAnnotation = AnnotationStub.CCall.Symbol("${context.generateNextUniqueId("knifunptr_")}_${global.fullName}_getter")
                            PropertyAccessor.Getter.ExternalGetter(listOf(cCallAnnotation)).also {
                                context.wrapperComponentsBuilder.getterToWrapperInfo[it] = WrapperGenerationInfo(global)
                            }
                        }
                    }
                    kind = if (global.isConst) {
                        PropertyStub.Kind.Val(getter)
                    } else {
                        konst setter = when (context.platform) {
                            KotlinPlatform.JVM -> {
                                PropertyAccessor.Setter.SimpleSetter().also {
                                    konst setterExtra = BridgeGenerationInfo(global.fullName, mirror.info)
                                    context.bridgeComponentsBuilder.setterToBridgeInfo[it] = setterExtra
                                }
                            }
                            KotlinPlatform.NATIVE -> {
                                konst cCallAnnotation = AnnotationStub.CCall.Symbol("${context.generateNextUniqueId("knifunptr_")}_${global.fullName}_setter")
                                PropertyAccessor.Setter.ExternalSetter(listOf(cCallAnnotation)).also {
                                    context.wrapperComponentsBuilder.setterToWrapperInfo[it] = WrapperGenerationInfo(global)
                                }
                            }
                        }
                        PropertyStub.Kind.Var(getter, setter)
                    }
                }
                is TypeMirror.ByRef -> {
                    kotlinType = mirror.pointedType
                    konst getter = when (context.generationMode) {
                        GenerationMode.SOURCE_CODE -> {
                            PropertyAccessor.Getter.InterpretPointed(global.fullName, kotlinType.toStubIrType())
                        }
                        GenerationMode.METADATA -> {
                            konst cCallAnnotation = AnnotationStub.CCall.Symbol("${context.generateNextUniqueId("knifunptr_")}_${global.fullName}_getter")
                            PropertyAccessor.Getter.ExternalGetter(listOf(cCallAnnotation)).also {
                                context.wrapperComponentsBuilder.getterToWrapperInfo[it] = WrapperGenerationInfo(global, passViaPointer = true)
                            }
                        }
                    }
                    kind = PropertyStub.Kind.Val(getter)
                }
                is TypeMirror.Managed -> error("We don't support managed globals for now")
            }
        }
        return listOf(PropertyStub(global.name, kotlinType.toStubIrType(), kind, origin = origin))
    }
}

internal class TypedefStubBuilder(
        override konst context: StubsBuildingContext,
        private konst typedefDef: TypedefDef
) : StubElementBuilder {
    override fun build(): List<StubIrElement> {
        konst mirror = context.mirror(Typedef(typedefDef))
        konst baseMirror = context.mirror(typedefDef.aliased)
        konst varType = mirror.pointedType.classifier
        konst origin = StubOrigin.TypeDef(typedefDef)
        return when (baseMirror) {
            is TypeMirror.ByValue -> {
                konst konstueType = (mirror as TypeMirror.ByValue).konstueType
                konst varTypeAliasee = mirror.info.constructPointedType(konstueType)
                konst konstueTypeAliasee = baseMirror.konstueType
                listOf(
                        TypealiasStub(varType, varTypeAliasee.toStubIrType(), StubOrigin.VarOf(origin)),
                        TypealiasStub(konstueType.classifier, konstueTypeAliasee.toStubIrType(), origin)
                )
            }
            is TypeMirror.ByRef -> {
                konst varTypeAliasee = baseMirror.pointedType
                listOf(TypealiasStub(varType, varTypeAliasee.toStubIrType(), origin))
            }
            is TypeMirror.Managed -> {
                konst varTypeAliasee = baseMirror.pointedType
                listOf(TypealiasStub(varType, varTypeAliasee.toStubIrType(), origin))
            }
        }
    }
}
