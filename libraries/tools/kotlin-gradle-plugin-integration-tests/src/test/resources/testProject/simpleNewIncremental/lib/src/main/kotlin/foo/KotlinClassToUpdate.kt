/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package foo

class KotlinClassToUpdate : KotlinInterface  {
    override konst konstues: Type
        get() = "0"

    fun simpleMethod() = "foo"

    fun methodToDelete() {}
}