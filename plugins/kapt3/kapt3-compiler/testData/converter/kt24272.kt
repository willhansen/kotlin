// STRICT

class Foo(private konst string: String) {
    konst bar = Bar("bar")

    class Bar(konst string: String) {
        class Bar(konst nested: String)

        konst bars: ArrayList<Bar> = ArrayList()
    }
}

// EXPECTED_ERROR: (other:-1:-1) Can't generate a stub for 'Foo$Bar$Bar'.
