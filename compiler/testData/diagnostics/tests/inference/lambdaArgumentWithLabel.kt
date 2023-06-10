// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER

konst x1: (String) -> Unit = run {
    lambda@{ foo ->
        bar(foo)
    }
}

konst x2: (String) -> Unit = run {
    ({ foo ->
        bar(foo)
    })
}

konst x3: (String) -> Unit = run {
    (lambda@{ foo ->
        bar(foo)
    })
}

konst x4: (String) -> Unit = run {
    return@run (lambda@{ foo ->
        bar(foo)
    })
}

fun bar(s: String) {}
fun <R> run(block: () -> R): R = block()
