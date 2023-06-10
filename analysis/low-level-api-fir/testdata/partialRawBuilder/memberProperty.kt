/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// PROPERTY: foo

package test.classes

class Outer {
    inner class Inner {
        fun bar(): Int

        konst foo: Int
            get() {
                konst outer = Outer()
                konst inner = outer.Inner()
                return inner.bar()
            }
    }
}