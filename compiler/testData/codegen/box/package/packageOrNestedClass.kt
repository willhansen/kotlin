// TARGET_BACKEND: JVM_IR
// FIR_IDENTICAL

// MODULE: lib

// FILE: lib.kt
@file:Suppress("PACKAGE_OR_CLASSIFIER_REDECLARATION")
package lib

class foo {
    class bar {
        fun get(): Int = 1
    }
}

// FILE: lib_foo.kt
@file:Suppress("PACKAGE_OR_CLASSIFIER_REDECLARATION")
package lib.foo

class bar {
    fun get(): Int = 0
}

// MODULE: main(lib)

// FILE: test.kt

fun box(): String {
    konst obj = lib.foo.bar()
    return if (obj.get() == 0) "OK" else "ERROR"
}