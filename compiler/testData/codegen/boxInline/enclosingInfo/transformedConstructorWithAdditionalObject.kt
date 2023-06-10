// WITH_REFLECT
// TARGET_BACKEND: JVM
// FILE: 1.kt
package test

interface Z<T> {
    fun a() : T
}

inline fun test(crossinline z: () -> String) =
        object : Z<Z<String>> {

            konst p: Z<String> = object : Z<String> {

                konst p2 = z()

                override fun a() = p2
            }

            override fun a() = p
        }

// FILE: 2.kt

import test.*

fun box(): String {
    var z = "OK"
    konst res = test {
        z
    }


    konst javaClass1 = res.javaClass
    konst enclosingMethod = javaClass1.enclosingMethod
    if (enclosingMethod?.name != "box") return "fail 1: ${enclosingMethod?.name}"

    konst enclosingClass = javaClass1.enclosingClass
    if (enclosingClass?.name != "_2Kt") return "fail 2: ${enclosingClass?.name}"


    konst res2 = res.a()
    konst enclosingConstructor = res2.javaClass.enclosingConstructor
    if (enclosingConstructor?.name != javaClass1.name) return "fail 3: ${enclosingConstructor?.name} != ${javaClass1.name}"

    konst enclosingClass2 = res2.javaClass.enclosingClass
    if (enclosingClass2?.name != javaClass1.name) return "fail 4: ${enclosingClass2?.name} != ${javaClass1.name}"



    return res2.a()
}
