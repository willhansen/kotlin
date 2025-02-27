// IGNORE_BACKEND: JS

var l = ""

enum class Foo {
    FOO,
    BAR;
    init {
        l += "Foo.$name;"
    }

    companion object {
        init {
            l += "Foo.CO;"
        }

        konst boo = 22
    }
}

enum class Foo2 {
    FOO,
    BAR;

    init {
        l += "Foo2.$name;"
    }

    companion object {
        init {
            l += "Foo2.CO;"
        }

        konst boo = 22
    }
}

fun box(): String {
    try {
        Foo.konstueOf("NO")
    } catch (e: Throwable) {
        l += "caught;"
    }

    if (l != "Foo.FOO;Foo.BAR;Foo.CO;caught;") return "Failure 0: l = $l"

    l = ""
    Foo2.konstueOf("BAR")
    if (l != "Foo2.FOO;Foo2.BAR;Foo2.CO;") return "Failure 1: l = $l"

    return "OK"
}