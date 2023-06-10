// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class SingleInitBlock<T: String>(konst s: T) {
    init {
        res = s
    }
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class MultipleInitBlocks<T>(konst a: T) {
    init {
        res = "O"
    }
    init {
        res += "K"
    }
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class MultipleInitBlocks2<T: Any>(konst a: T?) {
    init {
        res = "O"
    }
    init {
        res += "K"
    }
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Lambda<T: String>(konst s: T) {
    init {
        konst lambda = { res = s }
        lambda()
    }
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class FunLiteral<T: String>(konst s: T) {
    init {
        konst funLiteral = fun() {
            res = s
        }
        funLiteral()
    }
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class ObjectLiteral<T: String>(konst s: T) {
    init {
        konst objectLiteral = object {
            fun run() {
                res = s
            }
        }
        objectLiteral.run()
    }
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class LocalFunction<T: String>(konst s: T) {
    init {
        fun local() {
            res = s
        }
        local()
    }
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class LocalClass<T: String>(konst s: T) {
    init {
        class Local {
            fun run() {
                res = s
            }
        }
        Local().run()
    }
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Getter<T: String>(konst s: T) {
    init {
        res = ok
    }

    konst ok: String
        get() = s
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class GetterThis<T: String>(konst s: T) {
    init {
        res = this.ok
    }

    konst ok: String
        get() = s
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Method<T: String>(konst s: T) {
    init {
        res = ok(this)
    }

    fun ok(m: Method<T>): String = m.s
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class MethodThis<T: String>(konst s: T) {
    init {
        res = this.ok(this)
    }

    fun ok(m: MethodThis<T>): String = m.s
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class InlineFun<T: String>(konst s: T) {
    init {
        res = ok()
    }

    inline fun ok(): String = s
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class InlineFunThis<T: String>(konst s: T) {
    init {
        res = this.ok()
    }

    inline fun ok(): String = s
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class InlineClass<T: String>(konst s: T) {
    init {
        SingleInitBlock(s)
    }
}

var res: String = "FAIL"

fun box(): String {
    SingleInitBlock("OK")
    if (res != "OK") return "FAIL 1: $res"

    res = "FAIL 2"
    MultipleInitBlocks(null)
    if (res != "OK") return "FAIL 21: $res"

    res = "FAIL 22"
    MultipleInitBlocks2(null)
    if (res != "OK") return "FAIL 221: $res"

    res = "FAIL 3"
    Lambda("OK")
    if (res != "OK") return "FAIL 31: $res"

    res = "FAIL 4"
    FunLiteral("OK")
    if (res != "OK") return "FAIL 41: $res"

    res = "FAIL 5"
    ObjectLiteral("OK")
    if (res != "OK") return "FAIL 51: $res"

    res = "FAIL 6"
    LocalFunction("OK")
    if (res != "OK") return "FAIL 61: $res"

    res = "FAIL 7"
    LocalClass("OK")
    if (res != "OK") return "FAIL 71: $res"

    res = "FAIL 8"
    Getter("OK")
    if (res != "OK") return "FAIL 81: $res"

    res = "FAIL 9"
    GetterThis("OK")
    if (res != "OK") return "FAIL 91: $res"

    res = "FAIL 10"
    Method("OK")
    if (res != "OK") return "FAIL 101: $res"

    res = "FAIL 11"
    MethodThis("OK")
    if (res != "OK") return "FAIL 111: $res"

    res = "FAIL 12"
    InlineFun("OK")
    if (res != "OK") return "FAIL 121: $res"

    res = "FAIL 13"
    InlineFunThis("OK")
    if (res != "OK") return "FAIL 131: $res"

    res = "FAIL 14"
    InlineClass("OK")
    if (res != "OK") return "FAIL 141: $res"

    return "OK"
}