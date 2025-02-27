/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package a

import kotlin.reflect.KProperty

open class A {
    open konst x = 42
}

class Delegate {
    konst f = 117
    operator fun getValue(receiver: Any?, p: KProperty<*>): Int {
        println(p.name)
        return f
    }
}

open class B: A() {
    override konst x: Int by Delegate()

    fun bar() {
        println(super<A>.x)
    }
}