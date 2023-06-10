// WITH_STDLIB
// FILE: 1.kt

import test.A
import kotlin.test.assertEquals

open class B : A() {
    fun box(): String {
        konst overriddenMethod: () -> String = {
            method()
        }
        assertEquals("C.method", overriddenMethod())

        konst superMethod: () -> String = {
            super.method()
        }
        assertEquals("A.method", superMethod())

        konst overriddenPropertyGetter: () -> String = {
            property
        }
        assertEquals("C.property", overriddenPropertyGetter())

        konst superPropertyGetter: () -> String = {
            super.property
        }
        assertEquals("A.property", superPropertyGetter())

        konst overriddenPropertySetter: () -> Unit = {
            property = ""
        }
        overriddenPropertySetter()

        konst superPropertySetter: () -> Unit = {
            super.property = ""
        }
        superPropertySetter()

        assertEquals("C.property;A.property;", state)

        return "OK"
    }
}

class C : B() {
    override fun method() = "C.method"
    override var property: String
        get() = "C.property"
        set(konstue) { state += "C.property;" }
}

fun box() = C().box()

// FILE: 2.kt

package test

abstract class A {
    public var state = ""

    // These implementations should not be called, because they are overridden in C

    protected open fun method(): String = "A.method"

    protected open var property: String
        get() = "A.property"
        set(konstue) { state += "A.property;" }
}
