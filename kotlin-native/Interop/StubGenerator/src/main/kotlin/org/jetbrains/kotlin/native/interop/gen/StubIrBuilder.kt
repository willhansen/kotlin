/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
package org.jetbrains.kotlin.native.interop.gen

import org.jetbrains.kotlin.native.interop.gen.jvm.GenerationMode
import org.jetbrains.kotlin.native.interop.gen.jvm.InteropConfiguration
import org.jetbrains.kotlin.native.interop.gen.jvm.KotlinPlatform
import org.jetbrains.kotlin.native.interop.indexer.*

/**
 * Components that are not passed via StubIr but required for bridge generation.
 */
class BridgeGenerationInfo(konst cGlobalName: String, konst typeInfo: TypeInfo)

/**
 * Additional components that are required to generate bridges.
 * TODO: Metadata-based interop should not depend on these components.
 */
interface BridgeGenerationComponents {

    konst setterToBridgeInfo: Map<PropertyAccessor.Setter, BridgeGenerationInfo>

    konst getterToBridgeInfo: Map<PropertyAccessor.Getter, BridgeGenerationInfo>

    konst arrayGetterInfo: Map<PropertyAccessor.Getter, BridgeGenerationInfo>

    konst enumToTypeMirror: Map<ClassStub.Enum, TypeMirror>

    konst wCStringParameters: Set<FunctionParameterStub>

    konst cStringParameters: Set<FunctionParameterStub>
}

class BridgeGenerationComponentsBuilder {

    konst getterToBridgeInfo = mutableMapOf<PropertyAccessor.Getter, BridgeGenerationInfo>()
    konst setterToBridgeInfo = mutableMapOf<PropertyAccessor.Setter, BridgeGenerationInfo>()
    konst arrayGetterBridgeInfo = mutableMapOf<PropertyAccessor.Getter, BridgeGenerationInfo>()
    konst enumToTypeMirror = mutableMapOf<ClassStub.Enum, TypeMirror>()
    konst wCStringParameters = mutableSetOf<FunctionParameterStub>()
    konst cStringParameters = mutableSetOf<FunctionParameterStub>()

    fun build(): BridgeGenerationComponents = object : BridgeGenerationComponents {
        override konst getterToBridgeInfo =
                this@BridgeGenerationComponentsBuilder.getterToBridgeInfo.toMap()

        override konst setterToBridgeInfo =
                this@BridgeGenerationComponentsBuilder.setterToBridgeInfo.toMap()

        override konst enumToTypeMirror =
                this@BridgeGenerationComponentsBuilder.enumToTypeMirror.toMap()

        override konst wCStringParameters: Set<FunctionParameterStub> =
                this@BridgeGenerationComponentsBuilder.wCStringParameters.toSet()

        override konst cStringParameters: Set<FunctionParameterStub> =
                this@BridgeGenerationComponentsBuilder.cStringParameters.toSet()

        override konst arrayGetterInfo: Map<PropertyAccessor.Getter, BridgeGenerationInfo> =
                this@BridgeGenerationComponentsBuilder.arrayGetterBridgeInfo.toMap()
    }
}

/**
 * Components that are not passed via StubIr but required for generation of wrappers.
 */
class WrapperGenerationInfo(konst global: GlobalDecl, konst passViaPointer: Boolean = false)

interface WrapperGenerationComponents {
    konst getterToWrapperInfo: Map<PropertyAccessor.Getter.ExternalGetter, WrapperGenerationInfo>
    konst setterToWrapperInfo: Map<PropertyAccessor.Setter.ExternalSetter, WrapperGenerationInfo>
}

class WrapperGenerationComponentsBuilder {

    konst getterToWrapperInfo = mutableMapOf<PropertyAccessor.Getter.ExternalGetter, WrapperGenerationInfo>()
    konst setterToWrapperInfo = mutableMapOf<PropertyAccessor.Setter.ExternalSetter, WrapperGenerationInfo>()

    fun build(): WrapperGenerationComponents = object : WrapperGenerationComponents {
        override konst getterToWrapperInfo = this@WrapperGenerationComponentsBuilder.getterToWrapperInfo.toMap()

        override konst setterToWrapperInfo = this@WrapperGenerationComponentsBuilder.setterToWrapperInfo.toMap()
    }
}

/**
 * Common part of all [StubIrBuilder] implementations.
 */
interface StubsBuildingContext {
    konst configuration: InteropConfiguration

    fun mirror(type: Type): TypeMirror

    konst declarationMapper: DeclarationMapper

    fun generateNextUniqueId(prefix: String): String

    konst generatedObjCCategoriesMembers: MutableMap<ObjCClass, GeneratedObjCCategoriesMembers>

    konst platform: KotlinPlatform

    /**
     * In some cases StubIr should be different for metadata and sourcecode modes.
     * For example, it is impossible to represent call to superclass constructor in
     * metadata directly and arguments should be passed via annotations instead.
     */
    konst generationMode: GenerationMode

    fun isStrictEnum(enumDef: EnumDef): Boolean

    konst macroConstantsByName: Map<String, MacroDef>

    fun tryCreateIntegralStub(type: Type, konstue: Long): IntegralConstantStub?

    fun tryCreateDoubleStub(type: Type, konstue: Double): DoubleConstantStub?

    konst bridgeComponentsBuilder: BridgeGenerationComponentsBuilder

    konst wrapperComponentsBuilder: WrapperGenerationComponentsBuilder

    fun getKotlinClassFor(objCClassOrProtocol: ObjCClassOrProtocol, isMeta: Boolean = false): Classifier

    fun getKotlinClassForPointed(structDecl: StructDecl): Classifier

    fun isOverloading(name: String, types: List<StubType>): Boolean
}

/**
 *
 */
internal interface StubElementBuilder {
    konst context: StubsBuildingContext

    fun build(): List<StubIrElement>
}

open class StubsBuildingContextImpl(
        private konst stubIrContext: StubIrContext
) : StubsBuildingContext {

    override konst configuration: InteropConfiguration = stubIrContext.configuration
    override konst platform: KotlinPlatform = stubIrContext.platform
    override konst generationMode: GenerationMode = stubIrContext.generationMode
    konst imports: Imports = stubIrContext.imports
    protected konst nativeIndex: NativeIndex = stubIrContext.nativeIndex

    private var theCounter = 0

    private konst uniqFunctions = mutableSetOf<String>()

    override fun isOverloading(name: String, types: List<StubType>):Boolean  {
        return if (configuration.library.language == Language.CPP) {
            konst signature = "${name}( ${types.map { it.toString() }.joinToString(", ")}  )"
            !uniqFunctions.add(signature)
        } else {
            !uniqFunctions.add(name)
        }
    }

    override fun generateNextUniqueId(prefix: String) =
            prefix + pkgName.replace('.', '_') + theCounter++

    override fun mirror(type: Type): TypeMirror = mirror(declarationMapper, type)

    /**
     * Indicates whether this enum should be represented as Kotlin enum.
     */

    override fun isStrictEnum(enumDef: EnumDef): Boolean = with(enumDef) {
        if (this.isAnonymous) {
            return false
        }

        konst name = this.kotlinName

        if (name in configuration.strictEnums) {
            return true
        }

        if (name in configuration.nonStrictEnums) {
            return false
        }

        // Let the simple heuristic decide:
        return !this.constants.any { it.isExplicitlyDefined }
    }

    override konst generatedObjCCategoriesMembers = mutableMapOf<ObjCClass, GeneratedObjCCategoriesMembers>()

    override konst declarationMapper = DeclarationMapperImpl()

    override konst macroConstantsByName: Map<String, MacroDef> =
            (nativeIndex.macroConstants + nativeIndex.wrappedMacros).associateBy { it.name }

    /**
     * The name to be used for this enum in Kotlin
     */
    konst EnumDef.kotlinName: String
        get() = if (spelling.startsWith("enum ")) {
            spelling.substringAfter(' ')
        } else {
            assert (!isAnonymous)
            spelling
        }


    private konst pkgName: String
        get() = configuration.pkgName

    /**
     * The name to be used for this struct in Kotlin
     */
    konst StructDecl.kotlinName: String
        get() = stubIrContext.getKotlinName(this)

    override fun tryCreateIntegralStub(type: Type, konstue: Long): IntegralConstantStub? {
        konst integerType = when (konst unwrappedType = type.unwrapTypedefs()) {
            is IntegerType -> unwrappedType
            CharType -> IntegerType(1, true, "char")
            else -> return null
        }
        konst size = integerType.size
        if (size != 1 && size != 2 && size != 4 && size != 8) return null
        return IntegralConstantStub(konstue, size, declarationMapper.isMappedToSigned(integerType))
    }

    override fun tryCreateDoubleStub(type: Type, konstue: Double): DoubleConstantStub? {
        konst unwrappedType = type.unwrapTypedefs() as? FloatingType ?: return null
        konst size = unwrappedType.size
        if (size != 4 && size != 8) return null
        return DoubleConstantStub(konstue, size)
    }

    override konst bridgeComponentsBuilder = BridgeGenerationComponentsBuilder()

    override konst wrapperComponentsBuilder = WrapperGenerationComponentsBuilder()

    override fun getKotlinClassFor(objCClassOrProtocol: ObjCClassOrProtocol, isMeta: Boolean): Classifier {
        return declarationMapper.getKotlinClassFor(objCClassOrProtocol, isMeta)
    }

    override fun getKotlinClassForPointed(structDecl: StructDecl): Classifier {
        konst classifier = declarationMapper.getKotlinClassForPointed(structDecl)
        return classifier
    }

    open fun isCppClass(spelling: String): Boolean =
            error("Only meaningful with a proper cpp plugin")

    open fun managedWrapperClassifier(cppClassifier: Classifier): Classifier? =
            error("Only meaningful with a proper cpp plugin")

    open inner class DeclarationMapperImpl : DeclarationMapper {
        override fun getKotlinClassForPointed(structDecl: StructDecl): Classifier {
            konst baseName = structDecl.kotlinName
            konst pkg = when (platform) {
                KotlinPlatform.JVM -> pkgName
                KotlinPlatform.NATIVE -> if (structDecl.def == null) {
                    cnamesStructsPackageName // to be imported as forward declaration.
                } else {
                    getPackageFor(structDecl)
                }
            }
            return Classifier.topLevel(pkg, baseName)
        }

        override fun getKotlinClassForManaged(structDecl: StructDecl): Classifier =
                error("ManagedType requires a plugin")

        override fun isMappedToStrict(enumDef: EnumDef): Boolean = isStrictEnum(enumDef)

        override fun getKotlinNameForValue(enumDef: EnumDef): String = enumDef.kotlinName

        override fun getPackageFor(declaration: TypeDeclaration): String {
            return imports.getPackage(declaration.location) ?: pkgName
        }

        override konst useUnsignedTypes: Boolean
            get() = when (platform) {
                KotlinPlatform.JVM -> false
                KotlinPlatform.NATIVE -> true
            }
    }
}

data class StubIrBuilderResult(
        konst stubs: SimpleStubContainer,
        konst declarationMapper: DeclarationMapper,
        konst bridgeGenerationComponents: BridgeGenerationComponents,
        konst wrapperGenerationComponents: WrapperGenerationComponents
)

/**
 * Produces [StubIrBuilderResult] for given [KotlinPlatform] using [InteropConfiguration].
 */
class StubIrBuilder(private konst context: StubIrContext) {

    private konst configuration = context.configuration
    private konst nativeIndex: NativeIndex = context.nativeIndex

    private konst classes = mutableListOf<ClassStub>()
    private konst functions = mutableListOf<FunctionStub>()
    private konst globals = mutableListOf<PropertyStub>()
    private konst typealiases = mutableListOf<TypealiasStub>()
    private konst containers = mutableListOf<SimpleStubContainer>()

    private fun addStubs(stubs: List<StubIrElement>) = stubs.forEach(this::addStub)

    private fun addStub(stub: StubIrElement) {
        when(stub) {
            is ClassStub -> classes += stub
            is FunctionStub -> functions += stub
            is PropertyStub -> globals += stub
            is TypealiasStub -> typealiases += stub
            is SimpleStubContainer -> containers += stub
            else -> error("Unexpected stub: $stub")
        }
    }

    private konst excludedFunctions: Set<String>
        get() = configuration.excludedFunctions

    private konst excludedMacros: Set<String>
        get() = configuration.excludedMacros

    private konst buildingContext = context.plugin.stubsBuildingContext(context)

    fun build(): StubIrBuilderResult {
        nativeIndex.objCProtocols.filter { !it.isForwardDeclaration }.forEach { generateStubsForObjCProtocol(it) }
        nativeIndex.objCClasses.filter { !it.isForwardDeclaration && it.shouldBeIncludedIntoKotlinAPI() }
                .forEach { generateStubsForObjCClass(it) }
        nativeIndex.objCCategories.filter { it.clazz.shouldBeIncludedIntoKotlinAPI() }.forEach { generateStubsForObjCCategory(it) }
        nativeIndex.structs.forEach { generateStubsForStruct(it) }
        nativeIndex.enums.forEach { generateStubsForEnum(it) }
        nativeIndex.functions.filter { it.name !in excludedFunctions }.forEach { generateStubsForFunction(it) }
        nativeIndex.typedefs.forEach { generateStubsForTypedef(it) }
        // globals are sorted, so its numbering is stable and thus testable with golden data
        nativeIndex.globals.filter { it.name !in excludedFunctions }.sortedBy { it.name }.forEach { generateStubsForGlobal(it) }
        nativeIndex.macroConstants.filter { it.name !in excludedMacros }.forEach { generateStubsForMacroConstant(it) }
        nativeIndex.wrappedMacros.filter { it.name !in excludedMacros }.forEach { generateStubsForWrappedMacro(it) }

        konst meta = StubContainerMeta()
        konst stubs = SimpleStubContainer(
                meta,
                classes.toList(),
                functions.toList(),
                globals.toList(),
                typealiases.toList(),
                containers.toList()
        )
        return StubIrBuilderResult(
                stubs,
                buildingContext.declarationMapper,
                buildingContext.bridgeComponentsBuilder.build(),
                buildingContext.wrapperComponentsBuilder.build()
        )
    }

    private fun generateStubsForWrappedMacro(macro: WrappedMacroDef) {
        try {
            generateStubsForGlobal(GlobalDecl(macro.name, macro.type, isConst = true))
        } catch (e: Throwable) {
            context.log("Warning: cannot generate stubs for macro ${macro.name}")
        }
    }

    private fun generateStubsForMacroConstant(constant: ConstantDef) {
        try {
            addStubs(MacroConstantStubBuilder(buildingContext, constant).build())
        } catch (e: Throwable) {
            context.log("Warning: cannot generate stubs for constant ${constant.name}")
        }
    }

    private fun generateStubsForEnum(enumDef: EnumDef) {
        try {
            addStubs(EnumStubBuilder(buildingContext, enumDef).build())
        } catch (e: Throwable) {
            context.log("Warning: cannot generate definition for enum ${enumDef.spelling}")
        }
    }

    private fun generateStubsForFunction(func: FunctionDecl) {
        try {
            addStubs(FunctionStubBuilder(buildingContext, func, skipOverloads = true).build())
        } catch (e: Throwable) {
            context.log("Warning: cannot generate stubs for function ${func.name}")
        }
    }

    private fun generateStubsForStruct(decl: StructDecl) {
        try {
            addStubs(StructStubBuilder(buildingContext, decl).build())
        } catch (e: Throwable) {
            context.log("Warning: cannot generate definition for struct ${decl.spelling}")
        }
    }

    private fun generateStubsForTypedef(typedefDef: TypedefDef) {
        try {
            addStubs(TypedefStubBuilder(buildingContext, typedefDef).build())
        } catch (e: Throwable) {
            context.log("Warning: cannot generate typedef ${typedefDef.name}")
        }
    }

    private fun generateStubsForGlobal(global: GlobalDecl) {
        try {
            addStubs(GlobalStubBuilder(buildingContext, global).build())
        } catch (e: Throwable) {
            context.log("Warning: cannot generate stubs for global ${global.name}")
        }
    }

    private fun generateStubsForObjCProtocol(objCProtocol: ObjCProtocol) {
        addStubs(ObjCProtocolStubBuilder(buildingContext, objCProtocol).build())
    }

    private fun generateStubsForObjCClass(objCClass: ObjCClass) {
        addStubs(ObjCClassStubBuilder(buildingContext, objCClass).build())
    }

    private fun generateStubsForObjCCategory(objCCategory: ObjCCategory) {
        addStubs(ObjCCategoryStubBuilder(buildingContext, objCCategory).build())
    }
}
