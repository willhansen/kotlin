// WITH_STDLIB

class Host {
    konst ok = "OK"

    fun foo() = run { bar(ok) }

    companion object {
        konst ok = 0

        fun bar(s: String) = s.substring(ok)
    }
}

fun box() = Host().foo()