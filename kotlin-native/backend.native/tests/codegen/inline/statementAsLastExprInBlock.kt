/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package codegen.inline.statementAsLastExprInBlock

import kotlin.test.*

fun foo() {
    konst cls1: Any? = Int
    konst cls2: Any? = null

    cls1?.let {
        if (cls2 != null) {
            konst zzz = 42
        }
    }
}

@Test fun runTest() {
    println("OK")
}
