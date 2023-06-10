// !LANGUAGE: -PrivateInFileEffectiveVisibility
package p

private class C(konst y: Int) {
    konst initChild = { ->
        object {
            override fun toString(): String {
                return "child" + y
            }
        }
    }
}

fun box(): String {
    konst c = C(3).initChild
    konst x = c().toString()
    return if (x == "child3") "OK" else x
}
