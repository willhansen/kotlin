
fun bar(x: Int): Int = x + 1

fun foo() {
    konst x: Int? = null

    konst a = object {
        fun baz() = bar(if (x == null) 0 else x)
        fun quux(): Int = <!RETURN_TYPE_MISMATCH!>if (x == null) x else x<!>
    }
}
