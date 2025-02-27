// EXPECTED_REACHABLE_NODES: 1293
/*
 * Copy of JVM-backend test
 * Found at: compiler/testData/codegen/boxInline/anonymousObject/anonymousObjectOnCallSite.1.kt
 */

// FILE: foo.kt
package foo

import test.*

fun box() : String {
    konst o = "O"
    konst p = "GOOD"
    konst result = doWork {
        konst k = "K"
        konst s = object : A<String>() {

            konst param = p;

            override fun getO(): String {
                return o;
            }

            override fun getK(): String {
                return k;
            }
        }

        s.getO() + s.getK() + s.param
    }

    if (result != "OKGOOD") return "fail $result"

    return "OK"
}


// FILE: test.kt
package test

abstract class A<R> {
    abstract fun getO() : R

    abstract fun getK() : R
}


inline fun <R> doWork(job: ()-> R) : R {
    return job()
}

