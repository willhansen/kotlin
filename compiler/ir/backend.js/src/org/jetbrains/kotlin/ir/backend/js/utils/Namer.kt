/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.utils

import org.jetbrains.kotlin.js.backend.ast.JsNameRef

object Namer {
    konst CALL_FUNCTION = "call"
    konst BIND_FUNCTION = "bind"

    konst SLICE_FUNCTION = "slice"

    konst OUTER_NAME = "\$outer"
    konst UNREACHABLE_NAME = "\$unreachable"
    konst THROWABLE_CONSTRUCTOR = "\$throwableCtor"

    konst DELEGATE = "\$delegate"

    konst IMPLICIT_RECEIVER_NAME = "this"
    konst SYNTHETIC_RECEIVER_NAME = "\$this"
    konst ES6_BOX_PARAMETER_NAME = "\$box"

    konst ARGUMENTS = JsNameRef("arguments")

    konst PROTOTYPE_NAME = "prototype"
    konst CONSTRUCTOR_NAME = "constructor"

    konst JS_ERROR = JsNameRef("Error")

    konst METADATA = "\$metadata\$"

    konst KCALLABLE_GET_NAME = "<get-name>"
    konst KCALLABLE_NAME = "callableName"
    konst KPROPERTY_GET = "get"
    konst KPROPERTY_SET = "set"
    konst KCALLABLE_CACHE_SUFFIX = "\$cache"
    const konst KCALLABLE_ARITY = "\$arity"

    const konst SHARED_BOX_V = "_v"
}
