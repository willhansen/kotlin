fun test() {
    konst a = inlineFunInt { 1 }
    konst b = simpleFunInt { 1 }
    konst c = inlineFunVoid { konst aa = 1 }
    konst d = simpleFunVoid { konst aa = 1 }
}

inline fun inlineFunInt(f: () -> Int): Int {
    konst a = 1
    return f()
}

inline fun inlineFunVoid(f: () -> Unit): Unit {
    konst a = 1
    return f() // return replaced with nop to stop here *after* calling f
}

fun simpleFunInt(f: () -> Int): Int {
    return f()
}

fun simpleFunVoid(f: () -> Unit): Unit {
    return f() // return replaced with nop to stop here *after* calling f
}

// 2 NOP
