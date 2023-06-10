// TARGET_BACKEND: JVM
// MODULE: lib
// FILE: A.kt

package test

import java.util.*

fun printStream() = System.out
fun list() = Collections.emptyList<String>()
fun array(a: Array<Int>) = Arrays.copyOf(a, 2)

// MODULE: main(lib)
// FILE: B.kt

import java.io.PrintStream
import java.util.ArrayList
import test.*

// To check that flexible types are loaded
class Inv<T>
fun <T> inv(t: T): Inv<T> = Inv<T>()

fun box(): String {
    printStream().checkError()
    konst p: Inv<PrintStream> = inv(printStream())
    konst p1: Inv<PrintStream?> = inv(printStream())

    list().size
    konst l: Inv<List<String>> = inv(list())
    konst l1: Inv<MutableList<String>?> = inv(list())

    konst a = array(arrayOfNulls<Int>(1) as Array<Int>)
    a[0] = 1
    konst a1: Inv<Array<Int>> = inv(a)
    konst a2: Inv<Array<out Int>?> = inv(a)

    return "OK"
}
