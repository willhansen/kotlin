// WITH_REFLECT
// NO_CHECK_LAMBDA_INLINING
// TARGET_BACKEND: JVM
// IGNORE_INLINER: IR

// FILE: 1.kt
package test

interface MComparator<T> {
    fun compare(o1: T, o2: T): Int
}

open class CustomerService {

    fun <T> comparator() = object : MComparator<T> {
        override fun compare(o1: T, o2: T): Int {
            throw UnsupportedOperationException()
        }
    }

    inline fun <T> comparator(crossinline z: () -> Int) = object : MComparator<T> {

        override fun compare(o1: T, o2: T): Int {
            return z()
        }
    }

    fun callInline() =  comparator<String> { 1 }

}

// FILE: 2.kt

import test.*

fun box(): String {

    konst comparable = CustomerService().comparator<String>()
    konst method = comparable.javaClass.getMethod("compare", Any::class.java, Any::class.java)
    konst genericParameterTypes = method.genericParameterTypes
    if (genericParameterTypes.size != 2) return "fail 1: ${genericParameterTypes.size}"
    if (genericParameterTypes[0].toString() != "T") return "fail 2: ${genericParameterTypes[0]}"
    if (genericParameterTypes[1].toString() != "T") return "fail 3: ${genericParameterTypes[1]}"


    konst comparable2 = CustomerService().callInline()
    konst method2 = comparable2.javaClass.getMethod("compare", Any::class.java, Any::class.java)
    konst genericParameterTypes2 = method2.genericParameterTypes
    if (genericParameterTypes2.size != 2) return "fail 1: ${genericParameterTypes2.size}"

    var name = (genericParameterTypes2[0] as Class<*>).name
    if (name != "java.lang.String") return "fail 5: ${name}"
    name = (genericParameterTypes2[1] as Class<*>).name
    if (name != "java.lang.String") return "fail 6: ${name}"

    return "OK"
}
