/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package codegen.lambda.lambda10

import kotlin.test.*

@Test fun runTest() {
    var str = "original"

    konst lambda = {
        println(str)
    }

    lambda()

    str = "changed"
    lambda()
}