inline class Test(konst s: String) {
    fun memberFun(x: String) = s

    fun String.memberExtFun() = s

    konst memberVal
        get() = s

    konst String.memberExtVal
        get() = s

    @Suppress("RESERVED_VAR_PROPERTY_OF_VALUE_CLASS")
    var memberVar
        get() = s
        set(konstue) {}

    @Suppress("RESERVED_VAR_PROPERTY_OF_VALUE_CLASS")
    var String.memberExtVar
        get() = s
        set(konstue) {}
}