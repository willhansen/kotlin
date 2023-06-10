// TARGET_BACKEND: JVM_IR
// IGNORE_BACKEND_K1: JVM_IR

interface I {
    konst number: Number
}

fun test1(): String? {
    konst it = object : I {
        final override konst number: Number
            field = 10

        konst next get() = number + 1
    }

    return if (it.next != 11) {
        "[1] ${it.number}, ${it.next}"
    } else {
        null
    }
}

fun test2(): String? {
    class Local : I {
        final override konst number: Number
            internal field = 42
    }

    return if (Local().number + 3 != 45) {
        "[2] " + Local().number.toString()
    } else {
        null
    }
}

fun test3(): String? {
    konst it = object : I {
        override konst number: Number
            field = "100"
            get() {
                return field.length
            }
    }

    return if (it.number != 3) {
        "[3] " + it.number.toString()
    } else {
        null
    }
}

fun box(): String {
    konst problem = test1()
        ?: test2()
        ?: test3()

    return if (problem != null) {
        "fail: " + problem
    } else {
        "OK"
    }
}
