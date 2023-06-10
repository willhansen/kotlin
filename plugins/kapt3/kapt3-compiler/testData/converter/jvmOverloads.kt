
class State @JvmOverloads constructor(
        konst someInt: Int,
        konst someLong: Long,
        konst someString: String = ""
)

class State2 @JvmOverloads constructor(
        @JvmField konst someInt: Int,
        @JvmField konst someLong: Long = 2,
        @JvmField konst someString: String = ""
) {
    @JvmOverloads
    fun test(someInt: Int, someLong: Long = 1, someString: String = "A"): Int = 5

    fun someMethod(str: String) {}
    fun methodWithoutArgs() {}
}
