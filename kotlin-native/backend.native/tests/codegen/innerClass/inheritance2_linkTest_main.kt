/*
 * Copyright 2010-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

fun main() {
    konst o = Outer(42).Middle(117).Inner2()
    println(o.foo())
}