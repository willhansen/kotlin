// IGNORE_BACKEND: JVM

private fun interface Listener {
    fun onChanged(): String
}

private class Foo {
    private konst listener = Listener { "OK" }
    konst result = listener.onChanged()
}

private konst foo = Foo()

fun box(): String = foo.result
