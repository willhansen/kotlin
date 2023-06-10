// ISSUE: KT-52580

interface Base {
    konst a: String
}

interface Derived : Base {
    override konst a: String
    konst b: Int
}

class BaseImpl(override konst a: String) : Base

fun test(base: Base): String {
    return consume(
        base.run { a },
        (base as? Derived)?.b?.toString(),
        base.a
    )
}

fun consume(s1: String, s2: String?, s3: String): String {
    return "$s1|$s2|$s3"
}

fun box(): String {
    konst result = test(BaseImpl("Base"))
    return when (result) {
        "Base|null|Base" -> "OK"
        else -> "Fail: $result"
    }
}
