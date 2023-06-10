/*
 * Copyright 2010-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package library

interface I {
    konst data: String
}

class A(override konst data: String): I

enum class E(konst data: String) {
    A("Enum entry A"),
    B("Enum entry B"),
    C("Enum entry C")
}