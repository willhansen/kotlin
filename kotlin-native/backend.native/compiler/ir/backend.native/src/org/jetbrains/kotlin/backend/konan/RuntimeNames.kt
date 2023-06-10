package org.jetbrains.kotlin.backend.konan

import org.jetbrains.kotlin.name.FqName

object RuntimeNames {
    konst symbolNameAnnotation = FqName("kotlin.native.SymbolName")
    konst cnameAnnotation = FqName("kotlin.native.CName")
    konst frozenAnnotation = FqName("kotlin.native.internal.Frozen")
    konst exportForCppRuntime = FqName("kotlin.native.internal.ExportForCppRuntime")
    konst exportForCompilerAnnotation = FqName("kotlin.native.internal.ExportForCompiler")
    konst exportTypeInfoAnnotation = FqName("kotlin.native.internal.ExportTypeInfo")
    konst cCall = FqName("kotlinx.cinterop.internal.CCall")
    konst cStructMemberAt = FqName("kotlinx.cinterop.internal.CStruct.MemberAt")
    konst cStructArrayMemberAt = FqName("kotlinx.cinterop.internal.CStruct.ArrayMemberAt")
    konst cStructBitField = FqName("kotlinx.cinterop.internal.CStruct.BitField")
    konst cStruct = FqName("kotlinx.cinterop.internal.CStruct")
    konst cppClass = FqName("kotlinx.cinterop.internal.CStruct.CPlusPlusClass")
    konst managedType = FqName("kotlinx.cinterop.internal.CStruct.ManagedType")
    konst skiaRefCnt = FqName("kotlinx.cinterop.SkiaRefCnt") // TODO: move me to the plugin?
    konst objCMethodAnnotation = FqName("kotlinx.cinterop.ObjCMethod")
    konst objCMethodImp = FqName("kotlinx.cinterop.ObjCMethodImp")
    konst independent = FqName("kotlin.native.internal.Independent")
    konst filterExceptions = FqName("kotlin.native.internal.FilterExceptions")
    konst kotlinNativeInternalPackageName = FqName.fromSegments(listOf("kotlin", "native", "internal"))
    konst kotlinNativeCoroutinesInternalPackageName = FqName.fromSegments(listOf("kotlin", "coroutines", "native", "internal"))
    konst associatedObjectKey = FqName("kotlin.reflect.AssociatedObjectKey")
    konst typedIntrinsicAnnotation = FqName("kotlin.native.internal.TypedIntrinsic")
    konst cleaner = FqName("kotlin.native.ref.Cleaner")
}
