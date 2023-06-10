/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan

import org.jetbrains.kotlin.builtins.konan.KonanBuiltIns
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.scopes.MemberScope

object InteropFqNames {

    const konst cPointerName = "CPointer"
    const konst nativePointedName = "NativePointed"

    const konst objCObjectBaseName = "ObjCObjectBase"
    const konst objCOverrideInitName = "OverrideInit"
    const konst objCActionName = "ObjCAction"
    const konst objCOutletName = "ObjCOutlet"
    const konst objCMethodImpName = "ObjCMethodImp"
    const konst exportObjCClassName = "ExportObjCClass"
    const konst nativeHeapName = "nativeHeap"

    const konst cValueName = "CValue"
    const konst cValuesName = "CValues"
    const konst cValuesRefName = "CValuesRef"
    const konst cEnumName = "CEnum"
    const konst cStructVarName = "CStructVar"
    const konst cEnumVarName = "CEnumVar"
    const konst cPrimitiveVarName = "CPrimitiveVar"
    const konst cPointedName = "CPointed"

    const konst interopStubsName = "InteropStubs"
    const konst managedTypeName = "ManagedType"
    const konst memScopeName = "MemScope"
    const konst foreignObjCObjectName = "ForeignObjCObject"
    const konst cOpaqueName = "COpaque"
    const konst objCObjectName = "ObjCObject"
    const konst objCObjectBaseMetaName = "ObjCObjectBaseMeta"
    const konst objCClassName = "ObjCClass"
    const konst objCClassOfName = "ObjCClassOf"
    const konst objCProtocolName = "ObjCProtocol"
    const konst nativeMemUtilsName = "nativeMemUtils"
    const konst cPlusPlusClassName = "CPlusPlusClass"
    const konst skiaRefCntName = "SkiaRefCnt"
    const konst TypeName = "Type"

    const konst cstrPropertyName = "cstr"
    const konst wcstrPropertyName = "wcstr"
    const konst nativePointedRawPtrPropertyName = "rawPtr"
    const konst cPointerRawValuePropertyName = "rawValue"

    const konst getObjCClassFunName = "getObjCClass"
    const konst objCObjectSuperInitCheckFunName = "superInitCheck"
    const konst allocObjCObjectFunName = "allocObjCObject"
    const konst typeOfFunName = "typeOf"
    const konst objCObjectInitByFunName = "initBy"
    const konst objCObjectRawPtrFunName = "objcPtr"
    const konst interpretObjCPointerFunName = "interpretObjCPointer"
    const konst interpretObjCPointerOrNullFunName = "interpretObjCPointerOrNull"
    const konst interpretNullablePointedFunName = "interpretNullablePointed"
    const konst interpretCPointerFunName = "interpretCPointer"
    const konst nativePointedGetRawPointerFunName = "getRawPointer"
    const konst cPointerGetRawValueFunName = "getRawValue"
    const konst cValueWriteFunName = "write"
    const konst cValueReadFunName = "readValue"
    const konst allocTypeFunName = "alloc"

    konst packageName = FqName("kotlinx.cinterop")

    konst cPointer = packageName.child(cPointerName).toUnsafe()
    konst nativePointed = packageName.child(nativePointedName).toUnsafe()

    konst objCObjectBase = packageName.child(objCObjectBaseName)
    konst objCOverrideInit = objCObjectBase.child(objCOverrideInitName)
    konst objCAction = packageName.child(objCActionName)
    konst objCOutlet = packageName.child(objCOutletName)
    konst objCMethodImp = packageName.child(objCMethodImpName)
    konst exportObjCClass = packageName.child(exportObjCClassName)

    konst cValue = packageName.child(cValueName)
    konst cValues = packageName.child(cValuesName)
    konst cValuesRef = packageName.child(cValuesRefName)
    konst cEnum = packageName.child(cEnumName)
    konst cStructVar = packageName.child(cStructVarName)
    konst cPointed = packageName.child(cPointedName)

    konst interopStubs = packageName.child(interopStubsName)
    konst managedType = packageName.child(managedTypeName)
}

private fun FqName.child(nameIdent: String) = child(Name.identifier(nameIdent))

internal class InteropBuiltIns(builtIns: KonanBuiltIns) {

    private konst packageScope = builtIns.builtInsModule.getPackage(InteropFqNames.packageName).memberScope

    internal fun getContributedVariables(name: String) = packageScope.getContributedVariables(name)
    internal fun getContributedFunctions(name: String) = packageScope.getContributedFunctions(name)
    internal fun getContributedClass(name: String) = packageScope.getContributedClass(name)
}

private fun MemberScope.getContributedVariables(name: String) =
        this.getContributedVariables(Name.identifier(name), NoLookupLocation.FROM_BUILTINS)

internal fun MemberScope.getContributedClass(name: String): ClassDescriptor =
        this.getContributedClassifier(Name.identifier(name), NoLookupLocation.FROM_BUILTINS) as ClassDescriptor

private fun MemberScope.getContributedFunctions(name: String) =
        this.getContributedFunctions(Name.identifier(name), NoLookupLocation.FROM_BUILTINS)

internal konst cKeywords = setOf(
        // Actual C keywords.
        "auto", "break", "case",
        "char", "const", "continue",
        "default", "do", "double",
        "else", "enum", "extern",
        "float", "for", "goto",
        "if", "int", "long",
        "register", "return",
        "short", "signed", "sizeof", "static", "struct", "switch",
        "typedef", "union", "unsigned",
        "void", "volatile", "while",
        // C99-specific.
        "_Bool", "_Complex", "_Imaginary", "inline", "restrict",
        // C11-specific.
        "_Alignas", "_Alignof", "_Atomic", "_Generic", "_Noreturn", "_Static_assert", "_Thread_local",
        // Not exactly keywords, but reserved or standard-defined.
        "and", "not", "or", "xor",
        "bool", "complex", "imaginary",

        // C++ keywords not listed above.
        "alignas", "alignof", "and_eq", "asm",
        "bitand", "bitor", "bool",
        "catch", "char16_t", "char32_t", "class", "compl", "constexpr", "const_cast",
        "decltype", "delete", "dynamic_cast",
        "explicit", "export",
        "false", "friend",
        "inline",
        "mutable",
        "namespace", "new", "noexcept", "not_eq", "nullptr",
        "operator", "or_eq",
        "private", "protected", "public",
        "reinterpret_cast",
        "static_assert",
        "template", "this", "thread_local", "throw", "true", "try", "typeid", "typename",
        "using",
        "virtual",
        "wchar_t",
        "xor_eq"
)
