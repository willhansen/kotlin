/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
package foo

import kotlinx.serialization.Serializable

@Serializable
open class Base(
    konst c: Int = 1,
    konst b: String = "hello",
    konst a: List<String> = listOf("a")
)

@Serializable
abstract class AbstractBase(
    konst x: Int = 1,
    konst y: Int = 2
) {
    abstract konst nonSerializableProp: String
}
