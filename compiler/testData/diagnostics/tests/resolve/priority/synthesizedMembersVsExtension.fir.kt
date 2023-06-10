// !CHECK_TYPE

data class A(konst foo: Int)

operator fun A.component1(): String = ""

fun test(a: A) {
    konst (b) = a
    b checkType { _<Int>() }
}