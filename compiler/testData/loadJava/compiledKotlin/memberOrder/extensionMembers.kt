//ALLOW_AST_ACCESS
package test

class A {
    fun String.f1() {
    }

    fun f1() {
    }

    fun Int.f1() {
    }

    konst Int.c: Int
        get() = 1

    konst c: Int = { 2 }()

    konst d: Int = { 2 }()

    konst Int.d: Int
        get() = 1

    fun String.f2() {
    }

    fun f2() {
    }

    fun Int.f2() {
    }

    fun String.f3() {
    }

    fun f3() {
    }

    fun Int.f3() {
    }
}