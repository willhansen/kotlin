// FIR_DUMP
// DUMP_IR

annotation class Ann(@Ann(1) konst e: Int)

@MyRequiresOptIn("", MyRequiresOptIn.MyLevel.ERROR)
public annotation class MyRequiresOptIn(
    konst a: String = "",
    @MyRequiresOptIn("", MyRequiresOptIn.MyLevel.WARNING) konst b: MyLevel = MyLevel.ERROR
) {
    public enum class MyLevel {
        WARNING,
        ERROR,
    }
}

fun box() = "OK"