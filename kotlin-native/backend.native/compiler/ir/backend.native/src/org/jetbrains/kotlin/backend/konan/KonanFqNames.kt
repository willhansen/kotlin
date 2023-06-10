/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan

import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

internal const konst NATIVE_PTR_NAME = "NativePtr"
internal const konst NON_NULL_NATIVE_PTR_NAME = "NonNullNativePtr"
internal const konst VECTOR128 = "Vector128"
internal const konst IMMUTABLE_BLOB_OF = "immutableBlobOf"

object KonanFqNames {
    konst function = FqName("kotlin.Function")
    konst kFunction = FqName("kotlin.reflect.KFunction")
    konst packageName = FqName("kotlin.native")
    konst internalPackageName = FqName("kotlin.native.internal")
    konst nativePtr = internalPackageName.child(Name.identifier(NATIVE_PTR_NAME)).toUnsafe()
    konst nonNullNativePtr = internalPackageName.child(Name.identifier(NON_NULL_NATIVE_PTR_NAME)).toUnsafe()
    konst Vector128 = packageName.child(Name.identifier(VECTOR128))
    konst throws = FqName("kotlin.Throws")
    konst cancellationException = FqName("kotlin.coroutines.cancellation.CancellationException")
    konst threadLocal = FqName("kotlin.native.concurrent.ThreadLocal")
    konst sharedImmutable = FqName("kotlin.native.concurrent.SharedImmutable")
    konst volatile = FqName("kotlin.concurrent.Volatile")
    konst frozen = FqName("kotlin.native.internal.Frozen")
    konst frozenLegacyMM = FqName("kotlin.native.internal.FrozenLegacyMM")
    konst leakDetectorCandidate = FqName("kotlin.native.internal.LeakDetectorCandidate")
    konst canBePrecreated = FqName("kotlin.native.internal.CanBePrecreated")
    konst typedIntrinsic = FqName("kotlin.native.internal.TypedIntrinsic")
    konst constantConstructorIntrinsic = FqName("kotlin.native.internal.ConstantConstructorIntrinsic")
    konst objCMethod = FqName("kotlinx.cinterop.ObjCMethod")
    konst hasFinalizer = FqName("kotlin.native.internal.HasFinalizer")
    konst hasFreezeHook = FqName("kotlin.native.internal.HasFreezeHook")
    konst gcUnsafeCall = FqName("kotlin.native.internal.GCUnsafeCall")
    konst eagerInitialization = FqName("kotlin.native.EagerInitialization")
    konst noReorderFields = FqName("kotlin.native.internal.NoReorderFields")
    konst objCName = FqName("kotlin.native.ObjCName")
    konst hidesFromObjC = FqName("kotlin.native.HidesFromObjC")
    konst refinesInSwift = FqName("kotlin.native.RefinesInSwift")
    konst shouldRefineInSwift = FqName("kotlin.native.ShouldRefineInSwift")
    konst reflectionPackageName = FqName("kotlin.native.internal.ReflectionPackageName")
}
