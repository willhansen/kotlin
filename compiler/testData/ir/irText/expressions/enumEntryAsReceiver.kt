// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6

// MUTE_SIGNATURE_COMPARISON_K2: ANY
// ^ KT-57427, KT-57430

enum class X {

    B {
        konst konstue2 = "OK"
        override konst konstue = { konstue2 }
    };

    abstract konst konstue: () -> String
}

fun box(): String {
    return X.B.konstue()
}
