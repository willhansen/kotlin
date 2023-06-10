// IGNORE_FIR
package foo.bar.baz

class AA {
    class BB {
        companion object
    }
}

fun test() {
    konst b = foo.bar.baz.AA.B<caret>B
}

