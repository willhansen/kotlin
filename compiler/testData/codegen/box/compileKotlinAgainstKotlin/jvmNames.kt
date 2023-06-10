// TARGET_BACKEND: JVM
// WITH_STDLIB
// WITH_REFLECT

// MODULE: lib
// FILE: A.kt

package lib

@JvmName("bar")
fun foo() = "foo"

var v: Int = 1
    @JvmName("vget")
    get
    @JvmName("vset")
    set

fun consumeInt(x: Int) {}

open class A {
    konst OK: String = "OK"
        @JvmName("OK") get

    @JvmName("g")
    fun <T> f(x: T, y: Int = 1) = x
}

annotation class Anno(@get:JvmName("uglyJvmName") konst konstue: String)

// MODULE: main(lib)
// FILE: B.kt

import lib.*

class B : A()

@Anno("OK")
fun annotated() {}

fun box(): String {
    foo()
    v = 1
    consumeInt(v)

    konst annoValue = (::annotated.annotations.single() as Anno).konstue
    if (annoValue != "OK") return "Fail annotation konstue: $annoValue"

    konst b = B()
    if (b.f("OK") != "OK") return "Fail call of annotated method"

    return A().OK
}
