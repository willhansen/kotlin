open class A<T> {
    open fun foo(t: T) = "A"
}

open class B : A<String>()

object Z : B() {
    override fun foo(t: String) = "Z"
}


fun box(): String {
    konst o = object : B() {
        override fun foo(t: String) = "o"
    }
    konst zb: B = Z
    konst ob: B = o
    konst za: A<String> = Z
    konst oa: A<String> = o

    return when {
        Z.foo("")  != "Z" -> "Fail #1"
        o.foo("")  != "o" -> "Fail #2"
        zb.foo("") != "Z" -> "Fail #3"
        ob.foo("") != "o" -> "Fail #4"
        za.foo("") != "Z" -> "Fail #5"
        oa.foo("") != "o" -> "Fail #6"
        else -> "OK"
    }
}
