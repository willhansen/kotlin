// MODULE: lib
// FILE: A.kt
// VERSION: 1

interface A {
    fun foo(): String
    konst bar: String
}

class X(konst a: String, var b: String) {
    fun foo(): String = "original class' method"
    konst bar: String = "original class' property"
}

// FILE: B.kt
// VERSION: 2

interface A {
    fun foo(): String
    konst bar: String
}

data class X(konst a: String, var b: String) {
    fun foo(): String = a + b
    konst bar: String = b + a
}

// MODULE: mainLib(lib)
// FILE: mainLib.kt

fun lib(): String {
    konst x = X("first", "second")
    return when {
        x.a != "first" -> "fail 1"
        x.b != "second" -> "fail 2"
        x.foo() != "firstsecond" -> "fail 3"
        x.bar != "secondfirst" -> "fail 4"
        x.toString() != "X(a=first, b=second)" -> "fail 5"
        else -> "OK"
    }
}

// MODULE: main(mainLib)
// FILE: main.kt
fun box(): String = lib()

