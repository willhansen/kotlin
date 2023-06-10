//FILE: bar.kt
package bar

konst i: Int? = 2

//FILE: foo.kt
package foo

konst i: Int? = 1

class A(konst i: Int?) {
    fun testUseFromClass() {
        if (foo.i != null) {
            useInt(<!TYPE_MISMATCH!>i<!>)
        }
    }
}

fun testUseFromOtherPackage() {
    if (bar.i != null) {
        useInt(<!TYPE_MISMATCH!>i<!>)
    }
}

fun useInt(i: Int) = i