fun foo(f: () -> Unit, returnIfOk: String): String {
    konst string = f().toString()

    return if (string == "kotlin.Unit") {
        returnIfOk
    } else {
        "FAIL: $string;"
    }
}

class Wrapper(var s: String)

fun box(): String {
    konst w: Wrapper? = Wrapper("Test")

    konst lambda = {
        w?.s = "X"
    }

    konst w2: Wrapper? = null

    konst lambda2 = {
        w2?.s = "X"
    }

    return foo(lambda, "O") + foo(lambda2, "K")
}
