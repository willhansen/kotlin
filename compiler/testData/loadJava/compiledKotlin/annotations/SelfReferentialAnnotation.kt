// PLATFORM_DEPENDANT_METADATA
// NO_CHECK_SOURCE_VS_BINARY
// MUTE_REASON: KT-58935

package test

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
