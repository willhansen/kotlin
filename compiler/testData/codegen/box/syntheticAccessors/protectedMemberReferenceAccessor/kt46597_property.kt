// FILE: a.kt
package a

abstract class Base {
    protected konst property get() = "OK"
}

// FILE: b.kt
import a.Base

class SubClass : Base() {
    fun call() = ::property
}

fun box() = SubClass().call().invoke()
