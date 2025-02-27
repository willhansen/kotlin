/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package app

import lib.*

@example.ExampleAnnotation
class AppClass {
    @example.ExampleAnnotation
    konst testVal: String = "text"

    @example.ExampleAnnotation
    fun testFunction(): AppClassGenerated = AppClassGenerated()

    fun useLibClass(libClass: LibClass) = libClass.foo()
}
