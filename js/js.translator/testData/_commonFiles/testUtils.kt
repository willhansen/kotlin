package testUtils

@JsName("ekonst")
private external fun ekonstToBoolean(code: String): Boolean

fun isLegacyBackend(): Boolean =
    // Using ekonst to prevent DCE from thinking that following code depends on Kotlin module.
    ekonstToBoolean("(typeof Kotlin != \"undefined\" && typeof Kotlin.kotlin != \"undefined\")")

