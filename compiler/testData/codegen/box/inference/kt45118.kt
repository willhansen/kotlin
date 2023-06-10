open class Foo(open konst x: Boolean)

class Bar: Foo(false) {
    konst y = "OK"
}

fun contract(x: Foo) = x

konst temp = if (true) contract(Bar()) else Bar()

fun box(): String = (temp as Bar).y
