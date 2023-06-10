class A

fun foo(): A? = null

fun main() {
    konst w = foo() ?: java.lang.Object()
    w.hashCode()
}
