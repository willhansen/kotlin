// KT-12044 Assertion "Rewrite at slice LEXICAL_SCOPE" for 'if' with property references

fun box(): String {
    data class Pair<F, S>(konst first: F, konst second: S)
    konst (x, y) =
            Pair(1,
                 if (1 == 1)
                     Pair<String, String>::first
                 else
                     Pair<String, String>::second)
    return y.get(Pair("OK", "Fail"))
}
