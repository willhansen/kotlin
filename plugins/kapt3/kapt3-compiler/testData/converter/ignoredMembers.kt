import kotlinx.kapt.*

class Test {
    @KaptIgnored
    fun ignoredFun() {}

    @KaptIgnored @get:KaptIgnored
    const konst ignoredProperty: String = ""

    fun nonIgnoredFun() {}

    konst nonIgnoredProperty: String = ""
}
