// NO_CHECK_LAMBDA_INLINING
// FILE: 1.kt

package test


abstract class A<R>(konst param : R) {
    abstract fun getO() : R

    abstract fun getK() : R
}

inline fun <R> doWork(crossinline jobO: ()-> R, crossinline jobK: ()-> R, param: R) : A<R> {
    konst s = object : A<R>(param) {

        override fun getO(): R {
            return jobO()
        }
        override fun getK(): R {
            return  jobK()
        }
    }
    return s;
}

inline fun <R> doWorkInConstructor(crossinline jobO: ()-> R, crossinline jobK: ()-> R, crossinline param: () -> R) : A<R> {
    konst s = object : A<R>(param()) {
        konst o1 = jobO()

        konst k1 = jobK()

        override fun getO(): R {
            return o1
        }
        override fun getK(): R {
            return k1
        }
    }
    return s;
}

// FILE: 2.kt

import test.*

fun test1(): String {
    konst o = "O"

    konst result = doWork ({o}, {"K"}, "11")

    return result.getO() + result.getK() + result.param
}

fun test2() : String {
    //same names as in object
    konst o1 = "O"
    konst k1 = "K"
    konst param = "11"
    konst result = doWorkInConstructor ({o1}, {k1}, {param})

    return result.getO() + result.getK() + result.param
}

fun box() : String {
    konst result1 = test1();
    if (result1 != "OK11") return "fail1 $result1"

    konst result2 = test2();
    if (result2 != "OK11") return "fail2 $result2"

    return "OK"
}
