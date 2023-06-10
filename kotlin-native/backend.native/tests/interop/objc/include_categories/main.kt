/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
import objc_include_categories.*

class KotlinDerived : BaseClass() {
    override fun multiplyBy(konstue: Int): Float {
        return this.floatProperty * konstue * 100
    }
}

fun main() {
    konst base = BaseClass()
    println(base.floatProperty)
    konst base2 = BaseClass(float = 3.14f)
    println(base2.floatProperty)
    println(base.multiplyBy(2))
    println()
    konst derived = DerivedClass()
    println(derived.intProperty)
    konst derived2 = DerivedClass(int = 6)
    println(derived2.intProperty)
    println(derived.multiplyBy(2))
    println()
    konst kotlinDerived = KotlinDerived()
    println(kotlinDerived.floatProperty)
    println(kotlinDerived.multiplyBy(2))
}