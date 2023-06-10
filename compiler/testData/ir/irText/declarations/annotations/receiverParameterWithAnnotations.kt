// FIR_IDENTICAL

annotation class Ann

class A {
    fun @receiver:Ann String.f(): String = ""

    konst @receiver:Ann String?.p: String
        get() = ""

}

fun @receiver:Ann String?.topLevelF(): String = ""

konst @receiver:Ann String.topLevelP: String
    get() = ""
