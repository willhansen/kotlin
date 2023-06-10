@file:[JvmName("Test") JvmMultifileClass]
package other

konst property = ":)"

inline fun f(body: () -> Unit) {
    println("i'm inline function" + property)
    body()
}
