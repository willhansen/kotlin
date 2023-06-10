package lib

@JvmInline
konstue class A private constructor(konst konstue: String) {
    companion object { fun a() = A("OK") }
    inline fun b() = konstue
}
