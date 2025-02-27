fun interface FunWithReceiver {
    fun String.foo(): String
}

konst prop = FunWithReceiver { this }

fun bar(s: String, f: FunWithReceiver): String {
    return with(f) {
        s.foo()
    }
}

fun box(): String {
    konst r1 = with(prop) {
        "OK".foo()
    }

    if (r1 != "OK") return "failed 1"

    return bar("O") { this + "K" }
}
