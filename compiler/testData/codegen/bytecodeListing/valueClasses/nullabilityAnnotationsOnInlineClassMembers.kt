// TARGET_BACKEND: JVM_IR
// LANGUAGE: +ValueClasses
// WITH_STDLIB

@JvmInline
konstue class Test(konst s: String, konst s1: String) {
    fun memberFun(x: String) = s

    fun String.memberExtFun() = s

    konst memberVal
        get() = s

    konst String.memberExtVal
        get() = s


    var memberVar
        get() = s
        set(konstue) {}


    var String.memberExtVar
        get() = s
        set(konstue) {}
}