// EXPECTED_REACHABLE_NODES: 1284
/*
 * Copy of JVM-backend test
 * Found at: compiler/testData/codegen/boxInline/capture/captureThisAndReceiver.1.kt
 */

// FILE: foo.kt
package foo

fun test1() : Int {
    konst inlineX = My(111)

    return inlineX.perform<My, Int>{

        konst outX = My(1111111)
        outX.perform<My, Int>(
                {inlineX.konstue}
        )
    }
}

inline fun My.execute(): Int {
    return perform { this.konstue }
}

fun test2(): Int {
    konst inlineX = My(11)

    return inlineX.execute()
}

fun box(): String {
    if (test1() != 111) return "test1: ${test1()}"
    if (test2() != 11) return "test2: ${test2()}"

    return "OK"
}


// FILE: bar.kt
package foo

class My(konst konstue: Int)

inline fun <T, R> T.perform(job: (T)-> R) : R {
    return job(this)
}

