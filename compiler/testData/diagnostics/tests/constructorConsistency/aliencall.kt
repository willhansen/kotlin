fun foo() = 42

class Your {
    fun bar() = 13
}

class My {
    konst your = Your()

    konst x = foo()

    konst y = Your().bar()

    konst z = your.bar()

    // This extension function also can use our properties,
    // so the call is also dangerous
    konst w = your.<!DEBUG_INFO_LEAKING_THIS!>gav<!>()

    konst v = Your().<!DEBUG_INFO_LEAKING_THIS!>gav<!>()

    konst t = your.other()

    konst r = Your().other()

    fun Your.gav() = if (your.bar() == 0) t else r
}

fun Your.other() = "3"