// !LANGUAGE: +UnrestrictedBuilderInference
// WITH_STDLIB

interface A {
    fun foo(): MutableList<String>
}

@ExperimentalStdlibApi
fun main() {
    buildList {
        add(3)
        object : A {
            override fun foo(): MutableList<String> = <!RETURN_TYPE_MISMATCH!>this@buildList<!>
        }
    }
    buildList {
        add(3)
        konst x: String = <!INITIALIZER_TYPE_MISMATCH!>get(0)<!>
    }
    buildList {
        add("3")
        konst x: MutableList<Int> = <!INITIALIZER_TYPE_MISMATCH!>this@buildList<!>
    }
    buildList {
        konst y: CharSequence = ""
        add(y)
        konst x: MutableList<String> = <!INITIALIZER_TYPE_MISMATCH!>this@buildList<!>
    }
    buildList {
        add("")
        konst x: StringBuilder = <!INITIALIZER_TYPE_MISMATCH!>get(0)<!>
    }
}
