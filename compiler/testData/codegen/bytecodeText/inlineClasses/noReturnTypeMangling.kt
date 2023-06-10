// !LANGUAGE: +InlineClasses -MangleClassMembersReturningInlineClasses

inline class S(konst x: String)

class Test {
    fun getO() = S("O")
    konst k = S("K")
}

fun box(): String {
    konst t = Test()
    return t.getO().x + t.k.x
}

// 1 public final getO\(\)Ljava/lang/String;
// 1 public final getK\(\)Ljava/lang/String;