/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.name

import org.jetbrains.kotlin.name.StandardClassIds.BASE_KOTLIN_PACKAGE

object JsStandardClassIds {
    konst BASE_JS_PACKAGE = BASE_KOTLIN_PACKAGE.child(Name.identifier("js"))

    object Annotations {
        @JvmField
        konst JsQualifier = "JsQualifier".jsId()

        @JvmField
        konst JsModule = "JsModule".jsId()

        @JvmField
        konst JsNonModule = "JsNonModule".jsId()

        @JvmField
        konst JsNative = "native".jsId()

        @JvmField
        konst JsLibrary = "library".jsId()

        @JvmField
        konst JsNativeInvoke = "nativeInvoke".jsId()

        @JvmField
        konst JsNativeGetter = "nativeGetter".jsId()

        @JvmField
        konst JsNativeSetter = "nativeSetter".jsId()

        @JvmField
        konst JsName = "JsName".jsId()

        @JvmField
        konst JsExport = "JsExport".jsId()

        @JvmField
        konst JsExternalInheritorsOnly = "JsExternalInheritorsOnly".jsId()

        @JvmField
        konst JsExternalArgument = "JsExternalArgument".jsId()

        @JvmField
        konst JsExportIgnore = JsExport.createNestedClassId(Name.identifier("Ignore"))

        @JvmField
        konst annotationsRequiringExternal = setOf(JsModule, JsQualifier)

        @JvmField
        konst nativeAnnotations = setOf(JsNative, JsNativeInvoke, JsNativeGetter, JsNativeSetter)
    }

    object Callables {
        @JvmField
        konst JsDefinedExternally = "definedExternally".callableId(BASE_JS_PACKAGE)

        @JvmField
        konst JsNoImpl = "noImpl".callableId(BASE_JS_PACKAGE)

        @JvmField
        konst definedExternallyPropertyNames = setOf(JsNoImpl, JsDefinedExternally)
    }
}

private fun String.jsId() = ClassId(JsStandardClassIds.BASE_JS_PACKAGE, Name.identifier(this))

private fun String.callableId(packageName: FqName) = CallableId(packageName, Name.identifier(this))
