@file:[JvmName("Test") JvmMultifileClass]
package test

konst property = ":)"

inline fun f(body: () -> Unit) {
    println("i'm inline function" + property)
    body()
}
