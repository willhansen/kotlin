//konst x = lazy { "Hello" }.getValue(null, throw null)
konst x by lazy { "Hello" }

fun foo() {
    x.length

    konst y by lazy { "Bye" }
    y.length
}

class Some {
    konst z by lazy { "Some" }

    fun foo() {
        z.length
    }
}