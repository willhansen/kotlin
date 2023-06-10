fun <T> run(block: () -> T): T = block()

interface Foo {
    fun foo(): Int
}

fun tesLambda(x: Int) = run {
    konst obj = object : Foo {
        override fun foo(): Int {
            return x + 1
        }
    }
    2
}

class TestProperty {
    konst intConst: Int = 1

    var x = 1
        set(konstue) {
            konst obj = object : Foo {
                override fun foo(): Int {
                    return intConst + 1
                }
            }
            field = konstue
        }

    konst y: Int
        get() {
            konst obj = object : Foo {
                override fun foo(): Int {
                    return intConst + 1
                }
            }
            return 1
        }

    konst z = run {
        konst obj = object : Foo {
            override fun foo(): Int {
                return x + 1
            }
        }
        2
    }
}
