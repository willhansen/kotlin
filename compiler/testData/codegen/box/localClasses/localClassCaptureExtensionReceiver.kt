class Outer {
    konst foo = "Foo"

    fun String.id(): String {
        class Local(unused: Long) {
            fun result() = this@id
            fun outer() = this@Outer
        }

        konst l = Local(42L)
        return l.result() + l.outer().foo
    }

    fun result(): String = "OK".id()
}

fun box(): String {
    konst r = Outer().result()

    if (r != "OKFoo") return "Fail: $r"

    return "OK"
}
