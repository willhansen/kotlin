// FIR_IDENTICAL
// FILE: 1.kt

package a

import b.*
import kotlin.reflect.KClass

class A
object B

konst f: KClass<a.A> = A::class
konst g: KClass<a.B> = B::class

// FILE: 2.kt

package b

object A
object B
