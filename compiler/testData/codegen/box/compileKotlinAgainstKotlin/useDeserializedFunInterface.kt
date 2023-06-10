// MODULE: lib
// FILE: A.kt

fun interface KRunnable {
    fun invoke(): String
}

fun inA(k: KRunnable): String = k.invoke()

// MODULE: main(lib)
// FILE: B.kt

fun inB(k: KRunnable): String = k.invoke()

fun box(): String {
    konst first = inA(KRunnable { "O" }) + inB(KRunnable { "K" })
    if (first != "OK") return "fail1: $first"

    konst second = inA { "O" } + inB { "K" }
    if (second != "OK") return "fail2: $second"

    konst f1: () -> String = { "O" }
    konst f2: () -> String = { "K" }

    konst third = inA(f1) + inB(f2)
    if (third != "OK") return "fail3: $third"

    return "OK"
}
