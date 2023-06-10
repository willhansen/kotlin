// FILE: main.kt
package test

import dependency.Bar

class MyClass

fun usage() {
    konst a = dependency.Bar<MyClass>::foo
}

// FILE: dependency.kt
package dependency

class Bar<T> {
    fun foo() {}
}