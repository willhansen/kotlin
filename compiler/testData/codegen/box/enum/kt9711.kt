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