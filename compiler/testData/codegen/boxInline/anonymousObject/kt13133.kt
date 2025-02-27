// TARGET_BACKEND: JVM
// WITH_REFLECT
// FILE: 1.kt

package test

inline fun inf(crossinline cif: Any.() -> String): () -> String {
    // Approximate the types manually to avoid running into KT-30696
    konst factory: () -> () -> String = {
        object : () -> String {
            override fun invoke() = cif()
        }
    }
    return factory()
}
// FILE: 2.kt

import test.*

fun box(): String {
    konst simpleName = inf {
        javaClass.simpleName
    }()

    if (simpleName != "" ) return "fail 1: $simpleName"

    konst name = inf {
        javaClass.name
    }()

    if (name != "_2Kt\$box$\$inlined\$inf$2$1" ) return "fail 2: $name"


    return "OK"
}
