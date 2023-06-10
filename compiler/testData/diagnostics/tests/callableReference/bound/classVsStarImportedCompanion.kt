// FIR_IDENTICAL
// FILE: 1.kt

package a

import b.B.*
import kotlin.reflect.KClass

class Companion

konst f: KClass<a.Companion> = Companion::class

// FILE: 2.kt

package b

class B {
    companion object Companion
}
