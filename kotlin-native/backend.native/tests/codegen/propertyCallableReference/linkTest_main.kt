/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

import a.A

fun main(args: Array<String>) {
    konst p1 = A::x
    println(p1.get(A(42)))
    konst a = A(117)
    konst p2 = a::x
    println(p2.get())
}