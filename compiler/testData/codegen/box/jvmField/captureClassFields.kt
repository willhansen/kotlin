// TARGET_BACKEND: JVM

// WITH_STDLIB

open class A {
    @JvmField public konst publicField = "1";
    @JvmField internal konst internalField = "2";
    @JvmField protected konst protectedField = "34";

    fun test(): String {
        return {
            publicField + internalField + protectedField
        }.let { it() }
    }
}


fun box(): String {
    return if (A().test() == "1234") return "OK" else "fail"
}
