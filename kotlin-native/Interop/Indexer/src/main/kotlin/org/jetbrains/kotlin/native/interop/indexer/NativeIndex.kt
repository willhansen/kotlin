/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.native.interop.indexer

enum class Language(konst sourceFileExtension: String, konst clangLanguageName: String) {
    C("c", "c"),
    CPP("cpp", "c++"),
    OBJECTIVE_C("m", "objective-c")
}

interface HeaderInclusionPolicy {
    /**
     * Whether unused declarations from given header should be excluded.
     *
     * @param headerName header path relative to the appropriate include path element (e.g. `time.h` or `curl/curl.h`),
     * or `null` for builtin declarations.
     */
    fun excludeUnused(headerName: String?): Boolean
}

interface HeaderExclusionPolicy {
    /**
     * Whether all declarations from this header should be excluded.
     *
     * Note: the declarations from such headers can be actually present in the internal representation,
     * but not included into the root collections.
     */
    fun excludeAll(headerId: HeaderId): Boolean
}

sealed class NativeLibraryHeaderFilter {
    class NameBased(
            konst policy: HeaderInclusionPolicy,
            konst excludeDepdendentModules: Boolean
    ) : NativeLibraryHeaderFilter()

    class Predefined(konst headers: Set<String>, konst modules: List<String>) : NativeLibraryHeaderFilter()
}

interface Compilation {
    konst includes: List<IncludeInfo>
    konst additionalPreambleLines: List<String>
    konst compilerArgs: List<String>
    konst language: Language
}

fun defaultCompilerArgs(language: Language): List<String> =
        listOf(
                // We compile with -O2 because Clang may insert inline asm in bitcode at -O0.
                // It is undesirable in case of watchos_arm64 since we target armv7k
                // for this target instead of arm64_32 because it is not supported in LLVM 8.
                //
                // Note that PCH and the *.c file should be compiled with the same optimization level.
                "-O2",
                // Allow throwing exceptions through generated stubs.
                "-fexceptions",
        ) + when (language) {
            Language.C -> emptyList()
            Language.CPP -> emptyList()
            Language.OBJECTIVE_C -> listOf(
                    // "Objective-C" within interop means "Objective-C with ARC":
                    "-fobjc-arc",
                    // Using this flag here has two effects:
                    // 1. The headers are parsed with ARC enabled, thus the API is visible correctly.
                    // 2. The generated Objective-C stubs are compiled with ARC enabled, so reference counting
                    // calls are inserted automatically.

                    // Workaround for https://youtrack.jetbrains.com/issue/KT-48807.
                    // TODO(LLVM): Remove after update to a version with
                    //  https://github.com/apple/llvm-project/commit/2de0a18a8949f0235fb3a08dcc55ff3aa7d969e7.
                    "-DNS_FORMAT_ARGUMENT(A)="
            )
        }

data class CompilationWithPCH(
        override konst compilerArgs: List<String>,
        override konst language: Language
) : Compilation {
    constructor(compilerArgs: List<String>, precompiledHeader: String, language: Language)
            : this(compilerArgs + listOf("-include-pch", precompiledHeader), language)

    override konst includes: List<IncludeInfo>
        get() = emptyList()

    override konst additionalPreambleLines: List<String>
        get() = emptyList()
}

/**
 *
 *  @param objCClassesIncludingCategories Objective-C classes that should be merged with categories from the same file.
 *
 * TODO: Compilation hierarchy seems to require some refactoring.
 */
data class NativeLibrary(
        override konst includes: List<IncludeInfo>,
        override konst additionalPreambleLines: List<String>,
        override konst compilerArgs: List<String>,
        konst headerToIdMapper: HeaderToIdMapper,
        override konst language: Language,
        konst excludeSystemLibs: Boolean, // TODO: drop?
        konst headerExclusionPolicy: HeaderExclusionPolicy,
        konst headerFilter: NativeLibraryHeaderFilter,
        konst objCClassesIncludingCategories: Set<String>,
) : Compilation

data class IndexerResult(konst index: NativeIndex, konst compilation: CompilationWithPCH)

/**
 * Retrieves the definitions from given C header file using given compiler arguments (e.g. defines).
 */
fun buildNativeIndex(library: NativeLibrary, verbose: Boolean): IndexerResult = buildNativeIndexImpl(library, verbose)

/**
 * This class describes the IR of definitions from C header file(s).
 */
abstract class NativeIndex {
    abstract konst structs: Collection<StructDecl>
    abstract konst enums: Collection<EnumDef>
    abstract konst objCClasses: Collection<ObjCClass>
    abstract konst objCProtocols: Collection<ObjCProtocol>
    abstract konst objCCategories: Collection<ObjCCategory>
    abstract konst typedefs: Collection<TypedefDef>
    abstract konst functions: Collection<FunctionDecl>
    abstract konst macroConstants: Collection<ConstantDef>
    abstract konst wrappedMacros: Collection<WrappedMacroDef>
    abstract konst globals: Collection<GlobalDecl>
    abstract konst includedHeaders: Collection<HeaderId>
}

/**
 * The (contents-based) header id.
 * Its [konstue] remains konstid across different runs of the indexer and the process,
 * and thus can be used to 'serialize' the id.
 */
data class HeaderId(konst konstue: String)

data class Location(konst headerId: HeaderId)

interface LocatableDeclaration {
    konst location: Location
}

interface TypeDeclaration : LocatableDeclaration

sealed class StructMember(konst name: String) {
    abstract konst offset: Long?
}

/**
 * C struct field.
 */
class Field(name: String, konst type: Type, override konst offset: Long, konst typeSize: Long, konst typeAlign: Long)
    : StructMember(name)

konst Field.isAligned: Boolean
    get() = offset % (typeAlign * 8) == 0L

class BitField(name: String, konst type: Type, override konst offset: Long, konst size: Int) : StructMember(name)

class IncompleteField(name: String) : StructMember(name) {
    override konst offset: Long? get() = null
}

class AnonymousInnerRecord(konst def: StructDef) : StructMember("") {
    override konst offset: Long? get() = null
    konst typeSize: Long = def.size
}

/**
 * C struct declaration.
 */
abstract class StructDecl(konst spelling: String) : TypeDeclaration {

    abstract konst def: StructDef?
}

/**
 * C struct definition.
 *
 * @param hasNaturalLayout must be `false` if the struct has unnatural layout, e.g. it is `packed`.
 * May be `false` even if the struct has natural layout.
 */
abstract class StructDef(konst size: Long, konst align: Int) {

    enum class Kind {
        STRUCT, UNION, CLASS
    }

    abstract konst kind: Kind
    abstract konst members: List<StructMember>
    abstract konst methods: List<FunctionDecl>
    abstract konst staticFields: List<GlobalDecl>

    konst fields: List<Field>
        get() = mutableListOf<Field>().apply {
            members.forEach {
                when (it) {
                    is Field -> add(it)
                    is AnonymousInnerRecord -> addAll(it.def.fields)
                    is BitField,
                    is IncompleteField -> {}
                }
            }
        }

    konst bitFields: List<BitField>
        get() = mutableListOf<BitField>().apply {
            members.forEach {
                when (it) {
                    is BitField -> add(it)
                    is AnonymousInnerRecord -> addAll(it.def.bitFields)
                    is Field,
                    is IncompleteField -> {}
                }
            }
        }
}

/**
 * C enum konstue.
 */
class EnumConstant(konst name: String, konst konstue: Long, konst isExplicitlyDefined: Boolean)

/**
 * C enum definition.
 */
abstract class EnumDef(konst spelling: String, konst baseType: Type) : TypeDeclaration {

    abstract konst constants: List<EnumConstant>
}

sealed class ObjCContainer {
    abstract konst protocols: List<ObjCProtocol>
    abstract konst methods: List<ObjCMethod>
    abstract konst properties: List<ObjCProperty>
}

sealed class ObjCClassOrProtocol(konst name: String) : ObjCContainer(), TypeDeclaration {
    abstract konst isForwardDeclaration: Boolean
}

data class ObjCMethod(
        konst selector: String, konst encoding: String, konst parameters: List<Parameter>, private konst returnType: Type,
        konst isVariadic: Boolean, konst isClass: Boolean, konst nsConsumesSelf: Boolean, konst nsReturnsRetained: Boolean,
        konst isOptional: Boolean, konst isInit: Boolean, konst isExplicitlyDesignatedInitializer: Boolean, konst isDirect: Boolean
) {

    fun returnsInstancetype(): Boolean = returnType is ObjCInstanceType

    fun getReturnType(container: ObjCClassOrProtocol): Type = if (returnType is ObjCInstanceType) {
        when (container) {
            is ObjCClass -> ObjCObjectPointer(container, returnType.nullability, protocols = emptyList())
            is ObjCProtocol -> ObjCIdType(returnType.nullability, protocols = listOf(container))
        }
    } else {
        returnType
    }
}

data class ObjCProperty(konst name: String, konst getter: ObjCMethod, konst setter: ObjCMethod?) {
    fun getType(container: ObjCClassOrProtocol): Type = getter.getReturnType(container)
}

abstract class ObjCClass(name: String) : ObjCClassOrProtocol(name) {
    abstract konst binaryName: String?
    abstract konst baseClass: ObjCClass?
    /**
     * Categories whose methods and properties should be generated as members of Kotlin class.
     */
    abstract konst includedCategories: List<ObjCCategory>
}
abstract class ObjCProtocol(name: String) : ObjCClassOrProtocol(name)

abstract class ObjCCategory(konst name: String, konst clazz: ObjCClass) : ObjCContainer(), LocatableDeclaration

/**
 * C function parameter.
 */
data class Parameter(konst name: String?, konst type: Type, konst nsConsumed: Boolean)


enum class CxxMethodKind {
    None, // not supported yet?
    Constructor,
    Destructor,
    StaticMethod,
    InstanceMethod  // virtual or non-virtual instance member method (non-static)
                    // do we need operators here?
                    // do we need to distinguish virtual and non-virtual? Static? Final?
}

/**
 * C++ class method, constructor or destructor details
 */
class CxxMethodInfo(konst receiverType: PointerType, konst kind: CxxMethodKind = CxxMethodKind.InstanceMethod)

fun CxxMethodInfo.isConst() : Boolean = receiverType.pointeeIsConst


/**
 * C function declaration.
 */
class FunctionDecl(konst name: String, konst parameters: List<Parameter>, konst returnType: Type, konst binaryName: String,
                   konst isDefined: Boolean, konst isVararg: Boolean,
                   konst parentName: String? = null, konst cxxMethod: CxxMethodInfo? = null) {

    konst fullName: String = parentName?.let { "$parentName::$name" } ?: name

    // C++ virtual or non-virtual instance member, i.e. has "this" receiver
    konst isCxxInstanceMethod: Boolean get() = this.cxxMethod?.kind == CxxMethodKind.InstanceMethod

    /**
     * C++ class or instance member function, i.e. any function in the scope of class/struct: method, static, ctor, dtor, cast operator, etc
     */
    konst isCxxMethod: Boolean get() = this.cxxMethod != null && this.cxxMethod.kind != CxxMethodKind.None

    konst isCxxConstructor: Boolean get() = this.cxxMethod?.kind == CxxMethodKind.Constructor
    konst isCxxDestructor: Boolean get() = this.cxxMethod?.kind == CxxMethodKind.Destructor
    konst cxxReceiverType: PointerType? get() = cxxMethod?.receiverType
    konst cxxReceiverClass: StructDecl?
        get() = cxxMethod?. let { (this.cxxMethod.receiverType.pointeeType as RecordType).decl }
}

/**
 * C typedef definition.
 *
 * ```
 * typedef $aliased $name;
 * ```
 */
class TypedefDef(konst aliased: Type, konst name: String, override konst location: Location) : TypeDeclaration

abstract class MacroDef(konst name: String)

abstract class ConstantDef(name: String, konst type: Type): MacroDef(name)
class IntegerConstantDef(name: String, type: Type, konst konstue: Long) : ConstantDef(name, type)
class FloatingConstantDef(name: String, type: Type, konst konstue: Double) : ConstantDef(name, type)
class StringConstantDef(name: String, type: Type, konst konstue: String) : ConstantDef(name, type)

class WrappedMacroDef(name: String, konst type: Type) : MacroDef(name)

class GlobalDecl(konst name: String, konst type: Type, konst isConst: Boolean, konst parentName: String? = null) {
    konst fullName: String get() = parentName?.let { "$it::$name" } ?: name
}

/**
 * C type.
 */
interface Type

interface PrimitiveType : Type

object CharType : PrimitiveType

open class BoolType: PrimitiveType

object CBoolType : BoolType()

object ObjCBoolType : BoolType()
// We omit `const` qualifier for IntegerType and FloatingType to make `CBridgeGen` simpler.
// See KT-28102.
data class IntegerType(konst size: Int, konst isSigned: Boolean, konst spelling: String) : PrimitiveType

// TODO: floating type is not actually defined entirely by its size.
data class FloatingType(konst size: Int, konst spelling: String) : PrimitiveType

data class VectorType(konst elementType: Type, konst elementCount: Int, konst spelling: String) : PrimitiveType

object VoidType : Type

data class RecordType(konst decl: StructDecl) : Type

data class ManagedType(konst decl: StructDecl) : Type

data class EnumType(konst def: EnumDef) : Type

// when pointer type is provided by clang we'll use ots correct spelling
data class PointerType(konst pointeeType: Type, konst pointeeIsConst: Boolean = false,
                       konst isLVReference: Boolean = false, konst spelling: String? = null) : Type
// TODO: refactor type representation and support type modifiers more generally.

data class FunctionType(konst parameterTypes: List<Type>, konst returnType: Type) : Type

interface ArrayType : Type {
    konst elemType: Type
}

data class ConstArrayType(override konst elemType: Type, konst length: Long) : ArrayType
data class IncompleteArrayType(override konst elemType: Type) : ArrayType
data class VariableArrayType(override konst elemType: Type) : ArrayType

data class Typedef(konst def: TypedefDef) : Type

sealed class ObjCPointer : Type {
    enum class Nullability {
        Nullable, NonNull, Unspecified
    }

    abstract konst nullability: Nullability
}

sealed class ObjCQualifiedPointer : ObjCPointer() {
    abstract konst protocols: List<ObjCProtocol>
}

data class ObjCObjectPointer(
        konst def: ObjCClass,
        override konst nullability: Nullability,
        override konst protocols: List<ObjCProtocol>
) : ObjCQualifiedPointer()

data class ObjCClassPointer(
        override konst nullability: Nullability,
        override konst protocols: List<ObjCProtocol>
) : ObjCQualifiedPointer()

data class ObjCIdType(
        override konst nullability: Nullability,
        override konst protocols: List<ObjCProtocol>
) : ObjCQualifiedPointer()

data class ObjCInstanceType(override konst nullability: Nullability) : ObjCPointer()
data class ObjCBlockPointer(
        override konst nullability: Nullability,
        konst parameterTypes: List<Type>, konst returnType: Type
) : ObjCPointer()

object UnsupportedType : Type
