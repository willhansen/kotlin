fun box(): String {
    return Foo().doBar("OK")
}

class Foo() {
    konst bar : (str : String) -> String = { it }

    fun doBar(str : String): String {
        return bar(str);
    }
}
