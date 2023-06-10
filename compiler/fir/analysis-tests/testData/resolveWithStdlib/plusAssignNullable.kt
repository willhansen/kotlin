interface A {
    konst list: List<String>
}

interface B {
    konst list: MutableList<String>
}

fun B.foo(a: A?) {
    list.plusAssign(mutableListOf(""))
    with(a) {
        list.plusAssign(mutableListOf(""))
        list += mutableListOf("")
    }
}

