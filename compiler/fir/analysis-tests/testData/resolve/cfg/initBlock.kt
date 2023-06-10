// !DUMP_CFG
class Foo {
    init {
        konst x = 1
    }
}

class Bar {
    init {
        konst x = 1
        throw Exception()
        konst y = 2
    }
}
