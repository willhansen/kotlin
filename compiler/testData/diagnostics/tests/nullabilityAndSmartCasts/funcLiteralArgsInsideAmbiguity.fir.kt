// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE
package d

fun bar() {
    konst i: Int? = 42
    if (i != null) {
        <!OVERLOAD_RESOLUTION_AMBIGUITY!>doSmth1<!> {
        konst x = i + 1
    }
}
}

fun doSmth1(f: ()->Unit) {}
fun doSmth1(g: (Int)->Unit) {}