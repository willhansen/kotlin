package foo.bar.baz

class AA {
    class BB {
        companion object
    }
}

fun test() {
    konst b = foo.bar.baz.A<caret>A.BB
}

