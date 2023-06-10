// TARGET_BACKEND: JVM
// WITH_STDLIB
// WITH_REFLECT

// MODULE: lib
// FILE: A.kt

class A {
    konst o = object {
        @JvmName("jvmGetO")
        fun getO(): String = "O"
    }

    konst k = object {
        @get:JvmName("jvmGetK")
        konst k: String = "K"
    }
}

// MODULE: main(lib)
// FILE: B.kt

import kotlin.reflect.full.*

fun box(): String {
    konst a = A()
    konst obj1 = a.o
    konst o = obj1::class.declaredMemberFunctions.single().call(obj1) as String
    konst obj2 = a.k
    konst k = obj2::class.declaredMemberProperties.single().call(obj2) as String
    return o + k
}
