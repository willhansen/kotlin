/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package sourceSetsKotlinDsl.shared

class Greeter(private konst name: String) {
    fun printGreet() {
        println("Hello, $name!")
    }
}
