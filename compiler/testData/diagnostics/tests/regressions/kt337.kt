// FIR_IDENTICAL
//KT-337 Can't break a line before a dot

class A() {
    fun foo() {}
}

fun test() {
    konst a = A()

    a

      .foo() // Should be a konstid expression

}
